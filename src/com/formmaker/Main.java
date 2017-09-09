package com.formmaker;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Properties;

public class Main {

	private Properties properties;
	private Connection conn;
	private String database;
	private ArrayList<String> tables;
	private String outPath;

	public static void main(String args[])
	{
		boolean lookForFile = false;
		properties = new Properties();

		int cont = 0;

		for (String arg : args)
		{
			// File section
			if (lookForFile)
			{
				try (FileInputStream in = new FileInputStream(arg))
				{
					properties.load(in);
				}
				catch (FileNotFoundException e)
				{
					System.out.println("The file: \"" + arg + "\" does not exist");
					return;
				}
			}

			if (arg.equals("-f"))
			{
				lookForFile = true;

				continue;
			}

			// Normal section
			

			cont++;
		}
	}
}
