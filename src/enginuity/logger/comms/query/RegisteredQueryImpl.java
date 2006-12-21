/*
 *
 * Enginuity Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006 Enginuity.org
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
 *
 */

package enginuity.logger.comms.query;

import enginuity.logger.definition.EcuData;
import static enginuity.util.HexUtil.asBytes;
import static enginuity.util.HexUtil.asHex;
import static enginuity.util.ParamChecker.checkNotNull;

public final class RegisteredQueryImpl implements RegisteredQuery {
    private final EcuData ecuData;
    private final LoggerCallback callback;
    private final byte[] bytes;
    private final String hex;

    public RegisteredQueryImpl(EcuData ecuData, LoggerCallback callback) {
        checkNotNull(ecuData, callback);
        this.ecuData = ecuData;
        this.callback = callback;
        bytes = getAddressBytes(ecuData);
        hex = asHex(bytes);
    }

    public EcuData getEcuData() {
        return ecuData;
    }

    public String[] getAddresses() {
        return ecuData.getAddresses();
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getHex() {
        return hex;
    }

    public void setResponse(byte[] response) {
        callback.callback(response);
    }

    public boolean equals(Object object) {
        if (object instanceof RegisteredQueryImpl) {
            return getHex().equals(((RegisteredQueryImpl) object).getHex());
        }
        return false;
    }

    public int hashCode() {
        return getHex().hashCode();
    }

    public String toString() {
        return "0x" + getHex();
    }

    private byte[] getAddressBytes(EcuData ecuData) {
        String[] addresses = ecuData.getAddresses();
        byte[] bytes = new byte[0];
        for (String address : addresses) {
            byte[] tmp1 = asBytes(address);
            byte[] tmp2 = new byte[bytes.length + tmp1.length];
            System.arraycopy(bytes, 0, tmp2, 0, bytes.length);
            System.arraycopy(tmp1, 0, tmp2, bytes.length, tmp1.length);
            bytes = tmp2;
        }
        return bytes;
    }
}
