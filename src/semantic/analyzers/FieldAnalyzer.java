package semantic.analyzers;

import antlr.Java8Parser.VariableDeclaratorContext;
import antlr.Java8Parser.VariableDeclaratorListContext;
import error.checkers.MultipleVarDecChecker;
import error.checkers.TypeChecker;
import Execution.ExecutionManager;
import Execution.command.evaluation.MappingCommand;
import semantic.representation.JavaValue;
import semantic.symboltable.scope.ClassScope;
import semantic.utils.IdentifiedTokens;
import semantic.utils.RecognizedKeywords;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Analyzes the fields in the member declaration part
 *
 */
public class FieldAnalyzer implements ParseTreeListener {
	
	private ClassScope declaredClassScope;
	private IdentifiedTokens identifiedTokens;
	
	public FieldAnalyzer(IdentifiedTokens identifiedTokens, ClassScope declaredClassScope) {
		this.identifiedTokens = identifiedTokens;
		this.declaredClassScope = declaredClassScope;
	}
	
	public void analyze(VariableDeclaratorListContext varDecCtxList) {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, varDecCtxList);
	}

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
		if(ctx instanceof VariableDeclaratorContext) {
			VariableDeclaratorContext varCtx = (VariableDeclaratorContext) ctx;
			
			//check for duplicate declarations
			MultipleVarDecChecker multipleDeclaredChecker = new MultipleVarDecChecker(varCtx.variableDeclaratorId());
			multipleDeclaredChecker.verify();
			
			identifiedTokens.addToken(ClassAnalyzer.IDENTIFIER_KEY, varCtx.variableDeclaratorId().getText());
			createJavaValue();
			
			if(varCtx.variableInitializer() != null) {
				
				//we do not evaluate strings.
				if(identifiedTokens.containsTokens(ClassAnalyzer.PRIMITIVE_TYPE_KEY)) {
					String primitiveTypeString = identifiedTokens.getToken(ClassAnalyzer.PRIMITIVE_TYPE_KEY);
					if(primitiveTypeString.contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
						identifiedTokens.addToken(ClassAnalyzer.IDENTIFIER_VALUE_KEY, varCtx.variableInitializer().getText());
						return;
					}
				}
				
				MappingCommand mappingCommand = new MappingCommand(varCtx.variableDeclaratorId().getText(), varCtx.variableInitializer().expression());
				ExecutionManager.getExecutionManager().addCommand(mappingCommand);
				
				JavaValue declaredJavaValue = declaredClassScope.searchVariableIncludingLocal(varCtx.variableDeclaratorId().getText());
				
				//type check the javaValue
				TypeChecker typeChecker = new TypeChecker(declaredJavaValue, varCtx.variableInitializer().expression());
				typeChecker.verify();
			}
			
		}
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * Attempts to create an intermediate representation of the variable once a sufficient amount of info has been retrieved.
	 */
	private void createJavaValue() {
		
		if(identifiedTokens.containsTokens(ClassAnalyzer.ACCESS_CONTROL_KEY, ClassAnalyzer.PRIMITIVE_TYPE_KEY, ClassAnalyzer.IDENTIFIER_KEY)) {
			
			String classModifierString = identifiedTokens.getToken(ClassAnalyzer.ACCESS_CONTROL_KEY);
			String primitiveTypeString = identifiedTokens.getToken(ClassAnalyzer.PRIMITIVE_TYPE_KEY);
			String identifierString = identifiedTokens.getToken(ClassAnalyzer.IDENTIFIER_KEY);
			String identifierValueString;

			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Class modifier: " +classModifierString));
			
			if(identifiedTokens.containsTokens(ClassAnalyzer.IDENTIFIER_VALUE_KEY)) {
				identifierValueString = identifiedTokens.getToken(ClassAnalyzer.IDENTIFIER_VALUE_KEY);
				declaredClassScope.addInitializedVariableFromKeywords(classModifierString, primitiveTypeString, identifierString, identifierValueString);
			}
			else {
				declaredClassScope.addEmptyVariableFromKeywords(classModifierString, primitiveTypeString, identifierString);
			}
			
			JavaValue declaredValue = declaredClassScope.searchVariableIncludingLocal(identifierString);

			//verify if the declared variable is a constant
			if(identifiedTokens.containsTokens(ClassAnalyzer.CONST_CONTROL_KEY)) {
				declaredValue.markFinal();
			}
			
			//remove the following tokens
			identifiedTokens.removeToken(ClassAnalyzer.IDENTIFIER_KEY);
			identifiedTokens.removeToken(ClassAnalyzer.IDENTIFIER_VALUE_KEY);
		}
	}
}
