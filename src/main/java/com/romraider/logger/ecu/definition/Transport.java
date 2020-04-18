/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.romraider.logger.ecu.definition;

import static com.romraider.util.ParamChecker.checkNotNull;

public final class Transport {
    private final String id;
    private final String name;
    private final String description;
    
    private int baudRateOverride = 500000;
    private boolean extendedCANID = false; //11 or 29 
    private boolean saePWM = true; //PWM or VPW
    private boolean isoKWPFast = false; //Fast or 5 Baud init

    public Transport(String id, String name, String description) {
        checkNotNull(name, "id");
        checkNotNull(name, "name");
        checkNotNull(description, "description");
        this.id = id.toUpperCase();
        this.name = name;
        this.description = description;
    }
      
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
    
    
    /*
     * 
     * These settings are used by the ELM327 Connection to define the protocol further
     * 
     */
    
    public boolean isExtendedCANID() {
		return extendedCANID;
	}

	public void setExtendedCANID(String value) {
		if(value == null) return;
		
		if(value.trim().equals("11")) this.extendedCANID = false;
		else
			this.extendedCANID = true;
	}
	
	public void setExtendedCANID(boolean extendedCANID) {
		this.extendedCANID = extendedCANID;
	}
	public int getBaudRateOverride() {
		return baudRateOverride;
	}

	public void setBaudRateOverride(String baudRateOverride) {
		if(baudRateOverride == null) return;
		
		int rate = Integer.parseInt(baudRateOverride);
		this.baudRateOverride = rate;
	}
	
	public void setBaudRateOverride(int baudRateOverride) {
		this.baudRateOverride = baudRateOverride;
	}
	
	public boolean isSaePWM() {
		return saePWM;
	}

	public void setSaePWM(String saePWM) {
		if(saePWM == null) return;
		
		if(saePWM.toLowerCase().trim().equals("pwm")) this.saePWM = true;
		else 
			this.saePWM = false;
	}
	
	public void setSaePWM(boolean saePWM) {
		this.saePWM = saePWM;
	}


	public boolean isIsoKWPFast() {
		return isoKWPFast;
	}

	public void setIsoKWPFast(String isoKWPFast) {
		if(isoKWPFast == null )return;
		
		if(isoKWPFast.toLowerCase().trim().equals("fast")) this.isoKWPFast = true;
		else 
			this.isoKWPFast = false;
	}
    
	public void setIsoKWPFast(boolean isoKWPFast) {
		this.isoKWPFast = isoKWPFast;
	}
}
