package semantic.utils;

import java.util.HashMap;

/**
 * Holder for identified tokens found while parsing. Contains a key for easy retrieval of the actual text

 *
 */
public class IdentifiedTokens {
	
	private HashMap<String, String> tokenMapping;
	
	public IdentifiedTokens() {
		this.tokenMapping = new HashMap<>();
	}
	
	public void addToken(String key, String text) {
		this.tokenMapping.put(key, text);
	}
	
	public void removeToken(String key) {
		if(this.tokenMapping.containsKey(key)) this.tokenMapping.remove(key);
	}
	
	public String getToken(String key) {
		if(this.tokenMapping.containsKey(key)) return this.tokenMapping.get(key);
		else {
			System.err.println("IdentifiedTokens: " + key + " not found in list of tokens.");
			return null;
		}
	}
	
	public int getTokenListLength() {
		return this.tokenMapping.size();
	}
	
	
	/*
	 * Returns true if all of the specified keys has been found
	 */
	public boolean containsTokens(String...keys) {
		for (String key : keys) if (!this.tokenMapping.containsKey(key)) return false;
		return true;
	}
	
	public void clearTokens() {
		this.tokenMapping.clear();
	}
}
