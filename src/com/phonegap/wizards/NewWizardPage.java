package com.phonegap.wizards;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea;
import org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter;

import java.net.URI;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewWizardPage extends WizardPage {
	private Text containerText;
	private Text fileText;
	private ProjectContentsLocationArea locationArea;
	private String initialProjectFieldValue;
	Text projectNameField;
	Text htmlNameField;
	
	private Listener nameModifyListener = new Listener() {

		public void handleEvent(Event event) {
			setLocationForSelection();
			boolean valid = validatePage();
			setPageComplete(valid);
		}
	};


	/**
	 * Constructor for NewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
		setTitle("Title");
		setDescription("Description");
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NULL);
		initializeDialogUnits(parent);

		//PlatformUI.getWorkbench().getHelpSystem().setHelp(composite,
		//		IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		createProjectNameGroup(composite);
		locationArea = new ProjectContentsLocationArea(getErrorReporter(), composite);
		if(initialProjectFieldValue != null) {
			locationArea.updateProjectName(initialProjectFieldValue);
		}

		setButtonLayoutData(locationArea.getBrowseButton());

		setPageComplete(validatePage());
		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	/**
	 * Get an error reporter for the receiver.
	 * @return IErrorMessageReporter
	 */
	private IErrorMessageReporter getErrorReporter() {
		return new IErrorMessageReporter(){
			/* (non-Javadoc)
			 * @see org.eclipse.ui.internal.ide.dialogs.ProjectContentsLocationArea.IErrorMessageReporter#reportError(java.lang.String)
			 */
			public void reportError(String errorMessage, boolean b) {
				setErrorMessage(errorMessage);
				boolean valid = errorMessage == null;
				if(valid) {
					valid = validatePage();
				}
				setPageComplete(valid);
			}
		};
	}
	
	/**
	 * DONE
	 * Creates the project name specification controls.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private final void createProjectNameGroup(Composite parent) {
		
		// project specification group
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project label
		Label projectLabel = new Label(projectGroup, SWT.NONE);
		projectLabel.setText(IDEWorkbenchMessages.WizardNewProjectCreationPage_nameLabel);
		projectLabel.setFont(parent.getFont());

		// new project name entry field
		projectNameField = new Text(projectGroup, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = 255;
		projectNameField.setLayoutData(data);
		projectNameField.setFont(parent.getFont());

		// Set the initial value first before listener
		// to avoid handling an event during the creation.
		if (initialProjectFieldValue != null) {
			projectNameField.setText(initialProjectFieldValue);
		}
		projectNameField.addListener(SWT.Modify, nameModifyListener);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public String getFileName() {
		return fileText.getText();
	}

	/**
	 * Returns whether this page's controls currently all contain valid values.
	 * 
	 * @return <code>true</code> if all controls are valid, and
	 *         <code>false</code> if at least one is invalid
	 */
	protected boolean validatePage() {

//		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		String projectFieldContents = getProjectNameFieldValue();
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			setErrorMessage(null);
			setMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectNameEmpty);
			return false;
		}

		IStatus nameStatus = workspace.validateName(projectFieldContents,
				IResource.PROJECT);
		if (!nameStatus.isOK()) {
			setErrorMessage(nameStatus.getMessage());
			return false;
		}

		IProject handle = getProjectHandle();
		if (handle.exists()) {
			setErrorMessage(IDEWorkbenchMessages.WizardNewProjectCreationPage_projectExistsMessage);
			return false;
		}

		/*
		 * If not using the default value validate the location.
		 */
		if (!useDefaults()) {
			IStatus locationStatus = workspace.validateProjectLocationURI(
					handle, locationArea.getProjectLocationURI());
			if (!locationStatus.isOK()) {
				setErrorMessage(locationStatus.getMessage());
				return false;
			}
		}

		setErrorMessage(null);
		setMessage(null);
		return true;
	}
	
	/**
	 * Returns the useDefaults.
	 * 
	 * @return boolean
	 */
	public boolean useDefaults() {
		return locationArea.isDefault();
	}

	/**
	 * Returns the current project location path as entered by the user, or its
	 * anticipated initial value. Note that if the default has been returned the
	 * path in a project description used to create a project should not be set.
	 * 
	 * @return the project location path or its anticipated initial value.
	 */
	public IPath getLocationPath() {
		return new Path(locationArea.getProjectLocation());
	}

	/**
	 * /** Returns the current project location URI as entered by the user, or
	 * <code>null</code> if a valid project location has not been entered.
	 * 
	 * @return the project location URI, or <code>null</code>
	 * @since 3.2
	 */
	public URI getLocationURI() {
		return locationArea.getProjectLocationURI();
	}

	/**
	 * Creates a project resource handle for the current project name field
	 * value.
	 * <p>
	 * This method does not create the project resource; this is the
	 * responsibility of <code>IProject::create</code> invoked by the new
	 * project resource wizard.
	 * </p>
	 * 
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
	}

	/**
	 * Returns the current project name as entered by the user, or its
	 * anticipated initial value.
	 * 
	 * @return the project name, its anticipated initial value, or
	 *         <code>null</code> if no project name is known
	 */
	public String getProjectName() {
		if (projectNameField == null) {
			return initialProjectFieldValue;
		}

		return getProjectNameFieldValue();
	}

	/**
	 * Returns the value of the project name field with leading and trailing
	 * spaces removed.
	 * 
	 * @return the project name in the field
	 */
	private String getProjectNameFieldValue() {
		if (projectNameField == null) {
			return ""; //$NON-NLS-1$
		}

		return projectNameField.getText().trim();
	}
	
	/**
	 * Gets the html file name
	 * 
	 * @return - html file name
	 */
	public String getHTMLFileName() {
		return "index.html";//htmlNameField.getText();
	}

	
	/**
	 * Set the location to the default location if we are set to useDefaults.
	 */
	void setLocationForSelection() {
		locationArea.updateProjectName(getProjectNameFieldValue());
	}


	/**
	 * Sets the initial project name that this page will use when created. The
	 * name is ignored if the createControl(Composite) method has already been
	 * called. Leading and trailing spaces in the name are ignored. Providing
	 * the name of an existing project will not necessarily cause the wizard to
	 * warn the user. Callers of this method should first check if the project
	 * name passed already exists in the workspace.
	 * 
	 * @param name
	 *            initial project name for this page
	 * @see IWorkspace#validateName(String, int)
	 */
	public void setInitialProjectName(String name) {
		if (name == null) {
			initialProjectFieldValue = null;
		} else {
			initialProjectFieldValue = name.trim();
			if (locationArea != null) {
				locationArea.updateProjectName(name.trim());
			}
		}
	}

}