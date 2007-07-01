package enginuity.NewGUI;

import enginuity.util.LogManager;

public class TestNewGUI {
	public static void main(String[] args){
        LogManager.initLogging();
		NewGUI.getInstance().setVisible(true);
	}
}
