package Enginuity.DefinitionBuilder;

import Enginuity.XML.RomAttributeParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class SubaruMapDefinitionParser {
        
    public SubaruMapDefinitionParser() { }
    
    public String parseTable(byte[] data, int offset, String name, int filesize, int type, boolean size, boolean bytes) {
        System.out.println(data.length + " " + filesize * 1024);
        offset += (data.length - filesize * 1024);
        System.out.println(offset + " " + filesize);
        
        /* Table definitions:
           0x04 = y16 x8 z8
           0x14 = y16 x8 z16
           0x05 = y16 x16 z8
           0x15 = y16 x16 z16 */
        
        String output = "";
        
        int sizeX = 0;
        int sizeY = 0;
        int byteX = 0;
        int byteY = 0;
        int byteZ = 0;
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        
        System.out.println("Table type: " + (data[offset] & 0xff));
        
        if (type == 3) {
            
        // 3D TABLES -----------------------------------------------------------
            if (data[offset] == 0x04) { // y16 x8 z8
                byteX = 1;         byteY = 2;         byteZ = 1;
            } else if (data[offset] == 0x14) { // y16 x8 z16
                byteX = 1;         byteY = 2;         byteZ = 2;
            } else if (data[offset] == 0x05) { // y16 x16 z8
                byteX = 2;         byteY = 2;         byteZ = 1;
            } else if (data[offset] == 0x15) { // y16 x16 z16
                byteX = 2;         byteY = 2;         byteZ = 2;
            }            
            //set values
            sizeX = data[offset - 1] + 1;
            offsetX = offset - 1 - byteX * sizeX;
            sizeY = data[offset - 2 - byteX * sizeX] + 1;
            offsetY = offset - 2 - byteX * sizeX - byteY * sizeY;
            offsetZ = offset + 1; 
            
            //output values
            output = output + "      <table name=\"" + name + "\" type=\"3D\" storageaddress=\"0x" + Integer.toHexString(offsetZ).toUpperCase() + "\"";
            if (size) output = output + " sizex=\"" + sizeX + "\" sizey=\"" + sizeY + "\"";
            if (bytes) output = output + " storagetype=\"uint" + 8 * byteZ + "\"";
            output = output + ">\n";
            output = output + "         <table type=\"X Axis\" storageaddress=\"0x" + Integer.toHexString(offsetX).toUpperCase() + "\"";
            if (size) output = output + " sizex=\"" + sizeX + "\"";
            if (bytes) output = output + " storagetype=\"uint" + 8 * byteX + "\"";
            output = output + "/>\n";
            output = output + "         <table type=\"Y Axis\" storageaddress=\"0x" + Integer.toHexString(offsetY).toUpperCase() + "\"";
            if (size) output = output + " sizey=\"" + sizeY + "\"";
            if (bytes) output = output + " storagetype=\"uint" + 8 * byteY + "\"";
            output = output + "/>\n";
            output = output + "      </table>\n";
        } else if (type == 2) {

            byteY = 2; byteZ = 2;
            //set values
            sizeY = data[offset - 1] + 1;
            offsetY = offset - 1 - byteY * sizeY;
            offsetZ = offset;
            output = output + "      <table name=\"" + name + "\" type=\"2D\" storageaddress=\"0x" + Integer.toHexString(offsetZ).toUpperCase() + "\"";
            if (size) output = output + " sizey=\"" + sizeY + "\"";
            if (bytes) output = output + " storagetype=\"uint" + 8 * byteZ + "\"";
            output = output + ">\n";
            output = output + "         <table type=\"Y Axis\" storageaddress=\"0x" + Integer.toHexString(offsetY).toUpperCase() + "\"";
            if (size) output = output + " sizey=\"" + sizeY + "\"";
            if (bytes) output = output + " storagetype=\"uint" + 8 * byteY + "\"";
            output = output + "/>\n";
            output = output + "      </table>\n";             
        }       
        
        return output;
    }
}