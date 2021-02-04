package semantic.analyzers;

import Execution.command.ICondCommand;
import Execution.command.ICtrlCommand;
import antlr.Java8Lexer;
import antlr.Java8Parser.*;
import error.checkers.MultipleVarDecChecker;
import error.checkers.TypeChecker;
import Execution.ExecutionManager;
import Execution.command.evaluation.MappingCommand;
import semantic.representation.JavaValue;
import semantic.statements.StatementControlOverseer;
import semantic.symboltable.scope.LocalScope;
import semantic.symboltable.scope.LocalScopeCreator;
import semantic.utils.IdentifiedTokens;
import semantic.utils.RecognizedKeywords;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Analyzes a local variable declaration
 *
 */
public class LocalVariableAnalyzer implements ParseTreeListener {

	private final static String PRIMITIVE_TYPE_KEY = "PRIMITIVE_TYPE_KEY";
	private final static String IDENTIFIER_KEY = "IDENTIFIER_KEY";
	private final static String IDENTIFIER_VALUE_KEY = "IDENTIFIER_VALUE_KEY";

	private IdentifiedTokens identifiedTokens;
	private boolean executeMappingImmediate = false;
	private boolean hasPassedArrayDeclaration = false;

	public static boolean currentlyConst = false;

	public LocalVariableAnalyzer() {

	}

