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

package com.romraider.logger.ecu.comms.query;

public interface EcuQuery extends Query {

    /**
     * Return a list of Strings, each entry in the list is an address of this
     * query.  For byte data there's only one address in the list, for word
     * data there are two addresses in the list, for Dword there are four
     * addresses in the list. 
     */
    String[] getAddresses();

    /**
     * Return a byte array made up from all of the addresses for this query.
     */
    byte[] getBytes();

    /**
     * Return a String made up from all of the addresses for this query in hex
     * format.
     */
    String getHex();

    /**
     * Set the byte array that represents the response data for the query.
     * @param bytes
     */
    void setResponse(byte[] bytes);
}
