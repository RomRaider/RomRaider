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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.apache.regexp.RE;

import com.izforge.izpack.installer.InstallData;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.VariableSubstitutor;

/*---------------------------------------------------------------------------*/
/**
 * This class assists the user in entering serial numbers. <BR>
 * <BR>
 * Serial numbers, license number, CD keys and the like are often lenghty alpha-numerical numbers.
 * In many cases they are devided into multiple parts by dash or point separators. Entering these in
 * a single text field can be a frustrating experience for the user. This class provides a way of
 * presenting the user with an assembly of input fields that are arranged in the same way as the
 * key, with the separators already in place. Immideate testing for format compliance if performed
 * ans soon as each field is completed. In addition, the cursor is automatically advanced to make
 * entering numbers as painless as possible. <br>
 * <br>
 * <b>Formatting:</b>
 * 
 * <ul>
 * <li><code>N:X:Y </code>- numeric field, accepts digits only
 * <li><code>H:X:Y </code>- hex field, accepts only hexadecimal digits
 * <li><code>A:X:Y </code>- alpha field, accepts only letters, no digits
 * <li><code>AN:X:Y</code>- alpha-numeric field, accepts digits and letters
 * </ul>
 * <b>Example:</b> <br>
 * <br>
 * <code>"N:4:4 - H:6:6 - AN:3:3 x A:5:5"</code><br>
 * <br>
 * This formatting string will produce a serial number field consisting of four separate input
 * fields. The fisrt input field will accept four numeric digits, the second six hexa-decimal
 * digits, the third three alpha-numeric digits and the fourth five letters. The first three input
 * fields will be separated by '-' and the third and fourth by 'x'. The following snapshot was
 * obtained with this setting: <br>
 * <br>
 * <img src="doc-files/RuleInputField-1.gif"/>
 * 
 * @version 0.0.1 / 10/19/02
 * @author Elmar Grom
 */
