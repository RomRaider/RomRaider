/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.tts;

import static com.romraider.util.ThreadUtil.runAsDaemon;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;

public class Speaker {
    private static final String VOICE_NAME = "kevin16";
    private static final VoiceManager VOICE_MANAGER = VoiceManager.getInstance();
    private static final Voice VOICE = VOICE_MANAGER.getVoice(VOICE_NAME);

    static {
        VOICE.allocate();
    }

    private Speaker() {
        throw new UnsupportedOperationException();
    }

    public static void say(final String message) {
        runAsDaemon(new Runnable() {
            public void run() {
                try {
                    VOICE.speak(message);
                } catch (Exception e) {
                    // ignore
                }
            }
        });
    }

    public static void end() {
        VOICE.deallocate();
    }
}
