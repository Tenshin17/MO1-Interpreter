//package baraco.semantics.analyzers;
package LoopsCondStmt.Analyze;

import baraco.antlr.lexer.BaracoLexer;
import baraco.antlr.parser.BaracoParser;
import baraco.builder.errorcheckers.MultipleVariableDeclarationChecker;
import baraco.builder.errorcheckers.TypeChecker;
import baraco.execution.ExecutionManager;
import baraco.execution.commands.controlled.IAttemptCommand;
import baraco.execution.commands.controlled.IConditionalCommand;
import baraco.execution.commands.controlled.IControlledCommand;
import baraco.execution.commands.evaluation.MappingCommand;
import baraco.representations.BaracoValue;
import baraco.representations.RecognizedKeywords;
import baraco.semantics.statements.StatementControlOverseer;
import baraco.semantics.symboltable.scopes.LocalScope;
import baraco.semantics.symboltable.scopes.LocalScopeCreator;
import baraco.semantics.utils.IdentifiedTokens;

import antlr.Java8Lexer;
import antlr.Java8Parser;
import VarAndConstDec.javaValue;
import VarAndConstDec.javaKeywords;
import LoopsCondStmt.Stmt.StmtCntrl;
import symboltable.scope.LocalScope;
import symboltable.scope.LocalScopeCreator;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

public class LocalVarAnalyze implements ParseTreeListener {

    private final static String TAG = "MobiProg_LocalVariableAnalyzer";

    private final static String FINAL_TYPE_KEY = "FINAL_TYPE_KEY";
    private final static String PRIMITIVE_TYPE_KEY = "PRIMITIVE_TYPE_KEY";
    private final static String IDENTIFIER_KEY = "IDENTIFIER_KEY";
    private final static String IDENTIFIER_VALUE_KEY = "IDENTIFIER_VALUE_KEY";

    private IdentifiedTokens identifiedTokens;
    private boolean executeMappingImmediate = false;
    private boolean hasPassedArrayDeclaration = false;

    public LocalVarAnalyze() {

    }

    public void analyze(Java8Parser.LocalVariableDeclarationContext localVarDecCtx) {
        this.identifiedTokens = new IdentifiedTokens();

        ParseTreeWalker treeWalker = new ParseTreeWalker();
        treeWalker.walk(this, localVarDecCtx);

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
        this.analyzeVariables(ctx);
    }

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {
        // TODO Auto-generated method stub

    }

    private void analyzeVariables(ParserRuleContext ctx) {

        if(ctx instanceof Java8Parser.VariableModifierContext){
            Java8Parser.VariableModifierContext varModCtx = (Java8Parser.VariableModifierContext) ctx;

            if (ctx.getTokens(Java8Lexer.FINAL).size() > 0) {
                this.identifiedTokens.addToken(FINAL_TYPE_KEY, varModCtx.getText());
            }

        }

        if(ctx instanceof Java8Parser.TypeTypeContext) {
            Java8Parser.TypeTypeContext typeCtx = (Java8Parser.TypeTypeContext) ctx;
            //clear tokens for reuse
            //this.identifiedTokens.clearTokens();

            if(ClassAnalyzer.isPrimitiveDeclaration(typeCtx)) {
                Java8Parser.PrimitiveTypeContext primitiveTypeCtx = typeCtx.primitiveType();
                this.identifiedTokens.addToken(PRIMITIVE_TYPE_KEY, primitiveTypeCtx.getText());

            }

            //check if its array declaration
            else if(ClassAnalyzer.isPrimitiveArrayDeclaration(typeCtx)) {
                //Console.log(LogType.DEBUG, "Primitive array declaration: " +typeCtx.getText());
                ArrayAnalyzer arrayAnalyzer = new ArrayAnalyzer(this.identifiedTokens, LocalScopeCreator.getInstance().getActiveLocalScope());
                arrayAnalyzer.analyze(typeCtx.getParent());
                this.hasPassedArrayDeclaration = true;
            }

            //this is for class type ctx
            else {
                //a string identified
                if(typeCtx.classOrInterfaceType().getText().contains(javaKeywords.PRIMITIVE_TYPE_STRING)) {
                    Java8Parser.ClassOrInterfaceTypeContext classInterfaceCtx = typeCtx.classOrInterfaceType();
                    this.identifiedTokens.addToken(PRIMITIVE_TYPE_KEY, classInterfaceCtx.getText());
                }
            }


        }

        else if(ctx instanceof Java8Parser.VariableDeclaratorContext) {

            Java8Parser.VariableDeclaratorContext varCtx = (Java8Parser.VariableDeclaratorContext) ctx;

            if(this.hasPassedArrayDeclaration) {

                return;
            }

            //check for duplicate declarations
            if(this.executeMappingImmediate == false) {
                MultipleVariableDeclarationChecker multipleDeclaredChecker = new MultipleVariableDeclarationChecker(varCtx.variableDeclaratorId());
                multipleDeclaredChecker.verify();
            }

            this.identifiedTokens.addToken(IDENTIFIER_KEY, varCtx.variableDeclaratorId().getText());
            this.createBaracoValue();

            if(varCtx.variableInitializer() != null) {

                //we do not evaluate strings.
                if(this.identifiedTokens.containsTokens(PRIMITIVE_TYPE_KEY)) {
                    String primitiveTypeString = this.identifiedTokens.getToken(PRIMITIVE_TYPE_KEY);

                    if(primitiveTypeString.contains(javaKeywords.PRIMITIVE_TYPE_STRING)) {
                        this.identifiedTokens.addToken(IDENTIFIER_VALUE_KEY, varCtx.variableInitializer().getText());
                    }
                }

                this.processMapping(varCtx);

                LocalScope localScope = LocalScopeCreator.getInstance().getActiveLocalScope();
                javaValue declaredBaracoValue = localScope.searchVariableIncludingLocal(varCtx.variableDeclaratorId().getText());

                //type check the mobivalue
                TypeChecker typeChecker = new TypeChecker(declaredBaracoValue, varCtx.variableInitializer().expression());
                typeChecker.verify();
            }

        }

    }

