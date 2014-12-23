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

package com.romraider.logger.ecu.definition;

import static com.romraider.logger.ecu.definition.EcuDataType.SWITCH;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;

public final class EcuSwitchImpl implements EcuSwitch {
    private final String id;
    private final String name;
    private final String description;
    private final EcuAddress address;
    private final String group;
    private final String subgroup;
    private final int groupsize;
    private final EcuDataConvertor[] convertors;
    private int selectedConvertorIndex;
    private boolean fileLogController;
    private boolean selected;

    public EcuSwitchImpl(
            String id, String name, String description, EcuAddress address,
            String group, String subgroup, String groupsize,
            EcuDataConvertor[] convertors) {
        checkNotNullOrEmpty(id, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNull(address, "address");
        checkNotNullOrEmpty(convertors, "convertors");
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.group = group;
        this.subgroup = subgroup;
        this.groupsize = groupsize == null ? 0 : Integer.parseInt(groupsize);
        this.convertors = convertors;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public EcuAddress getAddress() {
        return address;
    }

    @Override
    public String getGroup() {
        return group;
    }

    @Override
    public String getSubgroup() {
        return subgroup;
    }

    @Override
    public int getGroupSize() {
        return groupsize;
    }

    @Override
    public EcuDataConvertor getSelectedConvertor() {
        return convertors[selectedConvertorIndex];
    }

    @Override
    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }

    @Override
    public void selectConvertor(EcuDataConvertor convertor) {
        for (int i = 0; i < convertors.length; i++) {
            EcuDataConvertor dataConvertor = convertors[i];
            if (convertor == dataConvertor) {
                selectedConvertorIndex = i;
            }
        }
    }

    @Override
    public EcuDataType getDataType() {
        return SWITCH;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setFileLogController(boolean fileLogController) {
        this.fileLogController = fileLogController;
    }

    public boolean isFileLogController() {
        return fileLogController;
    }
}
