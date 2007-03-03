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

package enginuity.logger.ecu.profile;

import enginuity.logger.ecu.profile.xml.UserProfileHandler;
import static enginuity.util.ParamChecker.checkNotNullOrEmpty;
import static enginuity.util.SaxParserFactory.getSaxParser;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public final class UserProfileLoaderImpl implements UserProfileLoader {

    public UserProfile loadProfile(String userProfileFilePath) {
        checkNotNullOrEmpty(userProfileFilePath, "userProfileFilePath");
        try {
            InputStream inputStream = new BufferedInputStream(new FileInputStream(new File(userProfileFilePath)));
            try {
                UserProfileHandler handler = new UserProfileHandler();
                getSaxParser().parse(inputStream, handler);
                return handler.getUserProfile();
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            System.out.println("Error loading user profile file: " + userProfileFilePath);
            return null;
        }
    }
}
