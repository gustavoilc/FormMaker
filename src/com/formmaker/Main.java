package com.formmaker;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Main
{

	private static Properties properties;
	private static Connection conn;
	private static String database;
	private static ArrayList<String> tables;
	private static String outPath;

	public static void main(String args[])
	{
		boolean lookForFile = false;
		properties = new Properties();

		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];
			
			// File section
			if (lookForFile)
			{
				try (FileInputStream in = new FileInputStream(arg))
				{
					properties.load(in);

					if (properties.containsKey("connection"))
					{
						try
						{
							Class.forName("com.mysql.jdbc.Driver");
							conn = DriverManager.getConnection(properties.getProperty("connection"));
						} catch (ClassNotFoundException | SQLException ex)
						{
							System.out.println("Can't estabilish connection, check the connection string");
						}
					}
					
					if (properties.contains("database"))
					{
						database = properties.getProperty("database");
					}
					if (properties.contains("out"))
					{
						database = properties.getProperty("out");
					}
					if (properties.contains("tables"))
					{
						tables = new ArrayList<>(Arrays.asList(properties.getProperty("tables").split(",")));
					}
				} catch (IOException e)
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
			switch (arg)
			{
				case "-c":
				{
					try
					{
						if (args.length - 1 == i)
							break;
						
						arg = args[++i];
						
						Class.forName("com.mysql.jdbc.Driver");
						conn = DriverManager.getConnection(arg);
					} catch (ClassNotFoundException | SQLException ex)
					{
						System.out.println("Can't estabilish connection, check the connection string");
					}
					
					break;
				}
				case "-d":
				{
					if (args.length - 1 == i)
						break;

					database = args[++i];
				}
				case "-o":
				{
					if (args.length - 1 == i)
						break;

					outPath = args[++i];
				}
				case "-t":
				{
					if (args.length - 1 == i)
						break;

					tables = new ArrayList<>(Arrays.asList(args[++i].split(",")));
				}
			}
		}
		
		if (conn == null)
		{
			System.out.println("Can't estabilish connection, check the connection string");
			
			return;
		}
	}
}
