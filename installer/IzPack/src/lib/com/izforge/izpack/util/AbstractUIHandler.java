/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
 * 
 * Copyright 2003 Tino Schwarze
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

package com.izforge.izpack.util;

/**
 * This interface describes basic functionality neccessary for user interaction.
 * 
 * All methods or functions which perform work and need to notify or ask the user use a listener for
 * such purposes. This way, we can separate UI from function.
 * 
 */

public interface AbstractUIHandler
{

    /**
     * Notify the user about something.
     * 
     * The difference between notification and warning is that a notification should not need user
     * interaction and can savely be ignored.
     * 
     * @param message The notification.
     */
    public void emitNotification(String message);

    /**
     * Warn the user about something.
     * 
     * @param title The message title (used for dialog name, might not be displayed)
     * @param message The warning message.
     * @return true if the user decided not to continue
     */
    public boolean emitWarning(String title, String message);

    /**
     * Notify the user of some error.
     * 
     * @param title The message title (used for dialog name, might not be displayed)
     * @param message The error message.
     */
    public void emitError(String title, String message);

    // constants for asking questions
    // must all be >= 0!
    public static final int ANSWER_CANCEL = 45;

    public static final int ANSWER_YES = 47;

    public static final int ANSWER_NO = 49;

    // values for choices to present to the user
    public static final int CHOICES_YES_NO = 37;

    public static final int CHOICES_YES_NO_CANCEL = 38;

    /**
     * Ask the user a question.
     * 
     * @param title The title of the question (useful for dialogs). Might be null.
     * @param question The question.
     * @param choices The set of choices to present. Either CHOICES_YES_NO or CHOICES_YES_NO_CANCEL
     * 
     * @return The user's choice. (ANSWER_CANCEL, ANSWER_YES or ANSWER_NO)
     */
    public int askQuestion(String title, String question, int choices);

    /**
     * Ask the user a question.
     * 
     * @param title The title of the question (useful for dialogs). Might be null.
     * @param question The question.
     * @param choices The set of choices to present. Either CHOICES_YES_NO or CHOICES_YES_NO_CANCEL
     * @param default_choice The default choice. One of ANSWER_CANCEL, ANSWER_YES or ANSWER_NO.
     * 
     * @return The user's choice. (ANSWER_CANCEL, ANSWER_YES or ANSWER_NO)
     */
    public int askQuestion(String title, String question, int choices, int default_choice);

}
