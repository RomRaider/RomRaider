package com.romraider.logger.utec.test;

import com.romraider.logger.utec.mapData.UtecMapData;

public class TestUtecMap {

    public static void main(String[] args) {
        UtecMapData umd = new UtecMapData("/Users/emorgan/stage2utec.TXT");
        umd.writeMapToFile("/Users/emorgan/save.txt");


    }
}
