package enginuity.ramtune.test.command.executor;

import enginuity.ramtune.test.command.Command;
import enginuity.ramtune.test.command.CommandResult;

public interface CommandExecutor {

    CommandResult executeCommand(Command command);
}
