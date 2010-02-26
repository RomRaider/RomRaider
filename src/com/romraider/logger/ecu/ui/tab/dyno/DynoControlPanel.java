/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2009 RomRaider.com
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

package com.romraider.logger.ecu.ui.tab.dyno;

import static com.centerkey.utils.BareBonesBrowserLaunch.openURL;
import static com.romraider.Version.CARS_DEFS_URL;
import static com.romraider.util.ParamChecker.checkNotNull;
import static java.awt.GridBagConstraints.CENTER;
import static java.awt.GridBagConstraints.HORIZONTAL;
import static java.awt.GridBagConstraints.NONE;
import static javax.swing.JOptionPane.DEFAULT_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.WARNING_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static javax.swing.JOptionPane.showOptionDialog;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.romraider.editor.ecu.ECUEditor;
import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.ui.DataRegistrationBroker;
import com.romraider.logger.ecu.ui.tab.DynoChartPanel;

public final class DynoControlPanel extends JPanel {
    private static final long serialVersionUID = 3787020251963102201L;
    private static final String CARS_FILE = "cars_def.xml";
    private static final String MISSING_CAR_DEF = "Missing cars_def.xml";
    private static final Logger LOGGER = Logger.getLogger(DynoControlPanel.class);
    private static final String ENGINE_SPEED = "P8";
    private static final String VEHICLE_SPEED = "P9";
    private static final String IAT = "P11";
    private static final String THROTTLE_ANGLE = "P13";
    private static final String ATM = "P24";
    private static final String MANUAL = "manual";
    private static final String IMPERIAL = "Imperial";
    private static final String METRIC = "Metric";
    private static final String CAR_MASS_TT ="Base mass of car from factory";
    private static final String DELTA_MASS_TT ="Mass of all occupants and accessories added";
    private static final String HUMIDITY_TT ="Current relative Humidity";
    private static final String TIRE_WIDTH_TT ="Tire width in millimeters";
    private static final String TIRE_ASPECT_TT ="Tire aspect ratio in percentage";
    private static final String WHEEL_SIZE_TT ="Wheel (rim) size in inches";
    private static final String CAR_SELECT_TT ="Select car, default is first in list";
    private static final String GEAR_SELECT_TT ="Select gear, default is 2nd for 4AT, 3rd for 5MT and 4th for 6MT";
    private static final String RPM_MIN_TT ="RPM min is updated after WOT";
    private static final String RPM_MAX_TT ="RPM max is updated after WOT";
    private static final String ELEVATION_TT ="Elevation is calculated from ECU ATM sensor";
    private static final String AMB_TEMP_TT ="Ambient Temperature is updated from IAT sensor";
    private static final String ORDER_TT ="Lower number provides more smoothing";
    private final DataRegistrationBroker broker;
    private final DynoChartPanel chartPanel;
    private final Component parent;
    private List<ExternalData> externals = new ArrayList<ExternalData>();
    private List<EcuParameter> params = new ArrayList<EcuParameter>();
    private List<EcuSwitch> switches = new ArrayList<EcuSwitch>();
    private String[] gearList;
    private double tSize;
    private double rpm2mph;
    private double mass;
    private double altitude;
    private double humidity;
    private double pressure;
    private double airTemp;
    private double pSat;
    private double P_v;
    private double P_d;
    private double airDen;
    private double[] timeArray;
    private double[] speedArray;
    private double fToE = 0;
    private double sToE = 0;
    private double tToS = 0;
    private long fTime = 0;
    private long sTime = 0;
    private long eTime = 0;
    private long ttTime = 0;
    private long stTime = 0;
    private int order;
    private boolean getEnv = false;
    private boolean wotSet = false;
    private String carInfo;
    private String[] carTypeArr;
    private String[] carMassArr;
    private String[] dragCoeffArr;
    private String[] rollCoeffArr;
    private String[] frontalAreaArr;
    private String[] gearRatioArr;
    private String[][] gearsRatioArr;
    private String[] finalRatioArr;
    private String[] transArr;
    private String[] widthArr;
    private String[] aspectArr;
    private String[] sizeArr;
    private JTextField carMass = new JTextField("0", 4);
    private JTextField deltaMass = new JTextField("225", 4);
    private JTextField dragCoeff = new JTextField("0", 4);
    private JTextField rollCoeff = new JTextField("0", 4);
    private JTextField frontalArea = new JTextField("0", 4);
    private JTextField rpmMin = new JTextField("2000", 4);
    private JTextField rpmMax = new JTextField("6500", 4);
    private JTextField elevation = new JTextField("200", 4);
    private JTextField relHumid = new JTextField("60", 4);
    private JTextField ambTemp = new JTextField("68", 4);
    private JTextField gearRatio = new JTextField("0", 4);
    private JTextField finalRatio = new JTextField("0", 4);
    private JTextField transmission = new JTextField("0", 4);
    private JTextField tireWidth = new JTextField("0", 4);
    private JTextField tireAspect = new JTextField("0", 4);
    private JTextField tireSize = new JTextField("0", 4);
    private JLabel elevLabel = new JLabel("Elevation (ft)");
    private JLabel tempLabel = new JLabel("Air Temperature (\u00b0F)");
    private JLabel deltaMassLabel = new JLabel("Delta Weight (lbs)");
    private JLabel carMassLabel = new JLabel("Base Weight (lbs)");
//    private static final String SI = "SI";
    private String units = IMPERIAL;
    private String preUnits = IMPERIAL;
    private String elevUnits = "ft";
    private String tempUnits = "\u00b0F";
    private double atm = 0;
    private String pressUnits = "psi";
    private String pressText = "14.7";
    private String iatLogUnits = "F";
    private String atmLogUnits = "psi";
    private String vsLogUnits = "mph";
//    private String hpUnits = "hp(I)";
//    private String tqUnits = "lbf-ft";

