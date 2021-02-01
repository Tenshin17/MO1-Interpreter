package error.checkers;

import antlr.Java8Parser.VariableDeclaratorIdContext;
import error.CustomErrorStrategy;
import error.ParserHandler;
import Execution.ExecutionManager;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.searching.VariableSearcher;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import semantic.symboltable.scope.LocalScopeCreator;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Checks for multiple declarations of variables.
 *
 */
public class MultipleVarDecChecker implements IErrorChecker, ParseTreeListener {

	private VariableDeclaratorIdContext varDecIdCtx;
	private int lineNumber;
	
	public MultipleVarDecChecker(VariableDeclaratorIdContext varDecIdCtx) {
		this.varDecIdCtx = varDecIdCtx;
		
		Token firstToken = this.varDecIdCtx.getStart();
		this.lineNumber = firstToken.getLine();
	}
	
	/* (non-Javadoc)
	 * @see error.checkers.IErrorChecker#verify()
	 */
	@Override
	public void verify() {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, this.varDecIdCtx);
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
		if(ctx instanceof VariableDeclaratorIdContext) {
			VariableDeclaratorIdContext varDecCtx = (VariableDeclaratorIdContext) ctx;
			this.verifyVariableOrConst(varDecCtx.getText());
		}
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub
		
	}
	
	private void verifyVariableOrConst(String identifierString) {
		JavaValue javaValue = null;
		
		if(ExecutionManager.getExecutionManager().isInFunctionExecution()) {
			JavaMethod javaMethod = ExecutionManager.getExecutionManager().getCurrentFunction();
			javaValue = VariableSearcher.searchVariableInFunction(javaMethod, identifierString);
		}
		
		//if after function finding, java value is still null, search local scope
		if(javaValue == null) {
			javaValue = LocalScopeCreator.searchVariableInLocalIterative(identifierString, LocalScopeCreator.getInstance().getActiveLocalScope());
		}
		
		//if java value is still null, search class
		if(javaValue == null) {
			ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
			javaValue = VariableSearcher.searchVariableInClass(classScope, identifierString);
		}
		
		
		if(javaValue != null) {
			CustomErrorStrategy.reportSemanticError(CustomErrorStrategy.MULTIPLE_VARIABLE, identifierString, this.lineNumber);
		}
	}
	

}
