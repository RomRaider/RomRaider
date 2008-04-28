/*
 * Created on May 31, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.romraider.logger.utec.tts;

import com.romraider.logger.utec.properties.UtecProperties;
import com.romraider.tts.Speaker;

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
