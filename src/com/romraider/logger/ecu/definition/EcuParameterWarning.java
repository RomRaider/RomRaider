package com.romraider.logger.ecu.definition;

import com.romraider.logger.ecu.definition.EcuParameterWarningType;

public class EcuParameterWarning {
	private EcuParameterWarningType warningType;
	private double warningValue;
	private boolean audible;
	private boolean visible;
	
	public EcuParameterWarning() {
		warningType = EcuParameterWarningType.WARN_NONE;
		warningValue = 0.0;
		audible = false;
		visible = false;
	}
	
	public EcuParameterWarning(EcuParameterWarningType warningType, double warningValue, boolean audible, boolean visible) {
		this.warningType = warningType;
		this.warningValue = warningValue;
		this.audible = audible;
		this.visible = visible;
	}
	
	public EcuParameterWarningType getWarningType() {
		return warningType;
	}
	
	public void setWarningType(EcuParameterWarningType warningType) {
		this.warningType = warningType;
	}

	public double getWarningValue() {
		return warningValue;
	}
	
	public void setWarningValue(double warningValue) {
		this.warningValue = warningValue;
	}
	
	public boolean isAudible() {
		return audible;
	}
	
	public void setAudible(boolean audible) {
		this.audible = audible;
	}
	
	public boolean isVisible() {
		return visible;
	}
	
	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
}
