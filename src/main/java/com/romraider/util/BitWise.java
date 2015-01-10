/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2015 RomRaider.com
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

package com.romraider.util;

import java.util.Stack;

import org.nfunk.jep.ParseException;
import org.nfunk.jep.function.PostfixMathCommand;

/**
 * Performs a bitwise operation on the inStack variables. The parameters are popped
 * off the <code>inStack</code> and then processed.
 * <p>
 * These bitwise operations are supported:
 * <ol>
 * <li>bitwise AND  &
 * <li>bitwise inclusive OR  |
 * <li>bitwise exclusive OR  ^
 * <li>signed left shift  <<
 * <li>signed right shift  >>
 * <li>unsigned right shift  >>>
 * <li>unary bitwise complement  ~</ol>
 * <p>
 * <dl>
 * <dt>Usage:</dt>
 * <dd>Three parameters must be passed in the <code>BitWise</code> function.
 * The first parameter is a value such as the mask used by the operation <i>
 * (this value is required but ignored when selecting the unary bitwise
 * complement operation)</i>.  The second parameter is the variable 'x' to
 * perform the operation on and the third parameter is the index number from
 * the above list to select the operation to be performed.</dd>
 * <dt>Syntax:</dt>
 * <dd><code>BitWise(Number mask, Number variable, Number operation)</code></dd>
 * <dt>Example:</dt>
 * <dd>Perform the AND operation on the variable 'x' using a mask of 0x18</dd>
 * <dd><code>BitWise(24, x, 1)</code></dd>
 * <dd><i>Note: the values must be specified in decimal format since using hex format
 * 0x18 will cause a parsing error</i></dd> 
 * </dl>
 * @param   mask        - a value such as the mask or count of bits for Shift
 * @param   variable    - the variable 'x'
 * @param   operation   - the index number from the above list to select the operation
 *
 * @return  the resulting value is pushed back to the top of <code>inStack</code>
 * 
 * @exception ParseException is thrown when parse errors are encountered
 */
class BitWise extends PostfixMathCommand {

    public BitWise() {
        numberOfParameters = 3;
    }
    
    /**
     * Runs a bitwise operation on the inStack variables. The parameters are popped
     * off the <code>inStack</code>, processed and the resulting value is 
     * pushed back to the top of <code>inStack</code>.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void run(Stack inStack) throws ParseException {

        checkStack(inStack);

        Object operation = inStack.pop();
        Object variable = inStack.pop();
        Object mask = inStack.pop();

        if (mask instanceof Double) {
            double r = 0;
            switch (((Double) operation).intValue()) {
                case 1:  r = ((Double) variable).intValue() & ((Double) mask).intValue();
                    break;
                case 2:  r = ((Double) variable).intValue() | ((Double) mask).intValue();
                    break;
                case 3:  r = ((Double) variable).intValue() ^ ((Double) mask).intValue();
                    break;
                case 4:  r = ((Double) variable).intValue() << ((Double) mask).intValue();
                    break;
                case 5:  r = ((Double) variable).intValue() >> ((Double) mask).intValue();
                    break;
                case 6:  r = ((Double) variable).intValue() >>> ((Double) mask).intValue();
                    break;
                case 7:  r = ~((Double) variable).intValue();
                    break;
                default:
                    break;
            }
            inStack.push(new Double(r));
        }
        else {
            throw new ParseException("Invalid parameter type");
        }
    }
}
