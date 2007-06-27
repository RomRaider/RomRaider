package enginuity.logger.aem.plugin;

public final class AemSettingsImpl implements AemSettings {
    private String port;

    public void setPort(String port) {
        this.port = port;
    }

    public String getPort() {
        return port;
    }
}
