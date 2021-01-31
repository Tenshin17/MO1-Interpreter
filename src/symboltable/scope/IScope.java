//package baraco.semantics.symboltable.scopes;
package symboltable.scope;

//import baraco.representations.BaracoValue;
import VarAndConstDec.javaValue;
public interface IScope {
//    public abstract BaracoValue searchVariableIncludingLocal(String identifier);
    public abstract javaValue searchVariableIncludingLocal(String identifier);
    public abstract boolean isParent();
}
