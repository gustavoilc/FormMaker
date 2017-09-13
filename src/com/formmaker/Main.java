package com.formmaker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

public class Main
{

	private static Properties properties;
	private static Connection conn;
	private static ArrayList<String> tables;
	private static File outPath;
	private static boolean errors = false;
	private static boolean test = true;
	private static String filePath;
	private static boolean labeled;
	private static boolean placeholder;

	public static void main(String args[])
	{
		boolean lookForFile = false;
		properties = new Properties();

		loop:
		for (int i = 0; i < args.length; i++)
		{
			String arg = args[i];

			if (arg.equals("-f"))
			{
			}

			// Normal section
			switch (arg)
			{
				case "-f":
				{
					lookForFile = true;
					if (args.length - 1 >= i)
					{
						System.out.println("Please specify the file path");

						return;
					}

					filePath = args[i + 1];

					break loop;
				}
				case "-c":
				{
					try
					{
						if (args.length - 1 == i)
						{
							break;
						}

						arg = args[++i];

						Class.forName("com.mysql.jdbc.Driver");
						conn = DriverManager.getConnection(arg);
					} catch (ClassNotFoundException | SQLException e)
					{
						System.out.println("Can't estabilish connection, check the connection string");

						if (errors)
						{
							e.printStackTrace();
						}

						return;
					}

					break;
				}
				case "-o":
				{
					if (args.length - 1 == i)
					{
						break;
					}

					if (!createOut(args[++i]))
					{
						return;
					}
				}
				case "-t":
				{
					if (args.length - 1 == i)
					{
						break;
					}

					tables = new ArrayList<>(Arrays.asList(args[++i].split(",")));

					if (tables == null || tables.isEmpty())
					{
						System.out.println("You must specify at least a table");

						return;
					}
				}
			}
		}

		if (test)
		{
			lookForFile = true;
			filePath = "test.properties";
		}

		// File section
		if (lookForFile)
		{
			try (FileInputStream in = new FileInputStream(filePath))
			{
				properties.load(in);

				if (properties.containsKey("connection"))
				{
					try
					{
						Class.forName("com.mysql.jdbc.Driver");
						conn = DriverManager.getConnection(properties.getProperty("connection"));
					} catch (ClassNotFoundException | SQLException e)
					{
						System.out.println("Can't estabilish connection, check the connection string");

						if (errors || test)
						{
							e.printStackTrace();
						}

						return;
					}
				}

				if (properties.containsKey("out"))
				{
					if (!createOut(properties.getProperty("out")))
					{
						return;
					}
					
					outPath = new File(properties.getProperty("out"));
				}
				if (properties.containsKey("tables"))
				{
					tables = new ArrayList<>(Arrays.asList(properties.getProperty("tables").split(",")));

					if (tables == null || tables.isEmpty())
					{
						System.out.println("You must specify at least a table");

						return;
					}
				}
				if (properties.containsKey("errors"))
				{
					errors = properties.getProperty("errors").equals("true");
				}
				if (properties.containsKey("labeled"))
				{
					labeled = properties.getProperty("labeled").equals("true");
				}
				if (properties.containsKey("placeholder"))
				{
					placeholder = properties.getProperty("placeholder").equals("true");
				}
			} catch (IOException e)
			{
				if (errors || test)
				{
					e.printStackTrace();
				}

				System.out.println("The file: \"" + filePath + "\" does not exist");
				return;
			}
		}

		if (conn == null)
		{
			System.out.println("You must specify a correct connection");

			return;
		}

		// If tables is not setted add all tables
		if (tables == null || tables.isEmpty())
		{
			PreparedStatement psTables = null;
			ResultSet rsTables = null;

			try
			{
				psTables = conn.prepareStatement("show tables");
				rsTables = psTables.executeQuery();
				tables = new ArrayList<>();

				while (rsTables.next())
				{
					tables.add(rsTables.getString(1));
				}
			} catch (SQLException e)
			{
				System.out.println("SQL error");

				if (errors || test)
				{
					e.printStackTrace();
				}

				return;
			} finally
			{
				try
				{
					if (rsTables != null)
					{
						rsTables.close();
					}
				} catch (SQLException e)
				{
				}
				try
				{
					if (psTables != null)
					{
						psTables.close();
					}
				} catch (SQLException e)
				{
				}
			}
		}

		// Start read tables
		for (String table : tables)
		{
			ResultSet rs = null;
			File file = new File(outPath, table + ".html");
			
			FileOutputStream fo = null;
			PrintWriter out = null;

			// Create file
			try
			{
				if (!file.exists())
				{
					file.createNewFile();
				}
				fo = new FileOutputStream(file);
				out = new PrintWriter(fo);
			}
			catch (IOException e)
			{
				System.out.println("Can not create \"" + table + ".html\" file");

				if (errors)
				{
					e.printStackTrace();
				}
				
				continue;
			}

			// Anlize table
			try (PreparedStatement ps = conn.prepareStatement("describe " + table);)
			{
				rs = ps.executeQuery();

				printUpperHTML(out);
				
				// Build fields
				while (rs.next())
				{
					// Parse properties
					String name = rs.getString(1);
					String webName = Tools.webWord(name);
					String type = rs.getString(2);
					boolean allowNull = rs.getString(3).equals("YES");
					String key = rs.getString(4);
					String def = rs.getString(5);
					String extra = rs.getString(6);
					boolean unsigned = type.contains("unsigned");
					String lenght = type.substring(type.indexOf("(") + 1, type.indexOf(")"));
					
					if (unsigned)
					{
						type = type.substring(0, type.indexOf("unsigned"));
					}
					if (!lenght.isEmpty())
					{
						type = type.substring(0, type.indexOf("("));
					}
					
					String metadata = "name=\"" + webName + "\"" +
							(placeholder ? " placeholder=\"" + name + "\"" : "") +
							(labeled ? " id=\"" + webName + "\"" : "") +
							(allowNull || key.equals("PRI") ? "" : " required");
					
					if (key.equals("PRI"))
					{
						out.println("\t\t\t<input type=\"hidden\" " + metadata + ">");
					}
					else
					{
						// Set label
						if (labeled)
							out.println("\t\t\t<label for=\"" + webName + "\">" + name + "</label>");
					
						switch (type)
						{
							case "int":
							case "smallint":
							case "tinyint":
							case "float":
							case "decimal":
							{
								out.println("\t\t\t<input type=\"number\" " + metadata + ">");
								
								break;
							}
							case "varchar":
							case "nvarchar":
							case "char":
							case "text":
							{
								if (type.equals("text") || Integer.parseInt(lenght) > 300)
								{
									out.println("\t\t\t<textarea " + metadata + "></textarea>");
								}
								
								break;
							}
						}
					}
				}

				out.println("\t\t\t<button type=\"submit\">Enviar</button>");
				
				printLowerHTML(out);
				
				out.flush();
			}
			catch (SQLException e)
			{
				System.out.println("SQL error");

				if (errors || test)
				{
					e.printStackTrace();
				}

				return;
			} finally
			{
				try
				{
					if (rs != null)
					{
						rs.close();
					}
				} catch (SQLException e)
				{
				}
			}
		}
		
		System.out.println("Done");
	}

