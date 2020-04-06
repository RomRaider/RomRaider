/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2020 RomRaider.com
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

package com.romraider.logger.ecu.comms.learning.tableaxis;

import static com.romraider.logger.ecu.definition.xml.ConverterMaxMinDefaults.getDefault;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.romraider.Settings;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.comms.query.EcuQueryData;
import com.romraider.logger.ecu.comms.query.EcuQueryImpl;
import com.romraider.logger.ecu.definition.EcuAddress;
import com.romraider.logger.ecu.definition.EcuAddressImpl;
import com.romraider.logger.ecu.definition.EcuData;
import com.romraider.logger.ecu.definition.EcuDataConvertor;
import com.romraider.logger.ecu.definition.EcuParameterConvertorImpl;
import com.romraider.logger.ecu.definition.EcuParameterImpl;
import com.romraider.util.HexUtil;

/**
 * Build a List of ECU Queries to retrieve a Table's axis values.
 */
public class DS2TableAxisQueryParameterSet {

    private DS2TableAxisQueryParameterSet() {
    }

    /**
     * Build a List of ECU Queries.
     * @param storageAddress - the starting address of the Table.
     * @param storageType - the data storage type.
     * @param expression -  the equation to convert byte data to a real number.
     * @param units - the value's unit of measure.
     * @param size - the length of the Table's axis.
     * @param group - the group for an EcuParameter.
     * @param subgroup - the subgroup for an EcuParameter.
     * @param groupsize - the groupsize for an EcuParameter.
     * @param endian - the data storage endian.
     * @return a List of ECU Query items. 
     */
    public static final List<EcuQuery> build(
            String storageAddress,
            String storageType,
            String expression,
            String units,
            String size,
            String group,
            String subgroup,
            String groupsize,
            String endian) {
        
        final List<EcuQuery> tableAxisQuery = new ArrayList<EcuQuery>();
        final String tableAddrStr = storageAddress.replaceAll("0x", "");
        int tableAddrBase = Integer.parseInt(tableAddrStr, 16);
        //Adjust address from ECU table def to linear RAM read address 
        //MS41 256kB - full
        if (tableAddrBase > 0x16000 && tableAddrBase < 0x17000) {
            tableAddrBase -= 0x4000;
        }
        //MS41 24kB - partial
        else if (tableAddrBase > 0x2000 && tableAddrBase < 0x3000) {
            tableAddrBase += 0x10000;
        }
        //MS42 512kB - full
        else if (tableAddrBase > 0x48000 && tableAddrBase < 0x49000) {
            //no change
        }
        //MS42 32kB - partial
        else if (tableAddrBase > 0x800 && tableAddrBase < 0x930) {
            tableAddrBase += 0x48000;
        }
        //MS43 512kB - full
        else if (tableAddrBase > 0x74000 && tableAddrBase < 0x74100) {
            //no change
        }
        //MS43 32kB - partial
        else if (tableAddrBase > 0x4000 && tableAddrBase < 0x4100) {
            tableAddrBase += 0x70000;
        }

        int dataSize = EcuQueryData.getDataLength(storageType);
        Settings.Endian dataEndian = Settings.Endian.BIG;
        if (endian != null) {
            dataEndian = endian.equalsIgnoreCase("little") ? Settings.Endian.LITTLE : Settings.Endian.BIG;
        }

        final int count = Integer.parseInt(size, 10);
        for (int i = 0; i < count; i++) {
            final String addrStr =
                    HexUtil.intToHexString(tableAddrBase + (i * dataSize));
            final String id = addrStr + "-" + i;
            final EcuAddress ea = new EcuAddressImpl(addrStr, dataSize, -1);
            final EcuParameterImpl epi =
                new EcuParameterImpl(id, addrStr, id, ea, group, subgroup, groupsize,
                    new EcuDataConvertor[] {
                        new EcuParameterConvertorImpl(
                            units, expression, "0.000", -1, storageType,
                            dataEndian, new HashMap<String, String>(),
                            getDefault()
                        )
                    }
                );
            tableAxisQuery.add(new EcuQueryImpl((EcuData) epi));
        }
        return tableAxisQuery;
    }
}
