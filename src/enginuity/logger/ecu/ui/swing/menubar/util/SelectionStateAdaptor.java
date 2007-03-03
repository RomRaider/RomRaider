package enginuity.logger.ecu.ui.swing.menubar.util;

import static enginuity.logger.ecu.ui.swing.menubar.action.AbstractAction.SELECTED_KEY;

import javax.swing.AbstractButton;
import javax.swing.Action;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public final class SelectionStateAdaptor implements PropertyChangeListener, ItemListener {
    private final Action action;
    private final AbstractButton button;

    public SelectionStateAdaptor(Action action, AbstractButton button) {
        this.action = action;
        this.button = button;
    }

    public void configure() {
        action.addPropertyChangeListener(this);
        button.addItemListener(this);
    }

    public void itemStateChanged(ItemEvent e) {
        action.putValue(SELECTED_KEY, e.getStateChange() == ItemEvent.SELECTED);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(SELECTED_KEY)) {
            button.setSelected((Boolean) evt.getNewValue());
        }
    }
}