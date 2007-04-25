package enginuity.ramtune.test.command.executor;

import enginuity.io.connection.EcuConnection;
import enginuity.io.connection.EcuConnectionImpl;
import enginuity.io.protocol.Protocol;
import static enginuity.util.ParamChecker.checkNotNull;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;

public final class CommandExecutorImpl implements CommandExecutor {
    private final Protocol protocol;
    private final String port;

    public CommandExecutorImpl(Protocol protocol, String port) {
        checkNotNull(protocol);
        checkNotNullOrEmpty(port, "port");
        this.protocol = protocol;
        this.port = port;
    }

    public byte[] executeCommand(byte[] command) {
        EcuConnection ecuConnection = new EcuConnectionImpl(protocol.getDefaultConnectionProperties(), port);
        try {
            return ecuConnection.send(command);
        } finally {
            ecuConnection.close();
        }
    }

}
