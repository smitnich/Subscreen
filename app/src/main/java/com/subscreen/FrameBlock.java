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
    static double frameRates[] = {23.976, 29.97, 23.976*2};
    static double[] frameRateMultipliers = {1, 29.97/23.976, 2};
    static double currentFramerateMultiplier = 1;
    static String[] frameRateStrings = {"24","30","48"};
    double frameRateModifier = 0;
    public String text;
    public long startFrame;
    public long endFrame;
    //If we don't start at the beginning, calculate what frame the starting text block is;
    //this will be needed in order to convert framerates later
    public long frameOffset;
    SubtitlePlayer playerInstance = null;
    public boolean showFramerates = true;

    //Since the time that a user pauses for is not related to the framerate, this should not
    //be based on the framerate modifier
    public long pauseTime;
    public FrameBlock(String input, long s, long e, SubtitlePlayer tmp)
    {
        pauseTime = 0;
        frameOffset = 0;
        playerInstance = tmp;
        startFrame = s;
        endFrame = e;
        text = input;
        setFrameRate(0);
    }
    public boolean showFramerates()
    {
        return showFramerates;
    }
    public void addSyncMessage(String message)
    {
        text = message + text;
    }
    public void setFrameRate(int choice)
    {
        currentFramerateMultiplier = frameRateMultipliers[choice];
        frameRateModifier = 1000.0/frameRates[choice];
    }
    public void firstDelay() throws InterruptedException
    {
        Date currentTime = new Date();
        long toSleep = (long) Math.floor(startFrame*frameRateModifier + playerInstance.getOffset()
                - (currentTime.getTime() - playerInstance.rootTime)*currentFramerateMultiplier);
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
        long toSleep = (long) Math.floor(endFrame*frameRateModifier + playerInstance.getOffset()
                -  (currentTime.getTime() - playerInstance.rootTime)*currentFramerateMultiplier);
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
        return (long) Math.floor(startFrame*frameRateModifier)-(currentTime.getTime() - playerInstance.rootTime);
    }
    public long convertFramerate(double newFPS, int index)
    {
        //Time taken = now - pauseTime - start time
        //Convert to number of frames that have passed with other framerate, make sure to
        //move forward this many frames starting from the initial subtitle block
        Date currentTime = new Date();
        double numFrames = (currentTime.getTime()-playerInstance.rootTime)/frameRateModifier-pauseTime;
        double oldModifier = frameRateModifier;
        setFrameRate(index);
        double mod = newFPS/(1000/oldModifier);
        //Set the current block to the value in numframes afterwards
        return  Math.round(numFrames*mod);
    }
}
