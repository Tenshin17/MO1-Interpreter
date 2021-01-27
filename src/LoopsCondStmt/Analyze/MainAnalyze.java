package baraco.semantics.analyzers;
//package LoopsCondStmt.Analyze;

import baraco.builder.ParserHandler;
import baraco.execution.ExecutionManager;
import baraco.semantics.symboltable.SymbolTableManager;
import baraco.semantics.symboltable.scopes.ClassScope;
import baraco.semantics.symboltable.scopes.LocalScope;
import baraco.semantics.symboltable.scopes.LocalScopeCreator;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import baraco.antlr.parser.BaracoParser.*;

public class MainAnalyze implements ParseTreeListener{

    public MainAnalyzer() {

    }

    public void analyze(MainDeclarationContext ctx) {
        if(!ExecutionManager.getInstance().hasFoundEntryPoint()) {
            ExecutionManager.getInstance().reportFoundEntryPoint(ParserHandler.getInstance().getCurrentClassName());

            //automatically create a local scope for main() whose parent is the class scope
            ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
            LocalScope localScope = LocalScopeCreator.getInstance().openLocalScope();
            localScope.setParent(classScope);
            classScope.setParentLocalScope(localScope);

            ParseTreeWalker treeWalker = new ParseTreeWalker();
            treeWalker.walk(this, ctx);


        }
        else {
            System.out.println("Already found main in " + ExecutionManager.getInstance().getEntryClassName());
        }
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {
        if(parserRuleContext instanceof MethodBodyContext) {
            BlockContext blockCtx = ((MethodBodyContext) parserRuleContext).block();

            BlockAnalyzer blockAnalyzer = new BlockAnalyzer();
            blockAnalyzer.analyze(blockCtx);
        }
    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {

    }
}
