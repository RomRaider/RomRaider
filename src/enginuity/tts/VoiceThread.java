/*
 * Created on May 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package enginuity.tts;

/**
 * @author botman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VoiceThread extends Thread{

	private String message = null;
	
	public VoiceThread(String message){
		this.message = message;
		
	}
	
	public void run(){
		SpeakKnock.speakString(message);
	}
}
