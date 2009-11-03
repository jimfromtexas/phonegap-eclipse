package com.phonegap.runtime;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.CommonTab;

//import com.aptana.ide.debug.internal.ui.launchConfigurations.DebugSettingsTab;

public class SimulatorLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup
{

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.ILaunchConfigurationDialog,
	 *      java.lang.String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode)
	{
		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new SimulatorLaunchTab(),
				new CommonTab()
				//new DebugSettingsTab()
			};
		setTabs(tabs);
	}	

}
