package com.romraider.ramtune.test.command.executor;

import com.romraider.io.connection.ConnectionProperties;
import com.romraider.io.connection.EcuConnection;
import com.romraider.io.connection.EcuConnectionImpl;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

public final class CommandExecutorImpl implements CommandExecutor {
    private final ConnectionProperties connectionProperties;
    private final String port;

    public CommandExecutorImpl(ConnectionProperties connectionProperties, String port) {
        checkNotNull(connectionProperties);
        checkNotNullOrEmpty(port, "port");
        this.connectionProperties = connectionProperties;
        this.port = port;
    }

    public byte[] executeCommand(byte[] command) {
        EcuConnection ecuConnection = new EcuConnectionImpl(connectionProperties, port);
        try {
            return ecuConnection.send(command);
        } finally {
            ecuConnection.close();
        }
    }

}
