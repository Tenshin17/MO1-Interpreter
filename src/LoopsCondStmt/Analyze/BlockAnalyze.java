//package baraco.semantics.analyzers;
package LoopsCondStmt.Analyze;

//import baraco.semantics.symboltable.scopes.LocalScopeCreator;
//import baraco.antlr.parser.BaracoParser.*;
import antlr.*;
import symboltable.scope.LocalScopeCreator;

import java.util.List;

public class BlockAnalyze {

    public BlockAnalyze() {
        LocalScopeCreator.getInstance().openLocalScope();
    }

    public void analyze(BlockContext ctx) {

        List<BlockStatementContext> blockListCtx = ctx.blockStatement();

        for(BlockStatementContext blockStatementCtx : blockListCtx) {
            if(blockStatementCtx.statement() != null) {
                StatementContext statementCtx = blockStatementCtx.statement();
                StatementAnalyzer statementAnalyzer = new StatementAnalyzer();
                statementAnalyzer.analyze(statementCtx);
            }
            else if(blockStatementCtx.localVariableDeclarationStatement() != null) {
                LocalVariableDeclarationStatementContext localVarDecStatementCtx = blockStatementCtx.localVariableDeclarationStatement();

                LocalVariableAnalyzer localVarAnalyzer = new LocalVariableAnalyzer();
                localVarAnalyzer.analyze(localVarDecStatementCtx.localVariableDeclaration());
            }
        }

        LocalScopeCreator.getInstance().closeLocalScope();
    }
}
