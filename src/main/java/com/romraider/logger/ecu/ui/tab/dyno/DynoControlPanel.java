/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2022 RomRaider.com
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

import static com.romraider.Settings.COMMA;
import static com.romraider.Settings.SEMICOLON;
import static com.romraider.Version.CARS_DEFS_URL;
import static com.romraider.logger.car.util.SpeedCalculator.calculateMph;
import static com.romraider.logger.car.util.SpeedCalculator.calculateRpm;
import static com.romraider.logger.car.util.TorqueCalculator.calculateTorque;
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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.romraider.Settings;
import com.romraider.editor.ecu.ECUEditor;
import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.EcuParameter;
import com.romraider.logger.ecu.definition.EcuSwitch;
import com.romraider.logger.ecu.definition.ExternalData;
import com.romraider.logger.ecu.definition.LoggerData;
import com.romraider.logger.ecu.ui.DataRegistrationBroker;
import com.romraider.net.BrowserControl;
import com.romraider.swing.util.NumberVerifier;
import com.romraider.swing.util.SelectionVerifier;
import com.romraider.util.NumberUtil;
import com.romraider.util.ResourceUtil;
import com.romraider.util.SettingsManager;

public final class DynoControlPanel extends JPanel {
    private static final long serialVersionUID = 3787020251963102201L;
    private static final Logger LOGGER = Logger.getLogger(DynoControlPanel.class);
    private static final ResourceBundle rb = new ResourceUtil().getBundle(
            DynoControlPanel.class.getName());
    private static final String CARS_FILE = "cars_def.xml";
    private static final String MISSING_CAR_DEF = rb.getString("MISSING");
    private static final String ENGINE_SPEED = "P8";
    private static final String VEHICLE_SPEED = "P9";
    private static final String IAT = "P11";
    private static final String THROTTLE_ANGLE = "P13";
    private static final String THROTTLE_VOLTS = "P19";
    private static final String ATM = "P24";
    private static final String MANUAL = "manual";
    private static final String IMPERIAL = "Imperial";
    private static final String METRIC = "Metric";
    private static final String DYNO_MODE = "Dyno";
    private static final String ET_MODE = "ET";
    private static final String COLON = ":";
    private static final String TAB = "\u0009";
    private static final String RR_LOG_TIME = "Time";
    private static final String COBB_AP_TIME = "Seconds";
    private static final String COBB_ATR_TIME = "Time Stamp";
    private static final String AEM_LOG_TIME = "Time/s";
    private static final String OP2_LOG_TIME = "time";
    private static final String LOG_RPM = "RPM";
    private static final String LOG_ES = "Engine Speed";
    private static final String LOG_TA = "Throttle";
    private static final String LOG_VS = "Vehicle Speed";
    private static final String LOG_VS_I = "mph";
    private static final String LOG_VS_M = "km/h";
    private static final double KPH_2_MPH = 1.609344;
    private final DataRegistrationBroker broker;
    private final DynoChartPanel chartPanel;
    private final Component parent;
    private static FocusAdapter allTextSelector;
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
    private double fToE;
    private double sToE;
    private double tToS;
    private double reFfToE;
    private double reFsToE;
    private double reFtToS;
    private double reFauc;
    private double auc;
    private double aucStart;
    private double tpsMin;
    private long fTime;
    private long sTime;
    private long eTime;
    private long ttTime;
    private long stTime;
    private boolean getEnv;
    private boolean wotSet;
    private String path;
    private String carInfo;
    private String[] carTypeArr = {MISSING_CAR_DEF};
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
    private final JTextField carMass = new JTextField("0", 4);
    private final JTextField deltaMass = new JTextField("225", 4);
    private final JTextField dragCoeff = new JTextField("0", 4);
    private final JTextField rollCoeff = new JTextField("0", 4);
    private final JTextField frontalArea = new JTextField("0", 4);
    private final JTextField rpmMin = new JTextField("2000", 4);
    private final JTextField rpmMax = new JTextField("6500", 4);
    private final JTextField elevation = new JTextField("200", 4);
    private final JTextField relHumid = new JTextField("60", 4);
    private final JTextField ambTemp = new JTextField("68", 4);
    private final JTextField gearRatio = new JTextField("0", 4);
    private final JTextField finalRatio = new JTextField("0", 4);
    private final JTextField transmission = new JTextField("0", 4);
    private final JTextField tireWidth = new JTextField("0", 4);
    private final JTextField tireAspect = new JTextField("0", 4);
    private final JTextField tireSize = new JTextField("0", 4);
    private final JLabel elevLabel = new JLabel(rb.getString("ELEVATION_I"));
    private final JLabel tempLabel = new JLabel(rb.getString("AIRTEMP_I"));
    private final JLabel deltaMassLabel = new JLabel(rb.getString("DELTAWEIGHT_I"));
    private final JLabel carMassLabel = new JLabel(rb.getString("BASEWEIGHT_I"));
    //    private static final String SI = "SI";
    private String units = IMPERIAL;
    private String preUnits = IMPERIAL;
    private String elevUnits = "ft";
    private String tempUnits = "\u00b0F";
    private double atm;
    private String pressUnits = "psi";
    private String pressText = String.format("%1.2f", 14.7);
    private String iatLogUnits = "F";
    private String atmLogUnits = "psi";
    private String vsLogUnits = LOG_VS_I;
    private String throttle_param = THROTTLE_ANGLE;
    private final double[] results = new double[5];
    private final String[] resultStrings = new String[6];
    //    private String hpUnits = "hp(I)";
    //    private String tqUnits = "lbf-ft";
    private double distance;
    private long lastET;
    private final double[] etResults = new double[12];

    private final JPanel filterPanel = new JPanel();
    private final JPanel unitsPanel = new JPanel();
    private final JPanel iPanel = new JPanel();
    private final JPanel refPanel = new JPanel();
    private final JPanel etPanel = new JPanel();
    private final JComboBox orderComboBox = buildPolyOrderComboBox();
    private final JComboBox carSelectBox = buildCarSelectComboBox();
    private final JComboBox gearSelectBox = buildGearComboBox();
    private final JButton interpolateButton = new JButton(rb.getString("RECALC"));
    private final JToggleButton recordDataButton = new JToggleButton(rb.getString("RECORD"));
    private final JToggleButton recordButton = buildRecordDataButton();
    private final JRadioButton dButton = new JRadioButton(DYNO_MODE);
    private final JRadioButton eButton = new JRadioButton(ET_MODE);
    private final JRadioButton iButton = new JRadioButton(rb.getString("IMPERIAL"));
    private final JRadioButton mButton = new JRadioButton(rb.getString("METRIC"));
    private final JCheckBox loadFileCB = new JCheckBox(rb.getString("LOADFILE"));
    private final JComboBox tpsComboBox = new JComboBox(new Object[]{"%", "VDC"});


