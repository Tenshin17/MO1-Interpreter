package LoopsCondStmt.Analyze;

//package baraco.semantics.analyzers;

//import baraco.antlr.parser.Java8Parser;
//import baraco.builder.errorcheckers.MultipleVariableDeclarationChecker;
//import baraco.execution.ExecutionManager;
//import baraco.execution.commands.controlled.IAttemptCommand;
//import baraco.execution.commands.controlled.IConditionalCommand;
//import baraco.execution.commands.controlled.IControlledCommand;
//import baraco.execution.commands.evaluation.ArrayInitializeCommand;
//import baraco.representations.javaArray;
//import baraco.representations.BaracoValue;
//import baraco.semantics.statements.StatementControlOverseer;
//import baraco.semantics.symboltable.scopes.ClassScope;
//import baraco.semantics.symboltable.scopes.LocalScope;
//import baraco.semantics.utils.IdentifiedTokens;

import antlr.Java8Parser;
import Command.ICondCommand;
import Command.ICtrlCommand;
import EvalSimpCompExp.ArrayInitCom;
import VarAndConstDec.javaArray;
import VarAndConstDec.javaValue;
import LoopsCondStmt.Stmt.StmtCntrl;
import symboltable.scope.ClassScope;
import symboltable.scope.LocalScope;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.io.Console;

public class ArrayAnalyze implements ParseTreeListener {

    private final static String ARRAY_PRIMITIVE_KEY = "ARRAY_PRIMITIVE_KEY";
    private final static String ARRAY_IDENTIFIER_KEY = "ARRAY_IDENTIFIER_KEY";

    private IdentifiedTokens identifiedTokens;
    private ClassScope declaredClassScope;
    private LocalScope localScope;
    private javaArray declaredArray;

    public ArrayAnalyzer( IdentifiedTokens identifiedTokens, ClassScope declaredClassScope) {
        this.identifiedTokens = identifiedTokens;
        this.declaredClassScope = declaredClassScope;
    }

    public ArrayAnalyzer( IdentifiedTokens identifiedTokens, LocalScope localScope) {
        this.identifiedTokens = identifiedTokens;
        this.localScope = localScope;
    }

    public void analyze(ParserRuleContext ctx) {
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
        if(ctx instanceof Java8Parser.PrimitiveTypeContext) {
            Java8Parser.PrimitiveTypeContext primitiveCtx = (Java8Parser.PrimitiveTypeContext) ctx;
            this.identifiedTokens.addToken(ARRAY_PRIMITIVE_KEY, primitiveCtx.getText());
        }
        else if(ctx instanceof Java8Parser.VariableDeclaratorIdContext) {
            Java8Parser.VariableDeclaratorIdContext varDecIdCtx = (Java8Parser.VariableDeclaratorIdContext) ctx;
            MultipleVariableDeclarationChecker multipleDeclaredChecker = new MultipleVariableDeclarationChecker(varDecIdCtx);
            multipleDeclaredChecker.verify();
            this.identifiedTokens.addToken(ARRAY_IDENTIFIER_KEY, varDecIdCtx.getText());

            this.analyzeArray();
        }
        else if(ctx instanceof Java8Parser.CreatedNameContext) {
            Java8Parser.CreatedNameContext createdNameCtx = (Java8Parser.CreatedNameContext) ctx;
            //Console.log(LogType.DEBUG, "Array created name: " +createdNameCtx.getText());
        }

        else if(ctx instanceof Java8Parser.ArrayCreatorRestContext) {
            Java8Parser.ArrayCreatorRestContext arrayCreatorCtx = (Java8Parser.ArrayCreatorRestContext) ctx;
            this.createInitializeCommand(arrayCreatorCtx);
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {

    }

    private void analyzeArray() {

        if(this.declaredClassScope != null) {
            if(this.identifiedTokens.containsTokens(ClassAnalyzer.ACCESS_CONTROL_KEY, ARRAY_PRIMITIVE_KEY, ARRAY_IDENTIFIER_KEY)) {
                String accessControlString = this.identifiedTokens.getToken(ClassAnalyzer.ACCESS_CONTROL_KEY);
                String arrayTypeString = this.identifiedTokens.getToken(ARRAY_PRIMITIVE_KEY);
                String arrayIdentifierString = this.identifiedTokens.getToken(ARRAY_IDENTIFIER_KEY);

                //initialize an array mobivalue
                this.declaredArray = javaArray.createArray(arrayTypeString, arrayIdentifierString);
                BaracoValue baracoValue = new BaracoValue(this.declaredArray, BaracoValue.PrimitiveType.ARRAY);

                this.declaredClassScope.addBaracoValue(accessControlString, arrayIdentifierString, baracoValue);
                //Console.log(LogType.DEBUG, "Creating array with type " +arrayTypeString+ " variable " +arrayIdentifierString);

                this.identifiedTokens.clearTokens();
            }
        }
        else if(this.localScope != null) {
            if(this.identifiedTokens.containsTokens(ARRAY_PRIMITIVE_KEY, ARRAY_IDENTIFIER_KEY)) {
                String arrayTypeString = this.identifiedTokens.getToken(ARRAY_PRIMITIVE_KEY);
                String arrayIdentifierString = this.identifiedTokens.getToken(ARRAY_IDENTIFIER_KEY);

                //initialize an array mobivalue
                this.declaredArray = javaArray.createArray(arrayTypeString, arrayIdentifierString);
                BaracoValue mobiValue = new BaracoValue(this.declaredArray, BaracoValue.PrimitiveType.ARRAY);

                this.localScope.addMobiValue(arrayIdentifierString, mobiValue);
                //Console.log(LogType.DEBUG, "Creating array with type " +arrayTypeString+ " variable " +arrayIdentifierString);

                this.identifiedTokens.clearTokens();
            }
        }

    }

    private void createInitializeCommand(Java8Parser.ArrayCreatorRestContext arrayCreatorCtx) {
        ArrayInitializeCommand arrayInitializeCommand = new ArrayInitializeCommand(this.declaredArray, arrayCreatorCtx);

        //ExecutionManager.getInstance().addCommand(arrayInitializeCommand);

        StatementControlOverseer statementControl = StatementControlOverseer.getInstance();
        //add to conditional controlled command
        if(statementControl.isInConditionalCommand()) {
            IConditionalCommand conditionalCommand = (IConditionalCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInPositiveRule()) {
                conditionalCommand.addPositiveCommand(arrayInitializeCommand);
            }
            else {
                conditionalCommand.addNegativeCommand(arrayInitializeCommand);
            }
        }

        else if(statementControl.isInControlledCommand()) {
            IControlledCommand controlledCommand = (IControlledCommand) statementControl.getActiveControlledCommand();
            controlledCommand.addCommand(arrayInitializeCommand);
        }
        else if (statementControl.isInAttemptCommand()) {
            IAttemptCommand attemptCommand = (IAttemptCommand) statementControl.getActiveControlledCommand();

            if(statementControl.isInTryBlock()) {
                attemptCommand.addTryCommand(arrayInitializeCommand);
            } else {
                attemptCommand.addCatchCommand(statementControl.getCurrentCatchType(), arrayInitializeCommand);
            }
        }
        else {
            ExecutionManager.getInstance().addCommand(arrayInitializeCommand);
        }

    }
}