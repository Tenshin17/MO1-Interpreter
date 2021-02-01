package semantic.analyzers;

import antlr.Java8Parser.*;
import error.checkers.MultipleFuncDecChecker;
import Execution.ExecutionManager;
import semantic.representation.JavaMethod;
import semantic.representation.JavaMethod.FunctionType;
import semantic.symboltable.scope.ClassScope;
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
 * Analyzes method declarations and properly stores them in the symbol table
 *
 */
public class MethodAnalyzer implements ParseTreeListener {
	
	private ClassScope declaredClassScope;
	private IdentifiedTokens identifiedTokens;
	private JavaMethod declaredJavaMethod;
	
	public MethodAnalyzer(IdentifiedTokens identifiedTokens, ClassScope declaredClassScope) {
		this.identifiedTokens = identifiedTokens;
		this.declaredClassScope = declaredClassScope;
		declaredJavaMethod = new JavaMethod();
	}
	
	public void analyze(MethodDeclarationContext ctx) {
		ExecutionManager.getExecutionManager().openFunctionExecution(declaredJavaMethod);
		
		ParseTreeWalker treeWalker = new ParseTreeWalker();
		treeWalker.walk(this, ctx);
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
		if(ctx instanceof MethodDeclarationContext) {
			MethodDeclarationContext methodDecCtx = (MethodDeclarationContext) ctx;
			MultipleFuncDecChecker funcDecChecker = new MultipleFuncDecChecker(methodDecCtx);
			funcDecChecker.verify();
			
			analyzeIdentifier(methodDecCtx.methodHeader().methodDeclarator().Identifier()); //get the function identifier
		}
		else {
			analyzeMethod(ctx);
		}
		
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		if(ctx instanceof MethodDeclarationContext) {
			ExecutionManager.getExecutionManager().closeFunctionExecution();
		}
	}
	
	private void analyzeMethod(ParserRuleContext ctx) {
		
		if(ctx instanceof UnannTypeContext) {
			UnannTypeContext typeCtx = (UnannTypeContext) ctx;
			
			//return type is a primitive type
			if(typeCtx.unannPrimitiveType() != null) {
				UnannPrimitiveTypeContext primitiveTypeCtx = typeCtx.unannPrimitiveType();
				declaredJavaMethod.setReturnType(JavaMethod.identifyFunctionType(primitiveTypeCtx.getText()));
			}
			//return type is a string or a class type
			else {
				analyzeClassOrInterfaceType(typeCtx.unannReferenceType().unannClassOrInterfaceType());
			}
		}
		
		else if(ctx instanceof FormalParameterListContext) {
			FormalParameterListContext formalParamsCtx = (FormalParameterListContext) ctx;
			analyzeParameters(formalParamsCtx);
			storeJavaMethod();
		}
		
		else if(ctx instanceof MethodBodyContext) {
			BlockContext blockCtx = ((MethodBodyContext) ctx).block();
			
			BlockAnalyzer blockAnalyzer = new BlockAnalyzer();
			declaredJavaMethod.setParentLocalScope(LocalScopeCreator.getInstance().getActiveLocalScope());
			blockAnalyzer.analyze(blockCtx.blockStatements());
			
		}
	}
	
	private void analyzeClassOrInterfaceType(UnannClassOrInterfaceTypeContext classOrInterfaceCtx) {
		//a string identified
		if(classOrInterfaceCtx.getText().contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
			declaredJavaMethod.setReturnType(FunctionType.STRING_TYPE);
		}
		//a class identified
		else {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Class identified: " + classOrInterfaceCtx.getText()));
		}
	}
	
	private void analyzeIdentifier(TerminalNode identifier) {
		declaredJavaMethod.setFunctionName(identifier.getText());
	}
	
	private void analyzeParameters(FormalParameterListContext formalParamsCtx) {
		if(formalParamsCtx != null) {
			ParameterAnalyzer parameterAnalyzer = new ParameterAnalyzer(declaredJavaMethod);
			parameterAnalyzer.analyze(formalParamsCtx);
		}
	}
	
	/*
	 * Stores the created function in its corresponding class scope
	 */
	private void storeJavaMethod() {
		if(this.identifiedTokens.containsTokens(ClassAnalyzer.ACCESS_CONTROL_KEY)) {
			String accessToken = identifiedTokens.getToken(ClassAnalyzer.ACCESS_CONTROL_KEY);
			
			if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.CLASS_MODIFIER_PRIVATE, accessToken)) {
				declaredClassScope.addPrivateJavaMethod(declaredJavaMethod.getFunctionName(), declaredJavaMethod);
			}
			else if(RecognizedKeywords.matchesKeyword(RecognizedKeywords.CLASS_MODIFIER_PUBLIC, accessToken)) {
				declaredClassScope.addPublicJavaMethod(declaredJavaMethod.getFunctionName(), declaredJavaMethod);
			}
			
			identifiedTokens.clearTokens(); //clear tokens for reuse
		}
		declaredClassScope.addPublicJavaMethod(declaredJavaMethod.getFunctionName(), declaredJavaMethod);
		identifiedTokens.clearTokens(); //clear tokens for reuse
	}

}
