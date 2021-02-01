package error.checkers;

import antlr.Java8Parser.MethodDeclarationContext;
import error.*;
import semantic.representation.JavaMethod;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import org.antlr.v4.runtime.Token;

/**
 * Checks for duplicate function declarations
 *
 */
public class MultipleFuncDecChecker implements IErrorChecker {
	
	private MethodDeclarationContext methodDecCtx;
	private int lineNumber;
	
	public MultipleFuncDecChecker(MethodDeclarationContext methodDecCtx) {
		this.methodDecCtx = methodDecCtx;
		
		Token firstToken = methodDecCtx.getStart();
		this.lineNumber = firstToken.getLine();
	}

	@Override
	public void verify() {
		this.verifyFunctionCall(this.methodDecCtx.methodHeader().methodDeclarator().Identifier().getText());
	}
	
	private void verifyFunctionCall(String identifierString) {

		ClassScope classScope = SymbolTableManager.getInstance().getClassScope(
				ParserHandler.getInstance().getCurrentClassName());
		JavaMethod javaMethod = classScope.searchMethod(identifierString);
		
		if(javaMethod != null) {
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.MULTIPLE_FUNCTION, identifierString, lineNumber);
		}
	}

}
