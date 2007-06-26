/*
 * Created on May 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package enginuity.logger.utec.tts;

import enginuity.logger.utec.properties.UtecProperties;
import enginuity.tts.Speaker;

public final class UtecSpeaker {
    private UtecSpeaker() {
        throw new UnsupportedOperationException();
    }

    public static void say(String message) {
        String[] toSpeak = UtecProperties.getProperties("utec.sound");
        boolean parsedBoolean = Boolean.parseBoolean(toSpeak[0]);
        if (parsedBoolean) {
            Speaker.say(message);
        }
    }
}
