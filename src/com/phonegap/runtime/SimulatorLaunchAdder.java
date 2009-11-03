package com.phonegap.runtime;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchConfigurationManager;
import org.eclipse.debug.internal.ui.launchConfigurations.LaunchHistory;
import org.eclipse.debug.ui.IDebugUIConstants;

public class SimulatorLaunchAdder
{

	private static final class Launcher implements ILaunch {
		private final ILaunchConfiguration config;

		private Launcher(ILaunchConfiguration config) {
			this.config = config;
		}

		public ILaunchConfiguration getLaunchConfiguration()
		{
			return config;
		}

		/* All other methods are stubs */
		public Object[] getChildren()
		{
			return null;
		}

		public IDebugTarget getDebugTarget()
		{
			return null;
		}

		public IProcess[] getProcesses()
		{
			return null;
		}

		public IDebugTarget[] getDebugTargets()
		{
			return null;
		}

		public void addDebugTarget(IDebugTarget target)
		{
		}

		public void removeDebugTarget(IDebugTarget target)
		{
		}

		public void addProcess(IProcess process)
		{
		}

		public void removeProcess(IProcess process)
		{
		}

		public ISourceLocator getSourceLocator()
		{
			return null;
		}

		public void setSourceLocator(ISourceLocator sourceLocator)
		{
		}

		public String getLaunchMode()
		{
			return null;
		}

		public void setAttribute(String key, String value)
		{
		}

		public String getAttribute(String key)
		{
			return null;
		}

		public boolean hasChildren()
		{
			return false;
		}

		public boolean canTerminate()
		{
			return false;
		}

		public boolean isTerminated()
		{
			return false;
		}

		public void terminate() throws DebugException
		{
		}

		public Object getAdapter(Class adapter)
		{
			return null;
		}
	}

	/**
	 * Adds an AIR launch
	 * 
	 * @param projectName
	 * @param configName
	 * @param appFile
	 */
	public static void addSimulatorLaunch(String projectName, String configName, String appFile)
	{
		try
		{
			
			LaunchConfigurationManager manager = DebugUIPlugin.getDefault().getLaunchConfigurationManager();
			ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
			ILaunchConfigurationType airType = launchManager
					.getLaunchConfigurationType("com.phonegap.debug.SimulatorLaunchConfigurationType"); //$NON-NLS-1$
			if (airType != null)
			{
				ILaunchConfigurationWorkingCopy wc = airType.newInstance(null, DebugPlugin.getDefault()
						.getLaunchManager().generateUniqueLaunchConfigurationNameFrom(configName)); //$NON-NLS-1$
				wc.setAttribute("APPLICATION", appFile);
				wc.setAttribute("PROJECT", projectName);
				final ILaunchConfiguration config = wc.doSave();
				LaunchHistory history = manager.getLaunchHistory(IDebugUIConstants.ID_RUN_LAUNCH_GROUP);
				LaunchHistory history_debug = manager.getLaunchHistory(IDebugUIConstants.ID_DEBUG_LAUNCH_GROUP);
				Launcher launcher = new Launcher(config);
				// Launch history hack
				history.launchAdded(launcher);
				history_debug.launchAdded(launcher);
			}
		}
		catch (CoreException ce)
		{
			//IdeLog.logError(ApolloPlugin.getDefault(), "Error adding AIR launch configuration");
		}
	}
}
