/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2002 Elmar Grom
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels;

import java.awt.Toolkit;

import javax.swing.JTextField;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

/*---------------------------------------------------------------------------*/
/**
 * One line synopsis. <BR>
 * <BR>
 * Enter detailed class description here.
 * 
 * @see UserInputPanel
 * 
 * @version 0.0.1 / 10/20/02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class RuleTextField extends JTextField
{

    /**
     * 
     */
    private static final long serialVersionUID = 3976731454594365493L;

    /** Used to specify numeric input only */
    public static final int N = 1;

    /** Used to specify hexadecimal input only */
    public static final int H = 2;

    /** Used to specify alphabetic input only */
    public static final int A = 3;

    /** Used to specify open input (no restrictions) */
    public static final int O = 4;

    /** Used to specify alpha-numeric input only */
    public static final int AN = 5;

    private int columns;

    private int editLength;

    private boolean unlimitedEdit;

    private Toolkit toolkit;

    public RuleTextField(int digits, int editLength, int type, boolean unlimitedEdit,
                         Toolkit toolkit)
    {
        super(digits + 1);

        setColumns(digits);
        this.toolkit = toolkit;
        this.editLength = editLength;
        this.unlimitedEdit = unlimitedEdit;
        Rule rule = new Rule();
        rule.setRuleType(type, editLength, unlimitedEdit);
        setDocument(rule);
    }

    protected Document createDefaultModel()
    {
        Rule rule = new Rule();
        return (rule);
    }

    public int getColumns()
    {
        return (columns);
    }

    public int getEditLength()
    {
        return (editLength);
    }

    public boolean unlimitedEdit()
    {
        return (unlimitedEdit);
    }

    public void setColumns(int columns)
    {
        super.setColumns(columns + 1);
        this.columns = columns;
    }

    // --------------------------------------------------------------------------
    //
    // --------------------------------------------------------------------------

    class Rule extends PlainDocument
    {

        /**
         * 
         */
        private static final long serialVersionUID = 3258134643651063862L;

        private int editLength;

        private int type;

        private boolean unlimitedEdit;

        public void setRuleType(int type, int editLength, boolean unlimitedEdit)
        {
            this.type = type;
            this.editLength = editLength;
            this.unlimitedEdit = unlimitedEdit;
        }

        public void insertString(int offs, String str, AttributeSet a) throws BadLocationException
        {
            // --------------------------------------------------
            // don't process if we get a null reference
            // --------------------------------------------------
            if (str == null) { return; }

            // --------------------------------------------------
            // Compute the total length the string would become
            // if the insert request were be honored. If this
            // size is within the specified limits, apply further
            // rules, otherwise give an error signal and return.
            // --------------------------------------------------
            int totalSize = getLength() + str.length();

            if ((totalSize <= editLength) || (unlimitedEdit))
            {
                boolean error = false;

                // test for numeric type
                if (type == N)
                {
                    for (int i = 0; i < str.length(); i++)
                    {
                        if (!Character.isDigit(str.charAt(i)))
                        {
                            error = true;
                        }
                    }
                }
                // test for hex type
                else if (type == H)
                {
                    for (int i = 0; i < str.length(); i++)
                    {
                        char focusChar = Character.toUpperCase(str.charAt(i));
                        if (!Character.isDigit(focusChar) && (focusChar != 'A')
                                && (focusChar != 'B') && (focusChar != 'C') && (focusChar != 'D')
                                && (focusChar != 'E') && (focusChar != 'F'))
                        {
                            error = true;
                        }
                    }
                }
                // test for alpha type
                else if (type == A)
                {
                    for (int i = 0; i < str.length(); i++)
                    {
                        if (!Character.isLetter(str.charAt(i)))
                        {
                            error = true;
                        }
                    }
                }
                // test for alpha-numeric type
                else if (type == AN)
                {
                    for (int i = 0; i < str.length(); i++)
                    {
                        if (!Character.isLetterOrDigit(str.charAt(i)))
                        {
                            error = true;
                        }
                    }
                }
                // test for 'open' -> no limiting rule at all
                else if (type == O)
                {
                    // let it slide...
                }
                else
                {
                    System.out.println("type = " + type);
                }

                // ------------------------------------------------
                // if we had no error when applying the rules, we
                // are ready to insert the string, otherwise give
                // an error signal.
                // ------------------------------------------------
                if (!error)
                {
                    super.insertString(offs, str, a);
                }
                else
                {
                    toolkit.beep();
                }
            }
            else
            {
                toolkit.beep();
            }
        }
    }
}
/*---------------------------------------------------------------------------*/
