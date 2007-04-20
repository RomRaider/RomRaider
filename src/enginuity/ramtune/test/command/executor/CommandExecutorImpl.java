package enginuity.ramtune.test.command.executor;

import enginuity.io.connection.ConnectionProperties;
import enginuity.io.connection.EcuConnection;
import enginuity.io.connection.EcuConnectionImpl;
import enginuity.ramtune.test.command.Command;
import enginuity.ramtune.test.command.CommandResult;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class CommandExecutorImpl implements CommandExecutor {
    private final EcuConnection ecuConnection;

    public CommandExecutorImpl(ConnectionProperties connectionProperties, String port) {
        checkNotNull(connectionProperties);
        checkNotNullOrEmpty(port, "port");
        this.ecuConnection = new EcuConnectionImpl(connectionProperties, port);
    }

    public CommandResult executeCommand(Command command) {
        ecuConnection.send(command.getBytes());
        return null;
    }
}
