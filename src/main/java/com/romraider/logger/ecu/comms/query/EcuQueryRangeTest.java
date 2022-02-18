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

package com.romraider.logger.ecu.comms.query;

import static com.romraider.util.HexUtil.hexToInt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Inspect the address of each query to determine if a single query
 * with a start address and byte length can be substituted as opposed
 * to querying each address separately.
 */
public final class EcuQueryRangeTest {
    private static final Logger LOGGER =
            Logger.getLogger(EcuQueryRangeTest.class);
    private final Collection<EcuQuery> queries;
    private int datalength;
    private int maxLength;


    /**
     * Initialize the class with a collection of queries and the maximum
     * distance between the lowest and highest allowed.
     * @param queries - collection of queries
     * @param maxLength - the maximum data length
     */
    public EcuQueryRangeTest(Collection<EcuQuery> queries, int maxLength) {
        this.queries = queries;
        this.maxLength = maxLength;
    }

    /**
     * Inspect the address of each query to determine if a single query
     * with a start address and byte length can be substituted as opposed
     * to querying each address separately.
     */
    public final Collection<EcuQuery> validate() {
        if (queries == null) {
            datalength = -1;
            return null;
        }

        final List<EcuQuery> queryList = (List<EcuQuery>) queries;
        final Collection<EcuQuery> newQuery = new ArrayList<EcuQuery>();
        datalength = 0;
        int lowestAddress = Integer.MAX_VALUE;
        int highestAddress = 0;
        for (EcuQuery query : queryList) {
            final int dataTypeLength = EcuQueryData.getDataLength(query);
            for (int i = 0; i < dataTypeLength; i++) {
                int address = hexToInt(query.getAddresses()[0]) + i;
                if (address < lowestAddress) {
                    lowestAddress = address;
                    newQuery.clear();
                    newQuery.add(query);
                }
                if (address > highestAddress) {
                    highestAddress = address;
                }
                if (LOGGER.isTraceEnabled())
                    LOGGER.trace(
                        String.format(
                                "addr:%d size:%d lowest:%d highest:%d",
                                address, highestAddress - lowestAddress + 1,
                                lowestAddress, highestAddress));
            }
        }
        datalength = highestAddress - lowestAddress + 1;
        if (datalength > maxLength) {
            datalength = 0;
            newQuery.clear();
        }
        return newQuery;
    }

    /**
     * Return the datalength calculated between the lowest address and the
     * highest address (included the datatype length) for the queries.
     * @return the length or -1 if not properly initialized
     */
    public final int getLength() {
        if (LOGGER.isTraceEnabled())
            LOGGER.trace(
                String.format("EcuQueryRange length:%d", datalength));
        return datalength;
    }
}
