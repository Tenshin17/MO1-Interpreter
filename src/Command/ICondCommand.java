package Command;

public interface ICondCommand extends ICommand {
    public abstract ICtrlCommand.ControlTypeEnum getControlType();
    public abstract void addPositiveCommand(ICommand command);
    public abstract void addNegativeCommand(ICommand command);
}
