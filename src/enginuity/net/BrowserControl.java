/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.net;

import java.io.IOException;

public class BrowserControl {

    public static void displayURL(String url) {
        boolean windows = isWindowsPlatform();
        String cmd = null;
        try {
            if (windows) {
                // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
                cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
                Runtime.getRuntime().exec(cmd);
            } else {
                cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
                Process p = Runtime.getRuntime().exec(cmd);
                try {
                    int exitCode = p.waitFor();
                    if (exitCode != 0) {
                        cmd = UNIX_PATH + " " + url;
                        Runtime.getRuntime().exec(cmd);
                    }
                }
                catch (InterruptedException x) {
                    System.err.println("Error bringing up browser, cmd='" +
                            cmd + "'");
                    System.err.println("Caught: " + x);
                }
            }
        }
        catch (IOException x) {
            // couldn't exec browser
            System.err.println("Could not invoke browser, command=" + cmd);
            System.err.println("Caught: " + x);
        }
    }

    public static boolean isWindowsPlatform() {
        String os = System.getProperty("os.name");
        if (os != null && os.startsWith(WIN_ID)) {
            return true;
        } else {
            return false;
        }
    }

    private static final String WIN_ID = "Windows";
    private static final String WIN_PATH = "rundll32";
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
    private static final String UNIX_PATH = "netscape";
    private static final String UNIX_FLAG = "-remote openURL";
}