package com.romraider.logger.innovate.mts.events;

import com4j.DISPID;
import com4j.IID;

/**
 * _IMTSEvents Interface
 */
@IID("{4A8AA6AC-E180-433E-8871-A2F8D2413F03}")
public abstract class _IMTSEvents {
    /**
     * Triggered to indicate MTS connection result
     */
    @DISPID(1)
    public void connectionEvent(
            int result) {
        throw new UnsupportedOperationException();
    }

    /**
     * Triggered when an error occurs on the MTS data stream
     */
    @DISPID(2)
    public void connectionError() {
        throw new UnsupportedOperationException();
    }

    /**
     * Triggered when new sample data is available
     */
    @DISPID(3)
    public void newData() {
        throw new UnsupportedOperationException();
    }

}
