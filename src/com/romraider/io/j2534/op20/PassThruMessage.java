/*
 * RomRaider Open-Source Tuning, Logging and Reflashing
 * Copyright (C) 2006-2012 RomRaider.com
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

package com.romraider.io.j2534.op20;

import com.jinvoke.Embedded;
import com.jinvoke.NativeStruct;

@NativeStruct
public final class PassThruMessage {
    public int ProtocolID; /* vehicle network protocol */
    public int RxStatus; /* receive message status */
    public int TxFlags; /* transmit message flags */
    public int Timestamp; /* receive message timestamp(in microseconds) */
    public int DataSize; /* byte size of message payload in the Data array */
    public int ExtraDataIndex; /* start of extra data(i.e. CRC, checksum, etc) in Data array */
    @Embedded(length = 4128)
    public byte[] Data = new byte[4128]; /* message payload or data */
}
