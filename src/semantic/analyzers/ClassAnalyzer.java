package semantic.analyzers;

import antlr.Java8Lexer;
import antlr.Java8Parser.*;
import Execution.ExecutionManager;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import semantic.utils.IdentifiedTokens;
import semantic.utils.RecognizedKeywords;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

/**
 * A bridge for analyzing creation of a class
 *
 */
public class ClassAnalyzer implements ParseTreeListener {
	
	private ClassScope declaredClassScope;
	private IdentifiedTokens identifiedTokens;
	
	public final static String ACCESS_CONTROL_KEY = "ACCESS_CONTROL_KEY";
	public final static String CONST_CONTROL_KEY = "CONST_CONSTROL_KEY";
	public final static String STATIC_CONTROL_KEY = "STATIC_CONTROL_KEY";
	public final static String PRIMITIVE_TYPE_KEY = "PRIMITIVE_TYPE_KEY";
	public final static String IDENTIFIER_KEY = "IDENTIFIER_KEY";
	public final static String IDENTIFIER_VALUE_KEY = "IDENTIFIER_VALUE_KEY";
	
	public ClassAnalyzer() {
		
	}
	
	public void analyze(ClassDeclarationContext ctx) {
		// String className = ctx.Identifier().getText();
		System.out.println("Class accessed: Main");

		declaredClassScope = new ClassScope("Main");
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
		if(ctx instanceof ClassDeclarationContext) {
			SymbolTableManager.getInstance().addClassScope(declaredClassScope.getClassName(), declaredClassScope);
		}
		
		this.analyzeClassMembers(ctx);
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		
	}
	
	private void analyzeClassMembers(ParserRuleContext ctx) {
		if(ctx instanceof ClassModifierContext) {
			ClassModifierContext classModifierCtx = (ClassModifierContext) ctx;
			
			analyzeModifier(classModifierCtx);
		}
		
		else if(ctx instanceof FieldDeclarationContext) {
			FieldDeclarationContext fieldCtx = (FieldDeclarationContext) ctx;
			
			if(fieldCtx.unannType() != null) {
				UnannTypeContext typeCtx = fieldCtx.unannType();
				
				//check if its a primitive type
				if(ClassAnalyzer.isPrimitiveDeclaration(typeCtx)) {
					UnannPrimitiveTypeContext primitiveTypeCtx = typeCtx.unannPrimitiveType();
					identifiedTokens.addToken(PRIMITIVE_TYPE_KEY, primitiveTypeCtx.getText());
					
					//create a field analyzer to walk through declarations
					FieldAnalyzer fieldAnalyzer = new FieldAnalyzer(identifiedTokens, declaredClassScope);
					fieldAnalyzer.analyze(fieldCtx.variableDeclaratorList());
					
					//clear tokens for reause
					identifiedTokens.clearTokens();
				}
				
				//check if its array declaration
				else if(ClassAnalyzer.isPrimitiveArrayDeclaration(typeCtx)) {
					ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Primitive array declaration: " +fieldCtx.getText()));
					ArrayAnalyzer arrayAnalyzer = new ArrayAnalyzer(identifiedTokens, declaredClassScope);
					arrayAnalyzer.analyze(fieldCtx);
				}
				
				//this is for class type ctx
				else {
					
					//a string identified
					if(typeCtx.unannReferenceType().unannClassOrInterfaceType().getText().contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
						UnannClassOrInterfaceTypeContext classInterfaceCtx = typeCtx.unannReferenceType().unannClassOrInterfaceType();
						identifiedTokens.addToken(PRIMITIVE_TYPE_KEY, classInterfaceCtx.getText());
					}
					
					//create a field analyzer to walk through declarations
					FieldAnalyzer fieldAnalyzer = new FieldAnalyzer(identifiedTokens, declaredClassScope);
					fieldAnalyzer.analyze(fieldCtx.variableDeclaratorList());
					
					//clear tokens for reause
					identifiedTokens.clearTokens();
				}
			}
		}
		
		else if(ctx instanceof MethodDeclarationContext) {
			MethodDeclarationContext methodDecCtx = (MethodDeclarationContext) ctx;
			MethodAnalyzer methodAnalyzer = new MethodAnalyzer(identifiedTokens, declaredClassScope);
			methodAnalyzer.analyze(methodDecCtx);
			
			//reuse tokens
			identifiedTokens.clearTokens();
		}
	}
	
	public static boolean isPrimitiveDeclaration(UnannTypeContext typeCtx) {
		if(typeCtx.unannPrimitiveType() != null) {
			List<TerminalNode> lBrackToken = typeCtx.getTokens(Java8Lexer.LBRACK);
			List<TerminalNode> rBrackToken = typeCtx.getTokens(Java8Lexer.RBRACK);
			
			return (lBrackToken.size() == 0 && rBrackToken.size() == 0);
		}
		
		return false;
	}
	
	public static boolean isPrimitiveArrayDeclaration(UnannTypeContext typeCtx) {
		if(typeCtx.unannPrimitiveType() != null) {
			List<TerminalNode> lBrackToken = typeCtx.getTokens(Java8Lexer.LBRACK);
			List<TerminalNode> rBrackToken = typeCtx.getTokens(Java8Lexer.RBRACK);
			
			return (lBrackToken.size() > 0 && rBrackToken.size() > 0);
		}
		
		return false;
	}
	
	private void analyzeModifier(ClassModifierContext ctx) {
		if(ctx.getTokens(Java8Lexer.PUBLIC).size() > 0 || ctx.getTokens(Java8Lexer.PRIVATE).size() > 0
				|| ctx.getTokens(Java8Lexer.PROTECTED).size() > 0) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Detected accessor: " +ctx.getText()));
			this.identifiedTokens.addToken(ACCESS_CONTROL_KEY, ctx.getText());
		}
		else if(ctx.getTokens(Java8Lexer.FINAL).size() > 0) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Detected const: " +ctx.getText()));
			this.identifiedTokens.addToken(CONST_CONTROL_KEY, ctx.getText());
		}
		else if(ctx.getTokens(Java8Lexer.STATIC).size() > 0) {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Detected static: " +ctx.getText()));
			this.identifiedTokens.addToken(STATIC_CONTROL_KEY, ctx.getText());
		}
	}
	
}
