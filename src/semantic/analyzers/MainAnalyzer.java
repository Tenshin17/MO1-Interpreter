package semantic.analyzers;

import antlr.Java8Parser.*;
import antlr.Java8Parser.BlockContext;
import antlr.Java8Parser.MethodBodyContext;
import error.ParserHandler;
import Execution.ExecutionManager;
import semantic.symboltable.SymbolTableManager;
import semantic.symboltable.scope.ClassScope;
import semantic.symboltable.scope.LocalScope;
import semantic.symboltable.scope.LocalScopeCreator;
import semantic.utils.StringUtils;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The entry point for the program. Only one main is allowed.
 *
 */
public class MainAnalyzer implements ParseTreeListener {

	public MainAnalyzer() {
		
	}
	
	public void analyze(MethodDeclarationContext ctx) {
	    if(!ExecutionManager.getExecutionManager().hasFoundEntryPoint()) {
			ExecutionManager.getExecutionManager().reportFoundEntryPoint(ParserHandler.getInstance().getCurrentClassName());

			//automatically create a local scope for main() whose parent is the class scope
			ClassScope classScope = SymbolTableManager.getInstance().getClassScope(ParserHandler.getInstance().getCurrentClassName());
			LocalScope localScope = LocalScopeCreator.getInstance().openLocalScope();
			localScope.setParent(classScope);
			classScope.setParentLocalScope(localScope);
			
			ParseTreeWalker treeWalker = new ParseTreeWalker();
			treeWalker.walk(this, ctx);

		}
		else {
			ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatDebug("Already found main in " +ExecutionManager.getExecutionManager().getEntryClassName()));
		}
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
		if(ctx instanceof MethodBodyContext) {
			BlockContext blockCtx = ((MethodBodyContext) ctx).block();
			
			BlockAnalyzer blockAnalyzer = new BlockAnalyzer();
			blockAnalyzer.analyze(blockCtx.blockStatements());
		}
	}

	@Override
	public void exitEveryRule(ParserRuleContext ctx) {
		// TODO Auto-generated method stub
		
	}
}
