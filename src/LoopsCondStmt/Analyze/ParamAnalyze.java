package baraco.semantics.analyzers;
//package LoopsCondStmt.Analyze;
//package LoopsCondStmt.Analyze;

import baraco.antlr.parser.BaracoParser;
import baraco.representations.BaracoArray;
import baraco.representations.BaracoMethod;
import baraco.representations.BaracoValue;
import baraco.representations.RecognizedKeywords;
import baraco.semantics.utils.IdentifiedTokens;
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
    private BaracoMethod declaredBaracoMethod;

    public ParameterAnalyzer(BaracoMethod declaredBaracoMethod) {
        this.declaredBaracoMethod = declaredBaracoMethod;
    }

    public void analyze(BaracoParser.FormalParameterListContext ctx) {
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
        if(ctx instanceof BaracoParser.FormalParameterContext) {
            BaracoParser.FormalParameterContext formalParamCtx = (BaracoParser.FormalParameterContext) ctx;
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

    private void analyzeParameter(BaracoParser.FormalParameterContext formalParamCtx) {
        if(formalParamCtx.typeType() != null) {
            BaracoParser.TypeTypeContext typeCtx = formalParamCtx.typeType();

            //return type is a primitive type
            if(ClassAnalyzer.isPrimitiveDeclaration(typeCtx)) {
                BaracoParser.PrimitiveTypeContext primitiveTypeCtx = typeCtx.primitiveType();
                this.identifiedTokens.addToken(PARAMETER_TYPE_KEY, primitiveTypeCtx.getText());
            }
            //check if its array declaration
            else if(ClassAnalyzer.isPrimitiveArrayDeclaration(typeCtx)) {
                BaracoParser.PrimitiveTypeContext primitiveTypeCtx = typeCtx.primitiveType();
                this.identifiedTokens.addToken(PARAMETER_TYPE_KEY, primitiveTypeCtx.getText());
                this.identifiedTokens.addToken(IS_ARRAY_KEY, IS_ARRAY_KEY);
            }

            //return type is a string or a class type
            else {
                //a string type
                if(typeCtx.classOrInterfaceType().getText().contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
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
            BaracoArray declaredArray = BaracoArray.createArray(typeString, identifierString);
            BaracoValue mobiValue = new BaracoValue(declaredArray, BaracoValue.PrimitiveType.ARRAY);
            this.declaredBaracoMethod.addParameter(identifierString, mobiValue);

            //Console.log(LogType.DEBUG, "Created array parameter for " +this.declaredBaracoMethod.getFunctionName());
        }
        else if(this.identifiedTokens.containsTokens(PARAMETER_TYPE_KEY, PARAMETER_IDENTIFIER_KEY)) {
            String typeString = this.identifiedTokens.getToken(PARAMETER_TYPE_KEY);
            String identifierString = this.identifiedTokens.getToken(PARAMETER_IDENTIFIER_KEY);

            BaracoValue mobiValue = BaracoValue.createEmptyVariableFromKeywords(typeString);
            this.declaredBaracoMethod.addParameter(identifierString, mobiValue);
        }

        this.identifiedTokens.clearTokens();
    }
}
