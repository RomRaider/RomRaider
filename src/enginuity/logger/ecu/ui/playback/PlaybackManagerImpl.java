package enginuity.logger.ecu.ui.playback;

import enginuity.logger.ecu.comms.query.Response;
import enginuity.logger.ecu.comms.query.ResponseImpl;
import enginuity.logger.ecu.definition.LoggerData;
import enginuity.logger.ecu.ui.handler.DataUpdateHandler;
import static enginuity.util.ThreadUtil.sleep;
import java.io.File;
import java.util.List;

//TODO: Finish me.
public final class PlaybackManagerImpl implements PlaybackManager {
    private final List<? extends LoggerData> loggerDatas;
    private final DataUpdateHandler[] dataUpdateHandlers;

    public PlaybackManagerImpl(List<? extends LoggerData> loggerDatas, DataUpdateHandler... dataUpdateHandlers) {
        this.loggerDatas = loggerDatas;
        this.dataUpdateHandlers = dataUpdateHandlers;
    }

    public void load(File file) {
        // TODO: Finish me!
        for (DataUpdateHandler handler : dataUpdateHandlers) {
            handler.registerData(loggerDatas.get(10));
            handler.registerData(loggerDatas.get(20));
            handler.registerData(loggerDatas.get(30));
        }
    }

    public void play() {
        double d = 0.0;
        while (true) {
            for (DataUpdateHandler handler : dataUpdateHandlers) {
                Response response = new ResponseImpl();
                response.setDataValue(loggerDatas.get(10), d);
                response.setDataValue(loggerDatas.get(20), d);
                response.setDataValue(loggerDatas.get(30), d);
                handler.handleDataUpdate(response);
                d += 100.0;
            }
            sleep(100L);
        }
    }

    public void play(int speed) {
        throw new UnsupportedOperationException();
    }

    public void step(int increment) {
        throw new UnsupportedOperationException();
    }

    public void pause() {
        throw new UnsupportedOperationException();
    }

    public void stop() {
        throw new UnsupportedOperationException();
    }

    public void reset() {
        throw new UnsupportedOperationException();
    }
}
