package com.subscreen;
import java.util.Date;


public class TextBlock {
	public String text;
	public long startTime;
	public long endTime;
	public static long rootTime;
	public Output outputTo;
	public TextBlock(String input, long s, long e)
	{
		startTime = s;
		endTime = e;
		text = input;
	}
	TextBlock(String input, long s)
	{
		startTime = s;
		endTime = s;
		text = input;
	}
	void FirstDelay()
	{
		Date currentTime = new Date();
		long toSleep = startTime - (currentTime.getTime() - rootTime);
		if (toSleep <= 0)
			return;
		try {
			Thread.sleep(toSleep);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	void secondDelay()
	{
		Date currentTime = new Date();
		long toSleep = endTime - (currentTime.getTime() - rootTime);
		if (toSleep <= 0)
			return;
		try {
			Thread.sleep(toSleep);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	void getText(Output _outputTo)
	{
		outputTo = _outputTo;
		outputTo.outputText(text);
	}
}
