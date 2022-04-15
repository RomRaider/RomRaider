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

package com.romraider.xml;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.w3c.dom.Document;
import com.romraider.logger.ecu.definition.EcuDefinition;
import com.romraider.logger.ecu.definition.EcuDefinitionImpl;
import com.romraider.logger.ecu.definition.xml.EcuDefinitionDocumentLoader;
import com.romraider.maps.Scale;


public class TableScaleUnmarshallerTest {
    private final Document document = getDocument();
    private final TableScaleUnmarshaller tableScaleHandler = new TableScaleUnmarshaller();

    /** Test creation of scales from XML definition file scalingbase.
     */
    @Test
    public void testScalingBase() {
        final List<Scale> expectedScales = buildExpectedScales();
        tableScaleHandler.unmarshallBaseScales(document.getDocumentElement());
        final Map<String, Scale> scales = tableScaleHandler.getScales();
        for (final Scale scale : scales.values()) {
            System.out.println(scale);
        }
        assertEquals(expectedScales.size(), scales.size());
        for (Scale expectedScale : expectedScales) {
            assertEquals(true, scales.values().contains(expectedScale));
        }
    }

    private Document getDocument() {
        final EcuDefinition ecuDef = new EcuDefinitionImpl("2F12795606", "A2WC522S",
                "05 Outback XT", "32BITBASE", new File("src/test/definitions/scalingbase_test.xml"));
        final Document document = EcuDefinitionDocumentLoader.getDocument(ecuDef);
        return document;
    }

    private List<Scale> buildExpectedScales() {
        final List<Scale> expectedScales = new ArrayList<Scale>();

        final Scale BoostTarget_barabsolute = new Scale();
        BoostTarget_barabsolute.setCategory("Metric");
        BoostTarget_barabsolute.setName("BoostTarget_barabsolute");
        BoostTarget_barabsolute.setExpression("x*.001333224");
        BoostTarget_barabsolute.setByteExpression("x/.001333224");
        BoostTarget_barabsolute.setUnit("Boost Target (bar absolute)");
        BoostTarget_barabsolute.setFormat("#0.000");
        BoostTarget_barabsolute.setCoarseIncrement(0.1);
        BoostTarget_barabsolute.setFineIncrement(0.01);
        expectedScales.add(BoostTarget_barabsolute);

        final Scale BoostTarget_psirelativesealevel = new Scale();
        BoostTarget_psirelativesealevel.setCategory("Standard");
        BoostTarget_psirelativesealevel.setName("BoostTarget_psirelativesealevel");
        BoostTarget_psirelativesealevel.setExpression("(x-760)*.01933677");
        BoostTarget_psirelativesealevel.setByteExpression("(x/.01933677)+760");
        BoostTarget_psirelativesealevel.setUnit("Boost Target (psi relative sea level)");
        BoostTarget_psirelativesealevel.setFormat("0.00");
        BoostTarget_psirelativesealevel.setCoarseIncrement(0.5);
        BoostTarget_psirelativesealevel.setFineIncrement(0.01);
        expectedScales.add(BoostTarget_psirelativesealevel);

        final Scale rpm = new Scale();
        rpm.setCategory("Default");
        rpm.setName("rpm");
        rpm.setExpression("x");
        rpm.setByteExpression("x");
        rpm.setUnit("RPM");
        rpm.setFormat("#");
        rpm.setCoarseIncrement(100.0);
        rpm.setFineIncrement(50.0);
        expectedScales.add(rpm);

        final Scale targetboostcomppercent = new Scale();
        targetboostcomppercent.setCategory("Default");
        targetboostcomppercent.setName("targetboostcomppercent");
        targetboostcomppercent.setExpression("(x*.78125)-100");
        targetboostcomppercent.setByteExpression("(x+100)/.78125");
        targetboostcomppercent.setUnit("raw value");
        targetboostcomppercent.setFormat("0.0");
        targetboostcomppercent.setCoarseIncrement(1.0);
        targetboostcomppercent.setFineIncrement(0.4);
        expectedScales.add(targetboostcomppercent);

        final Scale WastegateDutyCycle_ = new Scale();
        WastegateDutyCycle_.setCategory("Default");
        WastegateDutyCycle_.setName("WastegateDutyCycle_%");
        WastegateDutyCycle_.setExpression("x*.00390625");
        WastegateDutyCycle_.setByteExpression("x/.00390625");
        WastegateDutyCycle_.setUnit("Wastegate Duty Cycle (%)");
        WastegateDutyCycle_.setFormat("0.0");
        WastegateDutyCycle_.setCoarseIncrement(1.0);
        WastegateDutyCycle_.setFineIncrement(0.2);
        WastegateDutyCycle_.setMin(0.1);
        WastegateDutyCycle_.setMax(99.9);
        expectedScales.add(WastegateDutyCycle_);

        return expectedScales;
    }
}
