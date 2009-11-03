
package com.phonegap;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class PhoneGapPlugin extends AbstractUIPlugin
{

	/**
	 * The plug-in ID
	 */
	public static final String PLUGIN_ID = "com.phonegap"; //$NON-NLS-1$

	/**
	 * Runtime names preference
	 */
	public static final String RUNTIME_NAMES_PREFERENCE = PLUGIN_ID + ".runtime.names_1.0"; //$NON-NLS-1$

	/**
	 * Runtime locations preference
	 */
	public static final String RUNTIME_LOCATIONS_PREFERENCE = PLUGIN_ID + ".runtime.locations_1.0"; //$NON-NLS-1$

	/**
	 * Default runtime location preference
	 */
	public static final String RUNTIME_LOCATION_DEFAULT_PREFERENCE = PLUGIN_ID + ".runtime.location.default_1.0"; //$NON-NLS-1$

	/**
	 * Default name location preference
	 */
	public static final String RUNTIME_NAME_DEFAULT_PREFERENCE = PLUGIN_ID + ".runtime.name.default_1.0"; //$NON-NLS-1$

	/**
	 * PhoneGap nature id
	 */
	public static final String PHONEGAP_NATURE_ID = "com.phonegap.phonegapnature"; //$NON-NLS-1$

	/**
	 * Preference delimiter
	 */
	public static final String PREFERENCE_DELIMITER = ";"; //$NON-NLS-1$
	

	/**
	 * JSLanguage Environment Name
	 */
	public static final String JS_LANG_ENV_ADOBE_AIR = "Adobe AIR";  //$NON-NLS-1$

	// The shared instance
	private static PhoneGapPlugin plugin;
	private static Hashtable<String, Image> images = new Hashtable<String, Image>();

	private static String embeddedSDKLocation = "";
	private static boolean checked = false;

	private static String runtimeLocation = null;
	private static boolean rtChecked = false;

	/**
	 * The constructor
	 */
	public PhoneGapPlugin()
	{
		plugin = this;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path.
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path)
	{
		return AbstractUIPlugin.imageDescriptorFromPlugin(PLUGIN_ID, path); //$NON-NLS-1$
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception
	{
		super.start(context);

		getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener()
		{

			public void propertyChange(PropertyChangeEvent event)
			{
				savePluginPreferences();
			}

		});		
	}

	/**
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception
	{
		plugin = null;
		super.stop(context);
	}

	/**
	 * getImage
	 * 
	 * @param path
	 * @return Image
	 */
	public static Image getImage(String path)
	{
		if (images.get(path) == null)
		{
			ImageDescriptor id = getImageDescriptor(path);

			if (id == null)
			{
				return null;
			}

			Image i = id.createImage();

			images.put(path, i);

			return i;
		}
		else
		{
			return (Image) images.get(path);
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static PhoneGapPlugin getDefault()
	{
		return plugin;
	}

	/**
	 * Gets the default sdk name
	 * 
	 * @return - default name
	 */
	public static String getEmbeddedSDKName()
	{
		return "Default PhoneGap SDK"; //$NON-NLS-1$
	}

	/**
	 * Gets the runtime location to install
	 * 
	 * @return - string path of runtime
	 */
	public static String getRuntimeLocation()
	{
		if (!rtChecked)
		{
			rtChecked = true;
			IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(
					"com.phonegap.runtime");
			if (configs != null && configs.length > 0)
			{
				String bundleName = configs[0].getContributor().getName();
				String path = configs[0].getAttribute("path");
				if (bundleName != null && path != null)
				{
					Bundle bundle = Platform.getBundle(bundleName);
					if (bundle != null)
					{
						URL location = bundle.getEntry(path);
						if (location != null)
						{
							try
							{
								location = FileLocator.toFileURL(location);
								if (location != null)
								{
									runtimeLocation = location.getPath();
								}
							}
							catch (IOException e)
							{
							}
						}
					}
				}
			}
		}
		return runtimeLocation;
	}

	/**
	 * Gets the default sdk password
	 * 
	 * @return - default sdk
	 */
	public static String getEmbeddedSDKLocation()
	{
		if (!checked)
		{
			checked = true;
			IConfigurationElement[] configs = Platform.getExtensionRegistry().getConfigurationElementsFor(
					"com.phonegap.sdk");
			if (configs != null && configs.length > 0)
			{
				String bundleName = configs[0].getContributor().getName();
				String path = configs[0].getAttribute("path");
				if (bundleName != null && path != null)
				{
					Bundle bundle = Platform.getBundle(bundleName);
					if (bundle != null)
					{
						URL location = bundle.getEntry(path);
						if (location != null)
						{
							try
							{
								location = FileLocator.toFileURL(location);
								if (location != null)
								{
									embeddedSDKLocation = location.getPath();
								}
							}
							catch (IOException e)
							{
								//IdeLog.logError(PhoneGapPlugin.getDefault(), "Unable to locate Embedded SDK Location", e);
							}
						}
					}
				}
			}
		}
		return embeddedSDKLocation;
	}
	
	public static String getDefaultPhoneGapSDKName() {
		final IPreferenceStore store = PhoneGapPlugin.getDefault().getPreferenceStore();
		String sdkName = store.getString(PhoneGapPlugin.RUNTIME_NAME_DEFAULT_PREFERENCE);
		if ( sdkName == null || sdkName.length() == 0){
			sdkName = PhoneGapPlugin.getEmbeddedSDKName();
		}
		return sdkName;
	}
	
	public static String getDefaultPhoneGapSDKLocation() {
		final IPreferenceStore store = PhoneGapPlugin.getDefault().getPreferenceStore();
		String sdkLocation = store.getString(PhoneGapPlugin.RUNTIME_LOCATION_DEFAULT_PREFERENCE);
		if (sdkLocation == null || sdkLocation.length() == 0)
		{
			sdkLocation = PhoneGapPlugin.getEmbeddedSDKLocation();
		} 
		if ( sdkLocation != null )
		{
			File sdkDir = null;
			sdkDir = new File(sdkLocation);
			if ( sdkDir != null ){
				if ( sdkDir.exists() ) {
					return sdkDir.getPath();
				}
				else {
					final String myloc = sdkLocation;
					Job job = new UIJob("PhoneGap SDK Does Not Exist"){

						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {

							//CoreUIUtils.logAndDialogError(CoreUIUtils.getActiveShell(), PhoneGapPlugin.getDefault(), "AIR SDK Does Not Exist", "Your Air SDK no longer exists in this location: \n\n\t" + myloc + "\n\nPlease update your Default SDK before continuing AIR developement.");

							return null;
						}
						
					};
					job.setPriority(Job.INTERACTIVE);
					job.schedule();
				}
			}
		}
		return sdkLocation;
	}
}