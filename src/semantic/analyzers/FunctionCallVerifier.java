package semantic.analyzers;

import antlr.Java8Parser.*;
import antlr.Java8Parser.MethodInvocationContext;
import error.ParserHandler;
import error.checkers.ParameterMismatchChecker;
import Execution.command.evaluation.EvaluationCommand;
import semantic.representation.JavaMethod;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

public class FunctionCallVerifier implements ParseTreeListener {

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
			if(ctx instanceof MethodInvocationContext) {
				MethodInvocationContext exprCtx = (MethodInvocationContext) ctx;
				if (EvaluationCommand.isFunctionCall(exprCtx)) {
					if(exprCtx.methodName().Identifier() == null)
						return;
					
					String functionName = exprCtx.methodName().Identifier().getText();

					ClassScope classScope = SymbolTableManager.getInstance().getClassScope(
							ParserHandler.getInstance().getCurrentClassName());
					JavaMethod javaMethod = classScope.searchMethod(functionName);
					
					if (exprCtx.argumentList() != null) {
						ParameterMismatchChecker paramsMismatchChecker = new ParameterMismatchChecker(javaMethod, exprCtx.argumentList());
						paramsMismatchChecker.verify();
					}
				}
			}
		}

		@Override
		public void exitEveryRule(ParserRuleContext ctx) {
			// TODO Auto-generated method stub
			
		}
		
	}