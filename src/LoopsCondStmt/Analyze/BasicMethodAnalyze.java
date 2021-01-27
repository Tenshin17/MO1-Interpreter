package baraco.semantics.analyzers;
//package LoopsCondStmt.Analyze;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import baraco.antlr.parser.BaracoParser;
import baraco.builder.BuildChecker;
import baraco.builder.ErrorRepository;
import baraco.builder.errorcheckers.MultipleMethodDeclarationChecker;
import baraco.execution.ExecutionManager;
import baraco.representations.BaracoMethod;
import baraco.representations.RecognizedKeywords;
import baraco.semantics.symboltable.SymbolTableManager;
import baraco.semantics.symboltable.scopes.ClassScope;
import baraco.semantics.symboltable.scopes.LocalScopeCreator;
import baraco.semantics.utils.IdentifiedTokens;

public class BasicMethodAnalyze implements ParseTreeListener {

    private ClassScope declaredClassScope;
    private BaracoMethod declaredBaracoFunction;
    private boolean paramsFlag = false;

    public BasicMethodAnalyzer() {
        this.declaredClassScope = SymbolTableManager.getInstance().getLatestScope();
        this.declaredBaracoFunction = new BaracoMethod();
    }

    public void analyze(BaracoParser.MethodDeclarationContext ctx) {
        ExecutionManager.getInstance().openFunctionExecution(this.declaredBaracoFunction);

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
        if(ctx instanceof BaracoParser.MethodDeclarationContext) {
            BaracoParser.MethodDeclarationContext methodDecCtx = (BaracoParser.MethodDeclarationContext) ctx;
            MultipleMethodDeclarationChecker funcDecChecker = new MultipleMethodDeclarationChecker(methodDecCtx);
            funcDecChecker.verify();

            this.analyzeIdentifier(methodDecCtx.Identifier()); //get the function identifier
        }
        else {
            this.analyzeMethod(ctx);
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        if(ctx instanceof BaracoParser.MethodDeclarationContext) {

            BaracoParser.MethodDeclarationContext mdCtx = (BaracoParser.MethodDeclarationContext) ctx;

            if (!this.declaredBaracoFunction.hasValidReturns()) {

                int lineNumber = 0;

                if (mdCtx.Identifier() != null)
                    lineNumber = mdCtx.Identifier().getSymbol().getLine();

                BuildChecker.reportCustomError(ErrorRepository.NO_RETURN_STATEMENT, "", this.declaredBaracoFunction.getMethodName(), lineNumber);
            }


            ExecutionManager.getInstance().closeFunctionExecution();
        }
    }

    private void analyzeMethod(ParserRuleContext ctx) {

        if(ctx instanceof BaracoParser.TypeTypeContext && !paramsFlag) {
            BaracoParser.TypeTypeContext typeCtx = (BaracoParser.TypeTypeContext) ctx;

            //return type is a primitive type
            if(typeCtx.primitiveType() != null) {
                BaracoParser.PrimitiveTypeContext primitiveTypeCtx = typeCtx.primitiveType();
                this.declaredBaracoFunction.setReturnType(BaracoMethod.identifyFunctionType(primitiveTypeCtx.getText()));
            }
            //return type is a string or a class type
            else {
                this.analyzeClassOrInterfaceType(typeCtx.classOrInterfaceType());
            }
        }

        else if(ctx instanceof BaracoParser.FormalParametersContext) {

            paramsFlag = true;

            BaracoParser.FormalParametersContext formalParamsCtx = (BaracoParser.FormalParametersContext) ctx;
            this.analyzeParameters(formalParamsCtx);
            this.storeMobiFunction();
        }

        else if(ctx instanceof BaracoParser.MethodBodyContext) {

            BaracoParser.BlockContext blockCtx = ((BaracoParser.MethodBodyContext) ctx).block();

            BlockAnalyzer blockAnalyzer = new BlockAnalyzer();
            this.declaredBaracoFunction.setParentLocalScope(LocalScopeCreator.getInstance().getActiveLocalScope());
            blockAnalyzer.analyze(blockCtx);

        }

    }

    private void analyzeClassOrInterfaceType(BaracoParser.ClassOrInterfaceTypeContext classOrInterfaceCtx) {
        //a string identified
        if(classOrInterfaceCtx.getText().contains(RecognizedKeywords.PRIMITIVE_TYPE_STRING)) {
            this.declaredBaracoFunction.setReturnType(BaracoMethod.MethodType.STRING_TYPE);
        }
        //a class identified
        else {
            //Console.log(LogType.DEBUG, "Class identified: " + classOrInterfaceCtx.getText());
        }
    }

    private void analyzeIdentifier(TerminalNode identifier) {
        this.declaredBaracoFunction.setMethodName(identifier.getText());
    }

    private void analyzeParameters(BaracoParser.FormalParametersContext formalParamsCtx) {
        if(formalParamsCtx.formalParameterList() != null) {
            ParameterAnalyzer parameterAnalyzer = new ParameterAnalyzer(this.declaredBaracoFunction);
            parameterAnalyzer.analyze(formalParamsCtx.formalParameterList());
        }
    }

    /*
     * Stores the created function in its corresponding class scope
     */
    private void storeMobiFunction() {
        this.declaredClassScope.addPrivateBaracoMethod(this.declaredBaracoFunction.getMethodName(), this.declaredBaracoFunction);
    }

}
