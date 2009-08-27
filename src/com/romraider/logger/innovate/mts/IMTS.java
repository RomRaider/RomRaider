package com.romraider.logger.innovate.mts;

import com4j.Com4jObject;
import com4j.IID;
import com4j.VTID;

/**
 * IMTS Interface
 */
@IID("{FCE3DA3F-110C-4781-B751-ABDC039BCF18}")
public interface IMTS extends Com4jObject {
    /**
     * Get number of MTS ports available
     */
    @VTID(7)
    int portCount();

    /**
     * CurrentMTSPort
     */
    @VTID(8)
    int currentPort();

    /**
     * CurrentMTSPort
     */
    @VTID(9)
    void currentPort(
            int pVal);

    /**
     * MTS Port Name
     */
    @VTID(10)
    java.lang.String portName();

    /**
     * Attempt an MTS Connection
     */
    @VTID(11)
    void connect();

    /**
     * Disconnect MTS Port
     */
    @VTID(12)
    void disconnect();

    /**
     * Number of MTS Inputs
     */
    @VTID(13)
    int inputCount();

    /**
     * Current MTS Input
     */
    @VTID(14)
    int currentInput();

    /**
     * Current MTS Input
     */
    @VTID(15)
    void currentInput(
            int pVal);

    /**
     * Name of Current Input
     */
    @VTID(16)
    java.lang.String inputName();

    /**
     * Units used by Input
     */
    @VTID(17)
    java.lang.String inputUnit();

    /**
     * Name of Device Providing Input
     */
    @VTID(18)
    java.lang.String inputDeviceName();

    /**
     * Type of Device Providing Input
     */
    @VTID(19)
    int inputDeviceType();

    /**
     * Type of Input
     */
    @VTID(20)
    int inputType();

    /**
     * Channel on Device providing Input
     */
    @VTID(21)
    int inputDeviceChannel();

    /**
     * AFR Multiplier for Input (if used)
     */
    @VTID(22)
    float inputAFRMultiplier();

    /**
     * Minimum Value (units) for Input
     */
    @VTID(23)
    float inputMinValue();

    /**
     * Max value (in units) for Input
     */
    @VTID(24)
    float inputMaxValue();

    /**
     * Voltage equivelent to Input Min Value
     */
    @VTID(25)
    float inputMinVolt();

    /**
     * Voltage equivelent to Input Max Value
     */
    @VTID(26)
    float inputMaxVolt();

    /**
     * Raw Sample (0-1023) for Input
     */
    @VTID(27)
    int inputSample();

    /**
     * Status and Function of Input Sample
     */
    @VTID(28)
    int inputFunction();

    /**
     * Call to start NewData events from MTS Connection
     */
    @VTID(29)
    void startData();

}
