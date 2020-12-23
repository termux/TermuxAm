package com.termux.termuxam;

import android.content.Intent;

import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;

public class IntentCmdTest {
    @Test
    public void testExtraStringArray() throws URISyntaxException {
        ShellCommand shellCommand = new ShellCommand();
        shellCommand.init(new String[] { "--esa", "extra-name", "aaa,bbb\\,ccc,ddd" }, 0);

        Intent intent = IntentCmd.parseCommandArgs(shellCommand, null);
        String[] resultExtra = intent.getStringArrayExtra("extra-name");
        Assert.assertArrayEquals(new String[] { "aaa", "bbb,ccc", "ddd" }, resultExtra);
    }

    @Test
    public void testExtraStringArrayList() throws URISyntaxException {
        ShellCommand shellCommand = new ShellCommand();
        shellCommand.init(new String[] { "--esal", "extra-name", "aaa,bbb\\,ccc,ddd" }, 0);

        Intent intent = IntentCmd.parseCommandArgs(shellCommand, null);
        ArrayList<String> resultExtra = intent.getStringArrayListExtra("extra-name");
        Assert.assertArrayEquals(new Object[] { "aaa", "bbb,ccc", "ddd" }, resultExtra.toArray());
    }
}