	public static boolean createOut(String path)
	{
		path = path == null ? "" : path;

		outPath = new File(path);
		
		if (!outPath.exists())
		{
			System.out.println("\"" + properties.getProperty("out") + "\" does not exist.");
			return false;
		}
		if (!outPath.isDirectory())
		{
			System.out.println("\"" + properties.getProperty("out") + "\" must be a directory.");
			return false;
		}

		return true;
	}

	private static void printUpperHTML(PrintWriter out)
	{
		out.print("<!DOCTYPE html>\n"
				+ "<html lang=\"es\">\n"
				+ "\t<head>\n"
				+ "\t\t<title>Galería de Arte Mexicano</title>\n"
				+ "\t\t<meta charset=\"UTF-8\">\n"
				+ "\t\t<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
				+ "\t\t<link href=\"css/main.css\" rel=\"stylesheet\">\n"
				+ "\t\t<script src=\"js/main.js\"></script>\n"
				+ "\t\t<script src=\"js/altaArtista.js\"></script>\n"
				+ "\t</head>\n"
				+ "\t<body>\n"
				+ "\t\t<form>\n");
	}

	private static void printLowerHTML(PrintWriter out)
	{
		out.print("\t\t</form>\n"
				+ "\t</body>\n"
				+ "</html>");
	}
}
