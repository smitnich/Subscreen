package com.subscreen;
import java.util.Date;


public class TimeBlock implements TextBlock {
	public String text;
	public long startTime;
	public long endTime;
    public long offset = 0;
	public TimeBlock(String input, long s, long e)
	{
		startTime = s;
		endTime = e;
		text = input;
	}
	public TimeBlock(String input, long s)
	{
		startTime = s;
		endTime = s;
		text = input;
	}
	public void firstDelay() throws InterruptedException
	{
		Date currentTime = new Date();
		long toSleep = startTime - (currentTime.getTime() - Main.getOffset() - Main.rootTime);
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
		long toSleep = endTime - (currentTime.getTime() - Main.getOffset() - Main.rootTime);
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
