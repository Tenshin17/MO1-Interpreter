package semantic.mapping;

import antlr.Java8Parser;
import antlr.Java8Parser.ExpressionContext;
import antlr.Java8Parser.NormalClassDeclarationContext;
import antlr.Java8Parser.MethodDeclaratorContext;
import error.ParserHandler;
import semantic.representation.JavaValue;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Maps an identifier to a given value found in the symbol table manager in the class level.

 *
 */
public class ClassIdentifierMapper implements ParseTreeListener, IValueMapper {
	
	private JavaValue javaValue;
	private String originalExp = null;
	private String modifiedExp = null;
	
	public ClassIdentifierMapper(String originalExp) {
		this.originalExp = originalExp;
		this.modifiedExp = originalExp;
	}

	@Override
	public void analyze(ExpressionContext exprCtx) {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, exprCtx);
	}

	@Override
	public void analyze(NormalClassDeclarationContext exprCtx) {
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, exprCtx);
	}

	@Override
	public void analyze(MethodDeclaratorContext exprCtx) {
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
		if(ctx instanceof NormalClassDeclarationContext) {
			NormalClassDeclarationContext primaryCtx = (NormalClassDeclarationContext) ctx;
			
			if(primaryCtx.Identifier() != null) {
				String variableKey = primaryCtx.getText();
				ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
				
				this.javaValue = classScope.searchVariableIncludingLocal(variableKey);
				this.modifiedExp = this.modifiedExp.replace(variableKey, this.javaValue.getValue().toString());
			}
		}
	}
	
	@Override
	public JavaValue getJavaValue() {
		return this.javaValue;
	}

	@Override
	public String getOriginalExp() {
		return this.originalExp;
	}

	@Override
	public String getModifiedExp() {
		return this.modifiedExp;
	}

}
