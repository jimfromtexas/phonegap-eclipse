package com.phonegap.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.operation.*;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import java.io.*;
import org.eclipse.ui.*;
import org.eclipse.ui.ide.IDE;

import com.phonegap.PhoneGapPlugin;
import com.phonegap.core.*;
import com.phonegap.runtime.*;

public class NewProjectWizard extends Wizard implements IExecutableExtension, INewWizard
{
	private NewWizardPage page;

	private ISelection selection;
	private IProject newProject;
	private IWorkbench workbench;


	/**
	 * Constructor for SampleNewWizard.
	 */
	public NewProjectWizard() {
		super();
		setNeedsProgressMonitor(true);
		super.setWindowTitle("PhoneGap Project Wizard");
		super.setHelpAvailable(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		
		/*
		 *
		super.addPages();

		page = new ApolloNewProjectCreationPage("projectPage"); //$NON-NLS-1$
		page.setDescription(Messages.ApolloProjectCreationWizard_SpecifyProject);
		page.setTitle(Messages.ApolloProjectCreationWizard_CreateAirProject);
		page.setImageDescriptor(PhoneGapPlugin.getImageDescriptor("icons/air_folder_48x48.png")); //$NON-NLS-1$
		this.addPage(page);
		 */
		
		page = new NewWizardPage("");
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		
		final IProject p = createNewProject();

		try
		{
			WorkspaceModifyOperation op = new WorkspaceModifyOperation()
			{
				protected void execute(IProgressMonitor monitor)
				{
					createProject(p, monitor != null ? monitor : new NullProgressMonitor());
					
					//doFinish(containerName, fileName, monitor);

					// make sure the AIR API is selected by default
					//JSLanguageEnvironment.enableEnvironment(PhoneGapPlugin.JS_LANG_ENV_ADOBE_AIR);
					//JSLanguageEnvironment.resetEnvironment();
				}
			};

			getContainer().run(false, true, op);
		} catch (InvocationTargetException x) {
			return false;
		} catch (InterruptedException x) {
			return false;
		}

		return true;
	}
	
	/**
	 * createNewProject
	 *
	 * @return
	 */
	private IProject createNewProject()
	{
		if (newProject != null)
		{
			return newProject;
		}

		// get a project handle
		final IProject newProjectHandle = page.getProjectHandle();
		// get a project descriptor
		IPath newPath = null;
		if (!page.useDefaults())
		{
			newPath = page.getLocationPath();
		}

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProjectDescription description = workspace.newProjectDescription(newProjectHandle.getName());
		description.setLocation(newPath);
		description.setNatureIds(new String[] { PhoneGapPlugin.PHONEGAP_NATURE_ID });
		//ICommand command = description.newCommand();
		//command.setBuilderName(JaxerAIRBuilder.BUILDER);
		//description.setBuildSpec(new ICommand[] { command });

		// create the new project operation
		WorkspaceModifyOperation op = new WorkspaceModifyOperation()
		{
			protected void execute(IProgressMonitor monitor) throws CoreException
			{
				if (monitor != null)
				{
					//Messages.ApolloProjectCreationWizard_CreatingAIRProject
					monitor.beginTask("creating stuff ...", 1);
				}
				createProject(description, newProjectHandle, monitor);
				if (monitor != null)
				{
					monitor.worked(1);
					monitor.done();
				}

			}
		};

		// run the new project creation operation
		try
		{
			getContainer().run(true, true, op);
		}
		catch (InterruptedException e)
		{
			return null;
		}
		catch (InvocationTargetException e)
		{
			// ie.- one of the steps resulted in a core exception
			Throwable t = e.getTargetException();
			if (t instanceof CoreException)
			{
				if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS)
				{
//					MessageDialog.openError(getShell(), EclipseUIUtils.ResourceMessages_NewProject_errorMessage, NLS
//							.bind(EclipseUIUtils.ResourceMessages_NewProject_caseVariantExistsError, newProjectHandle
//									.getName()));
				}
				else
				{
//					ErrorDialog.openError(getShell(), EclipseUIUtils.ResourceMessages_NewProject_errorMessage, null, // no
							// special
							// message
//							((CoreException) t).getStatus());
				}
			}
			else
			{
				// CoreExceptions are handled above, but unexpected runtime
				// exceptions and errors may still occur.
//				EclipseUIUtils.getIDEWorkbenchPlugin().getLog().log(
//						new Status(IStatus.ERROR, EclipseUIUtils.IDEWorkbenchPlugin_IDE_WORKBENCH, 0, t.toString(), t));
//				MessageDialog.openError(getShell(), EclipseUIUtils.ResourceMessages_NewProject_errorMessage, NLS.bind(
//						EclipseUIUtils.ResourceMessages_NewProject_internalError, t.getMessage()));
			}
			return null;
		}

		newProject = newProjectHandle;

		return newProject;
	}

