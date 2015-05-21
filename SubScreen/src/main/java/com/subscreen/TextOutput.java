package com.subscreen;

/**
 * Created by Nick on 1/1/2015.
 */
public class TextOutput implements Output {
    public void outputText(final String text) {
        System.out.println(text);
    }
    public void clearText()
    {
        System.out.println("");
    }
}
