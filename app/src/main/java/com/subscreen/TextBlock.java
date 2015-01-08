package com.subscreen;

import java.util.Date;

/**
 * Created by Nick on 1/1/2015.
 */
public interface TextBlock {
    public void firstDelay();
    public void secondDelay();
    public void getText(Output _outputTo);
    public long getStartTime();
}
