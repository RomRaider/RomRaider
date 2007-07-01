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

package enginuity.util;

import org.apache.log4j.Logger;
import org.jdesktop.jdic.filetypes.Action;
import org.jdesktop.jdic.filetypes.Association;
import org.jdesktop.jdic.filetypes.AssociationService;

import java.io.File;


public final class FileAssociator {
    private static final Logger LOGGER = Logger.getLogger(FileAssociator.class);

    private FileAssociator() {
        throw new UnsupportedOperationException();
    }

    public static boolean addAssociation(String extension, String command, /*String iconFileName,*/ String description) {
        // Add association 
        // StringTokenizer osName = new StringTokenizer(System.getProperties().getProperty("os.name"));

        // remove association if it already exists

        LOGGER.debug("Removing 1...");
        removeAssociation(extension);
        LOGGER.debug("Removing 2...");

        AssociationService serv = new AssociationService();
        Association logassoc = new Association();

        logassoc.addFileExtension(extension.toUpperCase());
        logassoc.setDescription(description);
        logassoc.addAction(new Action("open", command + " %1"));
        logassoc.setIconFileName(new File("").getAbsolutePath() + "/graphics/enginuity-ico.ico");

        LOGGER.debug("Adding ...\n" + logassoc + "\n\n\n");

        try {
            serv.registerUserAssociation(logassoc);
        } catch (Exception e) {
            LOGGER.error("Error adding association", e);
        }

        return true;
    }


    public static boolean removeAssociation(String extension) {
        AssociationService serv = new AssociationService();
        Association logassoc = serv.getFileExtensionAssociation(extension.toUpperCase());

        LOGGER.debug("Removing ...\n" + logassoc + "\n\n\n");

        try {
            serv.unregisterUserAssociation(logassoc);
        } catch (Exception e) {
            LOGGER.error("Error removing association", e);
        }
        return true;
    }
}