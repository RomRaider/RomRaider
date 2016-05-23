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

import static com.romraider.logger.ecu.definition.EcuDataType.PARAMETER;
import static com.romraider.util.ParamChecker.checkNotNull;
import static com.romraider.util.ParamChecker.checkNotNullOrEmpty;
import java.util.HashSet;
import java.util.Set;

public final class EcuDerivedParameterImpl implements EcuParameter {
    private final String id;
    private final String name;
    private final String description;
    private final EcuDerivedParameterConvertor[] convertors;
    private final EcuAddress address;
    private final Set<ConvertorUpdateListener> listeners = new HashSet<ConvertorUpdateListener>();
    private int selectedConvertorIndex;
    private boolean selected;

    public EcuDerivedParameterImpl(String id, String name, String description, EcuData[] ecuDatas,
                                   EcuDerivedParameterConvertor[] convertors) {
        checkNotNullOrEmpty(id, "id");
        checkNotNullOrEmpty(name, "name");
        checkNotNull(description, "description");
        checkNotNullOrEmpty(ecuDatas, "ecuDatas");
        checkNotNullOrEmpty(convertors, "convertors");
        this.id = id;
        this.name = name;
        this.description = description;
        this.convertors = convertors;
        this.address = buildCombinedAddress(ecuDatas);
        setEcuDatas(ecuDatas);
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
    public EcuDataConvertor getSelectedConvertor() {
        return convertors[selectedConvertorIndex];
    }

    @Override
    public EcuDataConvertor[] getConvertors() {
        return convertors;
    }

    @Override
    public void selectConvertor(EcuDataConvertor convertor) {
        if (convertor != getSelectedConvertor()) {
            for (int i = 0; i < convertors.length; i++) {
                EcuDerivedParameterConvertor parameterConvertor = convertors[i];
                if (convertor == parameterConvertor) {
                    selectedConvertorIndex = i;
                }
            }
            notifyUpdateListeners();
        }
    }

    @Override
    public EcuDataType getDataType() {
        return PARAMETER;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public void addConvertorUpdateListener(ConvertorUpdateListener listener) {
        checkNotNull(listener, "listener");
        listeners.add(listener);
    }

    @Override
    public String getGroup() {
        return null;
    }

    @Override
    public String getSubgroup() {
        return null;
    }

    @Override
    public int getGroupSize() {
        return 0;
    }

    private EcuAddress buildCombinedAddress(EcuData[] ecuDatas) {
        String[] addresses = new String[0];
        for (EcuData ecuData : ecuDatas) {
            String[] newAddresses = ecuData.getAddress().getAddresses();
            String[] tmp = new String[addresses.length + newAddresses.length];
            System.arraycopy(addresses, 0, tmp, 0, addresses.length);
            System.arraycopy(newAddresses, 0, tmp, addresses.length, newAddresses.length);
            addresses = tmp;
        }
        return new EcuAddressImpl(addresses);
    }

    private void setEcuDatas(EcuData[] ecuDatas) {
        for (EcuDerivedParameterConvertor convertor : convertors) {
            convertor.setEcuDatas(ecuDatas);
        }
    }

    private void notifyUpdateListeners() {
        for (ConvertorUpdateListener listener : listeners) {
            listener.notifyConvertorUpdate(this);
        }
    }
}
