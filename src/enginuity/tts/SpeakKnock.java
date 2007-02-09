/*
 * Created on May 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package enginuity.tts;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
/**
 * @author botman
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SpeakKnock {
	private static VoiceManager voiceManager = VoiceManager.getInstance();
    private static Voice theVoice = null;
    
    public static void speakKnock(int count){
    	if(theVoice == null){
    		theVoice = voiceManager.getVoice("kevin16");
    		theVoice.allocate();
    	}
    	
    	theVoice.speak("Knock! count "+count+"!");
    }
    
    public static void speakString(String message){
    	if(theVoice == null){
    		theVoice = voiceManager.getVoice("kevin16");
    		theVoice.allocate();
    	}
    	
    	theVoice.speak(message);
    }
    
    public static void end(){
    	theVoice.deallocate();
    }
}
