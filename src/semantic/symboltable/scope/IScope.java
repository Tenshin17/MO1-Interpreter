package semantic.symboltable.scope;

import semantic.representation.JavaValue;

/**
 * A generic scope interface
 *
 */
public interface IScope {
	public abstract JavaValue searchVariableIncludingLocal(String identifier);
	public abstract boolean isParent();
}