	public void analyze(LocalVariableDeclarationContext localVarDecCtx) {
		this.identifiedTokens = new IdentifiedTokens();

		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, localVarDecCtx);

	}

	public void constDone() { currentlyConst = false; }

	@Override
	public void visitTerminal(TerminalNode node) {
		// TODO Auto-generated method stub
	}

	@Override
	public void visitErrorNode(ErrorNode node) {
		// TODO Auto-generated method stub

	}

	@Override
	public void enterEveryRule(ParserRuleContext ctx) {
		analyzeVariables(ctx);
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub

	}

	private void analyzeVariables(ParserRuleContext ctx) {
		if(ctx instanceof UnannTypeContext) {
			UnannTypeContext typeCtx = (UnannTypeContext) ctx;
			//clear tokens for reuse
			//this.identifiedTokens.clearTokens();

			if(ClassAnalyzer.isPrimitiveDeclaration(typeCtx)) {
				UnannPrimitiveTypeContext unannPrimitiveTypeCtx = typeCtx.unannPrimitiveType();
				this.identifiedTokens.addToken(PRIMITIVE_TYPE_KEY, unannPrimitiveTypeCtx.getText());
			}

			//check if its array declaration
			else if(ClassAnalyzer.isPrimitiveArrayDeclaration(typeCtx)) {
				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Primitive array declaration: " +typeCtx.getText()));
				ArrayAnalyzer arrayAnalyzer = new ArrayAnalyzer(this.identifiedTokens, LocalScopeCreator.getInstance().getActiveLocalScope());
				arrayAnalyzer.analyze(typeCtx.getParent());
				this.hasPassedArrayDeclaration = true;
			}

			//this is for class type ctx
			else {
				//a string identified
				if(typeCtx.unannReferenceType().getText().contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
					UnannClassOrInterfaceTypeContext classInterfaceCtx = typeCtx.unannReferenceType().unannClassOrInterfaceType();
					this.identifiedTokens.addToken(PRIMITIVE_TYPE_KEY, classInterfaceCtx.getText());
				}
			}
		}
		else if (ctx instanceof VariableModifierContext){
			VariableModifierContext varModCtx = (VariableModifierContext) ctx;
			if(varModCtx.getTokens(Java8Lexer.FINAL).size() > 0){
				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Detected const / final: " + varModCtx.getText()));
				this.currentlyConst = true;
			}
		}
		else if(ctx instanceof VariableDeclaratorContext) {

			VariableDeclaratorContext varCtx = (VariableDeclaratorContext) ctx;

			if(this.hasPassedArrayDeclaration) {
				return;
			}

			//check for duplicate declarations
			if(!this.executeMappingImmediate) {
				MultipleVarDecChecker multipleDeclaredChecker = new MultipleVarDecChecker(varCtx.variableDeclaratorId());
				multipleDeclaredChecker.verify();
			}

			this.identifiedTokens.addToken(IDENTIFIER_KEY, varCtx.variableDeclaratorId().getText());
			this.createJavaValue();

			if(varCtx.variableInitializer() != null) {

				//we do not evaluate strings.
				if(this.identifiedTokens.containsTokens(PRIMITIVE_TYPE_KEY)) {
					String unannPrimitiveTypeString = this.identifiedTokens.getToken(PRIMITIVE_TYPE_KEY);
					if(unannPrimitiveTypeString.contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
						this.identifiedTokens.addToken(IDENTIFIER_VALUE_KEY, varCtx.variableInitializer().getText());
					}
				}

				this.processMapping(varCtx);

				LocalScope localScope = LocalScopeCreator.getInstance().getActiveLocalScope();
				JavaValue declaredJavaValue = localScope.searchVariableIncludingLocal(varCtx.variableDeclaratorId().getText());

				//type check the javaValue
				TypeChecker typeChecker = new TypeChecker(declaredJavaValue, varCtx.variableInitializer().expression());
				typeChecker.verify();
			}

		}

	}

	/*
	 * Local variable analyzer is also used for loops. Whenever there is a loop,
	 * mapping command should be executed immediately to update the value in the symbol table.
	 * Otherwise, it proceeds normally.
	 */
	private void processMapping(VariableDeclaratorContext varCtx) {
		if(this.executeMappingImmediate) {
			MappingCommand mappingCommand = new MappingCommand(varCtx.variableDeclaratorId().getText(), varCtx.variableInitializer().expression());
			mappingCommand.execute();
		}
		else {
			MappingCommand mappingCommand = new MappingCommand(varCtx.variableDeclaratorId().getText(), varCtx.variableInitializer().expression());

			StatementControlOverseer statementControl = StatementControlOverseer.getInstance();
			//add to conditional controlled command
			if(statementControl.isInConditionalCommand()) {
				ICondCommand conditionalCommand = (ICondCommand) statementControl.getActiveControlledCommand();

				if(statementControl.isInPositiveRule()) {
					conditionalCommand.addPositiveCommand(mappingCommand);
				}
				else {
					conditionalCommand.addNegativeCommand(mappingCommand);
				}
			}

			else if(statementControl.isInControlledCommand()) {
				ICtrlCommand controlledCommand = (ICtrlCommand) statementControl.getActiveControlledCommand();
				controlledCommand.addCommand(mappingCommand);
			}
			else {
				ExecutionManager.getExecutionManager().addCommand(mappingCommand);
			}
		}
	}

	public void markImmediateExecution() {
		this.executeMappingImmediate = true;
	}

	/*
	 * Attempts to create an intermediate representation of the variable once a sufficient amount of info has been retrieved.
	 */
	private void createJavaValue() {

		if(this.identifiedTokens.containsTokens(PRIMITIVE_TYPE_KEY, IDENTIFIER_KEY)) {

			String unannPrimitiveTypeString = this.identifiedTokens.getToken(PRIMITIVE_TYPE_KEY);
			String identifierString = this.identifiedTokens.getToken(IDENTIFIER_KEY);
			String identifierValueString;

			LocalScope localScope = LocalScopeCreator.getInstance().getActiveLocalScope();

			if(this.identifiedTokens.containsTokens(IDENTIFIER_VALUE_KEY)) {
				identifierValueString = this.identifiedTokens.getToken(IDENTIFIER_VALUE_KEY);
				localScope.addInitializedVariableFromKeywords(unannPrimitiveTypeString, identifierString, identifierValueString);
			}
			else {
				localScope.addEmptyVariableFromKeywords(unannPrimitiveTypeString, identifierString);
			}

			//remove the following tokens
			this.identifiedTokens.removeToken(IDENTIFIER_KEY);
			this.identifiedTokens.removeToken(IDENTIFIER_VALUE_KEY);

		}
	}
}