    private final JComboBox orderComboBox = buildPolyOrderComboBox();
    private final JComboBox carSelectBox = buildCarSelectComboBox();
    private final JComboBox gearSelectBox = buildGearComboBox();
    private final JToggleButton recordDataButton = new JToggleButton("Clear & Record Data");
    private final JRadioButton iButton = new JRadioButton(IMPERIAL);
    
    public DynoControlPanel(Component parent, DataRegistrationBroker broker, ECUEditor ecuEditor, DynoChartPanel chartPanel) {
        checkNotNull(parent, broker, chartPanel);
        this.parent = parent;
        this.broker = broker;
        this.chartPanel = chartPanel;

        addControls();
    }

    private void calculateEnv() {
    	if (units.equals(IMPERIAL)) {
        	altitude = parseDouble(elevation) * 0.3048;	// feet to meters
        	airTemp = (parseDouble(ambTemp) + 459.67) * 5 / 9;	//[K] = ([F] + 459.67) * 5/9
        	mass = (parseDouble(carMass) + parseDouble(deltaMass)) * 0.4536;	//lbs to kg
        	pressure = atm * 6894.75728 ;	// [Pa] = [psi] * 6894.75728
    	}
    	if (units.equals(METRIC)) {
        	altitude = parseDouble(elevation);	// meters
        	airTemp = parseDouble(ambTemp) + 273.15;	//[K] = [C] + 273.15
        	mass = (parseDouble(carMass) + parseDouble(deltaMass));	//kg
        	pressure = atm * 1000 ;	// [Pa] = [kPa] * 1000
    	}
//    	if (units.equals(SI)) {
//        	altitude = parseDouble(elevation);	// meters
//        	airTemp = parseDouble(ambTemp);	//[K]
//        	mass = (parseDouble(carMass) + parseDouble(deltaMass));	//kg
//    	}
    	tSize = parseDouble(tireSize) + parseDouble(tireWidth)/25.4 * parseDouble(tireAspect)/100 * 2;
    	rpm2mph = parseDouble(gearRatio) * parseDouble(finalRatio) / (tSize * 0.002975);
    	humidity = parseDouble(relHumid) / 100;
//        carInfo = (String) carSelectBox.getSelectedItem() + "(" + gearSelectBox.getSelectedItem() + "), Pres: " + pressText + 
//		  pressUnits + ", Hum: " + relHumid.getText().trim() + "%, Temp: " + ambTemp.getText().trim() + tempUnits;
        // Use elevation if ATM was not read from ECU
    	if (atm == 0) {
    		pressure = 101325 * Math.pow((1 - 22.5577 * Math.pow(10,-6) * altitude), 5.25578); 	//Pressure at altitude [Pa]
    	}
    	pSat = 610.78 * Math.pow(10, ((7.5 * airTemp - 2048.625) / (airTemp - 35.85)));	//Saturation vapor pressure [Pa]
    	P_v = humidity * pSat;
    	P_d = pressure - P_v;
    	airDen = P_d / (287.05 * airTemp) + P_v / (461.495 * airTemp);	//air density with humidity included [kg/m^3]
		carInfo = (String) carSelectBox.getSelectedItem() + "(" + gearSelectBox.getSelectedItem() + "), Elev: " + elevation.getText().trim() + 
		elevUnits + ", Pres: " + pressText + pressUnits + ", Hum: " + relHumid.getText().trim() + "%, Temp: " + ambTemp.getText().trim() + tempUnits;
    }
    
