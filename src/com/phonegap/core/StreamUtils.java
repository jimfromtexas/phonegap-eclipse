package com.phonegap.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

/**
 * @author Spike Washburn
 */
public class StreamUtils
{
	/**
	 * Protected constructor for utility class.
	 *
	 */
	protected StreamUtils()
	{
		
	}
	
	/**
	 * readContent
	 * 
	 * @param stream
	 * @param charset
	 * @return String
	 * @throws IOException
	 */
	public static String readContent(InputStream stream, String charset) throws IOException
	{
		if (stream == null)
		{
			return null;
		}

		InputStreamReader inputReader;
		if (charset != null)
		{
			inputReader = new InputStreamReader(stream, charset);
		}
		else
		{
			inputReader = new InputStreamReader(stream);
		}

		BufferedReader reader = new BufferedReader(inputReader);

		StringWriter sw = new StringWriter();

		try
		{
			char[] chars = new char[1024];
			int numRead = reader.read(chars);

			while (numRead != -1)
			{
				sw.write(chars, 0, numRead);
				numRead = reader.read(chars);
			}
		}
		finally
		{
			reader.close();
		}

		String contents = sw.toString();

		return contents;
	}
}
