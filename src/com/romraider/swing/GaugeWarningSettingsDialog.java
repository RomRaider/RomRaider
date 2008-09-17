package com.romraider.swing;

import com.romraider.logger.ecu.definition.EcuParameterWarning;
import com.romraider.logger.ecu.definition.EcuParameterWarningType;

import javax.swing.JDialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class GaugeWarningSettingsDialog extends JDialog implements ActionListener {

	EcuParameterWarning warningSettings;
	
    private static final String IGNORE = "ignore warnings";
    private static final String ABOVE = "warn above";
    private static final String BELOW = "warn below";
    private static final String OK = "OK";
    private static final String CANCEL = "Cancel";

    private javax.swing.JButton btnCancel;
    private javax.swing.JButton btnOk;
    private javax.swing.JTextField warningValue;
    private javax.swing.JComboBox warningType;
	
	public GaugeWarningSettingsDialog(EcuParameterWarning warningSettings) {
		this.warningSettings = warningSettings;
		
		setTitle("Warning Settings");
		setSize(300, 75);
		
		btnCancel = new javax.swing.JButton();
		btnOk = new javax.swing.JButton();
		if(warningSettings != null)
		{
			warningValue = new javax.swing.JTextField(String.valueOf(warningSettings.getWarningValue()));
		}
		else
		{
			warningValue = new javax.swing.JTextField("0.0");
		}
		warningType = new javax.swing.JComboBox(new Object[]{IGNORE, ABOVE, BELOW});
		
		warningValue.setSize(new Dimension(40, 40));
		if(warningSettings != null)
		{
			if(warningSettings.getWarningType() == EcuParameterWarningType.WARN_NONE){
				warningType.setSelectedItem(IGNORE);
			}else if(warningSettings.getWarningType() == EcuParameterWarningType.WARN_ABOVE){
				warningType.setSelectedItem(ABOVE);
			}else if(warningSettings.getWarningType() == EcuParameterWarningType.WARN_BELOW) {
				warningType.setSelectedItem(BELOW);
			}	
		}
		else
		{
			warningSettings = new EcuParameterWarning();
			warningType.setSelectedItem(IGNORE);
		}
		
		btnCancel.setMnemonic('C');
		btnCancel.setText("Cancel");
		btnCancel.addActionListener(this);

		btnOk.setMnemonic('O');
		btnOk.setText("OK");
		btnOk.addActionListener(this);
		
		getContentPane().setLayout(new FlowLayout());
		
		add(warningType);
		add(warningValue);
		add(btnOk);
		add(btnCancel);
	}
	
	public EcuParameterWarning getWarningSettings() {
		return warningSettings;
	}
	
	public void saveSettings() {
		if(warningSettings == null){
			warningSettings = new EcuParameterWarning();
		}
		
		if(warningType.getSelectedItem() == IGNORE){
			warningSettings.setWarningType(EcuParameterWarningType.WARN_NONE);
		}else if(warningType.getSelectedItem() == ABOVE){
			warningSettings.setWarningType(EcuParameterWarningType.WARN_ABOVE);
		}else if(warningType.getSelectedItem() == BELOW) {
			warningSettings.setWarningType(EcuParameterWarningType.WARN_BELOW);
		}
		
		warningSettings.setWarningValue(Double.valueOf(warningValue.getText()));
	}
	
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand() == OK)
		{
			saveSettings();
			setVisible(false);
		}
		else if(e.getActionCommand() == CANCEL)
		{
			setVisible(false);
		}
	}
}
	
