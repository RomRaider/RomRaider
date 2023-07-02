/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2021 RomRaider.com
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

//This object defines the scaling factor and offset for calculating real values

package com.romraider.maps;

import java.io.Serializable;
import java.util.HashMap;

import com.romraider.util.JEPUtil;

public class Scale implements Serializable {

    private static final long serialVersionUID = 5836610685159474795L;

    private String category = "Raw Value";
    private String name = "Raw Value";
    private String unit = "raw value";
    private String expression = "x";
    private String byteExpression = "x";
    private String format = "#.##";
    private double coarseIncrement = 2;
    private double fineIncrement = 1;
    private double min = 0.0;
    private double max = 0.0;
    
    HashMap<Double, Double> cachedValues = new HashMap<Double,Double>();
    int maxCacheSize = 100;

    @Override
    public String toString() {
        return  "\n    ---- Scale ----" +
                "\n    Category: " + getCategory() +
                "\n    Name: " + getName() +
                "\n    Expression: " + getExpression() +
                "\n    Byte Expression: " + getByteExpression() +
                "\n    Unit: " + getUnit() +
                "\n    Format: " + getFormat() +
                "\n    Coarse Increment: " + getCoarseIncrement() +
                "\n    Fine Increment: " + getFineIncrement() +
                "\n    Min: " + getMin() +
                "\n    Max: " + getMax() +
                "\n    ---- End Scale ----\n";
    }

    public boolean validate() {
    	//We use the approximation method here
    	if(getByteExpression() == null) return true;
    	
        if(expression.equals("x") && byteExpression.equals("x")) return true;

        double startValue = 5;
        // convert real world value of "5"
        double toReal = JEPUtil.evaluate(getExpression(), startValue);
        double endValue = JEPUtil.evaluate(getByteExpression(), toReal);

        // if real to byte doesn't equal 5, report conflict
        if (Math.abs(endValue - startValue) > .001) return false;
        else return true;
    }
   
    public double approximateToByteFunction(double input, int storageType, boolean signed) {
    	
    	// Check if we already calculated this
    	if(cachedValues.containsKey(input))
    	{
    		return cachedValues.get(input);
    	}
    	
    	long maxValue = (int) Math.pow(2, 8 * storageType);
    	long minValue = 0;
    	
    	if(signed) {
    		minValue = -maxValue/2;
    		maxValue = maxValue/2 - 1;   				
    	}
    	else {
    		maxValue--;
    	}
    	
    	double error = 1;
    	double lastError = 9999999;
    	
    	int currentStep = (int) ((maxValue - minValue) / 2);
    	int stepSize = (int) (Math.pow(2, 8 * storageType) / 2);;
    	double epsilon = 0.00001;
    	double output = 0;
    	
    	while(stepSize > 0 && error > epsilon) {  		
    		double minusValue = JEPUtil.evaluate(getExpression(), currentStep-stepSize);
    		double plusValue = JEPUtil.evaluate(getExpression(), currentStep+stepSize);
    		
    		double plusError = Math.abs(plusValue - input);
    		double minusError = Math.abs(minusValue - input);
    		    		
    		//Check if we need to go up or down
    		if(plusError < minusError) {
    			currentStep += stepSize;
    			error = plusError;
    		}
    		else {
    			currentStep -= stepSize;
    			error = minusError;
    		}
  		
    		if(error < lastError)
    			output = currentStep;
    		
    		if(error < epsilon)
    			break;
    		
			stepSize/=2;
			lastError = error;
    	}
    	
    	if(cachedValues.size() < maxCacheSize)
    	{
    		cachedValues.put(input, output);
    	}
    	
    	//System.out.println("Input: " + input + " from approx: " + JEPUtil.evaluate(getExpression(), output));
    	return currentStep;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public double getCoarseIncrement() {
        return coarseIncrement;
    }

    public void setCoarseIncrement(double increment) {
        this.coarseIncrement = increment;
    }

    public boolean isReady() {
        if (unit == null) {
            return false;
        } else if (expression == null) {
            return false;
        } else if (format == null) {
            return false;
        } else if (coarseIncrement < 1) {
            return false;
        }

        return true;
    }

    public String getByteExpression() {
        return byteExpression;
    }

    public void setByteExpression(String byteExpression) {
    	if(byteExpression.isEmpty())
    		this.byteExpression = null;
    	else
    		this.byteExpression = byteExpression;
    }

    public double getFineIncrement() {
        return fineIncrement;
    }

    public void setFineIncrement(double fineIncrement) {
        this.fineIncrement = fineIncrement;
    }

    /**
     *  <b>category</b> is used to group like scalings, such as Metric,
     *  Imperial, etc. (case insensitive).<br>
     *  This is the value shown in the Table Tool bar scaling selection list.
     * @return <b>category</b> name
     */
    public String getCategory() {
        return category;
    }

    /**
     * <b>name</b> is defined in a scalingbase element (case insensitive).<br>
     * <b>name</b> is used by the base attribute in a scaling element definition
     * to inherit from a scalingbase.
     * @return <b>name</b> as defined in scalingbase
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getMin() {
        return min;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public double getMax() {
        return max;
    }

    public void setMax(double max) {
        this.max = max;
    }

    @Override
    public boolean equals(Object other) {
        try {
            if(null == other) {
                return false;
            }

            if(other == this) {
                return true;
            }

            if(!(other instanceof Scale)) {
                return false;
            }

            Scale otherScale = (Scale)other;

            if( (null == this.getCategory() && null == otherScale.getCategory())
                    || (this.getCategory().isEmpty() && otherScale.getCategory().isEmpty()) )
            {
                ;// Skip Category compare if Category is null or empty.
            } else
            {
                if(!this.getCategory().equalsIgnoreCase(otherScale.getCategory()))
                {
                    return false;
                }
            }

            if( (null == this.getName() && null == otherScale.getName())
                    || (this.getName().isEmpty() && otherScale.getName().isEmpty()) )
            {
                ;// Skip name compare if name is null or empty.
            } else
            {
                if(!this.getName().equalsIgnoreCase(otherScale.getName()))
                {
                    return false;
                }
            }

            if(!this.getUnit().equals(otherScale.getUnit()))
            {
                return false;
            }

            if(!this.getExpression().equals(otherScale.getExpression()))
            {
                return false;
            }

            if(!this.getByteExpression().equals(otherScale.getByteExpression()))
            {
                return false;
            }

            if(!this.getFormat().equals(otherScale.getFormat()))
            {
                return false;
            }

            if(this.getCoarseIncrement() != otherScale.getCoarseIncrement())
            {
                return false;
            }

            if(this.getFineIncrement() != otherScale.getFineIncrement())
            {
                return false;
            }

            if(this.getMin() != otherScale.getMin())
            {
                return false;
            }

            if(this.getMax() != otherScale.getMax())
            {
                return false;
            }

            return true;
        } catch(Exception ex) {
            return false;
        }
    }
}
