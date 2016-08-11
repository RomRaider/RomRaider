/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2016 RomRaider.com
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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

import com.romraider.logger.ecu.comms.learning.tableaxis.DS2TableAxisQueryParameterSet;
import com.romraider.logger.ecu.comms.query.EcuQuery;


public class DS2TableAxisQueryParameterSetTest {

	/** Test the address manipulation needed to convert the def table
	 *  address to the linear RAM read address.
	 */
	@Test
	public void testAddress() {
		//MS41 256kB - full
		List<EcuQuery> tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x16001", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x12001", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x12002", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x12003", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x12004", tableAxisQuery.get(3).getAddresses()[0]);
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x16fff", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x12FFF", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x13000", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x13001", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x13002", tableAxisQuery.get(3).getAddresses()[0]);

		//MS41 24kB - partial
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x2001", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x12001", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x12002", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x12003", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x12004", tableAxisQuery.get(3).getAddresses()[0]);
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x2fff", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x12FFF", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x13000", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x13001", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x13002", tableAxisQuery.get(3).getAddresses()[0]);

        //MS42 512kB - full
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x48001", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x48001", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x48002", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x48003", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x48004", tableAxisQuery.get(3).getAddresses()[0]);
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x48fff", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x48FFF", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x49000", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x49001", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x49002", tableAxisQuery.get(3).getAddresses()[0]);

		//MS42 32kB - partial
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x801", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x48801", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x48802", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x48803", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x48804", tableAxisQuery.get(3).getAddresses()[0]);
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x8ff", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x488FF", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x48900", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x48901", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x48902", tableAxisQuery.get(3).getAddresses()[0]);

		//MS43 512kB - full
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x74001", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x74001", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x74002", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x74003", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x74004", tableAxisQuery.get(3).getAddresses()[0]);
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x740ff", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x740FF", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x74100", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x74101", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x74102", tableAxisQuery.get(3).getAddresses()[0]);

		//MS43 32kB - partial
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x4001", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x74001", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x74002", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x74003", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x74004", tableAxisQuery.get(3).getAddresses()[0]);
		tableAxisQuery = DS2TableAxisQueryParameterSet.build(
				"0x40ff", "uint8", "x*5.45", "mg/stroke", "4",
				"0x06", "0x00", null, "little"
        );
		assertEquals("0x740FF", tableAxisQuery.get(0).getAddresses()[0]);
		assertEquals("0x74100", tableAxisQuery.get(1).getAddresses()[0]);
		assertEquals("0x74101", tableAxisQuery.get(2).getAddresses()[0]);
		assertEquals("0x74102", tableAxisQuery.get(3).getAddresses()[0]);
	}
}
