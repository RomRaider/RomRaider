package com.romraider.logger.innovate.mts;

import com4j.COM4J;

/**
 * Defines methods to create COM objects
 */
public abstract class ClassFactory {
    private ClassFactory() {
    } // instantiation is not allowed


    /**
     * MTS SDK v1.0
     */
    public static IMTS createMTS() {
        return COM4J.createInstance(IMTS.class, "{74087A4E-4AF1-4F8C-BACB-3959C212AAD2}");
    }
}
