package com.romraider.logger.ecu.ui.playback;

import java.io.File;

public interface PlaybackManager {
    void load(File file);

    void play();

    void play(int speed);

    void step(int increment);

    void pause();

    void stop();

    void reset();
}
