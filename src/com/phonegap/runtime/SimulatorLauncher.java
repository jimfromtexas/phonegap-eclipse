
package com.phonegap.runtime;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

import com.phonegap.core.StreamUtils;
import com.phonegap.core.StringUtils;

public class SimulatorLauncher
{
	private List<String> commandList;

	private String command;
	
	private String simulatorExecutable;

	/**
	 * Creates a new simulator launcher
	 * 
	 * @param sdkPath
	 * @param applicationFile
	 * @param args
	 */
	public SimulatorLauncher(IPath sdkPath, String applicationFile, String args)
	{
		commandList = new ArrayList<String>();
		command = ""; //$NON-NLS-1$

		String applicationXml = sdkPath.append("simulator\\PhoneGapSimulator-app.xml").makeAbsolute().toOSString();
		String simulatorBin = sdkPath.append("simulator").makeAbsolute().toOSString();

		IPath path = getSimulatorExecutable(sdkPath);
		if (path != null) {
			simulatorExecutable = path.toOSString();
		}
		if (simulatorExecutable != null)
		{
			commandList.add(simulatorExecutable);
			commandList.add(applicationXml);
			commandList.add(simulatorBin);
			commandList.add("--");
			commandList.add("url="+applicationFile);
		}

		if (args.length() > 0)
		{
			commandList.add(args);
		}

		for (int i = 0; i < commandList.size(); i++)
		{
			command += commandList.get(i) + " "; //$NON-NLS-1$
		}
/*
		try {
			String appContent = StreamUtils.readContent(
				this.getClass().getResourceAsStream("/com/phonegap/resources/application.xml"), //$NON-NLS-1$
				null
			);

			appContent = StringUtils.replace(appContent, "<content></content>", "<content>"+applicationFile+"</content>");

			BufferedWriter out = new BufferedWriter(new FileWriter(applicationXml));
			out.write(appContent);
			out.close();
		} catch(Exception ex) {
		}
*/
	}

	/**
	 * Executes the simulator command.
	 * 
	 * @return - process
	 */
	public Process execute()
	{
		Process process = null;
		if (!commandList.isEmpty())
		{
			Runtime rt = Runtime.getRuntime();
			try
			{
//				IdeLog.logInfo(ApolloPlugin.getDefault(), "ADL Execute: " + commandList);
//				process = rt.exec((String[]) commandList.toArray(new String[0]));
				ProcessBuilder builder = new ProcessBuilder(commandList);
				builder.redirectErrorStream(true);
				process = builder.start();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		return process;
	}

	/**
	 * @return - command executed
	 */
	public String getCommand()
	{
		return command;
	}
	
	/**
	 * 
	 * @return simulator executable
	 */
	public String getExecutable() {
		return simulatorExecutable;
	}

	/**
	 * Get simulator executable path for given SDK path
	 * @param sdkPath
	 * @return
	 */
	public static IPath getSimulatorExecutable(IPath sdkPath) {
		if (Platform.OS_WIN32.equals(Platform.getOS()))
		{
			return sdkPath.append("air\\bin\\adl.exe").makeAbsolute();
		}
		else if (Platform.OS_MACOSX.equals(Platform.getOS()))
		{
			return sdkPath.append("air\\bin\\adl").makeAbsolute();
		}
		else if (Platform.OS_LINUX.equals(Platform.getOS()))
		{
			return sdkPath.append("air\\bin\\adl").makeAbsolute();
		}
		return null;
	}
}
