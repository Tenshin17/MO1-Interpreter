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
			this.valueMapper = new FunctionIdentifierMapper(originalExp, FunctionTracker.getInstance().getLatestFunction());
		}
		else {
			this.valueMapper = new ClassIdentifierMapper(originalExp);
		}
	}

	@Override
	public void analyze(ExpressionContext exprCtx) {
		this.valueMapper.analyze(exprCtx);
	}

	@Override
	public void analyze(NormalClassDeclarationContext exprCtx) {
		this.valueMapper.analyze(exprCtx);
	}

	@Override
	public void analyze(MethodDeclaratorContext exprCtx) {
		this.valueMapper.analyze(exprCtx);
	}

	@Override
	public void analyze(ConditionalExpressionContext exprCtx) { this.valueMapper.analyze(exprCtx); }

    @Override
    public void analyze(PostfixExpressionContext exprCtx) { this.valueMapper.analyze(exprCtx); }

	@Override
	public String getOriginalExp() {
		return this.valueMapper.getOriginalExp();
	}

	@Override
	public String getModifiedExp() {
		return this.valueMapper.getModifiedExp();
	}

	@Override
	public JavaValue getJavaValue() {
		return this.valueMapper.getJavaValue();
	}
}
