package semantic.analyzers;

import antlr.Java8Parser.FormalParameterContext;
import antlr.Java8Parser.FormalParameterListContext;
import antlr.Java8Parser.UnannPrimitiveTypeContext;
import antlr.Java8Parser.UnannTypeContext;
import semantic.representation.JavaArray;
import semantic.representation.JavaMethod;
import semantic.representation.JavaValue;
import semantic.representation.JavaValue.PrimitiveType;
import semantic.utils.IdentifiedTokens;
import semantic.utils.RecognizedKeywords;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * An analyzer for method parameters
 *
 */
public class ParameterAnalyzer implements ParseTreeListener {

	private final static String PARAMETER_TYPE_KEY = "PARAMETER_TYPE_KEY";
	private final static String PARAMETER_IDENTIFIER_KEY = "PARAMETER_IDENTIFIER_KEY";
	private final static String IS_ARRAY_KEY = "IS_ARRAY_KEY";
	
	
	private IdentifiedTokens identifiedTokens;
	private JavaMethod declaredJavaMethod;
	
	public ParameterAnalyzer(JavaMethod declaredJavaMethod) {
		this.declaredJavaMethod = declaredJavaMethod;
	}
	
	public void analyze(FormalParameterListContext ctx) {
		identifiedTokens = new IdentifiedTokens();
		
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
		if(ctx instanceof FormalParameterContext) {
			FormalParameterContext formalParamCtx = (FormalParameterContext) ctx;
			this.analyzeParameter(formalParamCtx);
		}
	}

	/* (non-Javadoc)
	 * @see org.antlr.v4.runtime.tree.ParseTreeListener#exitEveryRule(org.antlr.v4.runtime.ParserRuleContext)
	 */
	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub

	}
	
	private void analyzeParameter(FormalParameterContext formalParamCtx) {
		if(formalParamCtx.unannType() != null) {
			UnannTypeContext typeCtx = formalParamCtx.unannType();
			
			//return type is a primitive type
			if(ClassAnalyzer.isPrimitiveDeclaration(typeCtx)) {
				UnannPrimitiveTypeContext unannPrimitiveTypeCtx = typeCtx.unannPrimitiveType();
				this.identifiedTokens.addToken(PARAMETER_TYPE_KEY, unannPrimitiveTypeCtx.getText());
			}
			//check if its array declaration
			else if(ClassAnalyzer.isPrimitiveArrayDeclaration(typeCtx)) {
				UnannPrimitiveTypeContext unannPrimitiveTypeCtx = typeCtx.unannPrimitiveType();
				this.identifiedTokens.addToken(PARAMETER_TYPE_KEY, unannPrimitiveTypeCtx.getText());
				this.identifiedTokens.addToken(IS_ARRAY_KEY, IS_ARRAY_KEY);
			}
			
			//return type is a string or a class type
			else {
				//a string type
				if(typeCtx.unannReferenceType().unannClassOrInterfaceType().getText().contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
					this.identifiedTokens.addToken(PARAMETER_TYPE_KEY, typeCtx.unannReferenceType().unannClassOrInterfaceType().getText());
				}
			}
		}
		
		if(formalParamCtx.variableDeclaratorId() != null) {
			TerminalNode identifier = formalParamCtx.variableDeclaratorId().Identifier();
			this.identifiedTokens.addToken(PARAMETER_IDENTIFIER_KEY, identifier.getText());
			
			this.createJavaValue();
		}
		
	}
	
	private void createJavaValue() {
		if(this.identifiedTokens.containsTokens(IS_ARRAY_KEY, PARAMETER_TYPE_KEY, PARAMETER_IDENTIFIER_KEY)) {
			String typeString = this.identifiedTokens.getToken(PARAMETER_TYPE_KEY);
			String identifierString = this.identifiedTokens.getToken(PARAMETER_IDENTIFIER_KEY);
			
			//initialize an array javaValue
			JavaArray declaredArray = JavaArray.createArray(typeString, identifierString);
			JavaValue javaValue = new JavaValue(declaredArray, PrimitiveType.ARRAY);
			this.declaredJavaMethod.addParameter(identifierString, javaValue);
			
			//Console.log(LogType.DEBUG, "Created array parameter for " +this.declaredJavaMethod.getFunctionName());
		}
		else if(this.identifiedTokens.containsTokens(PARAMETER_TYPE_KEY, PARAMETER_IDENTIFIER_KEY)) {
			String typeString = this.identifiedTokens.getToken(PARAMETER_TYPE_KEY);
			String identifierString = this.identifiedTokens.getToken(PARAMETER_IDENTIFIER_KEY);
			
			JavaValue javaValue = JavaValue.createEmptyVariableFromKeywords(typeString);
			this.declaredJavaMethod.addParameter(identifierString, javaValue);
		}
		
		this.identifiedTokens.clearTokens();
	}

}
