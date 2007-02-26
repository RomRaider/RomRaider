/*
 * Created on May 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package enginuity.tts;

import enginuity.logger.utec.properties.UtecProperties;

/**
 * @author botman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SpeakString extends Thread{

	private String message = null;
	
	public SpeakString(String message){
		this.message = message;
		this.speakNow();
	}
	
	public void run(){
		SpeakKnock.speakString(message);
	}
	
	public void speakNow(){
		String[] toSpeak = UtecProperties.getProperties("utec.sound");
		boolean parsedBoolean = Boolean.parseBoolean(toSpeak[0]);
		if(parsedBoolean){
			this.start();
		}
		
	}
}