    public double calcHp(double speed, double lastSpeed, long now, long lastUpdate) {
    	double timeDiff = now - lastUpdate; //system time in msec
    	double mph = calcMph(speed);
    	double lastMph = calcMph(lastSpeed);
    	// calculate slope and convert to G's (MPH/msec)
    	double accelG = ((timeDiff * (mph - lastMph)) / Math.pow(timeDiff, 2))* 45.5486542443;
    	// calculate Drive power = power required to propel vehicle mass at certain speed and acceleration.
    	// =Force*Velocity=Mass*Accel*Velocity
    	// Accel(m/s/s)*Mass(kg)*Velocity(m/s)
    	double dP = 9.8067 * accelG * mass * 0.44704 * mph;
    	// calculate Roll HP = Power required to counter rolling resistance
    	// Velocity(m/s)*Friction force
    	double rP = 0.44704 * mph * parseDouble(rollCoeff) * mass * 9.8067;
    	// calculate Wind HP = Power required to counter wind drag
    	// Aero drag
    	double aP = 0.5 * parseDouble(dragCoeff) * airDen * 0.0929 * parseDouble(frontalArea) * Math.pow(0.44704 * mph, 3);
    	double hp = dP + rP + aP;
    	if (units.equals(IMPERIAL)){
    		hp = hp  / 745.7;
    	}
    	if (units.equals(METRIC)){
    		hp = hp  / 1000;
    	}
//    	LOGGER.trace(mph + ":" + lastMph + ":" + accelG + ":" + dP + ":" + rP + ":" + aP + ":" + hp);
    	fTime = (mph <= 50) ? now : fTime;
    	sTime = (mph <= 60) ? now :	sTime;
    	eTime = (mph <= 80) ? now : eTime;
    	ttTime = (speed <= 3000) ? now : ttTime;
    	stTime = (speed <= 6000) ? now : stTime;
        fToE = (eTime - fTime) / 1000.0;		// 50-80mph, sec
        sToE = (eTime - sTime) / 1000.0;		// 60-80mph, sec
        tToS = (stTime - ttTime) / 1000.0;	// 3-6k rpm, sec
//        LOGGER.trace(fToE + " : " + sToE + " : " + tToS + " : " + mph + " : " + rpm  + " : " + now);
    	return hp;
    }

    public double calcTq(double rpm, double hp) {
        double tq = 0;
    	if (units.equals(IMPERIAL)){
    		tq = hp / rpm * 5252.113122;
    	}
    	if (units.equals(METRIC)){
    		tq = hp / rpm * 9549.296748;
    	}
//    	LOGGER.trace(hp + " : " + tq);
    	return tq;
    }
    private double calcMph(double rpm) {
		double mph = rpm / rpm2mph;
		return mph;
    }

    public void updateEnv(double iat, double at_press) {
    	getEnv = false;
        deregisterData(IAT, ATM);
    	if (units.equals(IMPERIAL)) {
        	if (iatLogUnits.equals("C")) iat = (iat * 9/5) + 32;
        	if (atmLogUnits.equals("bar")) at_press = at_press * 14.503773801;
        	if (at_press > 0) {
        		altitude = 145442 * (1 - Math.pow(( (at_press /(1.45 * Math.pow(10,-2))) / 1013.25), 0.190263)); // Altitude in ft from ATM in psi
        	}
    	}
    	if (units.equals(METRIC)) {
        	if (iatLogUnits.equals("F")) iat = (iat - 32) * 5/9;
        	if (atmLogUnits.equals("bar")) {
            	if (at_press > 0) {
            		altitude = 145442 * (1 - Math.pow(( ((at_press* 14.503773801) /(1.45 * Math.pow(10,-2))) / 1013.25), 0.190263)) * 0.3048; // Altitude in m from ATM in psi
            	}
        		at_press = at_press * 100;
        	}
        	if (atmLogUnits.equals("psi")) {
            	if (at_press > 0) {
            		altitude = 145442 * (1 - Math.pow(( (at_press /(1.45 * Math.pow(10,-2))) / 1013.25), 0.190263)) * 0.3048; // Altitude in m from ATM in psi
            	}
        		at_press = at_press * 6.89475728 ;
        	}
    	}
    	atm = at_press;
		ambTemp.setText(String.format("%1.1f",iat));
    	pressText = String.format("%1.3f",atm);
    	if (atm > 0) {
    		elevation.setText(String.format("%1.0f", altitude));
    	}
    	// disable user input if ECU parameters recorded
//    	ambTemp.setEnabled(false);
//    	elevation.setEnabled(false);
    	calculateEnv();
        updateChart();
    }

