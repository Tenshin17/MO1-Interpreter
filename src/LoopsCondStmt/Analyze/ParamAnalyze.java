package LoopsCondStmt.Analyze;

import baraco.antlr.parser.BaracoParser;
import baraco.representations.BaracoArray;
import baraco.representations.BaracoMethod;
import baraco.representations.BaracoValue;
import baraco.representations.RecognizedKeywords;
import baraco.semantics.utils.IdentifiedTokens;

import antlr.Java8Parser;
import VarAndConstDec.javaArray;
import VarAndConstDec.javaMethod;
import VarAndConstDec.javaValue;
import VarAndConstDec.javaKeywords;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ParamAnalyze implements ParseTreeListener {

    private final static String PARAMETER_TYPE_KEY = "PARAMETER_TYPE_KEY";
    private final static String PARAMETER_IDENTIFIER_KEY = "PARAMETER_IDENTIFIER_KEY";
    private final static String IS_ARRAY_KEY = "IS_ARRAY_KEY";


    private IdentifiedTokens identifiedTokens;
    private javaMethod declaredBaracoMethod;

    public ParamAnalyze(javaMethod declaredBaracoMethod) {
        this.declaredBaracoMethod = declaredBaracoMethod;
    }

    public void analyze(Java8Parser.FormalParameterListContext ctx) {
        this.identifiedTokens = new IdentifiedTokens();

        ParseTreeWalker treeWalker = new ParseTreeWalker();
        treeWalker.walk(this, ctx);
    }

    /* (non-Javadoc)
     * @see org.antlr.v4.runtime.tree.ParseTreeListener#visitTerminal(org.antlr.v4.runtime.tree.TerminalNode)
     */
    @Override
    public void visitTerminal(TerminalNode node) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.antlr.v4.runtime.tree.ParseTreeListener#visitErrorNode(org.antlr.v4.runtime.tree.ErrorNode)
     */
    @Override
    public void visitErrorNode(ErrorNode node) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.antlr.v4.runtime.tree.ParseTreeListener#enterEveryRule(org.antlr.v4.runtime.ParserRuleContext)
     */
    @Override
    public void enterEveryRule(ParserRuleContext ctx) {
        if(ctx instanceof Java8Parser.FormalParameterContext) {
            Java8Parser.FormalParameterContext formalParamCtx = (Java8Parser.FormalParameterContext) ctx;
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

    private void analyzeParameter(Java8Parser.FormalParameterContext formalParamCtx) {
        if(formalParamCtx.typeType() != null) {
            Java8Parser.TypeTypeContext typeCtx = formalParamCtx.typeType();

            //return type is a primitive type
            if(ClassAnalyzer.isPrimitiveDeclaration(typeCtx)) {
                Java8Parser.PrimitiveTypeContext primitiveTypeCtx = typeCtx.primitiveType();
                this.identifiedTokens.addToken(PARAMETER_TYPE_KEY, primitiveTypeCtx.getText());
            }
            //check if its array declaration
            else if(ClassAnalyzer.isPrimitiveArrayDeclaration(typeCtx)) {
                Java8Parser.PrimitiveTypeContext primitiveTypeCtx = typeCtx.primitiveType();
                this.identifiedTokens.addToken(PARAMETER_TYPE_KEY, primitiveTypeCtx.getText());
                this.identifiedTokens.addToken(IS_ARRAY_KEY, IS_ARRAY_KEY);
            }

            //return type is a string or a class type
            else {
                //a string type
                if(typeCtx.classOrInterfaceType().getText().contains(javaKeywords.PRIMITIVE_TYPE_STRING)) {
                    this.identifiedTokens.addToken(PARAMETER_TYPE_KEY, typeCtx.classOrInterfaceType().getText());
                }
            }
        }

        if(formalParamCtx.variableDeclaratorId() != null) {
            TerminalNode identifier = formalParamCtx.variableDeclaratorId().Identifier();
            this.identifiedTokens.addToken(PARAMETER_IDENTIFIER_KEY, identifier.getText());

            this.createBaracoValue();
        }

    }

    private void createBaracoValue() {
        if(this.identifiedTokens.containsTokens(IS_ARRAY_KEY, PARAMETER_TYPE_KEY, PARAMETER_IDENTIFIER_KEY)) {
            String typeString = this.identifiedTokens.getToken(PARAMETER_TYPE_KEY);
            String identifierString = this.identifiedTokens.getToken(PARAMETER_IDENTIFIER_KEY);

            //initialize an array mobivalue
            javaArray declaredArray = javaArray.createArray(typeString, identifierString);
            javaValue mobiValue = new javaValue(declaredArray, javaValue.PrimitiveType.ARRAY);
            this.declaredBaracoMethod.addParameter(identifierString, mobiValue);

            //Console.log(LogType.DEBUG, "Created array parameter for " +this.declaredBaracoMethod.getFunctionName());
        }
        else if(this.identifiedTokens.containsTokens(PARAMETER_TYPE_KEY, PARAMETER_IDENTIFIER_KEY)) {
            String typeString = this.identifiedTokens.getToken(PARAMETER_TYPE_KEY);
            String identifierString = this.identifiedTokens.getToken(PARAMETER_IDENTIFIER_KEY);

            javaValue mobiValue = javaValue.createEmptyVariableFromKeywords(typeString);
            this.declaredBaracoMethod.addParameter(identifierString, mobiValue);
        }

        this.identifiedTokens.clearTokens();
    }
}
