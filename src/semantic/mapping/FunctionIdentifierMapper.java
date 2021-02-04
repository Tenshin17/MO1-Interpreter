package semantic.mapping;

import antlr.Java8Parser;
import antlr.Java8Parser.ExpressionContext;
import antlr.Java8Parser.NormalClassDeclarationContext;
import antlr.Java8Parser.MethodDeclaratorContext;
import error.ParserHandler;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import semantic.symboltable.scope.LocalScope;
import semantic.symboltable.scope.LocalScopeCreator;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Maps an identifier to a given value found in the function level. First, we search the mapped parameters if a variable already exists.
 * Then we search its parent local scope using depth-first search.
 *
 */
public class FunctionIdentifierMapper implements ParseTreeListener, IValueMapper {
	
	private String originalExp = null;
	private String modifiedExp = null;
	
	private JavaMethod assignedFunction;
	private JavaValue javaValue;
	private LocalScope functionLocalScope;
	
	public FunctionIdentifierMapper(String originalExp, JavaMethod assignedFunction) {
		this.originalExp = originalExp;
		this.modifiedExp = originalExp;
		this.assignedFunction = assignedFunction;
		this.functionLocalScope = assignedFunction.getParentLocalScope();
	}
	
	@Override
	public void analyze(ExpressionContext exprCtx) {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, exprCtx);
	}

	@Override
	public void analyze(NormalClassDeclarationContext exprCtx) {
	}
	
	@Override
	public void analyze(MethodDeclaratorContext exprCtx) {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, exprCtx);
	}

	@Override
	public void analyze(Java8Parser.ConditionalExpressionContext exprCtx) {

	}

	@Override
	public void analyze(Java8Parser.PostfixExpressionContext exprCtx) {

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
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		if(ctx instanceof MethodDeclaratorContext) {
			MethodDeclaratorContext primaryCtx = (MethodDeclaratorContext) ctx;
			
			if(primaryCtx.Identifier() != null) {
				String variableKey = primaryCtx.getText();
				searchVariable(variableKey);
			}
		}
	}
	
	private void searchVariable(String identifierString) {
		if(this.assignedFunction.hasParameter(identifierString)) {
			this.modifiedExp = this.modifiedExp.replace(identifierString, this.assignedFunction.getParameter(identifierString).getValue().toString());
		}
		else {
			this.javaValue = LocalScopeCreator.searchVariableInLocalIterative(identifierString, this.functionLocalScope);
			
			if(this.javaValue != null) {
				this.modifiedExp = this.modifiedExp.replace(identifierString, this.javaValue.getValue().toString());
			}
			else {
				ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
				this.javaValue = classScope.searchVariableIncludingLocal(identifierString);

				this.modifiedExp = this.modifiedExp.replace(identifierString, this.javaValue.getValue().toString());
			}
		}
	}
	
	@Override
	public JavaValue getJavaValue() {
		return javaValue;
	}
	
	@Override
	public String getOriginalExp() {
		return originalExp;
	}
	
	@Override
	public String getModifiedExp() {
		return modifiedExp;
	}
}