/*---------------------------------------------------------------------------*/
public class RuleInputField extends JComponent implements KeyListener, FocusListener,
        CaretListener, ProcessingClient
{

    /**
     * 
     */
    private static final long serialVersionUID = 3832616275124958257L;

    /**
     * Used to specify the retsult format. This constant specifies to return the contents of all
     * fields concatenated into one long string, with separation between each component.
     */
    public static final int PLAIN_STRING = 1;

    /**
     * Used to specify the retsult format. This constant specifies to return the contents of all
     * fields together with all separators as specified in the field format concatenated into one
     * long string. In this case the resulting string looks just like the user saw it during data
     * entry
     */
    public static final int DISPLAY_FORMAT = 2;

    /**
     * Used to specify the retsult format. This constant specifies to return the contents of all
     * fields concatenated into one long string, with a special separator string inserted in between
     * the individual components.
     */
    public static final int SPECIAL_SEPARATOR = 3;

    /**
     * Used to specify the retsult format. This constant specifies to return the contents of all
     * fields in a somehow modified way. How the content is modified depends on the operation
     * performed by a encryption service class. The class must be provided at object instatiation.
     */
    public static final int ENCRYPTED = 4;

    /** Used internally to identify the default setting for the return format. */
    private static int DEFAULT = DISPLAY_FORMAT;

    private Vector items = new Vector();

    /**
     * This <code>Vector</code> holds a reference to each input field, in the order in which they
     * appear on the screen.
     */
    private Vector inputFields = new Vector();

    private boolean hasParams = false;

    private Map validatorParams;

    private RuleTextField activeField;

    private boolean backstep = false;

    private Toolkit toolkit;

    private String separator;

    private int resultFormat = DEFAULT;

    private InstallData idata = null;

    /**
     * Holds an instance of the <code>Validator</code> if one was specified and available
     */
    private Validator validationService;

    /**
     * Holds an instance of the <code>Processor</code> if one was specified and available
     */
    private Processor encryptionService;

    /**
     * @return true if this instance has any parameters to pass to the Validator instance.
     */
    public boolean hasParams()
    {
        return hasParams;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Constructs a rule input field.
     * 
     * @param format a string that specifies the formatting and to a limited degree the behavior of
     * this field.
     * @param preset a string that specifies preset values for specific sub-fields.
     * @param separator a string to be used for separating the contents of individual fields in the
     * string returned by <code>getText()</code>
     * @param validator A string that specifies a class to perform validation services. The string
     * must completely identify the class, so that it can be instantiated. The class must implement
     * the <code>RuleValidator</code> interface. If an attempt to instantiate this class fails, no
     * validation will be performed.
     * @param validatorParams A <code>java.util.Map</code> containing name/ value pairs, which
     * will be forwarded to the validator.
     * @param processor A string that specifies a class to perform processing services. The string
     * must completely identify the class, so that it can be instantiated. The class must implement
     * the <code>Processor</code> interface. If an attempt to instantiate this class fails, no
     * processing will be performed. Instead, the text is returned in the default formatting.
     * @param resultFormat specifies in which format the resulting text should be returned, wehn
     * <code>getText()</code> is called. The following values are legal:<br>
     * <ul>
     * <li>PLAIN_STRING
     * <li>DISPLAY_FORMAT
     * <li>SPECIAL_SEPARATOR
     * <li>ENCRYPTED
     * </ul>
     * @param toolkit needed to gain access to <code>beep()</code>
     */
    /*--------------------------------------------------------------------------*/
    public RuleInputField(String format, String preset, String separator, String validator,
            Map validatorParams, String processor, int resultFormat, Toolkit toolkit,
            InstallData idata)
    {
        this(format, preset, separator, validator, processor, resultFormat, toolkit, idata);
        this.validatorParams = validatorParams;
        this.hasParams = true;
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Constructs a rule input field.
     * 
     * @param format a string that specifies the formatting and to a limited degree the behavior of
     * this field.
     * @param preset a string that specifies preset values for specific sub-fields.
     * @param separator a string to be used for separating the contents of individual fields in the
     * string returned by <code>getText()</code>
     * @param validator A string that specifies a class to perform validation services. The string
     * must completely identify the class, so that it can be instantiated. The class must implement
     * the <code>RuleValidator</code> interface. If an attempt to instantiate this class fails, no
     * validation will be performed.
     * @param processor A string that specifies a class to perform processing services. The string
     * must completely identify the class, so that it can be instantiated. The class must implement
     * the <code>Processor</code> interface. If an attempt to instantiate this class fails, no
     * processing will be performed. Instead, the text is returned in the default formatting.
     * @param resultFormat specifies in which format the resulting text should be returned, wehn
     * <code>getText()</code> is called. The following values are legal:<br>
     * <ul>
     * <li>PLAIN_STRING
     * <li>DISPLAY_FORMAT
     * <li>SPECIAL_SEPARATOR
     * <li>ENCRYPTED
     * </ul>
     * @param toolkit needed to gain access to <code>beep()</code>
     */
    /*--------------------------------------------------------------------------*/
    public RuleInputField(String format, String preset, String separator, String validator,
            String processor, int resultFormat, Toolkit toolkit, InstallData idata)
    {
        this.toolkit = toolkit;
        this.separator = separator;
        this.resultFormat = resultFormat;
        this.idata = idata;

        com.izforge.izpack.gui.FlowLayout layout = new com.izforge.izpack.gui.FlowLayout();
        layout.setAlignment(com.izforge.izpack.gui.FlowLayout.LEFT);
        setLayout(layout);

        // ----------------------------------------------------
        // attempt to create an instance of the Validator
        // ----------------------------------------------------
        try
        {
            if (validator != null)
            {
                validationService = (Validator) Class.forName(validator).newInstance();
            }
        }
        catch (Throwable exception)
        {
            validationService = null;            
            Debug.trace(exception);
        }

        // ----------------------------------------------------
        // attempt to create an instance of the Processor
        // ----------------------------------------------------
        try
        {
            if (processor != null)
            {
                encryptionService = (Processor) Class.forName(processor).newInstance();
            }
        }
        catch (Throwable exception)
        {
            encryptionService = null;
            Debug.trace(exception);
        }

        // ----------------------------------------------------
        // create the fields and field separators
        // ----------------------------------------------------
        createItems(format);

        if ((preset != null) && (preset.length() > 0))
        {
            setFields(preset);
        }

        // ----------------------------------------------------
        // set the focus to the first field
        // ----------------------------------------------------
        activeField = (RuleTextField) inputFields.elementAt(0);
        activeField.grabFocus();
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the number of sub-fields composing this <code>RuleInputField</code>.
     * 
     * @return the number of sub-fields
     */
    /*--------------------------------------------------------------------------*/
    public int getNumFields()
    {
        return (inputFields.size());
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the contents of the field indicated by <code>index</code>.
     * 
     * @param index the index of the sub-field from which the contents is requested.
     * 
     * @return the contents of the indicated sub-field.
     * 
     * @exception IndexOutOfBoundsException if the index is out of bounds.
     */
    /*--------------------------------------------------------------------------*/
    public String getFieldContents(int index) throws IndexOutOfBoundsException
    {
        if ((index < 0) || (index > (inputFields.size() - 1))) { throw (new IndexOutOfBoundsException()); }

        return (((JTextField) inputFields.elementAt(index)).getText());
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Returns the validator parameters, if any. The caller should check for the existence of
     * validator parameters via the <code>hasParams()</code> method prior to invoking this method.
     * 
     * @return a java.util.Map containing the validator parameters.
     */
    public Map getValidatorParams()
    {
        return validatorParams;
    }

    /*---------------------------------------------------------------------------*/
    /**
     * Returns the field contents, assembled acording to the encryption and separator rules.
     * 
     * @return the field contents
     */
    /*--------------------------------------------------------------------------*/
    public String getText()
    {
        Object item;
        StringBuffer buffer = new StringBuffer();
        int size = inputFields.size();

        // ----------------------------------------------------
        // have the encryption service class perfrom the task
        // of assembling an output string. If we have no instance
        // of this class available set the formatting
        // instruction to the default setting.
        // ----------------------------------------------------
        if (resultFormat == ENCRYPTED)
        {
            if (encryptionService != null)
            {
                buffer.append(encryptionService.process(this));
            }
            else
            {
                resultFormat = DEFAULT;
            }
        }

        // ----------------------------------------------------
        // concatentate the field contents, with no separators
        // in between.
        // ----------------------------------------------------
        else if (resultFormat == PLAIN_STRING)
        {
            for (int i = 0; i < inputFields.size(); i++)
            {
                buffer.append(((JTextField) inputFields.elementAt(i)).getText());
            }
        }

        // ----------------------------------------------------
        // concatenate the field contents and setarators, as
        // specified for the display of the field.
        // ----------------------------------------------------
        else if (resultFormat == DISPLAY_FORMAT)
        {
            for (int i = 0; i < items.size(); i++)
            {
                item = items.elementAt(i);
                if (item instanceof JTextField)
                {
                    buffer.append(((JTextField) item).getText());
                }
                else
                {
                    buffer.append((String) item);
                }
            }
        }

        // ----------------------------------------------------
        // concatenate the field contents and insert the
        // separator string in between.
        // ----------------------------------------------------
        else if (resultFormat == SPECIAL_SEPARATOR)
        {
            for (int i = 0; i < size; i++)
            {
                buffer.append(((JTextField) inputFields.elementAt(i)).getText());

                if (i < (size - 1))
                {
                    buffer.append(separator);
                }
            }
        }

        return (buffer.toString());
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Creates the items that make up this field. Both separators and input fields are considered
     * items. The items created are stored in <code>items</code>. In addition, all fields are
     * stored in <code>inputFields</code>.
     * 
     * @param format a string that specifies the layout of the input fields and separators.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design
     * 
     * I used a simple StringTokenizer to break the format string into individual tokens. The
     * approach in building up the field is to consider each token a potential definition for an
     * input field. Therefore I attempt to create an instance of FieldSpec from each token.
     * FieldSpec analyzes the token and if it does not represent a valid specification for an input
     * field throws an exception. If I catch an exception, I know the token does not represent a
     * valid field specification. In this case I treat the token as a separator, even though this
     * might not be what the user had intended. However, this approach allows me to implement robust
     * behavior (no exception thrown) even though the user might have made a mistake in the
     * definition. The mistake should become immediately obvious when testing the code, since a
     * input field definition would show up as separator between two fields.
     * --------------------------------------------------------------------------
     */
    private void createItems(String format)
    {
        Object item;
        JTextField field;
        String token;
        FieldSpec spec;
        StringTokenizer tokenizer = new StringTokenizer(format);

        while (tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();
            try
            {
                spec = new FieldSpec(token);
                field = new RuleTextField(spec.getColumns(), spec.getEditLength(), spec.getType(),
                        spec.getUnlimitedEdit(), toolkit);

                // ------------------------------------------------
                // if the previous item is also a field, insert a
                // space as separator
                // ------------------------------------------------
                if (items.size() > 0)
                {
                    item = items.lastElement();

                    if (item instanceof JTextField)
                    {
                        items.add(" ");
                    }
                }

                items.add(field);
                inputFields.add(field);
                field.addFocusListener(this);
                field.addKeyListener(this);
                field.addCaretListener(this);
            }
            // --------------------------------------------------
            // if we were not successful creating an input field,
            // the token must be a separator or the definition
            // has an error. Simply insert it as separator,
            // when testing the installer the error should become
            // obvious to the developer.
            // --------------------------------------------------
            catch (Throwable exception)
            {
                if (items.size() == 0)
                {
                    items.add(token);
                }
                else
                {
                    item = items.lastElement();

                    // ----------------------------------------------
                    // if the previous item is also a separator,
                    // simply concatenate the token with a space
                    // inserted in between, don't add it as new
                    // separator.
                    // ----------------------------------------------
                    if (item instanceof String)
                    {
                        items.setElementAt(item + " " + token, (items.size() - 1));
                    }
                    else
                    {
                        items.add(token);
                    }
                }
            }
        }

        // ----------------------------------------------------
        // add the fields and separators to the component
        // ----------------------------------------------------
        for (int i = 0; i < items.size(); i++)
        {
            item = items.elementAt(i);

            if (item instanceof String)
            {
                add(new JLabel((String) item));
            }
            else
            {
                add((JTextField) item);
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Sets each field to a pre-defined value.
     * 
     * @param data a <code>String</code> containing the preset values for each field. The format
     * of the string is as follows: The content for the individuals fields must be separated by
     * whitespace. Each data block is preceeded by the index of the field to set (counting starts at
     * 0) followed by a colon ':'and after that the actual data for the field.
     */
    /*--------------------------------------------------------------------------*/
    private void setFields(String data)
    {
        StringTokenizer tokenizer = new StringTokenizer(data);
        String token;
        String indexString;
        int index;
        boolean process = false;
        String[] vals = null;
        int i = 0;

        vals = new String[tokenizer.countTokens()];
        while (tokenizer.hasMoreTokens())
        {
            token = tokenizer.nextToken();
            indexString = token.substring(0, token.indexOf(':'));

            try
            {
                index = Integer.parseInt(indexString);
                if (index < inputFields.size())
                {
                    String val = token.substring((token.indexOf(':') + 1), token.length());
                    String className = "";
                    if (val.indexOf(":") > -1)
                    {
                        className = val.substring(val.indexOf(":") + 1);
                        val = val.substring(0, val.indexOf(":"));
                    }

                    if (!"".equals(className) && !process)
                    {
                        process = true;
                    }
                    VariableSubstitutor vs = new VariableSubstitutor(idata.getVariables());
                    val = vs.substitute(val, null);
                    vals[i] = val;
                    i++;
                    ((JTextField) inputFields.elementAt(index)).setText(val);
                }
            }
            catch (Throwable exception)
            {
                exception.printStackTrace();
            }
        }

        if (process)
        {
            tokenizer = new StringTokenizer(data);
            while (tokenizer.hasMoreTokens())
            {
                token = tokenizer.nextToken();
                indexString = token.substring(0, token.indexOf(':'));

                try
                {
                    index = Integer.parseInt(indexString);
                    if (index < inputFields.size())
                    {
                        String val = token.substring((token.indexOf(':') + 1), token.length());
                        String className = "";
                        String presult = "";
                        if (val.indexOf(":") > -1)
                        {
                            className = val.substring(val.indexOf(":") + 1);
                            val = val.substring(0, val.indexOf(":"));
                        }

                        if (!"".equals(className))
                        {
                            Processor p = (Processor) Class.forName(className).newInstance();
                            presult = p.process(this);
                        }
                        String[] td = new RE("\\*").split(presult);
                        ((JTextField) inputFields.elementAt(index)).setText(td[index]);
                    }
                }
                catch (Throwable exception)
                {}
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method validates the field content. Validating is performed through a user supplied
     * service class that provides the validation rules.
     * 
     * @return <code>true</code> if the validation passes or no implementation of a validation
     * rule exists. Otherwise <code>false</code> is returned.
     */
    /*--------------------------------------------------------------------------*/
    public boolean validateContents()
    {
        if (validationService != null)
        {
            return (validationService.validate(this));
        }
        else
        {
            return (true);
        }
    }

    /*---------------------------------------------------------------------------*
     Implementation for KeyListener
     *---------------------------------------------------------------------------*/

    /*--------------------------------------------------------------------------*/
    /**
     * This method is invoked when a key has been typed. The event occurs when a key press is
     * followed by a key release.
     * 
     * @param event the key event forwarded by the system.
     */
    /*--------------------------------------------------------------------------*/
    public void keyTyped(KeyEvent event)
    {
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method is invoked when a key has been pressed. This method verifies the condition of the
     * input field in focus. Once the column count in the field has reached the specified maximum,
     * the rule specified for the field in question is invoked. In case the test result is positive,
     * focus is set to the next field. If hte test result is negative, the field content is marked
     * and the caret set to the start of the field.
     * 
     * @param event the key event forwarded by the system.
     */
    /*--------------------------------------------------------------------------*/
    public void keyPressed(KeyEvent event)
    {
        if (event.getKeyCode() == KeyEvent.VK_BACK_SPACE)
        {
            int caretPosition = activeField.getCaretPosition();

            if (caretPosition == 0)
            {
                int activeIndex = inputFields.indexOf(activeField);

                if (activeIndex > 0)
                {
                    activeIndex--;
                    backstep = true;
                    activeField = (RuleTextField) inputFields.elementAt(activeIndex);
                    activeField.grabFocus();
                }
            }
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * This method is invoked when a key has been released.
     * 
     * @param event the key event forwarded by the system.
     */
    /*--------------------------------------------------------------------------*/
    public void keyReleased(KeyEvent event)
    {
    }

    /*---------------------------------------------------------------------------*
     Implementation for FocusListener
     *---------------------------------------------------------------------------*/

    /*--------------------------------------------------------------------------*/
    /**
     * Invoked when a component gains the keyboard focus.
     * 
     * @param event the focus event forwardes by the sytem.
     */
    /*--------------------------------------------------------------------------*/
    /*
     * $ @design <- keep this tag in place and don't write on this line!
     * 
     * Enter design related documentation here.
     * --------------------------------------------------------------------------
     */
    public void focusGained(FocusEvent event)
    {
        activeField = (RuleTextField) event.getSource();

        if (backstep)
        {
            activeField.setCaretPosition(activeField.getText().length());
            backstep = false;
        }
        else
        {
            activeField.selectAll();
        }
    }

    /*--------------------------------------------------------------------------*/
    /**
     * Invoked when a component loses the keyboard focus. This method does nothing, we are only
     * interested in 'focus gained' events.
     * 
     * @param event the focus event forwardes by the sytem.
     */
    /*--------------------------------------------------------------------------*/
    public void focusLost(FocusEvent event)
    {
    }

    /*---------------------------------------------------------------------------*
     Implementation for CaretListener
     *---------------------------------------------------------------------------*/

    /*--------------------------------------------------------------------------*/
    /**
     * Called when the caret position is updated.
     * 
     * @param event the caret event received from the text field
     */
    /*--------------------------------------------------------------------------*/
    public void caretUpdate(CaretEvent event)
    {
        if (activeField != null)
        {
            String text = activeField.getText();
            int fieldSize = activeField.getEditLength();
            int caretPosition = activeField.getCaretPosition();
            int selection = activeField.getSelectionEnd() - activeField.getSelectionStart();

            if ((!inputFields.lastElement().equals(activeField)) && (!activeField.unlimitedEdit()))
            {
                if ((text.length() == fieldSize) && (selection == 0)
                        && (caretPosition == fieldSize) && !backstep)
                {
                    activeField.transferFocus();
                }
            }
        }
    }

    // ----------------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------------
    private static class FieldSpec
    {

        private int MIN_TOKENS = 2;

        private int MAX_TOKENS = 3;

        private int type;

        private int columns;

        private int editLength;

        private boolean unlimitedEdit = false;

        public FieldSpec(String spec) throws Exception
        {
            StringTokenizer tokenizer = new StringTokenizer(spec, ":");

            if ((tokenizer.countTokens() >= MIN_TOKENS) && (tokenizer.countTokens() <= MAX_TOKENS))
            {
                String token = tokenizer.nextToken().toUpperCase();
                // ------------------------------------------------
                // test the first token for a valid type identifier
                // if it's valid assign the token to the type.
                // ------------------------------------------------
                if ("N".equals(token))
                {
                    type = RuleTextField.N;
                }
                else if ("H".equals(token))
                {
                    type = RuleTextField.H;
                }
                else if ("A".equals(token))
                {
                    type = RuleTextField.A;
                }
                else if ("O".equals(token))
                {
                    type = RuleTextField.O;
                }
                else if ("AN".equals(token))
                {
                    type = RuleTextField.AN;
                }
                else
                {
                    throw (new Exception());
                }

                // ------------------------------------------------
                // test for a valid integer to define the size
                // of the field and assing to columns.
                // ------------------------------------------------
                try
                {
                    token = tokenizer.nextToken();
                    columns = Integer.parseInt(token);
                }
                catch (Throwable exception)
                {
                    throw (new Exception());
                }

                // ------------------------------------------------
                // test for a valid integer to define the edit
                // length and assign to editLength. If this fails
                // test for identifier for unlimited edit length.
                // If this works, set unlimitedEdit to true.
                // ------------------------------------------------
                try
                {
                    token = tokenizer.nextToken().toUpperCase();
                    editLength = Integer.parseInt(token);
                }
                catch (Throwable exception)
                {
                    if ("U".equals(token))
                    {
                        unlimitedEdit = true;
                    }
                    else
                    {
                        throw (new Exception());
                    }
                }

            }
            else
            {
                throw (new Exception());
            }
        }

        public int getColumns()
        {
            return (columns);
        }

        public int getEditLength()
        {
            return (editLength);
        }

        public int getType()
        {
            return (type);
        }

        public boolean getUnlimitedEdit()
        {
            return (unlimitedEdit);
        }

    }
    // ----------------------------------------------------------------------------

}
/*---------------------------------------------------------------------------*/
