/*
 * IzPack - Copyright 2001-2007 Julien Ponge, All Rights Reserved.
 * 
 * http://izpack.org/
 * http://developer.berlios.de/projects/izpack/
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

import java.util.Map;

import org.apache.regexp.RE;

import com.izforge.izpack.panels.ProcessingClient;
import com.izforge.izpack.panels.RuleInputField;
import com.izforge.izpack.panels.Validator;

/**
 * A validator to enforce non-empty fields.
 * 
 * This validator can be used for rule input fields in the UserInputPanel to make sure that the
 * user's entry matches a specified regular expression.
 * 
 * @author Mike Cunneen <mike dot cunneen at screwfix dot com>
 */
public class RegularExpressionValidator implements Validator
{

    public static final String STR_PATTERN_DEFAULT = "[a-zA-Z0-9._-]{3,}@[a-zA-Z0-9._-]+([.][a-zA-Z0-9_-]+)*[.][a-zA-Z0-9._-]{2,4}";

    private static final String PATTERN_PARAM = "pattern";

    public boolean validate(ProcessingClient client)
    {

        String patternString;

        RuleInputField field = (RuleInputField) client;
        if (field.hasParams())
        {
            Map paramMap = field.getValidatorParams();
            patternString = (String) paramMap.get(PATTERN_PARAM);

        }
        else
        {
            patternString = STR_PATTERN_DEFAULT;
        }

        RE pattern = new RE(patternString);
        return pattern.match(((RuleInputField) client).getText());
    }

}
