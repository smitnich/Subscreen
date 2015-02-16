package com.subscreen;

import java.util.Date;

/**
 * Created by Nick on 1/1/2015.
 * Certain formats are based on the frame instead of the time; this cannot be determined reliably
 * Allow for the user to switch between different framerates dynamically, allowing for them to pick
 * the proper one once they have gotten out of sync.
 */
public class FrameBlock implements TextBlock {
    //24, 30, and 48 fps seem to be all the movie framerates that currently exist
    double frameRates[] = {23.976, 29.97, 23.976*2};
    double frameRateModifier = 0;
    public String text;
    public long startFrame;
    public long endFrame;
    //Since the time that a user pauses for is not related to the framerate, this should not
    //be based on the framerate modifier
    public FrameBlock(String input, long s, long e)
    {
        startFrame = s;
        endFrame = e;
        text = input;
        setFrameRate(0);
    }
    public void setFrameRate(int choice)
    {
        frameRateModifier = 1000.0/frameRates[choice];
    }
    public void firstDelay() throws InterruptedException
    {
        Date currentTime = new Date();
        long toSleep = (long) Math.floor(startFrame*frameRateModifier) - (currentTime.getTime() - Main.getOffset() - Main.rootTime);
        if (toSleep <= 0)
            return;
        try {
            Thread.sleep(toSleep);
        } catch (InterruptedException e) {
            throw e;
        }
    }
    public void getText(Output _outputTo)
    {
        _outputTo.outputText(text);
    }
    public void secondDelay() throws InterruptedException
    {
        Date currentTime = new Date();
        long toSleep = (long) Math.floor(endFrame*frameRateModifier) - (currentTime.getTime() - Main.getOffset() - Main.rootTime);
        if (toSleep <= 0)
            return;
        try {
            Thread.sleep(toSleep);
        } catch (InterruptedException e) {
            throw e;
        }
    }
    public long getStartTime()
    {
        Date currentTime = new Date();
        return (long) Math.floor(startFrame*frameRateModifier)-(currentTime.getTime() - Main.rootTime);
    }
    long convertFramerate(double newFPS)
    {
        Date currentTime = new Date();
        return Math.round((currentTime.getTime()-Main.rootTime)*newFPS/1000);
    }
}
