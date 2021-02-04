package SimpleCom;

import Execution.ExecutionManager;
import Execution.command.ICommand;
import semantic.representation.JavaValue;
import semantic.representation.JavaValueSearch;
import semantic.utils.StringUtils;

import javax.swing.*;

public class ScanCom implements ICommand{

    private String messageToDisplay;
    private String identifier;

    public ScanCom(String messageToDisplay, String identifier) {
        this.messageToDisplay = StringUtils.removeQuotes(messageToDisplay);
        this.identifier = identifier;

    }

    @Override
    public void execute() {

        // Stop thread execution while getting input.
        ExecutionManager.getExecutionManager().blockExecution();

        // Get the input through a JDialog.
        JFrame frame = new JFrame("InputDialog");
        String valueEntered = "";
        String[] options = {"OK"};
        JPanel panel = new JPanel();
        JTextField txtInput = new JTextField(20);
        panel.add(new JLabel(messageToDisplay));
        panel.add(txtInput);

        while(valueEntered.equals("")) {
            int selectedOption = loadInputDialog(frame, panel, options);
            if (selectedOption == 0) {
                valueEntered = txtInput.getText();
            }
        }

        // Saves the value to identifier
        JavaValue javaValue = JavaValueSearch.searchJavaValue(identifier);

        javaValue.setValue(valueEntered);

        // Continue executing the thread
        ExecutionManager.getExecutionManager().resumeExecution();
    }

    private int loadInputDialog(JFrame frame, JPanel panel, String[] options){
        return JOptionPane.showOptionDialog(frame, panel,
                "Scan Command", JOptionPane.NO_OPTION,
                JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }
}
