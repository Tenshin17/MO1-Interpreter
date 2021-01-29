package baraco.semantics.analyzers;
//package LoopsCondStmt.Analyze;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import VarAndConstDec.javaMethod;
import VarAndConstDec.javaKeywords;
import symboltable.scope.ClassScope;
import symboltable.scope.LocalScopeCreator;
import symboltable.SymbolTableManager;
import antlr.Java8Parser;


//import baraco.antlr.parser.BaracoParser;
//import baraco.builder.BuildChecker;
//import baraco.builder.ErrorRepository;
//import baraco.builder.errorcheckers.MultipleMethodDeclarationChecker;
//import baraco.execution.ExecutionManager;
//import baraco.representations.BaracoMethod;
//import baraco.representations.RecognizedKeywords;
//import baraco.semantics.symboltable.SymbolTableManager;
//import baraco.semantics.symboltable.scopes.ClassScope;
//import baraco.semantics.symboltable.scopes.LocalScopeCreator;
//import baraco.semantics.utils.IdentifiedTokens;

public class BasicMethodAnalyze implements ParseTreeListener {

    private ClassScope declaredClassScope;
//    private BaracoMethod declaredBaracoFunction;
    private javaMethod declaredjavaFunction;
    private boolean paramsFlag = false;

    public BasicMethodAnalyzer() {
        this.declaredClassScope = SymbolTableManager.getInstance().getLatestScope();
//        this.declaredBaracoFunction = new BaracoMethod();
        this.declaredjavaFunction = new javaMethod();
    }

//    public void analyze(BaracoParser.MethodDeclarationContext ctx) {
    public void analyze(Java8Parser.MethodDeclarationContext ctx) {
        ExecutionManager.getInstance().openFunctionExecution(this.declaredjavaFunction);

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
        if(ctx instanceof Java8Parser.MethodDeclarationContext) {
            Java8Parser.MethodDeclarationContext methodDecCtx = (Java8Parser.MethodDeclarationContext) ctx;
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
        if(ctx instanceof Java8Parser.MethodDeclarationContext) {

            Java8Parser.MethodDeclarationContext mdCtx = (Java8Parser.MethodDeclarationContext) ctx;

            if (!this.declaredjavaFunction.hasValidReturns()) {

                int lineNumber = 0;

                if (mdCtx.Identifier() != null)
                    lineNumber = mdCtx.Identifier().getSymbol().getLine();

                BuildChecker.reportCustomError(ErrorRepository.NO_RETURN_STATEMENT, "", this.declaredjavaFunction.getMethodName(), lineNumber);
            }


            ExecutionManager.getInstance().closeFunctionExecution();
        }
    }

    private void analyzeMethod(ParserRuleContext ctx) {

        if(ctx instanceof Java8Parser.TypeTypeContext && !paramsFlag) {
            Java8Parser.TypeTypeContext typeCtx = (Java8Parser.TypeTypeContext) ctx;

            //return type is a primitive type
            if(typeCtx.primitiveType() != null) {
                Java8Parser.PrimitiveTypeContext primitiveTypeCtx = typeCtx.primitiveType();
                this.declaredjavaFunction.setReturnType(javaMethod.identifyFunctionType(primitiveTypeCtx.getText()));
            }
            //return type is a string or a class type
            else {
                this.analyzeClassOrInterfaceType(typeCtx.classOrInterfaceType());
            }
        }

        else if(ctx instanceof Java8Parser.FormalParametersContext) {

            paramsFlag = true;

            Java8Parser.FormalParametersContext formalParamsCtx = (Java8Parser.FormalParametersContext) ctx;
            this.analyzeParameters(formalParamsCtx);
            this.storeMobiFunction();
        }

        else if(ctx instanceof Java8Parser.MethodBodyContext) {

            Java8Parser.BlockContext blockCtx = ((Java8Parser.MethodBodyContext) ctx).block();

            BlockAnalyzer blockAnalyzer = new BlockAnalyzer();
            this.declaredjavaFunction.setParentLocalScope(LocalScopeCreator.getInstance().getActiveLocalScope());
            blockAnalyzer.analyze(blockCtx);

        }

    }

    private void analyzeClassOrInterfaceType(Java8Parser.ClassOrInterfaceTypeContext classOrInterfaceCtx) {
        //a string identified
        if(classOrInterfaceCtx.getText().contains(javaKeywords.PRIMITIVE_TYPE_STRING)) {
            this.declaredjavaFunction.setReturnType(javaMethod.MethodType.STRING_TYPE);
        }
        //a class identified
        else {
            //Console.log(LogType.DEBUG, "Class identified: " + classOrInterfaceCtx.getText());
        }
    }

    private void analyzeIdentifier(TerminalNode identifier) {
        this.declaredjavaFunction.setMethodName(identifier.getText());
    }

    private void analyzeParameters(Java8Parser.FormalParametersContext formalParamsCtx) {
        if(formalParamsCtx.formalParameterList() != null) {
            ParameterAnalyzer parameterAnalyzer = new ParameterAnalyzer(this.declaredjavaFunction);
            parameterAnalyzer.analyze(formalParamsCtx.formalParameterList());
        }
    }

    /*
     * Stores the created function in its corresponding class scope
     */
    private void storeMobiFunction() {
        this.declaredClassScope.addPrivatejavaMethod(this.declaredjavaFunction.getMethodName(), this.declaredjavaFunction);
    }

}
