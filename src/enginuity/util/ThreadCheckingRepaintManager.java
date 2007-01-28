package enginuity.util;

import javax.swing.*;
import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class ThreadCheckingRepaintManager extends RepaintManager {
    private int tabCount = 0;
    private boolean checkIsShowing = false;

    public ThreadCheckingRepaintManager() {
    }

    public ThreadCheckingRepaintManager(boolean checkIsShowing) {
        this.checkIsShowing = checkIsShowing;
    }

    public synchronized void addInvalidComponent(JComponent jComponent) {
        checkThread(jComponent);
        super.addInvalidComponent(jComponent);
    }

    private void checkThread(JComponent c) {
        if (!SwingUtilities.isEventDispatchThread() && checkIsShowing(c)) {
            System.out.println("----------Wrong Thread START");
            System.out.println(getStracktraceAsString(new Exception()));
            dumpComponentTree(c);
            System.out.println("----------Wrong Thread END");
        }
    }

    private String getStracktraceAsString(Exception e) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(byteArrayOutputStream);
        e.printStackTrace(printStream);
        printStream.flush();
        return byteArrayOutputStream.toString();
    }

    private boolean checkIsShowing(JComponent c) {
        return !this.checkIsShowing || c.isShowing();
    }

    public synchronized void addDirtyRegion(JComponent jComponent, int i, int i1, int i2, int i3) {
        checkThread(jComponent);
        super.addDirtyRegion(jComponent, i, i1, i2, i3);
    }

    private void dumpComponentTree(Component c) {
        System.out.println("----------Component Tree");
        resetTabCount();
        for (; c != null; c = c.getParent()) {
            printTabIndent();
            System.out.println(c);
            printTabIndent();
            System.out.println("Showing:" + c.isShowing() + " Visible: " + c.isVisible());
            incrementTabCount();
        }
    }

    private void resetTabCount() {
        this.tabCount = 0;
    }

    private void incrementTabCount() {
        this.tabCount++;
    }

    private void printTabIndent() {
        for (int i = 0; i < this.tabCount; i++) {
            System.out.print("\t");
        }
    }
}
