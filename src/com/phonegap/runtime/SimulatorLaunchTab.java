package com.phonegap.runtime;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

import com.phonegap.PhoneGapPlugin;

public class SimulatorLaunchTab extends AbstractLaunchConfigurationTab implements IPropertyChangeListener
{

	private Composite displayArea;
	private Label projectLabel;
	private Combo projectCombo;
	private Label mainFileLabel;
	private Combo mainFileCombo;
	private Label commandLineLabel;
	private Text commandLineText;

	private Label sdkLabel;
	private Combo sdkCombo;
	private Link sdkPrefLink;

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent)
	{
		displayArea = new Composite(parent, SWT.NONE);
		displayArea.setLayout(new GridLayout(2, false));
		displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		projectLabel = new Label(displayArea, SWT.LEFT);
		projectLabel.setText("Project");
		projectCombo = new Combo(displayArea, SWT.READ_ONLY | SWT.DROP_DOWN);
		projectCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (int i = 0; i < projects.length; i++)
		{
			try
			{
				boolean bb = projects[i].hasNature(PhoneGapPlugin.PHONEGAP_NATURE_ID);
				if (projects[i].hasNature(PhoneGapPlugin.PHONEGAP_NATURE_ID))
				{
					projectCombo.add(projects[i].getName());
				}
			}
			catch (CoreException e)
			{
				// Do nothing
			}
		}

		projectCombo.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectCombo.getText());
				mainFileCombo.removeAll();
				mainFileCombo.clearSelection();
				fillMainFileCombo(project);

				IFile appFile = project.getFile("index.html");
				if (appFile.exists())
				{
					mainFileCombo.setText(appFile.getName());
				} else {
					appFile = project.getFile("index.htm");
					if (appFile.exists())
					{
						mainFileCombo.setText(appFile.getName());
					}
				}

				validateConfiguration();
			}

		});

		mainFileLabel = new Label(displayArea, SWT.LEFT);
		mainFileLabel.setText("Select index file");
		mainFileCombo = new Combo(displayArea, SWT.READ_ONLY | SWT.DROP_DOWN);
		mainFileCombo.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				validateConfiguration();
			}

		});
		mainFileCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		sdkLabel = new Label(displayArea, SWT.LEFT);
		sdkLabel.setText("PhoneGap SDK");
		Composite sdkSelector = new Composite(displayArea, SWT.NONE);
		GridLayout sdkLayout = new GridLayout(2, false);
		sdkLayout.marginWidth = 0;
		sdkSelector.setLayout(sdkLayout);
		sdkSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sdkCombo = new Combo(sdkSelector, SWT.READ_ONLY | SWT.DROP_DOWN);
		sdkCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sdkCombo.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent e)
			{
				validateConfiguration();
			}

		});
		PhoneGapPlugin.getDefault().getPreferenceStore().addPropertyChangeListener(this);
		sdkPrefLink = new Link(sdkSelector, SWT.NONE);
		sdkPrefLink.setText("<a href=\"prefs\">Configure PhoneGap SDKs...</a>"); //$NON-NLS-1$
		sdkPrefLink.setLayoutData(new GridData(SWT.END, SWT.FILL, false, true));
		sdkPrefLink.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(sdkPrefLink.getShell(),
						"com.phonegap.preferences.SDKPreferencePage", //$NON-NLS-1$
						new String[] { "com.phonegap.preferences.SDKPreferencePage" }, null); //$NON-NLS-1$
				dialog.open();
			}

		});

		commandLineLabel = new Label(displayArea, SWT.LEFT);
		commandLineLabel.setText("Command line");
		commandLineText = new Text(displayArea, SWT.BORDER | SWT.SINGLE);
		commandLineText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		commandLineText.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent e)
			{
				validateConfiguration();
			}

		});

		validateConfiguration();
		setControl(displayArea);
	}

	private void fillSDKCombo()
	{
		IPreferenceStore store = PhoneGapPlugin.getDefault().getPreferenceStore();
		String sdk = store.getString(PhoneGapPlugin.RUNTIME_NAME_DEFAULT_PREFERENCE);
		String names = store.getString(PhoneGapPlugin.RUNTIME_NAMES_PREFERENCE);
		String[] sdkPrefNames = names.split(PhoneGapPlugin.PREFERENCE_DELIMITER);
		for (int i = 0; i < sdkPrefNames.length; i++)
		{
			if (sdkPrefNames[i].length() > 0)
			{
				sdkCombo.add(sdkPrefNames[i]);
			}
		}

		if (PhoneGapPlugin.getEmbeddedSDKLocation() != null)
		{
			sdkCombo.add(PhoneGapPlugin.getEmbeddedSDKLocation());
		}

		if (sdkCombo.indexOf(sdk) == -1)
		{
			sdkCombo.setText(PhoneGapPlugin.getDefaultPhoneGapSDKLocation());
		}
		else
		{
			sdkCombo.setText(sdk);
		}
	}

	private void fillMainFileCombo(IProject project)
	{
		try
		{
			IResource[] members = project.members();
			for (int i = 0; i < members.length; i++)
			{
				if (members[i] instanceof IFile)
				{
					String ext = ((IFile) members[i]).getFileExtension();
					if ("html".equals(ext) || "htm".equals(ext)) //$NON-NLS-1$
					{
						mainFileCombo.add(members[i].getName());
					}
				}
			}
			/*
			IFolder jaxer = project.getFolder(JaxerAIRBuilder.JAXER_BIN);
			if (jaxer.exists())
			{
				IFile jaxerApp = jaxer.getFile(DescriptorConstants.APPLICATION_FILE);
				if (jaxerApp.exists())
				{
					mainFileCombo.add(jaxerApp.getProjectRelativePath().toString());
				}
			}
			*/
		}
		catch (CoreException e)
		{
		}
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig)
	{
		return getErrorMessage() == null;
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName()
	{
		return "Main";
	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	public Image getImage()
	{
		return PhoneGapPlugin.getImage("icons/exporter.png"); //$NON-NLS-1$
	}

	private void validateConfiguration()
	{
		String project = projectCombo.getText();
		if (!new Path(project).isValidSegment(project))
		{
			setErrorMessage("Valid project selected");
			setDirty(true);
			updateLaunchConfigurationDialog();
			return;
		}
		IProject _project = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
		if (!_project.exists())
		{
			setErrorMessage("Valid project selected");
			setDirty(true);
			updateLaunchConfigurationDialog();
			return;
		}
		if (mainFileCombo.getItemCount() == 0)
		{
			setErrorMessage("No default file");
			setDirty(true);
			updateLaunchConfigurationDialog();
			return;
		}

		String application = mainFileCombo.getText();
		if (!new Path(application).isValidPath(application) || application.trim().length() < 1)
		{
			setErrorMessage("Valid file selected");
			setDirty(true);
			updateLaunchConfigurationDialog();
			return;
		}
		IFile file = _project.getFile(application);
		if (!file.exists())
		{
			setErrorMessage("Valid file selected");
			setDirty(true);
			updateLaunchConfigurationDialog();
			return;
		}
		if (sdkCombo.getItemCount() == 0)
		{
			setErrorMessage("Must add PhoneGap SDK");
			setDirty(true);
			updateLaunchConfigurationDialog();
			return;
		}
		String sdk = sdkCombo.getText();
		if (sdk.trim().length() == 0)
		{
			setErrorMessage("PhoneGap SDK specified");
			setDirty(true);
			updateLaunchConfigurationDialog();
			return;
		}

		setErrorMessage(null);
		setDirty(true);
		updateLaunchConfigurationDialog();
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration)
	{

		try
		{
			String project = configuration.getAttribute("PROJECT", ""); //$NON-NLS-1$
			String mainFile = configuration.getAttribute("APPLICATION", ""); //$NON-NLS-1$
			String phonegapSDK = configuration.getAttribute("PHONEGAPSDK", ""); //$NON-NLS-1$
			fillSDKCombo();
			if (!phonegapSDK.equals("")) //$NON-NLS-1$
			{
				sdkCombo.setText(phonegapSDK);
			}
			projectCombo.setText(project);

			if (new Path(project).isValidSegment(project))
			{
				IProject _project = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
				mainFileCombo.removeAll();
				fillMainFileCombo(_project);
				mainFileCombo.setText(mainFile);
			}

			String commandLine = configuration.getAttribute("COMMANDLINE", ""); //$NON-NLS-1$
			commandLineText.setText(commandLine);

			validateConfiguration();

		}
		catch (CoreException e)
		{

		}
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration)
	{
		configuration.setAttribute("PROJECT", projectCombo.getText());
		configuration.setAttribute("COMMANDLINE", commandLineText.getText());
		configuration.setAttribute("APPLICATION", mainFileCombo.getText());
		configuration.setAttribute("PHONEGAPSDK", sdkCombo.getText());
	}

	/**
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration)
	{

	}

	/**
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#dispose()
	 */
	public void dispose()
	{
		PhoneGapPlugin.getDefault().getPreferenceStore().removePropertyChangeListener(this);
		super.dispose();
	}

	/**
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event)
	{
		String property = event.getProperty();
		if (PhoneGapPlugin.RUNTIME_NAMES_PREFERENCE.equals(property)
				|| PhoneGapPlugin.RUNTIME_NAME_DEFAULT_PREFERENCE.equals(property))
		{
			sdkCombo.removeAll();
			sdkCombo.clearSelection();
			fillSDKCombo();
		}
	}

}
