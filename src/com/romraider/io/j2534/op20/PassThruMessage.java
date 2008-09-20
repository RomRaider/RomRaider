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