    public void updateChart() {
        order = (Integer) orderComboBox.getSelectedItem();
        timeArray = Arrays.copyOf(chartPanel.getTimeCoeff(1), chartPanel.getTimeCoeff(1).length);
        LOGGER.info("DYNO Time Coeffecients: " + Arrays.toString(timeArray));
        speedArray = Arrays.copyOf(chartPanel.getRpmCoeff(order), chartPanel.getRpmCoeff(order).length);
        LOGGER.info("DYNO RPM Coeffecients: " + Arrays.toString(speedArray));
    	int samples = chartPanel.getSampleCount();
    	LOGGER.info("DYNO Sample Count: " + samples);
        double lastRpm = 0;
        long lastUpdate = 0;
        for (int x = 0; x < samples; x++) {
    		double timeSample = 0;
        	double speedSample = 0;
            double nowHp = 0;
            double nowTq = 0;
        	for (int i = 0; i < order + 1; i++) {
//        		int pwr = order - i;
//        		timeSample = timeSample + (Math.pow(x,order-i) * timeArray[i]);
        		speedSample = speedSample + (Math.pow(x,order-i) * speedArray[i]);
//        		LOGGER.trace("sample: " + x + " i " + i + " pwr " + pwr + " coeff_i " + timeArray[i] + " time " + timeSample);
        	}
        	// this linearizes the time rather than polyfit
        	timeSample = (x * timeArray[0]) + timeArray[1];
//    		LOGGER.trace("sample: " + x + " time: " + timeSample);
        	if ( !isManual()) {	// if auto convert Speed sample to RPM
        		if (vsLogUnits.equals("mph")) speedSample = speedSample * rpm2mph;
        		if (vsLogUnits.equals("kph")) speedSample = speedSample * rpm2mph / 1.609344;
        	}
//    		LOGGER.trace("time: " + timeSample + " rpm: " + speedSample);
            if (x > 0) {
//        		LOGGER.trace(speedSample + ": " + lastRpm + ": " + timeSample + ": " + lastUpdate);
	            nowHp = calcHp(speedSample, lastRpm, (long) timeSample, lastUpdate);
	        	nowTq = calcTq(speedSample, nowHp);
	        	chartPanel.addData(speedSample, nowHp, nowTq);
            }
            lastUpdate = (long) timeSample;
            lastRpm = speedSample;
//            LOGGER.trace("time: " + timeSample + " rpm: " + speedSample + " hp: " + nowHp + " tq: " + nowTq);
        }
        chartPanel.interpolate(order, parseDouble(rpmMin), parseDouble(rpmMax), carInfo, fToE, sToE, tToS, units);
        parent.repaint();
    }

    public boolean isValidData(double rpm, double ta) {
    	if (wotSet && (ta < 99)) {
            recordDataButton.setSelected(false);
            rpmMax.setText(String.format("%1.0f", rpm));
            deregister();
    	}
    	else {
    		if (ta > 98) {
    			if (!wotSet) rpmMin.setText(String.format("%1.0f", rpm));
    			wotSet = true;
    			return true;
    		}
    	}
		wotSet = false;
		return false;
    }

    public boolean isManual() {
    	if (transmission.getText().trim().equals(MANUAL)) {
            return true;
    	}
    	else {
    		return false;
    	}
    }

    private void deregister() {
		if (isManual()) {
	        deregisterData(ENGINE_SPEED, THROTTLE_ANGLE);
		}
		else {
	        deregisterData(ENGINE_SPEED, VEHICLE_SPEED, THROTTLE_ANGLE);
		}
	    registerData(IAT, ATM);
	    getEnv = true;
    }

    public boolean getEnv() {
    	return getEnv;
    }

    private void addControls() {
        JPanel panel = new JPanel();

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        add(panel, gridBagLayout, buildFilterPanel(), 0, 0, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildRadioPanel(), 0, 1, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildInterpolatePanel(), 0, 2, 1, HORIZONTAL);
        add(panel);
    }

    private void add(JPanel panel, GridBagLayout gridBagLayout, JComponent component, int x, int y, int spanX, int fillType) {
        GridBagConstraints constraints = buildBaseConstraints();
        updateConstraints(constraints, x, y, spanX, 1, 1, 1, fillType);
        gridBagLayout.setConstraints(component, constraints);
        panel.add(component);
    }

    private JPanel buildRadioPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Measurement Units"));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        buildRadioButtons(panel);

