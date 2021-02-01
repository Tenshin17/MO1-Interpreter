package semantic.mapping;

import antlr.Java8Parser.PostfixExpressionContext;
import antlr.Java8Parser.ExpressionContext;
import antlr.Java8Parser.NormalClassDeclarationContext;
import antlr.Java8Parser.MethodDeclaratorContext;
import antlr.Java8Parser.ConditionalExpressionContext;
import semantic.representation.JavaValue;

public interface IValueMapper {

	void analyze(ExpressionContext exprCtx);
	void analyze(NormalClassDeclarationContext exprCtx);
	void analyze(MethodDeclaratorContext exprCtx);
	void analyze(ConditionalExpressionContext exprCtx);
	void analyze(PostfixExpressionContext exprCtx);
	String getOriginalExp();
	String getModifiedExp();
	JavaValue getJavaValue();

}