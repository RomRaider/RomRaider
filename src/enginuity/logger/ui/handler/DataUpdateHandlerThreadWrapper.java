package enginuity.logger.ui.handler;

import enginuity.logger.definition.EcuData;
import static enginuity.util.ThreadUtil.sleep;

import java.util.ArrayList;
import static java.util.Collections.synchronizedList;
import java.util.List;

public final class DataUpdateHandlerThreadWrapper implements DataUpdateHandler, Runnable {
    private final DataUpdateHandler wrappee;
    private final List<DataUpdate> updateList = synchronizedList(new ArrayList<DataUpdate>());
    private final List<DataUpdate> workingList = synchronizedList(new ArrayList<DataUpdate>());
    private boolean stop = false;

    public DataUpdateHandlerThreadWrapper(DataUpdateHandler wrappee) {
        this.wrappee = wrappee;
    }

    public void registerData(EcuData ecuData) {
        wrappee.registerData(ecuData);
    }

    public synchronized void handleDataUpdate(EcuData ecuData, byte[] value, long timestamp) {
        updateList.add(new DataUpdate(ecuData, value, timestamp));
    }

    public void deregisterData(EcuData ecuData) {
        wrappee.deregisterData(ecuData);
    }

    public void cleanUp() {
        stop = true;
        wrappee.cleanUp();
    }

    public void run() {
        while (!stop) {
            updateWorkingList();
            for (DataUpdate dataUpdate : workingList) {
                wrappee.handleDataUpdate(dataUpdate.getEcuData(), dataUpdate.getValue(), dataUpdate.getTimestamp());
            }
            sleep(3);
        }
    }

    private synchronized void updateWorkingList() {
        workingList.clear();
        for (DataUpdate dataUpdate : updateList) {
            workingList.add(dataUpdate);
        }
        updateList.clear();
    }

    private static final class DataUpdate {
        private final EcuData ecuData;
        private final byte[] value;
        private final long timestamp;

        public DataUpdate(EcuData ecuData, byte[] value, long timestamp) {
            this.ecuData = ecuData;
            this.value = value;
            this.timestamp = timestamp;
        }

        public EcuData getEcuData() {
            return ecuData;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public byte[] getValue() {
            return value;
        }

    }
}