    public DynoControlPanel(Component parent, DataRegistrationBroker broker, ECUEditor ecuEditor, DynoChartPanel chartPanel) {
        checkNotNull(parent, broker, chartPanel);
        this.parent = parent;
        this.broker = broker;
        this.chartPanel = chartPanel;
        addControls();
    }
    private void calculateEnv() {
        if (units.equals(IMPERIAL)) {
            altitude = parseDouble(elevation) * 0.3048;    // feet to meters
            airTemp = (parseDouble(ambTemp) + 459.67) * 5 / 9;    //[K] = ([F] + 459.67) * 5/9
            mass = (parseDouble(carMass) + parseDouble(deltaMass)) * 0.4536;    //lbs to kg
            pressure = atm * 6894.75728;    // [Pa] = [psi] * 6894.75728
        }
        if (units.equals(METRIC)) {
            altitude = parseDouble(elevation);    // meters
            airTemp = parseDouble(ambTemp) + 273.15;    //[K] = [C] + 273.15
            mass = (parseDouble(carMass) + parseDouble(deltaMass));    //kg
            pressure = atm * 1000;    // [Pa] = [kPa] * 1000
        }
        //        if (units.equals(SI)) {
        //            altitude = parseDouble(elevation);    // meters
        //            airTemp = parseDouble(ambTemp);    //[K]
        //            mass = (parseDouble(carMass) + parseDouble(deltaMass));    //kg
        //        }
        tSize = parseDouble(tireSize) + parseDouble(tireWidth) / 25.4 * parseDouble(tireAspect) / 100 * 2;
        rpm2mph = parseDouble(gearRatio) * parseDouble(finalRatio) / (tSize * 0.002975);
        humidity = parseDouble(relHumid) / 100;
        //        carInfo = (String) carSelectBox.getSelectedItem() + "(" + gearSelectBox.getSelectedItem() + "), Pres: " + pressText +
        //          pressUnits + ", Hum: " + relHumid.getText().trim() + "%, Temp: " + ambTemp.getText().trim() + tempUnits;
        // Use elevation if ATM was not read from ECU
        if (atm == 0) {
            pressure = 101325 * Math.pow((1 - 22.5577 * Math.pow(10, -6) * altitude), 5.25578);     //Pressure at altitude [Pa]
        }
        pSat = 610.78 * Math.pow(10, ((7.5 * airTemp - 2048.625) / (airTemp - 35.85)));    //Saturation vapor pressure [Pa]
        P_v = humidity * pSat;
        P_d = pressure - P_v;
        airDen = P_d / (287.05 * airTemp) + P_v / (461.495 * airTemp);    //air density with humidity included [kg/m^3]
        carInfo = carSelectBox.getSelectedItem() + "(" + gearSelectBox.getSelectedItem() + "); Elev: " + elevation.getText().trim() +
                elevUnits + "; Pres: " + pressText + pressUnits + "; Hum: " + relHumid.getText().trim() + "%; Temp: " + ambTemp.getText().trim() + tempUnits;
    }

    private double calcHp(double rpm, double accel, long now) {
        double mph = calculateMph(rpm, rpm2mph);
        double accelG = accel * 45.5486542443;
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
        if (units.equals(IMPERIAL)) {
            hp = hp / 745.7;
        }
        if (units.equals(METRIC)) {
            hp = hp / 1000;
        }
        // Calculate acceleration statistics
        fTime = (mph <= 50) ? now : fTime;
        sTime = (mph <= 60) ? now : sTime;
        eTime = (mph <= 80) ? now : eTime;
        ttTime = (rpm <= 3000) ? now : ttTime;
        stTime = (rpm <= 6000) ? now : stTime;
        fToE = (eTime - fTime) / 1000.0;        // 50-80mph, sec
        sToE = (eTime - sTime) / 1000.0;        // 60-80mph, sec
        tToS = (stTime - ttTime) / 1000.0;    // 3-6k rpm, sec
        return hp;
    }

    public double calcRpm(double vs) {
        return calculateRpm(vs, rpm2mph, vsLogUnits);
    }

    public void updateEnv(double iat, double at_press) {
        getEnv = false;
        deregisterData(IAT, ATM);
        if (units.equals(IMPERIAL)) {
            if (iatLogUnits.equals("C")) iat = (iat * 9 / 5) + 32;
            if (atmLogUnits.equals("bar")) at_press = at_press * 14.503773801;
            if (at_press > 0) {
                altitude = 145442 * (1 - Math.pow(((at_press / (1.45 * Math.pow(10, -2))) / 1013.25), 0.190263)); // Altitude in ft from ATM in psi
            }
        }
        if (units.equals(METRIC)) {
            if (iatLogUnits.equals("F")) iat = (iat - 32) * 5 / 9;
            if (atmLogUnits.equals("bar")) {
                if (at_press > 0) {
                    altitude = 145442 * (1 - Math.pow((((at_press * 14.503773801) / (1.45 * Math.pow(10, -2))) / 1013.25), 0.190263)) * 0.3048; // Altitude in m from ATM in psi
                }
                at_press = at_press * 100;
            }
            if (atmLogUnits.equals("psi")) {
                if (at_press > 0) {
                    altitude = 145442 * (1 - Math.pow(((at_press / (1.45 * Math.pow(10, -2))) / 1013.25), 0.190263)) * 0.3048; // Altitude in m from ATM in psi
                }
                at_press = at_press * 6.89475728;
            }
        }
        atm = at_press;
        ambTemp.setText(String.format("%1.1f", iat));
        pressText = String.format("%1.3f", atm);
        if (atm > 0) {
            elevation.setText(String.format("%1.0f", altitude));
        }
        // disable user input if ECU parameters recorded
        //        ambTemp.setEnabled(false);
        elevation.setEnabled(false);
        calculateEnv();
        updateChart();
    }

    private void updateChart() {
        chartPanel.quietUpdate(false);
        auc = 0;
        aucStart = 0;
        double maxHp = 0;
        double maxHpRpm = 0;
        double maxTq = 0;
        double maxTqRpm = 0;
        double nowHp = 0;
        double nowTq = 0;
        int order = (Integer) orderComboBox.getSelectedItem();
        double[] speedArray = Arrays.copyOf(chartPanel.getRpmCoeff(order), chartPanel.getRpmCoeff(order).length);
        LOGGER.info("DYNO Speed Coeffecients: " + Arrays.toString(speedArray));
        double[] accelArray = new double[(order)];
        for (int x = 0; x < order; x++) {
            accelArray[x] = (order - x) * speedArray[x];
        }
        LOGGER.info("DYNO Accel Coeffecients: " + Arrays.toString(accelArray));
        int samples = chartPanel.getSampleCount();
        LOGGER.info("DYNO Sample Count: " + samples);
        double timeMin = chartPanel.getTimeSample(0);
        double timeMax = chartPanel.getTimeSample(samples - 1);
        for (double x = timeMin; x <= timeMax; x = x + 10) {
            double speedSample = 0;
            double accelSample = 0;
            // Calculate smoothed SPEED from coefficients
            for (int i = 0; i <= order; i++) {
                int pwr = order - i;
                speedSample = speedSample + (Math.pow(x, pwr) * speedArray[i]);
            }
            // Calculate acceleration from first derivative of SPEED coefficients
            for (int i = 0; i < order; i++) {
                int pwr = order - i - 1;
                accelSample = accelSample + (Math.pow(x, pwr) * accelArray[i]);
            }
            if (isManual()) {
                accelSample = accelSample / rpm2mph;    // RPM acceleration from RPM
            } else {
                speedSample = calculateRpm(speedSample, rpm2mph, vsLogUnits);    // convert logged vs to RPM for AT
                if (vsLogUnits.equals(LOG_VS_M)) accelSample = accelSample / KPH_2_MPH;
            }
            if (checkInRange("RPM", rpmMin, rpmMax, speedSample)) {
                nowHp = calcHp(speedSample, accelSample, (long) x);
                nowTq = calculateTorque(speedSample, nowHp, units);
                chartPanel.addData(speedSample, nowHp, nowTq);
                if (nowHp > maxHp) {
                    maxHp = nowHp;
                    maxHpRpm = speedSample;
                }
                if (nowTq > maxTq) {
                    maxTq = nowTq;
                    maxTqRpm = speedSample;
                }
                if (speedSample >= 3000 && speedSample <= 6000) {
                    if (aucStart == 0) aucStart = (nowHp * speedSample) + (nowTq * speedSample);
                    auc = auc + Math.abs((nowHp * speedSample) + (nowTq * speedSample) - aucStart);
                }
            }
        }
        chartPanel.quietUpdate(true);
        auc = auc / 1e6 / tToS;
        results[0] = maxHp;
        results[1] = maxHpRpm;
        results[2] = maxTq;
        results[3] = maxTqRpm;
        String hpUnits = " hp(I)";
        String tqUnits = " lbf-ft";
        resultStrings[2] = "50-80 MPH: " + String.format("%1.2f", fToE) + " secs";
        resultStrings[3] = "60-80 MPH: " + String.format("%1.2f", sToE) + " secs";
        if (units.equals(METRIC)) {
            hpUnits = " kW";
            tqUnits = " N-m";
            resultStrings[2] = "80-130 km/h: " + String.format("%1.2f", fToE) + " secs";
            resultStrings[3] = "100-130 km/h: " + String.format("%1.2f", sToE) + " secs";
        }
        resultStrings[0] = carInfo;
        resultStrings[1] = "Max Pwr: " + String.format("%1.1f", maxHp) + hpUnits +
                " @ " + String.format("%1.0f", maxHpRpm) +
                " RPM / Max TQ: " + String.format("%1.1f", maxTq) + tqUnits +
                " @ " + String.format("%1.0f", maxTqRpm) + " RPM";
        resultStrings[4] = "3000-6000 RPM: " + String.format("%1.2f", tToS) + " secs";
        resultStrings[5] = "3000-6000 RPM: " + String.format("%1.2f", auc) + " AUC";
        if (reFfToE > 0) resultStrings[2] = resultStrings[2] + " (" + String.format("%1.2f", (fToE - reFfToE)) + ")";
        if (reFsToE > 0) resultStrings[3] = resultStrings[3] + " (" + String.format("%1.2f", (sToE - reFsToE)) + ")";
        if (reFtToS > 0) resultStrings[4] = resultStrings[4] + " (" + String.format("%1.2f", (tToS - reFtToS)) + ")";
        if (reFauc > 0) resultStrings[5] = resultStrings[5] + " (" + String.format("%1.2f", (auc - reFauc)) + ")";
        LOGGER.info("DYNO Results: " + carInfo);
        LOGGER.info("DYNO Results: " + resultStrings[1]);
        LOGGER.info("DYNO Results: " + resultStrings[2]);
        LOGGER.info("DYNO Results: " + resultStrings[3]);
        LOGGER.info("DYNO Results: " + resultStrings[4]);
        LOGGER.info("DYNO Results: " + resultStrings[5]);
        chartPanel.interpolate(results, resultStrings);
        parent.repaint();
    }

