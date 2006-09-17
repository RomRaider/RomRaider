package enginuity.logger.ui;

import javax.swing.*;
import java.awt.*;

public final class DashboardPanel extends JPanel {
    private boolean resizing = false;

    public DashboardPanel(LayoutManager layoutManager) {
        super(layoutManager);
    }

    //TODO: This thing is horrible and still dodgy - how to fix it properly???
    public void doLayout() {
        if (!resizing && getSize().getWidth() > getVisibleRect().getWidth()) {
            setSize(new Dimension((int) getVisibleRect().getWidth(), (int) getSize().getHeight()));
            super.doLayout();
            resizing = true;
        } else if (getSize().getWidth() <= getVisibleRect().getWidth()) {
            super.doLayout();
            resizing = false;
        }

    }
}
