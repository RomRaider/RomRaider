package enginuity.util;

import javax.swing.*;

public class ThreadCheckingRepaintManager extends RepaintManager {
    public synchronized void addInvalidComponent(JComponent component) {
        checkThread();
        super.addInvalidComponent(component);
    }

    private void checkThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            System.out.println("Wrong Thread");
            Thread.dumpStack();
        }
    }

    public synchronized void addDirtyRegion(JComponent component, int i, int i1, int i2, int i3) {
        checkThread();
        super.addDirtyRegion(component, i, i1, i2, i3);
    }
}