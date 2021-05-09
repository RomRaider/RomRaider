/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

package com.romraider.logger.ecu.comms.manager;


import java.util.Vector;

import org.apache.log4j.Logger;
import com.romraider.logger.ecu.comms.query.Response;
import com.romraider.logger.ecu.ui.handler.DataUpdateHandler;


public class AsyncDataUpdateHandler extends Thread {
    private static final Logger LOGGER = Logger.getLogger(AsyncDataUpdateHandler.class);

    Vector<Response> responsesToUpdate = new Vector<Response>();
	DataUpdateHandler[] handlers;
	private  boolean stop = false;
	private volatile boolean isRunning = false;
	
	public AsyncDataUpdateHandler(DataUpdateHandler[] handlers) {
		this.handlers = handlers;
		setName("AsyncDataUpdater");
	}
	
    public void run(){
    	LOGGER.info("Starting AsyncDataUpdateHandler");
    	stop = false;
    	
    	while(!stop) {	    	
    		isRunning = true;	
    		
	    	synchronized(responsesToUpdate) {		   
	    		Response r;	    		
	    		while(!responsesToUpdate.isEmpty()) {
	    			r = responsesToUpdate.get(0);
	    			
		    	   for(DataUpdateHandler handler: handlers) {
		    		   handler.handleDataUpdate(r);
		    	   }	
		    	   
		    	   responsesToUpdate.remove(0);		    	   
			  }
	    	}
		       
	       try {
	    	   Thread.sleep(16); //60 FPS
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
       }
    	
    	LOGGER.info("AsyncDataUpdater stopped.");
    	isRunning = false;
    }
    
    public void stopUpdater() {
    	stop = true;
    }
    
    public boolean isRunning() {
    	return isRunning;
    }
    
    public void addResponse(Response response) {
    	responsesToUpdate.add(response);   
    }
  }