        return panel;
    }

    private JPanel buildInterpolatePanel() {
    	JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Recalculate"));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        addLabeledComponent(panel, gridBagLayout, "Smoothing Factor", orderComboBox, 0);
        addComponent(panel, gridBagLayout, buildInterpolateButton(orderComboBox), 2);
        addMinMaxFilter(panel, gridBagLayout, "RPM Range", rpmMin, rpmMax, 4);
        add(panel, gridBagLayout, elevLabel, 0, 6, 3, HORIZONTAL);
        add(panel, gridBagLayout, elevation, 1, 7, 0, NONE);
        add(panel, gridBagLayout, tempLabel, 0, 8, 3, HORIZONTAL);
        add(panel, gridBagLayout, ambTemp, 1, 9, 0, NONE);
        addLabeledComponent(panel, gridBagLayout, "Rel Humidity (%)", relHumid, 10);

        return panel;
    }

    private void addLabeledComponent(JPanel panel, GridBagLayout gridBagLayout, String name, JComponent component, int y) {
        add(panel, gridBagLayout, new JLabel(name), 0, y, 3, HORIZONTAL);
        add(panel, gridBagLayout, component, 0, y + 1, 3, NONE);
    }

    private JPanel buildFilterPanel() {
        changeCars(0);
        setToolTips();
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Dyno Settings"));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);

        add(panel, gridBagLayout, new JLabel("Wheel (Width/Aspect-Diam.)"), 0, 15, 3, HORIZONTAL);
        add(panel, gridBagLayout, tireWidth, 0, 16, 1, NONE);
        add(panel, gridBagLayout, tireAspect, 1, 16, 1, NONE);
        add(panel, gridBagLayout, tireSize, 2, 16, 1, NONE);
        addLabeledComponent(panel, gridBagLayout, "Select Car", carSelectBox, 18);
        addLabeledComponent(panel, gridBagLayout, "Select Gear", gearSelectBox, 21);
        add(panel, gridBagLayout, deltaMassLabel, 0, 24, 3, HORIZONTAL);
        add(panel, gridBagLayout, deltaMass, 1, 25, 1, NONE);
        add(panel, gridBagLayout, carMassLabel, 0, 27, 3, HORIZONTAL);
        add(panel, gridBagLayout, carMass, 1, 28, 1, NONE);
        addComponent(panel, gridBagLayout, buildRecordDataButton(), 31);
//        addLabeledComponent(panel, gridBagLayout, "Drag Coeff", dragCoeff, 33);
//        addLabeledComponent(panel, gridBagLayout, "Frontal Area", frontalArea, 36);
//        addLabeledComponent(panel, gridBagLayout, "Rolling Resist Coeff", rollCoeff, 39);
        return panel;
    }

    private void setToolTips() {
    	relHumid.setToolTipText(HUMIDITY_TT);
        carMass.setToolTipText(CAR_MASS_TT);
        deltaMass.setToolTipText(DELTA_MASS_TT);
        tireWidth.setToolTipText(TIRE_WIDTH_TT);
        tireAspect.setToolTipText(TIRE_ASPECT_TT);
        tireSize.setToolTipText(WHEEL_SIZE_TT);
        carSelectBox.setToolTipText(CAR_SELECT_TT);
        gearSelectBox.setToolTipText(GEAR_SELECT_TT);
        rpmMin.setToolTipText(RPM_MIN_TT);
        rpmMax.setToolTipText(RPM_MAX_TT);
        elevation.setToolTipText(ELEVATION_TT);
        ambTemp.setToolTipText(AMB_TEMP_TT);
        orderComboBox.setToolTipText(ORDER_TT);
    }

    private JToggleButton buildRecordDataButton() {
    	if (!carTypeArr[0].trim().equals(MISSING_CAR_DEF)) {
	        recordDataButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent actionEvent) {
	                if (recordDataButton.isSelected()) {
	                    chartPanel.clear();
	                    parent.repaint();
	                	calculateEnv();
	                	if (isManual()) {
	                        registerData(ENGINE_SPEED, THROTTLE_ANGLE);
	                	}
	                	else {
	                        registerData(ENGINE_SPEED, VEHICLE_SPEED, THROTTLE_ANGLE);
	                	}
	                    chartPanel.startPrompt();
	                } else {
	                	deregister();
	                	chartPanel.clearPrompt();
	                }
	            }
	            
	        });
    	}
    	else {
    		recordDataButton.setText(MISSING_CAR_DEF);
    	}
        return recordDataButton;
    }

    public boolean isRecordData() {
        return recordDataButton.isSelected();
    }

    private void buildRadioButtons(JPanel panel) {
	    	//Create the radio buttons.
//		    JRadioButton iButton = new JRadioButton(IMPERIAL);
		    iButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent actionEvent) {
	            	buttonAction(iButton);
	            }
	        });
		    iButton.setActionCommand(IMPERIAL);
		    iButton.setSelected(true);
		
		    final JRadioButton mButton = new JRadioButton(METRIC);
		    mButton.addActionListener(new ActionListener() {
	            public void actionPerformed(ActionEvent actionEvent) {
	            	buttonAction(mButton);
	            }
	        });
		    mButton.setActionCommand(METRIC);
		
//		    final JRadioButton sButton = new JRadioButton(SI);
//		    sButton.addActionListener(new ActionListener() {
//	            public void actionPerformed(ActionEvent actionEvent) {
//	            	buttonAction(sButton);
//	            }
//	        });
//		    sButton.setActionCommand(SI);
		
		    //Group the radio buttons.
		    ButtonGroup group = new ButtonGroup();
		    group.add(iButton);
		    group.add(mButton);
//		    group.add(sButton);
		
		    panel.add(iButton);
		    panel.add(mButton);
