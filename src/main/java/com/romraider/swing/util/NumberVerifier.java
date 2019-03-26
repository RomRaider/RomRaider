/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2019 RomRaider.com
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

package com.romraider.swing.util;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.swing.InputVerifier;
import javax.swing.JComponent;
import javax.swing.JTextField;

import com.romraider.util.ResourceUtil;


/**
 * NumberVerifier checks a text field to ensure that the user has entered
 * a valid entry.  It errors if the field is left empty or if a locale specific
 * number cannot be parsed.
 */
public class NumberVerifier extends InputVerifier {
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            NumberVerifier.class.getName());
    private String fieldName;

    /**
     * NumberVerifier takes the field name to use in the messages to the user.
     * @param fieldName - String name of the field being verified.
     */
    public NumberVerifier(final String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public boolean verify(final JComponent input) {
        if (!(input instanceof JTextField)) {
            return true;
        }
        final JTextField inputField = (JTextField) input;
        final String itemSel = inputField.getText();
        if (itemSel.isEmpty()) {
            showMessageDialog(null,
                    MessageFormat.format(
                            rb.getString("EMPTY"), fieldName),
                    rb.getString("ERROR"), ERROR_MESSAGE);
            return false;
        }
        final Scanner s = new Scanner(itemSel);
        if (s.hasNextDouble()) {
            return true;
        }
        else {
            showMessageDialog(null,
                    MessageFormat.format(
                            rb.getString("INVALID"), fieldName),
                    rb.getString("ERROR"), ERROR_MESSAGE);
            return false;
        }
    }
}
