package com.subscreen;
import java.util.Date;


public class TimeBlock implements TextBlock {
	public String text;
	public long startTime;
	public long endTime;
    public long offset = 0;
    SubtitlePlayer playerInstance = null;
	public TimeBlock(String input, long s, long e, SubtitlePlayer tmpPlayer)
	{
        playerInstance = tmpPlayer;
		startTime = s;
		endTime = e;
		text = input;
	}
	public TimeBlock(String input, long s, SubtitlePlayer tmpPlayer)
	{
        playerInstance = tmpPlayer;
        startTime = s;
		endTime = s;
		text = input;
	}
	public void firstDelay() throws InterruptedException
	{
		Date currentTime = new Date();
		long toSleep = startTime - (currentTime.getTime() - playerInstance.getOffset() - playerInstance.rootTime);
		if (toSleep <= 0)
			return;
		try {
			Thread.sleep(toSleep);
		} catch (InterruptedException e) {
			throw e;
		}
	}
	public void secondDelay() throws InterruptedException
	{
		Date currentTime = new Date();
		long toSleep = endTime - (currentTime.getTime() - playerInstance.getOffset() - playerInstance.rootTime);
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
    public long getStartTime()
    {
        return startTime;
    }
}
