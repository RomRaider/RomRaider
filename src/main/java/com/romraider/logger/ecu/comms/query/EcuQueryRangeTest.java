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

package com.romraider.logger.ecu.comms.query;

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


    public EcuQueryRangeTest(Collection<EcuQuery> queries) {
        this.queries = queries;
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
            int dataSize = EcuQueryData.getDataLength(query);
            final int address = Integer.parseInt(query.getHex(), 16);
            if (address < lowestAddress) {
                lowestAddress = address;
                newQuery.clear();
                newQuery.add(query);
            }
            if (address > highestAddress) {
                highestAddress = address + dataSize - 1;
            }
            LOGGER.trace(
                    String.format(
                            "addr:%d size:%d lowest:%d highest:%d",
                            address, dataSize, lowestAddress, highestAddress));
        }
        datalength = highestAddress - lowestAddress;
        if (datalength <= 128) {
            datalength ++;
            return newQuery;
        }
        datalength = 0;
        return null;
    }

    /**
     * Return the datalength calculated between the lowest address and the
     * highest address (included the datatype length) for the queries.
     * @return the length or -1 if not properly initialized
     */
    public final int getLength() {
        LOGGER.trace(
                String.format("EcuQueryRange length:%d", datalength));
        return datalength;
    }
}
