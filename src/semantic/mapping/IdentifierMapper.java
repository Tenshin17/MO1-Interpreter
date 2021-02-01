package semantic.mapping;

import antlr.Java8Parser.*;
import antlr.Java8Parser.ExpressionContext;
import antlr.Java8Parser.NormalClassDeclarationContext;
import antlr.Java8Parser.MethodDeclaratorContext;
import Execution.FunctionTracker;
import semantic.representation.JavaValue;

/**
 * An identifier mapper that delegates the behavior to a class or function mapper depending on the control flow of execution.
 *
 */
public class IdentifierMapper implements IValueMapper{
	
	private IValueMapper valueMapper;
	
	public IdentifierMapper(String originalExp) {
		if(FunctionTracker.getInstance().isInsideFunction()) {
			valueMapper = new FunctionIdentifierMapper(originalExp, FunctionTracker.getInstance().getLatestFunction());
		}
		else {
			valueMapper = new ClassIdentifierMapper(originalExp);
		}
	}

	@Override
	public void analyze(ExpressionContext exprCtx) {
		valueMapper.analyze(exprCtx);
	}

	@Override
	public void analyze(NormalClassDeclarationContext exprCtx) {
		valueMapper.analyze(exprCtx);
	}

	@Override
	public void analyze(MethodDeclaratorContext exprCtx) {
		valueMapper.analyze(exprCtx);
	}

	@Override
	public void analyze(ConditionalExpressionContext exprCtx) { valueMapper.analyze(exprCtx); }

    @Override
    public void analyze(PostfixExpressionContext exprCtx) { valueMapper.analyze(exprCtx); }

	@Override
	public String getOriginalExp() {
		return valueMapper.getOriginalExp();
	}

	@Override
	public String getModifiedExp() {
		return valueMapper.getModifiedExp();
	}

	@Override
	public JavaValue getJavaValue() {
		return valueMapper.getJavaValue();
	}
}
