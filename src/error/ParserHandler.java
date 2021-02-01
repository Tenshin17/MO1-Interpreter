/**
 * 
 */
package error;

/**
 * Manages all parsing from different saved files

 *
 */
public class ParserHandler {

	private static ParserHandler parserHandler = null;
	
	public static ParserHandler getInstance() {
		if(parserHandler == null) {
			parserHandler = new ParserHandler();
		}
		return parserHandler;
	}
	
	private String currentClassName = "Main"; //the current class being parsed
	
	private ParserHandler() {
		
	}
	
	public void setCurrentClassName(String className) {
		this.currentClassName = className.replace(".java", "");
	}
	
	/*
	 * Returns the class name being parsed
    */
	public String getCurrentClassName() {
		return this.currentClassName;
	}

}