//		    panel.add(sButton);

	    }

	private void buttonAction(JRadioButton button) {
    	double result = 0;
	    units = button.getActionCommand();
	    if (units.equals(IMPERIAL)) {
			if (preUnits.equals(METRIC)){
	    		result = parseDouble(ambTemp)* 9/5 + 32;
	    		ambTemp.setText(String.format("%1.0f", result));
	    		result = parseDouble(carMass) / 0.4536;
	    		carMass.setText(String.format("%1.0f", result));
	    		result = parseDouble(deltaMass) / 0.4536;
	    		deltaMass.setText(String.format("%1.0f", result));
	    		result = parseDouble(elevation) / 0.3048;
	    		elevation.setText(String.format("%1.0f", result));
	        	atm = atm / 6.89475728 ;
	    	}
//			if (preUnits.equals(SI)){
//	    		result = parseDouble(ambTemp)* 9/5 - 459.67;
//	    		ambTemp.setText(String.format("%1.0f", result));
//	    		result = parseDouble(carMass) / 0.4536;
//	    		carMass.setText(String.format("%1.0f", result));
//	    		result = parseDouble(deltaMass) / 0.4536;
//	    		deltaMass.setText(String.format("%1.0f", result));
//	    		result = parseDouble(elevation) / 0.3048;
//	    		elevation.setText(String.format("%1.0f", result));
//	    	}
    		preUnits = IMPERIAL;
    	    elevUnits = "ft";
    	    tempUnits = "\u00b0F";
    		elevLabel.setText("Elevation (ft)");
	    	tempLabel.setText("Air Temperature (\u00b0F)");
	    	deltaMassLabel.setText("Delta Weight (lbs)");
	    	carMassLabel.setText("Base Weight (lbs)");
        	pressText = String.format("%1.2f",atm);
        	pressUnits = "psi";
		}
	    if (units.equals(METRIC)) {
			if (preUnits.equals(IMPERIAL)){
	    		result = (parseDouble(ambTemp) - 32) * 5/9;
	    		ambTemp.setText(String.format("%1.1f", result));
	    		result = parseDouble(carMass) * 0.4536;
	    		carMass.setText(String.format("%1.0f", result));
	    		result = parseDouble(deltaMass) * 0.4536;
	    		deltaMass.setText(String.format("%1.0f", result));
	    		result = parseDouble(elevation) * 0.3048;
	    		elevation.setText(String.format("%1.0f", result));
	        	atm = atm * 6.89475728 ;
	    	}
//			if (preUnits.equals(SI)){
//	    		result = parseDouble(ambTemp) - 273.15;
//	    		ambTemp.setText(String.format("%1.1f", result));
//	    	}
    		preUnits = METRIC;
    	    elevUnits = "m";
    	    tempUnits = "\u00b0C";
    		elevLabel.setText("Elevation (m)");
	    	tempLabel.setText("Air Temperature (\u00b0C)");
	    	deltaMassLabel.setText("Delta Weight (kg)");
	    	carMassLabel.setText("Base Weight (kg)");
        	pressText = String.format("%1.2f",atm);
        	pressUnits = "kPa";
	    }
//	    if (units.equals(SI)) {
//			if (preUnits.equals(IMPERIAL)){
//	    		result = (parseDouble(ambTemp) + 459.67) * 5/9;
//	    		ambTemp.setText(String.format("%1.1f", result));
//	    		result = parseDouble(carMass) * 0.4536;
//	    		carMass.setText(String.format("%1.0f", result));
//	    	    LOGGER.trace("units selcted: " + units + " result: " + result);
//	    		result = parseDouble(deltaMass) * 0.4536;
//	    		deltaMass.setText(String.format("%1.0f", result));
//	    		result = parseDouble(elevation) * 0.3048;
//	    		elevation.setText(String.format("%1.0f", result));
//	    	}
//			if (preUnits.equals(METRIC)){
//	    		result = parseDouble(ambTemp) + 273.15;
//	    		ambTemp.setText(String.format("%1.1f", result));
//	    	}
//    		preUnits = SI;
//    	    elevUnits = "m";
//    	    tempUnits = "K";
//    		elevLabel.setText("Elevation (m)");
//	    	tempLabel.setText("Air Temperature (K)");
//	    	deltaMassLabel.setText("Delta Weight (kg)");
//	    	carMassLabel.setText("Base Weight (kg)");
//	    }
	    LOGGER.info("DYNO: Measurement units selected: " + units);
	}

	private void registerData(String... ids) {
        for (String id : ids) {
            LoggerData data = findData(id);
            EcuDataConvertor convertor = data.getSelectedConvertor();
            String convertorUnits = convertor.getUnits();
            if (id.equals(ATM))	atmLogUnits = convertorUnits;
            if (id.equals(IAT)) iatLogUnits = convertorUnits;
            if (id.equals(VEHICLE_SPEED)) vsLogUnits = convertorUnits;
            if (data != null) broker.registerLoggerDataForLogging(data);
        }
    }

    private void deregisterData(String... ids) {
        for (String id : ids) {
            LoggerData data = findData(id);
            if (data != null) broker.deregisterLoggerDataFromLogging(data);
        }
    }

    private LoggerData findData(String id) {
        for (EcuParameter param : params) {
            if (id.equals(param.getId())) return param;
        }
        for (EcuSwitch sw : switches) {
            if (id.equals(sw.getId())) return sw;
        }
        for (ExternalData external : externals) {
            if (id.equals(external.getId())) return external;
        }
        return null;
    }

    private void addComponent(JPanel panel, GridBagLayout gridBagLayout, JComponent component, int y) {
        add(panel, gridBagLayout, component, 0, y, 3, HORIZONTAL);
    }

    private void addMinMaxFilter(JPanel panel, GridBagLayout gridBagLayout, String name, JTextField min, JTextField max, int y) {
        add(panel, gridBagLayout, new JLabel(name), 0, y, 3, HORIZONTAL);
        y += 1;
        add(panel, gridBagLayout, min, 0, y, 2, NONE);
        add(panel, gridBagLayout, new JLabel(" - "), 1, y, 0, NONE);
        add(panel, gridBagLayout, max, 2, y, 1, NONE);
    }

    private GridBagConstraints buildBaseConstraints() {
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.anchor = CENTER;
        constraints.fill = NONE;
        return constraints;
    }

    private void updateConstraints(GridBagConstraints constraints, int gridx, int gridy, int gridwidth, int gridheight, int weightx, int weighty, int fill) {
        constraints.gridx = gridx;
        constraints.gridy = gridy;
        constraints.gridwidth = gridwidth;
        constraints.gridheight = gridheight;
        constraints.weightx = weightx;
        constraints.weighty = weighty;
        constraints.fill = fill;
    }

    private JButton buildInterpolateButton( final JComboBox orderComboBox) {
        JButton interpolateButton = new JButton("Recalculate");
        interpolateButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	if (isValidRange(rpmMin, rpmMax)) {
	            	calculateEnv();
	            	updateChart();
            	}
            	else {
            		showMessageDialog(parent, "Invalid PRM range specified.", "Error", ERROR_MESSAGE);
            	}
            }
        });
        return interpolateButton;
    }

    private JComboBox buildPolyOrderComboBox() {
        final JComboBox orderComboBox = new JComboBox(new Object[]{5, 6, 7, 8, 9, 10, 11, 12, 13, 14 ,15 ,16, 17, 18, 19 ,20});
        orderComboBox.setSelectedItem(9);
        return orderComboBox;
    }

    private boolean areNumbers(JTextField... textFields) {
        for (JTextField field : textFields) {
            if (!isNumber(field)) return false;
        }
        return true;
    }

    private boolean isValidRange(JTextField min, JTextField max) {
        return areNumbers(min, max) && parseDouble(min) < parseDouble(max);
    }

    private boolean isNumber(JTextField textField) {
        try {
            parseDouble(textField);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private double parseDouble(JTextField field) {
        return Double.parseDouble(field.getText().trim());
    }

    public void setEcuParams(List<EcuParameter> params) {
        this.params = new ArrayList<EcuParameter>(params);
    }

    public void setEcuSwitches(List<EcuSwitch> switches) {
        this.switches = new ArrayList<EcuSwitch>(switches);
    }

    public void setExternalDatas(List<ExternalData> externals) {
        this.externals = new ArrayList<ExternalData>(externals);
    }

    private JComboBox buildCarSelectComboBox() {
        loadCars();
        final JComboBox selectComboBox = new JComboBox(carTypeArr);
        selectComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                changeCars(selectComboBox.getSelectedIndex());
            }
        });
