package Execution.command;

import java.util.ArrayList;

public class MethodList {

    private static MethodList instance = null;
    private ArrayList<String> methodNames;

    private MethodList() {
        this.methodNames = new ArrayList<String>();
    }

    public static MethodList getInstance() {
        if (instance == null) {
            instance = new MethodList();

        }

        return instance;
    }

    public void addMethodName(String methodName) {
        this.methodNames.add(methodName);
    }

    public boolean methodNameExists(String methodName) {
        return this.methodNames.contains(methodName);
    }

    public ArrayList<String> getMethodNames() {
        return this.methodNames;
    }
}
