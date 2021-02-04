package error.checkers;

import Execution.ExecutionManager;
import error.ParserHandler;
import semantic.utils.StringUtils;

public class ClassNameChecker implements IErrorChecker {

    private String className;
    private boolean successful = true;

    public ClassNameChecker(String readClassName) {
        this.className = readClassName;
    }

    @Override
    public void verify() {
        if(this.className.equals(ParserHandler.getInstance().getCurrentClassName()) == false) {
            this.successful = false;
            String additionalMsg = "Class name is " +this.className+ " while file name is " + ParserHandler.getInstance().getCurrentClassName();
            ExecutionManager.getExecutionManager().consoleListModel.addElement(StringUtils.formatError("Inconsistent class name. "+additionalMsg));
        }
    }

    /*
     * Corrects the class name so that the semantics analyzer can continue
     */
    public String correctClassName() {
        if(this.successful) {
            return this.className;
        }
        else {
            return ParserHandler.getInstance().getCurrentClassName();
        }
    }
}
