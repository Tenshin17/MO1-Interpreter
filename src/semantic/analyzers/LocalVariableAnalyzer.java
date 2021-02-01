package semantic.analyzers;

import antlr.Java8Lexer;
import antlr.Java8Parser.*;
import error.checkers.MultipleVarDecChecker;
import error.checkers.TypeChecker;
import Execution.ExecutionManager;
import Execution.command.evaluation.MappingCommand;
import semantic.representation.JavaValue;
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
			identifiedTokens.clearTokens();

			if(ClassAnalyzer.isPrimitiveDeclaration(typeCtx)) {
				UnannPrimitiveTypeContext unannPrimitiveTypeCtx = typeCtx.unannPrimitiveType();
				identifiedTokens.addToken(PRIMITIVE_TYPE_KEY, unannPrimitiveTypeCtx.getText());
			}

			//check if its array declaration
			else if(ClassAnalyzer.isPrimitiveArrayDeclaration(typeCtx)) {
				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Primitive array declaration: " +typeCtx.getText()));
				ArrayAnalyzer arrayAnalyzer = new ArrayAnalyzer(identifiedTokens, LocalScopeCreator.getInstance().getActiveLocalScope());
				arrayAnalyzer.analyze(typeCtx.getParent());
				hasPassedArrayDeclaration = true;
			}

			//this is for class type ctx
			else {
				//a string identified
				if(typeCtx.unannReferenceType().getText().contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
					UnannClassOrInterfaceTypeContext classInterfaceCtx = typeCtx.unannReferenceType().unannClassOrInterfaceType();
					identifiedTokens.addToken(PRIMITIVE_TYPE_KEY, classInterfaceCtx.getText());
				}
			}
		}
		else if (ctx instanceof VariableModifierContext){
			VariableModifierContext varModCtx = (VariableModifierContext) ctx;
			if(varModCtx.getTokens(Java8Lexer.FINAL).size() > 0){
				ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Detected const / final: " + varModCtx.getText()));
				currentlyConst = true;
			}
		}
		else if(ctx instanceof VariableDeclaratorContext) {

			VariableDeclaratorContext varCtx = (VariableDeclaratorContext) ctx;

			if(hasPassedArrayDeclaration) {
				return;
			}

			//check for duplicate declarations
			if(!executeMappingImmediate) {
				MultipleVarDecChecker multipleDeclaredChecker = new MultipleVarDecChecker(varCtx.variableDeclaratorId());
				multipleDeclaredChecker.verify();
			}

			identifiedTokens.addToken(IDENTIFIER_KEY, varCtx.variableDeclaratorId().getText());
			createJavaValue();

			if(varCtx.variableInitializer() != null) {

				//we do not evaluate strings.
				if(identifiedTokens.containsTokens(PRIMITIVE_TYPE_KEY)) {
					String unannPrimitiveTypeString = identifiedTokens.getToken(PRIMITIVE_TYPE_KEY);
					if(unannPrimitiveTypeString.contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
						identifiedTokens.addToken(IDENTIFIER_VALUE_KEY, varCtx.variableInitializer().getText());
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
			ExecutionManager.getExecutionManager().addCommand(mappingCommand);
		}
	}

	public void markImmediateExecution() {
		executeMappingImmediate = true;
	}

	/*
	 * Attempts to create an intermediate representation of the variable once a sufficient amount of info has been retrieved.
	 */
	private void createJavaValue() {

		if(identifiedTokens.containsTokens(PRIMITIVE_TYPE_KEY, IDENTIFIER_KEY)) {

			String unannPrimitiveTypeString = identifiedTokens.getToken(PRIMITIVE_TYPE_KEY);
			String identifierString = identifiedTokens.getToken(IDENTIFIER_KEY);
			String identifierValueString;

			LocalScope localScope = LocalScopeCreator.getInstance().getActiveLocalScope();

			if(identifiedTokens.containsTokens(IDENTIFIER_VALUE_KEY)) {
				identifierValueString = identifiedTokens.getToken(IDENTIFIER_VALUE_KEY);
				localScope.addInitializedVariableFromKeywords(unannPrimitiveTypeString, identifierString, identifierValueString);
			}
			else {
				localScope.addEmptyVariableFromKeywords(unannPrimitiveTypeString, identifierString);
			}

			//remove the following tokens
			identifiedTokens.removeToken(IDENTIFIER_KEY);
			identifiedTokens.removeToken(IDENTIFIER_VALUE_KEY);

		}
	}
}