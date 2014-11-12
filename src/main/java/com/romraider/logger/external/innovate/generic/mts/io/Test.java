/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2014 RomRaider.com
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

package com.romraider.logger.external.innovate.generic.mts.io;

import com4j.EventCookie;

public class Test implements MTSEvents {

    private MTS mts;
    private EventCookie connectionEventCookie;
    private int count = 0;

    public Test() {
        mts = null;
        connectionEventCookie = null;
    }

    private void printAvailableInputs() {
        System.out.println("Available inputs:");

        for (int idx = 0; idx < mts.inputCount(); ++idx) {
            mts.currentInput(idx);
            String inputName = mts.inputName();
            int inputType = mts.inputType();

            System.out.printf("inputName:%s, inputType:%d%n", inputName, inputType);
        }
    }

    private void waitFor(int milliseconds) throws InterruptedException {
        synchronized (this) {
            this.wait(milliseconds);
        }
    }

    private void getSamples(int numberOfSamples) throws InterruptedException {
        // note:
        // it sounds like the SDK allows up to 12.21 samples per second, which
        // should be more than sufficient for RomRaider
        System.out.println("Getting data samples:");

        // give the device time to start the acquisition of data
        waitFor(1000);

        for (int sampleCount = 0; sampleCount < numberOfSamples; ++sampleCount) {
            int data = mts.inputSample();
            int function = mts.inputFunction();

            // note if the sample data is lambda, you might need to multiply by 14.7 or inputAFRMultiplier()
            System.out.printf("\tSample %d: data = %d, function = %d%n", sampleCount + 1, data, function);

            // wait 200 milliseconds before grabbing the next sample
            waitFor(200);
        }
    }

    public void Run() throws InterruptedException {
        // create an instance of the MTSSDK COM component
        mts = MTSFactory.createMTS();

        // Note: you MUST call portCount() at least one before attempting to set
        // the current inputPort or the inputPort(int) will not do anything!
        int portCount = mts.portCount();

        System.out.printf("Found %d ports.%n", portCount);

        // register for MTS component events
        connectionEventCookie = mts.advise(MTSEvents.class, this);

        // set the current port before attempting to connect
        for (int i = 0; i < portCount; i++) {
            mts.currentPort(i);
            String portName = mts.portName();
    
            System.out.printf("Current MTS port is %d; port name = %s%n", i, portName);
    
            // attempt to connect to the specified device
            System.out.println("connect() called.");
            mts.connect();
    
            // show available inputs
            printAvailableInputs();
            mts.disconnect();
        }
        // attempt to get data
//        mts.currentInput(0);
        mts.currentPort(0);
        mts.connect();
//        mts.startData();

        // notes:
        // the inputFunction() method retrieves the meaning of the sample - this can be lambda/AFR,
        // error code, etc.  The inputSample() method retrieves the current data.
        // for instance, running this without having the O2 sensor plugged in will cause inputFunction() to
        // return 6 and inputSample() to return 9.
        // see the LM-2 manual for more information on error codes.
        //
        // list of function codes:
        //
        // MTS_FUNC_LAMBDA 0
        // MTS_FUNC_O2 1
        // MTS_FUNC_INCALIB 2
        // MTS_FUNC_RQCALIB 3
        // MTS_FUNC_WARMUP 4
        // MTS_FUNC_HTRCAL 5
        // MTS_FUNC_ERROR 6
        // MTS_FUNC_FLASHLEV 7
        // MTS_FUNC_SERMODE 8
        // MTS_FUNC_NOTLAMBDA 9
        // MTS_FUNC_INVALID 10

        // retrieve 10 samples
//        getSamples(10);
          System.out.println("Listening for 3600 seconds.");
           waitFor(3600000);

        // dispose of the event handler instance
        connectionEventCookie.close();

        // close the connection to the device
        System.out.println("disconnect() called.");
        mts.disconnect();

        // release COM resources
        mts.dispose();
    }

    // generated in response to a call to connect()
    // see the SDK doc for explanation of error codes

    public void connectionEvent(int result) {
       // Triggered in response to every Connect() call
       // Includes an integer result code:
       //   0 = success
       //   -1 no data read,
       //   < -1 = unspecified internal error
        if (result == 0) {
            mts.startData();
            System.out.printf("Innovate MTS connection success: %d%n", result);
        }
        else if (result == -1) {
            System.out.printf("No Innovate MTS Data detected: %d%n", result);
        }
        else {
            System.out.printf("Innovate MTS Connect Error: %d%n", result);
        }
    }

    public void connectionError() {
        // Triggered in response to a data timeout on
        // the MTS connection - Application should expect
        // no more data, but must still call Disconnect()
        System.out.printf("connectionError raised, shutting down.%n");
        connectionEventCookie.close();
        mts.disconnect();
        mts.dispose();
    }

    public void newData() {
        int i;
        float data = 0f;
        for (i = 0; i < mts.inputCount(); i++) {
//            i=0;
            mts.currentInput(i);
            float min = mts.inputMinValue();
            float max = mts.inputMaxValue();
            float sample = mts.inputSample();
            data = ((max - min) * ((float) sample / 1024f)) + min;
//            float multiplier = mts.inputAFRMultiplier();
//            int sampleMeaning = mts.inputFunction();
            String str = String.format(
                    "%d, %d, InputNo: %02d, InputName: %s, InputType: %d, DeviceName: %s, DeviceType: %d, DeviceChannel: %d, Units: %s, Multiplier: %f, MinValue: %f, MaxValue: %f, Sample: %f, Data: %f",
                    count, System.currentTimeMillis(), i, mts.inputName(), mts.inputType(), mts.inputDeviceName(), mts.inputDeviceType(), mts.inputDeviceChannel(), mts.inputUnit(), mts.inputAFRMultiplier(), mts.inputMinValue(), mts.inputMaxValue(), sample, data);

            System.out.printf("%s%n", str);
//            System.out.printf("newData raised for Input: %02d, Function: %d, Data: %f,\tMultiplier: %f%n", i, sampleMeaning, data, multiplier);
            count++;
        }
    }

    /**
     * @param args
     * @throws InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        Test test = new Test();
        test.Run();
    }
}