//        carSelectBox.setSelectedItem("05 USDM OBXT WGN LTD 5MT");
        return selectComboBox;
    }

    private JComboBox buildGearComboBox() {
//    	makeGearList();
    	final JComboBox gearSelectBox = new JComboBox();
        gearSelectBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
            	gearRatio.setText(gearsRatioArr[carSelectBox.getSelectedIndex()][gearSelectBox.getSelectedIndex() + 1]);
            	LOGGER.info("DYNO: Car: " + carSelectBox.getSelectedItem() +  ", Changed gear to: " + gearSelectBox.getSelectedItem() + " (" + gearRatio.getText() + ")");
            }	
        });
        return gearSelectBox;
    }

    private void makeGearList() {
    	gearList = new String[Integer.valueOf(gearsRatioArr[carSelectBox.getSelectedIndex()][0])];
    	for (int g = 1; g <= Integer.valueOf(gearsRatioArr[carSelectBox.getSelectedIndex()][0]); g++){
    		gearList[g-1] = Integer.toString(g);
    	}
    }

    private void changeCars(int index) {
    	if (!carTypeArr[0].trim().equals(MISSING_CAR_DEF)) {
		    iButton.doClick();
	        carMass.setText((String) carMassArr[index]);
	        dragCoeff.setText((String)dragCoeffArr[index]);
	        rollCoeff.setText((String) rollCoeffArr[index]);
	        frontalArea.setText((String) frontalAreaArr[index]);
	        if (gearsRatioArr[index][0] == null) {
	        	gearRatio.setText((String) gearRatioArr[index]);
	        }
	        else {
	//        	gearRatio.setText(gearsRatioArr[index][gearSelectBox.getSelectedIndex() + 1]);
	        	gearRatio.setText(gearsRatioArr[index][1]);
	        }
	        finalRatio.setText((String) finalRatioArr[index]);
	        transmission.setText((String) transArr[index]);
	        tireWidth.setText((String) widthArr[index]);
	        tireAspect.setText((String) aspectArr[index]);
	        tireSize.setText((String) sizeArr[index]);
	    	makeGearList();
	       	LOGGER.info("DYNO: New Car Selected: " + carSelectBox.getSelectedItem()  + ", Gears: " + gearsRatioArr[carSelectBox.getSelectedIndex()][0] );	
			if (gearList == null){
				gearSelectBox.setModel( new DefaultComboBoxModel() );
			}
			else {
				int gear = Integer.parseInt((gearsRatioArr[carSelectBox.getSelectedIndex()][0])) - 3;
				gearSelectBox.setModel( new DefaultComboBoxModel(gearList));
		        gearSelectBox.setSelectedIndex(gear);
			}
    	}
   }

    private void loadCars(){
		try {
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
			Document carsDef = docBuilder.parse (new File(CARS_FILE));

			// normalize text representation
			carsDef.getDocumentElement().normalize();
			
			NodeList listOfCars = carsDef.getElementsByTagName("car");
			int totalCars = listOfCars.getLength();
		    carTypeArr = new String[totalCars];
		    carMassArr = new String[totalCars];
		    dragCoeffArr = new String[totalCars];
		    rollCoeffArr = new String[totalCars];
		    frontalAreaArr = new String[totalCars];
		    gearRatioArr = new String[totalCars];
		    gearsRatioArr = new String[totalCars][7];
		    finalRatioArr = new String[totalCars];
		    transArr = new String[totalCars];
		    widthArr = new String[totalCars];
		    aspectArr = new String[totalCars];
		    sizeArr = new String[totalCars];
			String[] tag = {"type", "carmass", "dragcoeff", "rollcoeff", "frontalarea",
					"finalratio", "transmission", "tirewidth", "tireaspect", "wheelsize"};
		    
			for(int s = 0; s < listOfCars.getLength() ; s++){
			
				Node carNode = listOfCars.item(s);
				if(carNode.getNodeType() == Node.ELEMENT_NODE){
					Element carElement = (Element)carNode;
					
					// Read element types and populate arrays
					for (int i = 0; i < tag.length; i++) {
						NodeList list = carElement.getElementsByTagName(tag[i]);
						Element element = (Element)list.item(0);
						if (element != null) {
							NodeList value = element.getChildNodes();
							String data = ((Node)value.item(0)).getNodeValue().trim();
					        switch (i) {
						        case 0:
						        	carTypeArr[s] = data;
//						        	gearRatioArr[s] = data;
									for (int g = 1; g <= 6; g++) {
										String gearNo = "gearratio" + g;
										NodeList grsList = carElement.getElementsByTagName(gearNo);
										Element carGrsElement = (Element)grsList.item(0);
										if (carGrsElement != null) {
											NodeList grsValueList = carGrsElement.getChildNodes();
											if (((Node)grsValueList.item(0)).getNodeValue().trim() != null) {
												gearsRatioArr[s][0] = Integer.toString(g);
												gearsRatioArr[s][g] = (String) ((Node)grsValueList.item(0)).getNodeValue().trim();
											}
										}
//										LOGGER.trace("Car: " + s + " Gear: " + g + " Ratio: " + gearsRatioArr[s][g]);
									}
						        	break;
						        case 1:
									carMassArr[s] = data;
						        	break;
						        case 2:
						        	dragCoeffArr[s] = data;
						        	break;
						        case 3:
						        	rollCoeffArr[s] = data;
						        	break;
						        case 4:
						        	frontalAreaArr[s] = data;
						        	break;
						        case 5:
						        	finalRatioArr[s] = data;
						        	break;
						        case 6:
						        	transArr[s] = data;
						        	break;
						        case 7:
						        	widthArr[s] = data;
						        	break;
						        case 8:
						        	aspectArr[s] = data;
						        	break;
						        case 9:
						        	sizeArr[s] = data;
						        	break;
				        	}
						}
					}
				}
			}
		}
		catch (SAXParseException err) {
			showMessageDialog(parent, "Parsing error" + ", line " + err.getLineNumber () + ", " + err.getSystemId () + ".\n" + err.getMessage (), "Error", ERROR_MESSAGE);
			LOGGER.error("DYNO: ** Parsing error" + ", line " + err.getLineNumber () + ", uri " + err.getSystemId ());
			LOGGER.error(" " + err.getMessage ());
		}
		catch (SAXException e) {
			Exception x = e.getException ();
			((x == null) ? e : x).printStackTrace ();
		}
		catch (Throwable t) {	// file not found
		        Object[] options = {"Yes", "No"};
		        int answer = showOptionDialog(this,
		                "Cars definition file not found.\nGo online to download the latest definition file?",
		                "Configuration", DEFAULT_OPTION, WARNING_MESSAGE, null, options, options[0]);
		        if (answer == 0) {
		            openURL(CARS_DEFS_URL);
		        } else {
		            showMessageDialog(parent, MISSING_CAR_DEF + 
		            		" file from installation directory.", "Error", ERROR_MESSAGE);
		        }
			carTypeArr = new String[] {MISSING_CAR_DEF};
			t.printStackTrace ();
		}
	}
}
