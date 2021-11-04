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

package com.romraider.logger.ecu.comms.learning.EcuDefinitionInheritance;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.romraider.logger.ecu.comms.learning.tableaxis.SSMTableAxisQueryParameterSet;
import com.romraider.logger.ecu.comms.query.EcuQuery;
import com.romraider.logger.ecu.definition.EcuDefinition;
import com.romraider.logger.ecu.definition.EcuDefinitionImpl;
import com.romraider.logger.ecu.definition.xml.EcuDefinitionDocumentLoader;
import com.romraider.logger.ecu.definition.xml.EcuDefinitionInheritanceList;
import com.romraider.logger.ecu.definition.xml.EcuTableDefinitionHandler;


public class EcuDefinitionInheritanceTest {

	/** Test the address manipulation needed to convert the def table
	 *  address to the linear RAM read address.
	 */
	@Test
	public void testAddress() {
	    List<String> AF_TABLE_NAMES = Arrays.asList(
	            "A/F Learning #1 Airflow Ranges",
	            "A/F Learning #1 Airflow Ranges ",
	            "A/F Learning Airflow Ranges");
	    Collection<EcuQuery> queries = new ArrayList<EcuQuery>();
        EcuDefinition ecuDef = new EcuDefinitionImpl("2F12795206", "A2WC510S",
                "05 outback xt", "A2WC510N", new File("src/test/definitions/LearningAirflowRanges.xml"));
        Document document = EcuDefinitionDocumentLoader.getDocument(ecuDef);
        queries = getTableAxisRanges(document, ecuDef, AF_TABLE_NAMES);
        assertEquals(3, queries.size());
        for (EcuQuery query : queries) {
            System.out.println(query);
        }
	}

    /**
     * Retrieve the table axis values from the ECU definition. First try the
     * 4-cyl table names, if still empty try the 6-cyl table name.
     */
    private final List<EcuQuery> getTableAxisRanges(
            Document document,
            EcuDefinition ecuDef,
            List<String> tableNames) {

        List<EcuQuery> tableAxis = new ArrayList<EcuQuery>();
        for (String tableName : tableNames) {
            tableAxis = loadTable(document, ecuDef, tableName);
            if (!tableAxis.isEmpty()) {
                break;
            }
        }
        return tableAxis;
    }

    /**
     * Build a List of EcuQueries to retrieve the axis and scaling of a table.
     * A table is found when the storageaddress parameter has been identified.
     */
    private final List<EcuQuery> loadTable(
            Document document,
            EcuDefinition ecuDef,
            String tableName) {

        final List<Node> inheritanceList =
                EcuDefinitionInheritanceList.getInheritanceList(document, ecuDef);
        final Map<String, String> tableMap =
                EcuTableDefinitionHandler.getTableDefinition(
                        document,
                        inheritanceList,
                        tableName);
        List<EcuQuery> tableAxisQuery = new ArrayList<EcuQuery>();
        if (tableMap.containsKey("storageaddress")) {
            tableAxisQuery = SSMTableAxisQueryParameterSet.build(
                    tableMap.get("storageaddress"),
                    tableMap.get("storagetype"),
                    tableMap.get("expression"),
                    tableMap.get("units"),
                    tableMap.get("sizey")
            );
        }
        return tableAxisQuery;
    }
}