    private void updateET() {
        chartPanel.quietUpdate(false);
        int order = 5;
        double x1 = 0;
        distance = 0;
        lastET = 0;

        double[] speedArray = Arrays.copyOf(chartPanel.getRpmCoeff(order), chartPanel.getRpmCoeff(order).length);
        LOGGER.info("DYNO Speed Coeffecients: " + Arrays.toString(speedArray));
        int samples = chartPanel.getSampleCount();
        LOGGER.info("DYNO Sample Count: " + samples);
        double timeMin = chartPanel.getTimeSample(0);
        double timeMax = chartPanel.getTimeSample(samples - 1);
        for (double x = timeMin; x <= timeMax; x = x + 1) {
            double speedSample = 0;
            // Calculate smoothed SPEED from coefficients
            for (int i = 0; i <= order; i++) {
                int pwr = order - i;
                speedSample = speedSample + (Math.pow(x, pwr) * speedArray[i]);
            }
            chartPanel.addData((x / 1000), speedSample);
            if (vsLogUnits.equals(LOG_VS_M)) speedSample = (speedSample / KPH_2_MPH);
            distance = distance + (speedSample * 5280 / 3600 * (x - lastET) / 1000);
            lastET = (long) x;
            x1 = x / 1000;
            if (distance <= 60) etResults[0] = x1;
            if (distance <= 60) etResults[1] = speedSample;
            if (distance <= 330) etResults[2] = x1;
            if (distance <= 330) etResults[3] = speedSample;
            if (distance <= 660) etResults[4] = x1;
            if (distance <= 660) etResults[5] = speedSample;
            if (distance <= 1000) etResults[6] = x1;
            if (distance <= 1000) etResults[7] = speedSample;
            if (distance <= 1320) etResults[8] = x1;
            if (distance <= 1320) etResults[9] = speedSample;
            if (speedSample <= 60) etResults[10] = x1;
            if (speedSample <= 60) etResults[11] = speedSample;
        }
        if (vsLogUnits.equals(LOG_VS_M)) {
            etResults[1] = etResults[1] * KPH_2_MPH;
            etResults[3] = etResults[3] * KPH_2_MPH;
            etResults[5] = etResults[5] * KPH_2_MPH;
            etResults[7] = etResults[7] * KPH_2_MPH;
            etResults[9] = etResults[9] * KPH_2_MPH;
            etResults[11] = etResults[11] * KPH_2_MPH;
        }
        chartPanel.quietUpdate(true);
        LOGGER.info("ET Split 60: " + String.format("%1.3f", etResults[0]));
        LOGGER.info("ET Split 330: " + String.format("%1.3f", etResults[2]));
        LOGGER.info("ET Split 1/8: " + String.format("%1.3f", etResults[4]) + " @ " + String.format("%1.2f", etResults[5]));
        LOGGER.info("ET Split 1000: " + String.format("%1.3f", etResults[6]));
        LOGGER.info("ET Split 1/4: " + String.format("%1.3f", etResults[8]) + " @ " + String.format("%1.2f", etResults[9]));
        LOGGER.info("ET 0 to " + String.format("%1.0f", etResults[11]) + " " + vsLogUnits + ": " + String.format("%1.3f", etResults[10]));
        chartPanel.updateEtResults(carInfo, etResults, vsLogUnits);
        parent.repaint();
    }

    public boolean isValidET(long now, double vs) {
        try {
            //if (LOGGER.isTraceEnabled())
            //    LOGGER.trace("lastET: " + lastET + " now: " + now + " VS: " + vs);
            if (vs > 0) {
                if (vsLogUnits.equals(LOG_VS_M)) vs = (vs / KPH_2_MPH);
                distance = distance + (vs * 5280 / 3600 * (now - lastET) / 1000);
                LOGGER.info("ET Distance (ft): " + distance);
                if (distance > 1330) {
                    recordDataButton.setSelected(false);
                    deregisterData(VEHICLE_SPEED);
                    chartPanel.clearPrompt();
                    updateET();
                    return false;
                }
                return true;
            }
            return false;
        }
        finally {
            lastET = now;
        }
    }

    public boolean isValidData(double rpm, double ta) {
        if (wotSet && (ta < tpsMin)) {
            recordDataButton.setSelected(false);
            rpmMax.setText(String.format("%1.0f", rpm));
            deregister();
        } else {
            if (ta > tpsMin) {
                if (!wotSet) rpmMin.setText(String.format("%1.0f", rpm));
                wotSet = true;
                return true;
            }
        }
        wotSet = false;
        return false;
    }

    public boolean isManual() {
        return transmission.getText().trim().equals(MANUAL);
    }

    private void deregister() {
        if (isManual()) {
            deregisterData(ENGINE_SPEED, throttle_param);
        } else {
            deregisterData(VEHICLE_SPEED, throttle_param);
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

        add(panel, gridBagLayout, buildModePanel(), 0, 0, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildFilterPanel(), 0, 1, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildRadioPanel(), 0, 2, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildInterpolatePanel(), 0, 3, 1, HORIZONTAL);
        add(panel, gridBagLayout, buildReferencePanel(), 0, 4, 1, HORIZONTAL);
        //        add(panel, gridBagLayout, buildEtPanel(), 0, 5, 1, HORIZONTAL);
        add(panel);
    }

    private void add(JPanel panel, GridBagLayout gridBagLayout, JComponent component, int x, int y, int spanX, int fillType) {
        GridBagConstraints constraints = buildBaseConstraints();
        updateConstraints(constraints, x, y, spanX, 1, 1, 1, fillType);
        gridBagLayout.setConstraints(component, constraints);
        panel.add(component);
    }

    private JPanel buildRadioPanel() {
        //        JPanel panel = new JPanel();
        unitsPanel.setBorder(new TitledBorder(rb.getString("MEASUREMENT")));

        GridBagLayout gridBagLayout = new GridBagLayout();
        unitsPanel.setLayout(gridBagLayout);
        buildRadioButtons(unitsPanel);

        return unitsPanel;
    }

    private JPanel buildModePanel() {
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder(rb.getString("MODE")));

        GridBagLayout gridBagLayout = new GridBagLayout();
        panel.setLayout(gridBagLayout);
        buildModeButtons(panel);

        return panel;
    }

