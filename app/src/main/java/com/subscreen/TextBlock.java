package com.subscreen;

import java.util.Date;

/**
 * Created by Nick on 1/1/2015.
 */
public interface TextBlock {
    public boolean showFramerates();
    public void firstDelay() throws InterruptedException;
    public void secondDelay() throws InterruptedException;
    public void getText(Output _outputTo);
    public long getStartTime();
    public void addSyncMessage(String message);
}