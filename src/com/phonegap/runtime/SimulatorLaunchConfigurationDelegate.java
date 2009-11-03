/**
 * Copyright (c) 2009 Nitobi
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Aptana Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 *
 * Redistribution, except as permitted by the above license, is prohibited.
 * Any modifications to this file must keep this entire header intact.
 */

package com.phonegap.runtime;

import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.progress.UIJob;

import com.phonegap.PhoneGapPlugin;
import com.phonegap.core.StreamUtils;
import com.phonegap.core.StringUtils;

/**
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public class SimulatorLaunchConfigurationDelegate extends LaunchConfigurationDelegate
{

	/**
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration,
	 *      java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
			throws CoreException
	{
		String projectName = configuration.getAttribute("PROJECT", ""); //$NON-NLS-1$
		String applicationFile = configuration.getAttribute("APPLICATION", ""); //$NON-NLS-1$
		String commandArgs = configuration.getAttribute("COMMANDLINE", ""); //$NON-NLS-1$
		String sdk = configuration.getAttribute("PHONEGAPSDK", ""); //$NON-NLS-1$

		if (projectName.length() == 0 || applicationFile.length() == 0)
		{
			monitor.setCanceled(true);
			return;
		}
		IProject _project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IFile file = _project.getFile(applicationFile);

		String sdkLocation = null;
		if (!sdk.equals("")) //$NON-NLS-1$
		{
			IPreferenceStore store = PhoneGapPlugin.getDefault().getPreferenceStore();
			String names = store.getString(PhoneGapPlugin.RUNTIME_NAMES_PREFERENCE);
			String locations = store.getString(PhoneGapPlugin.RUNTIME_LOCATIONS_PREFERENCE);
			String[] sdkPrefNames = names.split(PhoneGapPlugin.PREFERENCE_DELIMITER);
			String[] sdkPrefLocations = locations.split(PhoneGapPlugin.PREFERENCE_DELIMITER);
			for (int i = 0; i < sdkPrefNames.length; i++)
			{
				if (sdk.equals(sdkPrefNames[i]))
				{
					if (i < sdkPrefLocations.length)
					{
						sdkLocation = sdkPrefLocations[i];
						break;
					}
				}
			}
		}
		if ( sdkLocation == null ){
			sdkLocation = PhoneGapPlugin.getDefaultPhoneGapSDKLocation();
		}

//		if (sdkLocation == null)
//		{
//			// Fail back to default
//			sdkLocation = store.getString(PhoneGapPlugin.RUNTIME_LOCATION_DEFAULT_PREFERENCE);
//		}
//		if (sdkLocation == null || sdkLocation.length() == 0)
//		{
//			sdkLocation = PhoneGapPlugin.getDefaultSDKLocation();
//		}

		/* If you can't find an sdkLocaiton then don't run */
		if (sdkLocation == null || sdkLocation.length() == 0){
			Job job = new UIJob("No PhoneGap SDK Found Message") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					//CoreUIUtils.logAndDialogError(CoreUIUtils.getActiveShell(), PhoneGapPlugin.getDefault(), "No PhoneGap SDK Found", "To launch your PhoneGap application, you must set your default PhoneGap SDK.");
					return Status.OK_STATUS;
				}

			};
			job.setPriority(Job.INTERACTIVE);
			job.schedule();
			return;
		}
		
		IPath sdkPath = new Path(sdkLocation);
		String applicationContent = file.getLocation().makeAbsolute().toOSString();

		Map<String, String> attrs = new HashMap<String, String>();

		Process process = null;
		String executable = null;
		if (org.eclipse.debug.core.ILaunchManager.DEBUG_MODE.equals(mode)) {	
			SimulatorLauncher launcher = new SimulatorLauncher(sdkPath, applicationContent, commandArgs);
			attrs.put(IProcess.ATTR_CMDLINE, launcher.getCommand());
			executable = launcher.getExecutable();
			process = launcher.execute();
		} else if (org.eclipse.debug.core.ILaunchManager.RUN_MODE.equals(mode)) {
			SimulatorLauncher launcher = new SimulatorLauncher(sdkPath, applicationContent, commandArgs);
			attrs.put(IProcess.ATTR_CMDLINE, launcher.getCommand());
			executable = launcher.getExecutable();
			process = launcher.execute();
		}

		if (process == null)
		{
			UIJob errorJob = new UIJob("Error launching simulator.")
			{

				public IStatus runInUIThread(IProgressMonitor monitor)
				{
					MessageDialog.openError(Display.getDefault().getActiveShell(),
							"Error launching simulator.",
							"Check PhoneGap SDK location");
					PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(this.getDisplay()
							.getActiveShell(), "com.phonegap.preferences.SDKPreferencePage", //$NON-NLS-1$
							new String[] { "com.phonegap.preferences.SDKPreferencePage" }, null); //$NON-NLS-1$
					dialog.open();
					return Status.OK_STATUS;
				}

			};
			errorJob.schedule();
			monitor.setCanceled(true);
			return;
		}

		if (org.eclipse.debug.core.ILaunchManager.DEBUG_MODE.equals(mode)) {
			//c:\bin\phone-gap-sdk\adl c:\bin\phone-gap-sdk\application.xml c:\bin\phone-gap-sdk\bin-debug
			DebugPlugin.newProcess(launch, process, executable, attrs);
		}
		
		/*
		if (org.eclipse.debug.core.ILaunchManager.RUN_MODE == mode) {
			attrs.put(IProcess.ATTR_PROCESS_TYPE, "com.aptana.ide.apollo.AdlProcess"); //$NON-NLS-1$
			DebugPlugin.newProcess(launch, process, executable, attrs);
		} else if (org.eclipse.debug.core.ILaunchManager.DEBUG_MODE == mode) {
			JSDebugTarget debugTarget = null;
			DebugConnection controller = FDBDebugConnection.createConnection(debugHost);
			IProcess debugProcess = new JSDebugProcess(launch, process, false, executable, attrs);
			try {
				debugTarget = new JSDebugTarget(launch, "AIR Debugger", debugProcess, null, null, controller, true);
			} catch(CoreException e) {
				if ( debugTarget != null ) {
					debugTarget.terminate();
				} else {
					try {
						controller.dispose();
					} catch (IOException ignore) {
					}
				}
				if (debugProcess != null) {
					debugProcess.terminate();
				}
				throw e;
			}
		}
*/
	}

}