	/**
	 * copyFrameworkFiles
	 * 
	 * @param monitor
	 * @param project
	 * @param frameworks
	 * @return String
	 */
	public static void copyFrameworkFiles(IProgressMonitor monitor, IProject project, String[] frameworks)
	{
		String destinationDir = project.getLocation().toOSString();
		
		// add lib/air
		destinationDir = destinationDir + File.separator + "lib" + File.separator + "air";
		
		File destination = new File(destinationDir);
		
		if (monitor != null)
		{
			monitor.beginTask("Copying frameworks...", frameworks.length);
		}
		
		for (String sourceFile : frameworks)
		{
			try
			{
				File file = new File(sourceFile);
				String name = file.getName();

				if (monitor != null)
				{
					monitor.subTask(name);
				}

				FileUtils.copy(file.getParentFile(), destination, name);
				
				if (monitor != null)
				{
					monitor.worked(1);
				}
			}
			catch (Exception e)
			{
				//IdeLog.logError(PhoneGapPlugin.getDefault(), "Unable to copy file to project: " + sourceFile, e);
			}
		}
		
		try
		{
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}
		catch (CoreException e)
		{
			//IdeLog.logError(DocumentationPlugin.getDefault(), "Error refreshing project", e);
		}
	}
	
	/**
	 * copyLibraryFiles
	 * 
	 * @param monitor
	 * @param p
	 * @param selection
	 * @return String
	 */
	public static String copyLibraryFiles(IProgressMonitor monitor, IProject p, String[] selection)
	{
		String toOpen = null;

		for (int j = 0; j < selection.length; j++)
		{
			String destinationDir = p.getLocation().toOSString();
			String sourceDir = selection[j];

			try
			{
				File f = new File(sourceDir);
				File[] files = f.listFiles();

				if (monitor != null)
				{
					monitor.beginTask(StringUtils.format(
							"copying files...", selection[j]),
							files.length);
				}

				for (int i = 0; i < files.length; i++)
				{
					String name = files[i].getName();

					if (monitor != null)
					{
						monitor.subTask(name);
					}

					FileUtils.copy(sourceDir, destinationDir, name);
					if (toOpen == null && (name.toLowerCase().endsWith(".htm") || name.toLowerCase().endsWith(".html"))) //$NON-NLS-1$ //$NON-NLS-2$
					{
						toOpen = name;
					}

					if (monitor != null)
					{
						monitor.worked(1);
					}
				}
			}
			catch (Exception e)
			{
				// Log the error
			}
			finally
			{
				if (monitor != null)
				{
					monitor.done();
				}
			}

			try
			{
				p.refreshLocal(IResource.DEPTH_INFINITE, null);
			}
			catch (CoreException e)
			{
				// Log the error
			}
		}

		return toOpen;
	}

	
	/**
	 * Creates the project
	 * 
	 * @param p
	 * @param monitor
	 */
	protected void createProject(IProject p, IProgressMonitor monitor)
	{
		//copyLibraryFiles(monitor, p, libPage.getSelectedLibraries());
		//copyFrameworkFiles(monitor, p, frameworkPage.getSelectedFrameworks());

		if (newProject == null)
		{
			return;
		}

		// Open main HTML file
		try
		{
			String htmlFile = page.getHTMLFileName();
			
			if (new Path(htmlFile).isValidSegment(htmlFile))
			{
				IFile file = newProject.getFile(htmlFile);

				if (file.exists())
				{
					IWorkbenchPage page = workbench.getActiveWorkbenchWindow().getActivePage();
					IDE.openEditor(page, file, true);
				}
			}

		}
		catch (PartInitException e)
		{
		}

		BasicNewResourceWizard.selectAndReveal(newProject, workbench.getActiveWorkbenchWindow());
	}

	
	private void createProject(IProjectDescription description, IProject projectHandle, IProgressMonitor monitor) throws CoreException, OperationCanceledException
	{
		try
		{
			monitor.beginTask("", 3000);//$NON-NLS-1$

			projectHandle.create(description, new SubProgressMonitor(monitor, 1000));

			if (monitor.isCanceled())
			{
				throw new OperationCanceledException();
			}

			projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));