    private JPanel buildInterpolatePanel() {
        iPanel.setBorder(new TitledBorder(rb.getString("RECALC")));

        GridBagLayout gridBagLayout = new GridBagLayout();
        iPanel.setLayout(gridBagLayout);

        addLabeledComponent(iPanel, gridBagLayout, rb.getString("SMOOTHFAC"), orderComboBox, 0);
        addComponent(iPanel, gridBagLayout, buildInterpolateButton(orderComboBox), 2);
        addMinMaxFilter(iPanel, gridBagLayout, rb.getString("RPMRANGE"), rpmMin, rpmMax, 4);
        add(iPanel, gridBagLayout, elevLabel, 0, 6, 3, HORIZONTAL);
        add(iPanel, gridBagLayout, elevation, 1, 7, 0, NONE);
        add(iPanel, gridBagLayout, tempLabel, 0, 8, 3, HORIZONTAL);
        add(iPanel, gridBagLayout, ambTemp, 1, 9, 0, NONE);
        addLabeledComponent(iPanel, gridBagLayout, rb.getString("HUMIDITY"), relHumid, 10);
        setSelectAllFieldText(rpmMin);
        setSelectAllFieldText(rpmMax);
        setSelectAllFieldText(elevation);
        setSelectAllFieldText(ambTemp);
        setSelectAllFieldText(relHumid);
        return iPanel;
    }

    private JPanel buildReferencePanel() {
        refPanel.setBorder(new TitledBorder(rb.getString("REFTRACE")));

        GridBagLayout gridBagLayout = new GridBagLayout();
        refPanel.setLayout(gridBagLayout);
        add(refPanel, gridBagLayout, buildOpenReferenceButton(), 0, 0, 1, NONE);
        add(refPanel, gridBagLayout, buildSaveReferenceButton(), 1, 0, 1, NONE);
        add(refPanel, gridBagLayout, buildClearReferenceButton(), 2, 0, 1, NONE);

        return refPanel;
    }

    private JPanel buildEtPanel() {
        etPanel.setBorder(new TitledBorder(rb.getString("ELAPSEDTIME")));
        etPanel.setVisible(false);

        GridBagLayout gridBagLayout = new GridBagLayout();
        etPanel.setLayout(gridBagLayout);
        addLabeledComponent(etPanel, gridBagLayout, rb.getString("SELECTCAR"), carSelectBox, 0);
        addComponent(etPanel, gridBagLayout, recordButton, 2);
        add(etPanel, gridBagLayout, buildSaveReferenceButton(), 1, 3, 1, NONE);

        return etPanel;
    }

    private void addLabeledComponent(JPanel panel, GridBagLayout gridBagLayout, String name, JComponent component, int y) {
        add(panel, gridBagLayout, new JLabel(name), 0, y, 3, HORIZONTAL);
        add(panel, gridBagLayout, component, 0, y + 1, 3, NONE);
    }

    private JPanel buildFilterPanel() {
        changeCars(0);
        setToolTips();
        filterPanel.setBorder(new TitledBorder(rb.getString("DYNOSETTINGS")));

        GridBagLayout gridBagLayout = new GridBagLayout();
        filterPanel.setLayout(gridBagLayout);

        addLabeledComponent(filterPanel, gridBagLayout, rb.getString("MINTHROTTLE"), buildMinTpsField(), 14);
        add(filterPanel, gridBagLayout, new JLabel(rb.getString("WHEELDIM")), 0, 16, 3, HORIZONTAL);
        add(filterPanel, gridBagLayout, tireWidth, 0, 17, 1, NONE);
        add(filterPanel, gridBagLayout, tireAspect, 1, 17, 1, NONE);
        add(filterPanel, gridBagLayout, tireSize, 2, 17, 1, NONE);
        addLabeledComponent(filterPanel, gridBagLayout, rb.getString("SELECTCAR"), carSelectBox, 19);
        addLabeledComponent(filterPanel, gridBagLayout, rb.getString("SELECTGEAR"), gearSelectBox, 22);
        add(filterPanel, gridBagLayout, deltaMassLabel, 0, 25, 3, HORIZONTAL);
        add(filterPanel, gridBagLayout, deltaMass, 1, 26, 1, NONE);
        add(filterPanel, gridBagLayout, carMassLabel, 0, 28, 3, HORIZONTAL);
        add(filterPanel, gridBagLayout, carMass, 1, 29, 1, NONE);
        addComponent(filterPanel, gridBagLayout, recordButton, 32);
        addComponent(filterPanel, gridBagLayout, buildLoadFileCB(), 33);
        addComponent(filterPanel, gridBagLayout, buildResetButton(), 34);
        //        addLabeledComponent(panel, gridBagLayout, "Drag Coeff", dragCoeff, 33);
        //        addLabeledComponent(panel, gridBagLayout, "Frontal Area", frontalArea, 36);
        //        addLabeledComponent(panel, gridBagLayout, "Rolling Resist Coeff", rollCoeff, 39);
        setSelectAllFieldText(tireWidth);
        setSelectAllFieldText(tireAspect);
        setSelectAllFieldText(tireSize);
        setSelectAllFieldText(deltaMass);
        setSelectAllFieldText(carMass);
        gearSelectBox.setSelectedItem(getSettings().getSelectedGear());
        return filterPanel;
    }

    private void setToolTips() {
        relHumid.setToolTipText(applyProperty("HUMIDITY_TT"));
        carMass.setToolTipText(applyProperty("CAR_MASS_TT"));
        deltaMass.setToolTipText(applyProperty("DELTA_MASS_TT"));
        tireWidth.setToolTipText(applyProperty("TIRE_WIDTH_TT"));
        tireAspect.setToolTipText(applyProperty("TIRE_ASPECT_TT"));
        tireSize.setToolTipText(applyProperty("WHEEL_SIZE_TT"));
        carSelectBox.setToolTipText(applyProperty("CAR_SELECT_TT"));
        gearSelectBox.setToolTipText(applyProperty("GEAR_SELECT_TT"));
        rpmMin.setToolTipText(applyProperty("RPM_MIN_TT"));
        rpmMax.setToolTipText(applyProperty("RPM_MAX_TT"));
        elevation.setToolTipText(applyProperty("ELEVATION_TT"));
        ambTemp.setToolTipText(applyProperty("AMB_TEMP_TT"));
        orderComboBox.setToolTipText(applyProperty("ORDER_TT"));
        recordDataButton.setToolTipText(applyProperty("RECORD_TT"));
        dButton.setToolTipText(applyProperty("DYNO_TT"));
        eButton.setToolTipText(applyProperty("ET_TT"));
    }

