package enginuity.NewGUI.etable;

import org.apache.log4j.Logger;

public class ETableSaveState {
    private static final Logger LOGGER = Logger.getLogger(ETableSaveState.class);
    private Object[][] internalData;

    //private String name;
    public ETableSaveState(Object[][] data) {
        //this.name = name;
        int width = data.length;
        int height = data[0].length;

        //LOGGER.debug("Dimensions:  w:"+ width+"   h:"+height);
        this.internalData = new Object[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Object tempData = data[i][j];
                this.internalData[i][j] = tempData;
            }
        }

        LOGGER.debug("Sample: " + this.internalData[0][0]);
    }

    public Object[][] getData() {
        return this.internalData;
    }

    /*
     public String getName(){
         return name;
     }
     */
}
