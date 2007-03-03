package enginuity.logger.ecu.ui.swing.menubar.action;

import enginuity.logger.ecu.EcuLogger;

import javax.swing.Action;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractAction implements Action {
    public static final String SELECTED_KEY = "selected";
    private final Map<String, Object> valueMap = new HashMap<String, Object>();
    private boolean enabled = true;
    protected EcuLogger logger;

    public AbstractAction(EcuLogger logger) {
        this.logger = logger;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
    }

    public Object getValue(String key) {
        return valueMap.get(key);
    }

    public void putValue(String key, Object value) {
        valueMap.put(key, value);
    }
}