    private JButton buildResetButton() {
        JButton resetButton = new JButton(rb.getString("CLEARDATA"));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                chartPanel.clear();
                parent.repaint();
            }
        });
        resetButton.setToolTipText(applyProperty("RESET_TT"));
        return resetButton;
    }

    private JToggleButton buildRecordDataButton() {
        if (!carTypeArr[0].trim().equals(MISSING_CAR_DEF)) {
            recordDataButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    elevation.setEnabled(true);
                    if (dButton.isSelected()) {
                        if (loadFileCB.isSelected()) {
                            loadFromFile();
                        } else {
                            if (recordDataButton.isSelected()) {
                                System.out.println(tpsMin);
                                tpsComboBox.setSelectedItem(tpsComboBox.getSelectedItem());
                                chartPanel.clearGraph();
                                parent.repaint();
                                calculateEnv();
                                if (isManual()) {
                                    registerData(ENGINE_SPEED, throttle_param);
                                } else {
                                    registerData(VEHICLE_SPEED, throttle_param);
                                }
                                chartPanel.startPrompt("wot");
                            } else {
                                recordDataButton.setSelected(false);
                                if (isManual()) {
                                    deregisterData(ENGINE_SPEED, throttle_param);
                                } else {
                                    deregisterData(VEHICLE_SPEED, throttle_param);
                                }
                                chartPanel.clearPrompt();
                            }
                        }
                    }
                    if (eButton.isSelected()) {
                        if (recordDataButton.isSelected()) {
                            chartPanel.clear();
                            parent.repaint();
                            calculateEnv();
                            registerData(VEHICLE_SPEED);
                            chartPanel.startPrompt(vsLogUnits);
                            distance = 0;
                            lastET = 0;
                        } else {
                            deregisterData(VEHICLE_SPEED);
                            recordDataButton.setSelected(false);
                            chartPanel.clearPrompt();
                        }
                    }
                }
            });
        } else {
            recordDataButton.setText(MISSING_CAR_DEF);
        }
        return recordDataButton;
    }

    private JCheckBox buildLoadFileCB() {
        loadFileCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (loadFileCB.isSelected()) {
                    recordDataButton.setText(rb.getString("READFF"));
                } else {
                    recordDataButton.setText(rb.getString("RECORD"));
                }
            }

        });
        return loadFileCB;
    }

    public boolean isRecordData() {
        return recordDataButton.isSelected();
    }

    public boolean isRecordET() {
        return recordDataButton.isSelected() && eButton.isSelected();
    }

    private void buildModeButtons(JPanel panel) {
        dButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loadFileCB.setEnabled(true);
                chartPanel.setDyno();
                if (loadFileCB.isSelected()) {
                    recordDataButton.setText(rb.getString("LOADFF"));
                } else {
                    recordDataButton.setText(rb.getString("RECORD"));
                }
                //                etPanel.setVisible(false);
                //                filterPanel.setVisible(true);
                unitsPanel.setVisible(true);
                iPanel.setVisible(true);
                //                refPanel.setVisible(true);
                parent.repaint();
            }
        });
        dButton.setSelected(true);

        eButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                loadFileCB.setEnabled(false);
                loadFileCB.setSelected(false);
                chartPanel.setET();
                recordDataButton.setText(rb.getString("RECORDET"));
                //                filterPanel.setVisible(false);
                unitsPanel.setVisible(false);
                iPanel.setVisible(false);
                //                refPanel.setVisible(false);
                //                etPanel.setVisible(true);
                parent.repaint();
            }
        });
        ButtonGroup group = new ButtonGroup();
        group.add(dButton);
        group.add(eButton);

        panel.add(dButton);
        panel.add(eButton);
    }

    private void buildRadioButtons(JPanel panel) {
        iButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                buttonAction(iButton);
            }
        });
        //            iButton.setActionCommand(IMPERIAL);
        iButton.setSelected(true);

        mButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                buttonAction(mButton);
            }
        });
        //            mButton.setActionCommand(METRIC);

        //            final JRadioButton sButton = new JRadioButton(SI);
        //            sButton.addActionListener(new ActionListener() {
        //                public void actionPerformed(ActionEvent actionEvent) {
        //                    buttonAction(sButton);
        //                }
        //            });
        //            sButton.setActionCommand(SI);

        //Group the radio buttons.
        ButtonGroup group = new ButtonGroup();
        group.add(iButton);
        group.add(mButton);
        //            group.add(sButton);

        panel.add(iButton);
        panel.add(mButton);
        //            panel.add(sButton);

    }

    private void buttonAction(JRadioButton button) {
        double result = 0;
        units = button.getActionCommand();
        if (units.equals(IMPERIAL)) {
            if (preUnits.equals(METRIC)) {
                result = parseDouble(ambTemp) * 9 / 5 + 32;
                ambTemp.setText(String.format("%1.0f", result));
                result = parseDouble(carMass) / 0.4536;
                carMass.setText(String.format("%1.0f", result));
                result = parseDouble(deltaMass) / 0.4536;
                deltaMass.setText(String.format("%1.0f", result));
                result = parseDouble(elevation) / 0.3048;
                elevation.setText(String.format("%1.0f", result));
                atm = atm / 6.89475728;
            }
            //            if (preUnits.equals(SI)){
            //                result = parseDouble(ambTemp)* 9/5 - 459.67;
            //                ambTemp.setText(String.format("%1.0f", result));
            //                result = parseDouble(carMass) / 0.4536;
            //                carMass.setText(String.format("%1.0f", result));
            //                result = parseDouble(deltaMass) / 0.4536;
            //                deltaMass.setText(String.format("%1.0f", result));
            //                result = parseDouble(elevation) / 0.3048;
            //                elevation.setText(String.format("%1.0f", result));
            //            }
            preUnits = IMPERIAL;
            elevUnits = "ft";
            tempUnits = "\u00b0F";
            elevLabel.setText(rb.getString("ELEVATION_I"));
            tempLabel.setText(rb.getString("AIRTEMP_I"));
            deltaMassLabel.setText(rb.getString("DELTAWEIGHT_I"));
            carMassLabel.setText(rb.getString("BASEWEIGHT_I"));
            pressText = String.format("%1.2f", atm);
            pressUnits = "psi";
        }
        if (units.equals(METRIC)) {
            if (preUnits.equals(IMPERIAL)) {
                result = (parseDouble(ambTemp) - 32) * 5 / 9;
                ambTemp.setText(String.format("%1.1f", result));
                result = parseDouble(carMass) * 0.4536;
                carMass.setText(String.format("%1.0f", result));
                result = parseDouble(deltaMass) * 0.4536;
                deltaMass.setText(String.format("%1.0f", result));
                result = parseDouble(elevation) * 0.3048;
                elevation.setText(String.format("%1.0f", result));
                atm = atm * 6.89475728;
            }
            //            if (preUnits.equals(SI)){
            //                result = parseDouble(ambTemp) - 273.15;
            //                ambTemp.setText(String.format("%1.1f", result));
            //            }
            preUnits = METRIC;
            elevUnits = "m";
            tempUnits = "\u00b0C";
            elevLabel.setText(rb.getString("ELEVATION_M"));
            tempLabel.setText(rb.getString("AIRTEMP_M"));
            deltaMassLabel.setText(rb.getString("DELTAWEIGHT_M"));
            carMassLabel.setText(rb.getString("BASEWEIGHT_M"));
            pressText = String.format("%1.2f", atm);
            pressUnits = "kPa";
        }
        //        if (units.equals(SI)) {
        //            if (preUnits.equals(IMPERIAL)){
        //                result = (parseDouble(ambTemp) + 459.67) * 5/9;
        //                ambTemp.setText(String.format("%1.1f", result));
        //                result = parseDouble(carMass) * 0.4536;
        //                carMass.setText(String.format("%1.0f", result));
        //                if (LOGGER.isTraceEnabled())
        //                    LOGGER.trace("units selcted: " + units + " result: " + result);
        //                result = parseDouble(deltaMass) * 0.4536;
        //                deltaMass.setText(String.format("%1.0f", result));
        //                result = parseDouble(elevation) * 0.3048;
        //                elevation.setText(String.format("%1.0f", result));
        //            }
        //            if (preUnits.equals(METRIC)){
        //                result = parseDouble(ambTemp) + 273.15;
        //                ambTemp.setText(String.format("%1.1f", result));
        //            }
        //            preUnits = SI;
        //            elevUnits = "m";
        //            tempUnits = "K";
        //            elevLabel.setText("Elevation (m)");
        //            tempLabel.setText("Air Temperature (K)");
        //            deltaMassLabel.setText("Delta Weight (kg)");
        //            carMassLabel.setText("Base Weight (kg)");
        //        }
        if (resultStrings[0] != null) interpolateButton.doClick();
        LOGGER.info("DYNO Measurement units selected: " + units);
    }

    private void loadFromFile() {
        final JFileChooser openFile = new JFileChooser();
        if (path != null) openFile.setCurrentDirectory(new File(path));
        final JButton openButton = new JButton("Open");

        int returnVal = openFile.showOpenDialog(openButton);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final File logFile = openFile.getSelectedFile();
            path = logFile.getParent();
            BufferedReader inputStream = null;
            recordDataButton.setSelected(false);
            chartPanel.clearGraph();
            parent.repaint();
            calculateEnv();

            try {
                inputStream = new BufferedReader(new FileReader(logFile));
                LOGGER.info("DYNO Opening log file: " + logFile.getName());
                boolean atrTime = false;
                double timeMult = 1;
                double startTime = -999999999;
                int timeCol = 0;
                int rpmCol = 0;
                int vsCol = 0;
                int taCol = 0;
                double minRpm = 3500;
                double maxRpm = 0;
                String delimiter = SEMICOLON;
                String line = inputStream.readLine();
                String[] headers;
                headers = line.split(SEMICOLON);
                if (headers.length < 3) {
                    headers = line.split(TAB);
                    if (headers.length > 2) {
                        delimiter = TAB;
                    }
                    else {
                        headers = line.split(COMMA);
                        if (headers.length > 2) delimiter = COMMA;
                    }
                }
                for (int x = 0; x < headers.length; x++) {
                    if (headers[x].contains(RR_LOG_TIME)) timeCol = x;
                    if (headers[x].contains(COBB_AP_TIME) ||
                            headers[x].contains(AEM_LOG_TIME) ||
                            headers[x].contains(OP2_LOG_TIME)) {
                        timeCol = x;
                        timeMult = 1000;
                    }
                    if (headers[x].contains(COBB_ATR_TIME)) {
                        timeCol = x;
                        timeMult = 1000;
                        atrTime = true;
                    }
                    if (headers[x].toUpperCase().contains(LOG_RPM) || headers[x].contains(LOG_ES)) rpmCol = x;
                    if (headers[x].contains(LOG_TA)) taCol = x;
                    if (headers[x].contains(LOG_VS)) vsCol = x;
                    if (headers[x].contains(LOG_VS_I)) vsLogUnits = LOG_VS_I;
                    if (headers[x].contains(LOG_VS_M)) vsLogUnits = LOG_VS_M;
                }
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace("DYNO log file conversions: Time Column: " + timeCol + "; Time X: " + timeMult +
                        "; RPM Column: " + rpmCol + "; TA Column: " + taCol + "; VS Column: " + vsCol +
                        "; VS units: " + vsLogUnits);
                while ((line = inputStream.readLine()) != null) {

                	//Convert everything to . notation
                    String[] values = line.split(delimiter);

                    for(int i=0; i < values.length;i++) {
                    	values[i] = values[i].replace(',', '.');
                    }

                    if (Double.parseDouble(values[taCol]) > tpsMin) {
                        double logTime = 0;
                        if (atrTime) {
                            String[] timeStamp = values[timeCol].split(COLON);
                            if (timeStamp.length == 3) {
                                logTime = (Double.parseDouble(timeStamp[0]) * 3600) +
                                        (Double.parseDouble(timeStamp[1]) * 60) +
                                        Double.parseDouble(timeStamp[2]) * timeMult;
                            } else {
                                logTime = (Double.parseDouble(timeStamp[0]) * 60) +
                                		Double.parseDouble(timeStamp[1]) * timeMult;
                            }
                        } else {
                            logTime = Double.parseDouble(values[timeCol]) * timeMult;
                        }
                        if (startTime == -999999999) startTime = logTime;
                        logTime = logTime - startTime;
                        double logRpm = 0;
                        if (isManual()) {
                            logRpm = Double.parseDouble(values[rpmCol]);
                            minRpm = Math.min(minRpm, logRpm);
                            maxRpm = Math.max(maxRpm, logRpm);
                        } else {
                            logRpm = Double.parseDouble(values[vsCol]);
                            minRpm = Math.min(minRpm, calculateRpm(logRpm, rpm2mph, vsLogUnits));
                            maxRpm = Math.max(maxRpm, calculateRpm(logRpm, rpm2mph, vsLogUnits));
                        }
                        chartPanel.addRawData(logTime, logRpm);
                        if (LOGGER.isTraceEnabled())
                            LOGGER.trace("DYNO log file time: " + logTime + "; speed: " + logRpm);
                    }
                }
                inputStream.close();
                rpmMin.setText(String.format("%1.0f", minRpm));
                rpmMax.setText(String.format("%1.0f", maxRpm));
                interpolateButton.doClick();
            }
            catch (IOException e) {
				e.printStackTrace();
            }
            finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        } else {
            LOGGER.info("DYNO Open log file command cancelled by user.");
        }
    }

    private JButton buildOpenReferenceButton() {
        final JButton openButton = new JButton(rb.getString("OPEN"));

        openButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final JFileChooser openFile = new JFileChooser();
                if (path != null) openFile.setCurrentDirectory(new File(path));
                int returnVal = openFile.showOpenDialog(openButton);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    final File traceFile = openFile.getSelectedFile();
                    path = traceFile.getParent();
                    BufferedReader inputStream = null;
                    chartPanel.clearRefTrace();

                    try {
                        inputStream = new BufferedReader(new FileReader(traceFile));
                        LOGGER.info("DYNO Opening trace file: " + traceFile.getName());

                        String line = inputStream.readLine();
                        String[] refStats = line.split(TAB);
                        reFfToE = Double.parseDouble(refStats[3]);
                        reFsToE = Double.parseDouble(refStats[4]);
                        reFtToS = Double.parseDouble(refStats[5]);
                        reFauc = Double.parseDouble(refStats[6]);
                        LOGGER.info("DYNO Reference Stats: " + Arrays.toString(refStats));
                        while ((line = inputStream.readLine()) != null) {
                            String[] values = line.split(COMMA);
                            if (Double.parseDouble(values[0]) >= 0) {
                                chartPanel.setRefTrace(Double.parseDouble(values[0]), Double.parseDouble(values[1]), Double.parseDouble(values[2]));
                            }
                        }
                        inputStream.close();
                        if (refStats[0].equalsIgnoreCase(IMPERIAL)) {
                            iButton.setSelected(true);
                            buttonAction(iButton);
                        }
                        if (refStats[0].equalsIgnoreCase(METRIC)) {
                            mButton.setSelected(true);
                            buttonAction(mButton);
                        }
                        chartPanel.updateRefTrace(refStats);
                    }
                    catch (IOException e) {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                } else {
                    LOGGER.info("DYNO Open trace file command cancelled by user.");
                }
            }
        });
        return openButton;
    }

    private JButton buildSaveReferenceButton() {
        final JButton saveButton = new JButton(rb.getString("SAVE"));

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                final JFileChooser openFile = new JFileChooser();
                if (path != null) openFile.setCurrentDirectory(new File(path));
                
                int returnVal = openFile.showSaveDialog(saveButton);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    final File traceFile = openFile.getSelectedFile();
                    path = traceFile.getParent();
                    BufferedWriter outputStream = null;

                    try {
                        if (dButton.isSelected()) {
                            outputStream = new BufferedWriter(new FileWriter(traceFile));
                            LOGGER.info("DYNO Saving trace to file: " + traceFile.getName());
                            String line = units + TAB + orderComboBox.getSelectedItem() +
                                    TAB + resultStrings[1] +
                                    TAB + fToE +
                                    TAB + sToE +
                                    TAB + tToS +
                                    TAB + auc;
                            outputStream.write(line, 0, line.length());
                            outputStream.newLine();

                            for (int x = 0; x < chartPanel.getPwrTqCount(); x++) {
                                line = chartPanel.getPwrTq(x);
                                outputStream.write(line, 0, line.length());
                                outputStream.newLine();
                            }
                        }
                        if (eButton.isSelected()) {
                            outputStream = new BufferedWriter(new FileWriter(traceFile));
                            LOGGER.info("DYNO Saving ET to file: " + traceFile.getName());
                            String line = carInfo;
                            outputStream.write(line, 0, line.length());
                            outputStream.newLine();
                            line = "60ft/18.3m:" + TAB + String.format("%1.3f", etResults[0]) + "\" @ " + String.format("%1.2f", etResults[1]) + " " + vsLogUnits;
                            outputStream.write(line, 0, line.length());
                            outputStream.newLine();
                            line = "330ft/100m:" + TAB + String.format("%1.3f", etResults[2]) + "\" @ " + String.format("%1.2f", etResults[3]) + " " + vsLogUnits;
                            outputStream.write(line, 0, line.length());
                            outputStream.newLine();
                            line = "1/2 track:" + TAB + String.format("%1.3f", etResults[4]) + "\" @ " + String.format("%1.2f", etResults[5]) + " " + vsLogUnits;
                            outputStream.write(line, 0, line.length());
                            outputStream.newLine();
                            line = "1,000ft/305m:" + TAB + String.format("%1.3f", etResults[6]) + "\" @ " + String.format("%1.2f", etResults[7]) + " " + vsLogUnits;
                            outputStream.write(line, 0, line.length());
                            outputStream.newLine();
                            line = "1/4 mile/402m:" + TAB + String.format("%1.3f", etResults[8]) + "\" @ " + String.format("%1.2f", etResults[9]) + " " + vsLogUnits;
                            outputStream.write(line, 0, line.length());
                            outputStream.newLine();
                        }
                        outputStream.close();
                    }
                    catch (IOException e) {
                        try {
                            outputStream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                } else {
                    LOGGER.info("DYNO Save trace file command cancelled by user.");
                }
            }
        });
        return saveButton;
    }

    private JButton buildClearReferenceButton() {
        final JButton clearButton = new JButton(rb.getString("CLEAR"));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                reFfToE = 0;
                reFsToE = 0;
                reFtToS = 0;
                reFauc = 0;
                chartPanel.clearRefTrace();
                if (results[0] > 0) interpolateButton.doClick();
            }
        });
        return clearButton;
    }

    private void registerData(String... ids) {
        for (String id : ids) {
            LoggerData data = findData(id);
            if (data == null) {
                final String msg = MessageFormat.format(
                        rb.getString("MISSINGID"), id);
                notifyError(msg);
            }
            EcuDataConvertor convertor = data.getSelectedConvertor();
            String convertorUnits = convertor.getUnits();
            if (id.equals(ATM)) atmLogUnits = convertorUnits;
            if (id.equals(IAT)) iatLogUnits = convertorUnits;
            if (id.equals(VEHICLE_SPEED)) vsLogUnits = convertorUnits;
            broker.registerLoggerDataForLogging(data);
        }
    }

    private void deregisterData(String... ids) {
        for (String id : ids) {
            LoggerData data = findData(id);
            if (data != null) broker.deregisterLoggerDataFromLogging(data);
        }
    }

    /**
     * Find the LoggerData item from the parameter ID string in the currently
     * loaded parameter list.
     * @param id - String of the parameter to find
     * @return LoggerData item or null if ID is not found
     */
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
        add(panel, gridBagLayout, component, 0, y, 3, NONE);
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

    private JButton buildInterpolateButton(final JComboBox orderComboBox) {
        interpolateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                if (dButton.isSelected()) {
                    interpolateButton.setEnabled(true);
                    if (isValidRange(rpmMin, rpmMax)) {
                        calculateEnv();
                        updateChart();
                    } else {
                        notifyError(rb.getString("INVALIDRPM"));
                    }
                }
            }
        });
        return interpolateButton;
    }

    private JComboBox buildPolyOrderComboBox() {
        final JComboBox orderComboBox = new JComboBox(new Object[]{5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
        orderComboBox.setSelectedItem(9);
        return orderComboBox;
    }

    private JPanel buildMinTpsField() {
        final JPanel tpsPanel = new JPanel();
        final JTextField minTpsField = new JFormattedTextField(new DecimalFormat("#.##"));
        minTpsField.setText("0");
        minTpsField.setToolTipText(applyProperty("TPS_SELECT_TT"));
        minTpsField.setColumns(6);
        setSelectAllFieldText(minTpsField);
        minTpsField.addFocusListener (new FocusListener () {
            @Override
            public void focusGained(FocusEvent e) {
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (!minTpsField.getText().isEmpty()) {
                    tpsMin = parseDouble(minTpsField);
                    LOGGER.info("DYNO TPS threshold set to: " + tpsMin);
                }
            }
        });
        minTpsField.setInputVerifier(new NumberVerifier("Threshold"));
        String thres = getSettings().getDynoThreshold();
        minTpsField.setText(thres);

        tpsMin = parseDouble(minTpsField);
        tpsPanel.add(minTpsField);

        tpsComboBox.setToolTipText(applyProperty("TPS_MODE_TT"));
        tpsComboBox.setSelectedItem(getSettings().getDynoThrottle());
        final SelectionVerifier verifier = new SelectionVerifier();
        tpsComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tpsComboBox.setPopupVisible(false);
                final String itemSel = (String) tpsComboBox.getSelectedItem();
                if ("%".equals(itemSel)) {
                    if (null == findData(THROTTLE_ANGLE)) {
                        disableRecordButton();
                        notifyError(rb.getString("TANOSUPPORT"));
                        verifier.set(false);
                    }
                    else {
                        throttle_param = THROTTLE_ANGLE;
                        enableRecordButton();
                        verifier.set(true);
                    }
                }
                else if ("VDC".equals(itemSel)) {
                    if (null == findData(THROTTLE_VOLTS)) {
                        disableRecordButton();
                        notifyError(rb.getString("TVNOSUPPORT"));
                        verifier.set(false);
                    }
                    else {
                        throttle_param = THROTTLE_VOLTS;
                        enableRecordButton();
                        verifier.set(true);
                    }
                }
                LOGGER.info("DYNO Throttle parameter set to: " + throttle_param);
            }
        });
        tpsComboBox.setInputVerifier(verifier);
        tpsPanel.add(tpsComboBox);
        return tpsPanel;
    }

    private boolean areNumbers(JTextField... textFields) {
        for (JTextField field : textFields) {
            if (!isNumber(field)) return false;
        }
        return true;
    }

    private boolean checkInRange(String name, JTextField min, JTextField max, double value) {
        if (isValidRange(min, max)) {
            return inRange(value, min, max);
        } else {
            return false;
        }
    }

    private boolean isValidRange(JTextField min, JTextField max) {
        return areNumbers(min, max) && parseDouble(min) < parseDouble(max);
    }

    private boolean inRange(double value, JTextField min, JTextField max) {
        return inRange(value, parseDouble(min), parseDouble(max));
    }

    private boolean inRange(double val, double min, double max) {
        return val >= min && val <= max;
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
    	String s = field.getText().trim().replace(',', '.');
    	return Double.parseDouble(s);
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
        selectComboBox.setSelectedItem(getSettings().getSelectedCar());
        selectComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                changeCars(selectComboBox.getSelectedIndex());
            }
        });
        return selectComboBox;
    }

    private JComboBox buildGearComboBox() {
        //        makeGearList();
        final JComboBox gearSelectBox = new JComboBox();
        gearSelectBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                gearRatio.setText(gearsRatioArr[carSelectBox.getSelectedIndex()][gearSelectBox.getSelectedIndex() + 1]);
                LOGGER.info("DYNO Car: " + carSelectBox.getSelectedItem() + ", Changed gear to: " + gearSelectBox.getSelectedItem() + " (" + gearRatio.getText() + ")");
            }
        });
        return gearSelectBox;
    }

    private void makeGearList() {
        gearList = new String[Integer.valueOf(gearsRatioArr[carSelectBox.getSelectedIndex()][0])];
        for (int g = 1; g <= Integer.valueOf(gearsRatioArr[carSelectBox.getSelectedIndex()][0]); g++) {
            gearList[g - 1] = Integer.toString(g);
        }
    }

    public void checkDynoDefs() {
    	if (carTypeArr[0].trim().equals(MISSING_CAR_DEF)){
	        Object[] options = {"Yes", "No"};
	        int answer = showOptionDialog(parent,
	                rb.getString("CDNOTFOUND"),
	                rb.getString("CONFIGURATION"),
	                DEFAULT_OPTION, WARNING_MESSAGE,
	                null, options, options[0]);
	        if (answer == 0) {
	            BrowserControl.displayURL(CARS_DEFS_URL);
	        } else {
	            final String msg = MessageFormat.format(
	                    rb.getString("MISSINGCD"), MISSING_CAR_DEF);
	            showMessageDialog(parent,
	            msg,
	            rb.getString("NOTICE"), WARNING_MESSAGE);
	        }
    	}
    }

    private void changeCars(int index) {
        if (!carTypeArr[0].trim().equals(MISSING_CAR_DEF)) {
            iButton.doClick();
            carMass.setText(carMassArr[index]);
            dragCoeff.setText(dragCoeffArr[index]);
            rollCoeff.setText(rollCoeffArr[index]);
            frontalArea.setText(frontalAreaArr[index]);
            if (gearsRatioArr[index][0] == null) {
                gearRatio.setText(gearRatioArr[index]);
            } else {
                //            gearRatio.setText(gearsRatioArr[index][gearSelectBox.getSelectedIndex() + 1]);
                gearRatio.setText(gearsRatioArr[index][1]);
            }
            finalRatio.setText(finalRatioArr[index]);
            transmission.setText(transArr[index]);
            tireWidth.setText(widthArr[index]);
            tireAspect.setText(aspectArr[index]);
            tireSize.setText(sizeArr[index]);
            makeGearList();
            LOGGER.info("DYNO New Car Selected: " + carSelectBox.getSelectedItem() + ", Gears: " + gearsRatioArr[carSelectBox.getSelectedIndex()][0]);
            if (gearList == null) {
                gearSelectBox.setModel(new DefaultComboBoxModel());
            } else {
                int gear = Integer.parseInt((gearsRatioArr[carSelectBox.getSelectedIndex()][0])) - 3;
                gearSelectBox.setModel(new DefaultComboBoxModel(gearList));
                gearSelectBox.setSelectedIndex(gear);
            }
        }
    }

    private void loadCars() {
        try {
            File carDef = null;

            //Look through some folders to find the car definition file
            //Do NOT delete the "", this also searches through the local folder!
            final String searchPaths[] = {getSettings().getLoggerDefinitionFilePath(), getSettings().getLoggerProfileFilePath(), ""};

            for (String s : searchPaths) {
            	File f = new File(s);
            	File path_test = new File(f.getParent(), CARS_FILE);

            	if(path_test.exists()) {
                    LOGGER.info("Loaded dyno definition file from " + path_test.getAbsolutePath());
            		carDef = path_test;
            		break;
            	}
            }

            if(carDef == null) throw new FileNotFoundException(MISSING_CAR_DEF);

            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document carsDef = docBuilder.parse(carDef);

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

            for (int s = 0; s < listOfCars.getLength(); s++) {

                Node carNode = listOfCars.item(s);
                if (carNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element carElement = (Element) carNode;

                    // Read element types and populate arrays
                    for (int i = 0; i < tag.length; i++) {
                        NodeList list = carElement.getElementsByTagName(tag[i]);
                        Element element = (Element) list.item(0);
                        if (element != null) {
                            NodeList value = element.getChildNodes();
                            String data = value.item(0).getNodeValue().trim();
                            switch (i) {
                            case 0:
                                carTypeArr[s] = data;
                                //                                    gearRatioArr[s] = data;
                                for (int g = 1; g <= 6; g++) {
                                    String gearNo = "gearratio" + g;
                                    NodeList grsList = carElement.getElementsByTagName(gearNo);
                                    Element carGrsElement = (Element) grsList.item(0);
                                    if (carGrsElement != null) {
                                        NodeList grsValueList = carGrsElement.getChildNodes();
                                        if (grsValueList.item(0).getNodeValue().trim() != null) {
                                            gearsRatioArr[s][0] = Integer.toString(g);
                                            gearsRatioArr[s][g] = grsValueList.item(0).getNodeValue().trim();
                                        }
                                    }
                                    //if (LOGGER.isTraceEnabled())
                                    //    LOGGER.trace("Car: " + s + " Gear: " + g + " Ratio: " + gearsRatioArr[s][g]);
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
            final String msg = MessageFormat.format(
                    rb.getString("PARSEERROR"),
                    err.getLineNumber(),
                    err.getSystemId(),
                    err.getMessage());
            notifyError(msg);
            LOGGER.error("DYNO ** Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId());
            LOGGER.error(" " + err.getMessage());
        }
        catch (SAXException e) {
            Exception x = e.getException();
            ((x == null) ? e : x).printStackTrace();
        }
        catch (Throwable t) {    // file not found
            carTypeArr = new String[]{MISSING_CAR_DEF};
            LOGGER.warn("No " + CARS_FILE + " file found, possible missing DTD issue?");
            //t.printStackTrace();
        }
    }

    private static void setSelectAllFieldText(JTextComponent comp) {
        // Ensures that all the text in a JTextComponent will be
        // selected whenever the cursor is in that field (gains focus):
        if (allTextSelector == null) {
            allTextSelector = new java.awt.event.FocusAdapter() {
                @Override
                public void focusGained(FocusEvent ev) {
                    JTextComponent textComp = (JTextComponent) ev.getSource();
                    textComp.selectAll();
                }
            };
        }
        comp.addFocusListener(allTextSelector);
    }

    private Settings getSettings() {
        return SettingsManager.getSettings();
    }

    private void enableRecordButton() {
        if (!recordDataButton.isEnabled()) {
            recordDataButton.setEnabled(true);
        }
    }

    private void disableRecordButton() {
        if (recordDataButton.isEnabled()) {
            recordDataButton.setEnabled(false);
        }
    }

    private String applyProperty(String key) {
        if (rb.containsKey(key)) {
            return rb.getString(key);
        }
        return "null";
    }

    private void notifyError(final String msg) {
        showMessageDialog(parent,
                msg,
                rb.getString("ERROR"), ERROR_MESSAGE);
    }

    public void saveSettings() {
        getSettings().setSelectedCar((String) carSelectBox.getSelectedItem());
        getSettings().setSelectedGear((String) gearSelectBox.getSelectedItem());
        getSettings().setDynoThreshold(NumberUtil.stringValue(tpsMin));
        getSettings().setDynoThrottle((String) tpsComboBox.getSelectedItem());
    }
}