			String htmlFile = page.getHTMLFileName();
			
			String rootContent = StreamUtils.readContent(
				this.getClass().getResourceAsStream("/com/phonegap/resources/root.html"), //$NON-NLS-1$
				null
			);
			projectHandle.getFile(htmlFile).create(new ByteArrayInputStream(rootContent.getBytes()), true, null);
			IFolder img = projectHandle.getFolder("img");
			img.create(true, true, null);
			IFolder js = projectHandle.getFolder("js");
			js.create(true, true, null);
			IFolder css = projectHandle.getFolder("css");
			css.create(true, true, null);

			SimulatorLaunchAdder.addSimulatorLaunch(projectHandle.getName(), projectHandle.getName(), "");

			/*
			 * THIS COULD BE APPLICABLE FOR POINTING TO THE PHONEGAP SDK IN PREFERENCES
			 * 
			String names = PhoneGapPlugin.getDefault().getPreferenceStore().getString(
					PhoneGapPlugin.RUNTIME_NAMES_PREFERENCE);
			if (names.length() == 0 && PhoneGapPlugin.getDefaultAirSDKLocation() == null)
			{
				UIJob errorJob = new UIJob(Messages.ApolloProjectCreationWizard_NoAIRInstalled)
				{

					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						MessageDialog.openInformation(getShell(),
								Messages.ApolloProjectCreationWizard_NoAIRSDKInstalled,
								Messages.ApolloProjectCreationWizard_NoAirMessage);
						PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(this.getDisplay()
								.getActiveShell(), "com.phonegap.preferences.SDKPreferencePage", //$NON-NLS-1$
								new String[] { "com.phonegap.preferences.SDKPreferencePage" }, null); //$NON-NLS-1$
						dialog.open();
						return Status.OK_STATUS;
					}

				};
				errorJob.schedule();
			}
			*/
			
			
			/*
			monitor.beginTask("", 3000);//$NON-NLS-1$

			projectHandle.create(description, new SubProgressMonitor(monitor, 1000));

			if (monitor.isCanceled())
			{
				throw new OperationCanceledException();
			}

			projectHandle.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 1000));

			String htmlFile = projectPage.getHTMLFileName();

			DescriptorWriter descWriter = new DescriptorWriter();
			DescriptorModel model = propertiesPage.getModel();
			if (model.getName() == null || model.getName().trim().length() == 0)
			{
				model.setName(projectHandle.getName());
			}

			File icon16External = new File(model.getIcon16());
			File icon32External = new File(model.getIcon32());
			File icon48External = new File(model.getIcon48());
			File icon128External = new File(model.getIcon128());

			IFolder icons = projectHandle.getFolder("icons"); //$NON-NLS-1$
			icons.create(true, true, null);

			String fileName16 = icon16External.getName();
			IFile icon16Internal = icons.getFile(fileName16);
			if ( icon16Internal.exists() ){
				int lastIndexOf = fileName16.lastIndexOf(".");
				String suffix16 = "_16";
				icon16Internal = icons.getFile( fileName16.substring(0,lastIndexOf) + suffix16 + fileName16.substring(lastIndexOf));
			}
			try
			{
				icon16Internal.create(new FileInputStream(icon16External), true, null);
				model.setIcon16(icon16Internal.getProjectRelativePath().toString());
			}
			catch (FileNotFoundException e)
			{
				IdeLog.logError(PhoneGapPlugin.getDefault(), "File note found: " + icon16External.getAbsolutePath(), e);
			}
			
			String fileName32 = icon32External.getName();
			IFile icon32Internal = icons.getFile(fileName32);
			if ( icon32Internal.exists() ){
				int lastIndexOf = fileName32.lastIndexOf(".");
				String suffix32 = "_32";
				icon32Internal = icons.getFile( fileName32.substring(0,lastIndexOf) + suffix32 + fileName32.substring(lastIndexOf));
			}
			try
			{
				icon32Internal.create(new FileInputStream(icon32External), true, null);
				model.setIcon32(icon32Internal.getProjectRelativePath().toString());
			}
			catch (FileNotFoundException e)
			{
				IdeLog.logError(PhoneGapPlugin.getDefault(), "File note found: " + icon32External.getAbsolutePath(), e);
			}
			
			String fileName48 = icon48External.getName();
			IFile icon48Internal = icons.getFile(fileName48);
			if ( icon48Internal.exists() ){
				int lastIndexOf = fileName48.lastIndexOf(".");
				String suffix48 = "_48";
				icon48Internal = icons.getFile( fileName48.substring(0,lastIndexOf) + suffix48 + fileName48.substring(lastIndexOf));
			}
			try
			{
				icon48Internal.create(new FileInputStream(icon48External), true, null);
				model.setIcon48(icon48Internal.getProjectRelativePath().toString());
			}
			catch (FileNotFoundException e)
			{
				IdeLog.logError(PhoneGapPlugin.getDefault(), "File note found: " + icon48External.getAbsolutePath(), e);
			}

			String fileName128 = icon128External.getName();
			IFile icon128Internal = icons.getFile(fileName128);
			if ( icon128Internal.exists() ){
				int lastIndexOf = fileName128.lastIndexOf(".");
				String suffix128 = "_128";
				icon128Internal = icons.getFile( fileName128.substring(0,lastIndexOf) + suffix128 + fileName128.substring(lastIndexOf));
			}
			try
			{
				icon128Internal.create(new FileInputStream(icon128External), true, null);
				model.setIcon128(icon128Internal.getProjectRelativePath().toString());
			}
			catch (FileNotFoundException e)
			{
				IdeLog.logError(PhoneGapPlugin.getDefault(), "File note found: " + icon128External.getAbsolutePath(), e);
			}
			
			model.setContent(htmlFile);
			
			// TODO expose to custom application descriptor names?
			descWriter.createInitialDescriptor(projectHandle, DescriptorConstants.APPLICATION_FILE, model);
			
			// get framework names so we can inject them into the root html file
			final List<String> frameworks = new ArrayList<String>();
			final IWorkbench workbench = PlatformUI.getWorkbench();
			Display display = workbench.getDisplay();

			// execute callback in the correct thread
			display.syncExec(new Runnable() {
				public void run()
				{
					frameworks.addAll(Arrays.asList(frameworkPage.getSelectedFrameworks()));
				}
			});
			
			StringBuilder embedString = new StringBuilder();
			
			for (String framework : frameworks)
			{
				String scriptTag;
				File file = new File(framework);
				String name = file.getName();
				
				if (framework.toLowerCase(Locale.getDefault()).endsWith(".js"))
				{
					scriptTag = "<script type=\"text/javascript\" src=\"lib/air/" + name + "\"></script>";
				}
				else
				{
					scriptTag = "<script type=\"application/x-shockwave-flash\" src=\"lib/air/" + name + "\"></script>";
				}
				
				embedString.append(scriptTag);
				embedString.append("\r\n        ");
			}
			
			if (projectPage.useApplicationSandbox())
			{
				String rootContent = StreamUtils.readContent(
					this.getClass().getResourceAsStream("/com/phonegap/resources/onesandbox/root.html"), //$NON-NLS-1$
					null
				);
				rootContent = rootContent.replace("<!--FRAMEWORK_REFERENCES-->", embedString.toString());
				projectHandle.getFile(htmlFile).create(new ByteArrayInputStream(rootContent.getBytes()), true, null);

				this.createProjectFileFromResource(
					projectHandle.getFile("LocalFile.txt"),
					"/com/phonegap/resources/onesandbox/LocalFile.txt" //$NON-NLS-1$
				);

				this.createProjectFileFromResource(
					projectHandle.getFile("sample.css"),
					"/com/phonegap/resources/onesandbox/sample.css" //$NON-NLS-1$
				);
			}
			else
			{
				String rootContent = StreamUtils.readContent(
					this.getClass().getResourceAsStream("/com/phonegap/resources/twosandboxes/root.html"), //$NON-NLS-1$
					null
				);
				rootContent = rootContent.replace("<!--FRAMEWORK_REFERENCES-->", embedString.toString());
				projectHandle.getFile(htmlFile).create(new ByteArrayInputStream(rootContent.getBytes()), true, null);

				this.createProjectFileFromResource(
					projectHandle.getFile("SampleUI.html"),
					"/com/phonegap/resources/twosandboxes/SampleUI.html" //$NON-NLS-1$
				);

				this.createProjectFileFromResource(
					projectHandle.getFile("LocalFile.txt"),
					"/com/phonegap/resources/onesandbox/LocalFile.txt" //$NON-NLS-1$
				);

				this.createProjectFileFromResource(
					projectHandle.getFile("sample.css"),
					"/com/phonegap/resources/onesandbox/sample.css" //$NON-NLS-1$
				);
			}

			// Configure jaxer options
			try
			{
				IServer server =HTMLPreviewHelper.getEmbeddedWebServer();
				if (server != null ) {
					String serverURL = HTMLPreviewHelper.getServerHostURL(server);
					projectHandle.setPersistentProperty(new QualifiedName("", JaxerAIRBuilder.BUILD_PROJECT), Boolean.toString(projectPage.buildJaxerAIR()));
					projectHandle.setPersistentProperty(new QualifiedName("", JaxerAIRBuilder.JAXER_SERVER_ADDRESS), serverURL );
				}

			}
			catch (Exception e)
			{
				IdeLog.logError(PhoneGapPlugin.getDefault(), Messages.ApolloProjectCreationWizard_UnableToCreateProject, e);
			}
			if (projectPage.buildJaxerAIR())
			{
//				try {
//					IServer jaxer = ServerManager.getInstance().findServer(JaxerServerProvider.INTERNAL_JAXER_SERVER_ID);
//					if (jaxer != null && (jaxer instanceof JaxerServer) && (jaxer.getServerState() == JaxerServer.STATE_STOPPED)  ) {
//						jaxer.start("run", null, null);
//					}
//				} catch (Exception e){
//					IdeLog.logError(PhoneGapPlugin.getDefault(), "Failed to Start Jaxer Server", e);
//				}
				AirLaunchAdder.addAirLaunch(projectHandle.getName(), projectHandle.getName() + " (Jaxer version)", JaxerAIRBuilder.JAXER_BIN + "/" + DescriptorConstants.APPLICATION_FILE);
			}
			else
			{
				AirLaunchAdder.addAirLaunch(projectHandle.getName(), projectHandle.getName(),
						DescriptorConstants.APPLICATION_FILE);
			}

			String names = PhoneGapPlugin.getDefault().getPreferenceStore().getString(
					PhoneGapPlugin.RUNTIME_NAMES_PREFERENCE);
			if (names.length() == 0 && PhoneGapPlugin.getDefaultAirSDKLocation() == null)
			{
				UIJob errorJob = new UIJob(Messages.ApolloProjectCreationWizard_NoAIRInstalled)
				{

					public IStatus runInUIThread(IProgressMonitor monitor)
					{
						MessageDialog.openInformation(getShell(),
								Messages.ApolloProjectCreationWizard_NoAIRSDKInstalled,
								Messages.ApolloProjectCreationWizard_NoAirMessage);
						PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(this.getDisplay()
								.getActiveShell(), "com.phonegap.preferences.SDKPreferencePage", //$NON-NLS-1$
								new String[] { "com.phonegap.preferences.SDKPreferencePage" }, null); //$NON-NLS-1$
						dialog.open();
						return Status.OK_STATUS;
					}

				};
				errorJob.schedule();
			}

			 */
		}
		catch (Exception ex)
		{
		}
		finally
		{
			monitor.done();
		}
	}

	/**
	 * createProjectFileFromResource
	 *
	 * @param file
	 * @param resource
	 * @throws CoreException
	 */
	private void createProjectFileFromResource(IFile file, String resource) throws CoreException
	{
		file.create(
			this.getClass().getResourceAsStream(resource),
			true,
			null
		);
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 */

	private void doFinish(
		String containerName,
		String fileName,
		IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream();
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 */

	private InputStream openContentStream() {
		String contents =
			"This is the initial file contents for *.mpe file that should be word-sorted in the Preview page of the multi-page editor";
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "PhoneGap_Eclipse", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
	 *      java.lang.String, java.lang.Object)
	 */
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data)
			throws CoreException
	{

	}
	
	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
	}
}