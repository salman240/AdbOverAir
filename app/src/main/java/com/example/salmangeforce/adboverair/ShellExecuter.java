package com.example.salmangeforce.adboverair;

import android.content.Context;
import android.widget.Toast;

import java.io.File;

public class ShellExecuter {

	ShellExecuter() {}

	public void Executer(String command, Context context) {

		try
		{
			Runtime.getRuntime().exec(command);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
		}

	}


	public static String execCmd(String command) throws java.io.IOException {
		java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(command).getInputStream()).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}


}//class ends.