    /*
     * Local variable analyzer is also used for loops. Whenever there is a loop,
     * mapping command should be executed immediately to update the value in the symbol table.
     * Otherwise, it proceeds normally.
     */
    private void processMapping(Java8Parser.VariableDeclaratorContext varCtx) {
        if(this.executeMappingImmediate) {
            MappingCommand mappingCommand = new MappingCommand(varCtx.variableDeclaratorId().getText(), varCtx.variableInitializer().expression());
            mappingCommand.execute();
        }
        else {
            MappingCommand mappingCommand = new MappingCommand(varCtx.variableDeclaratorId().getText(), varCtx.variableInitializer().expression());

            StatementControlOverseer statementControl = StatementControlOverseer.getInstance();
            //add to conditional controlled command
            if(statementControl.isInConditionalCommand()) {
                IConditionalCommand conditionalCommand = (IConditionalCommand) statementControl.getActiveControlledCommand();

                if(statementControl.isInPositiveRule()) {
                    conditionalCommand.addPositiveCommand(mappingCommand);
                }
                else {
                    conditionalCommand.addNegativeCommand(mappingCommand);
                }
            }

            else if(statementControl.isInControlledCommand()) {
                IControlledCommand controlledCommand = (IControlledCommand) statementControl.getActiveControlledCommand();
                controlledCommand.addCommand(mappingCommand);
            }
            else if (statementControl.isInAttemptCommand()) {
                IAttemptCommand attemptCommand = (IAttemptCommand) statementControl.getActiveControlledCommand();

                if(statementControl.isInTryBlock()) {
                    attemptCommand.addTryCommand(mappingCommand);
                } else {
                    attemptCommand.addCatchCommand(statementControl.getCurrentCatchType(), mappingCommand);
                }
            }
            else {
                ExecutionManager.getInstance().addCommand(mappingCommand);
            }

        }
    }

    public void markImmediateExecution() {
        this.executeMappingImmediate = true;
    }

    /*
     * Attempts to create an intermediate representation of the variable once a sufficient amount of info has been retrieved.
     */
    private void createBaracoValue() {

        if(this.identifiedTokens.containsTokens(PRIMITIVE_TYPE_KEY, IDENTIFIER_KEY)) {

            String primitiveTypeString = this.identifiedTokens.getToken(PRIMITIVE_TYPE_KEY);
            String identifierString = this.identifiedTokens.getToken(IDENTIFIER_KEY);
            String identifierValueString = null;

            LocalScope localScope = LocalScopeCreator.getInstance().getActiveLocalScope();

            if(this.identifiedTokens.containsTokens(IDENTIFIER_VALUE_KEY)) {
                identifierValueString = this.identifiedTokens.getToken(IDENTIFIER_VALUE_KEY);
                localScope.addInitializedVariableFromKeywords(primitiveTypeString, identifierString, identifierValueString);

                if (this.identifiedTokens.containsTokens(FINAL_TYPE_KEY))
                    localScope.addFinalInitVariableFromKeyWords(primitiveTypeString, identifierString, identifierValueString);

            }
            else {
                localScope.addEmptyVariableFromKeywords(primitiveTypeString, identifierString);

                if (this.identifiedTokens.containsTokens(FINAL_TYPE_KEY))
                    localScope.addFinalEmptyVariableFromKeywords(primitiveTypeString, identifierString);
            }

            //remove the following tokens
            this.identifiedTokens.removeToken(IDENTIFIER_KEY);
            this.identifiedTokens.removeToken(IDENTIFIER_VALUE_KEY);

        }
    }
}
