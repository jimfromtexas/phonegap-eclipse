package com.phonegap.preferences;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionStatusDialog;

import com.phonegap.PhoneGapPlugin;

/**
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public class SDKDialog extends SelectionStatusDialog
{

	private Label sdkDirectoryLabel;
	private Label sdkNameLabel;

	private Text sdkDirectoryText;
	private String flexDirectory;
	private Text sdkNameText;
	private String flexName;

	private Button browseSdkDirectory;

	private Composite displayArea;

	private List<String> sdkNames;

	/**
	 * @param parentShell
	 */
	public SDKDialog(Shell parentShell)
	{
		super(parentShell);
		setStatusLineAboveButtons(true);
		sdkNames = new ArrayList<String>();
		flexName = ""; //$NON-NLS-1$
		flexDirectory = ""; //$NON-NLS-1$
	}

	private void checkDialogForErrors()
	{
		String dir = sdkDirectoryText.getText();
		String name = sdkNameText.getText();

		File file = new File(dir);
		if (dir.trim().length() == 0)
		{
			updateStatus(createStatus(IStatus.ERROR, "Enter PhoneGap SDK location"));
		}
		else if (!file.isDirectory() || !file.exists())
		{
			updateStatus(createStatus(IStatus.ERROR, "Director does not exist: " + dir));
		}
		else if (name.indexOf(PhoneGapPlugin.PREFERENCE_DELIMITER) != -1)
		{
			updateStatus(createStatus(IStatus.ERROR, "Invalid character"));
		}
		else if (sdkNames.contains(name))
		{
			updateStatus(createStatus(IStatus.ERROR, "PhoneGap SDK name in use"));
		}
		else if (name.trim().length() == 0)
		{
			updateStatus(createStatus(IStatus.ERROR, "Enter SDK name"));
		}
		else
		{
			updateStatus(createStatus(IStatus.OK, null));
		}
	}

	/**
	 * Sets the names of the existing installed Flex SDks
	 * 
	 * @param names -
	 *            list of string names
	 */
	public void setSDKNames(List<String> names)
	{
		if (names != null)
		{
			this.sdkNames = names;
		}
	}

	/**
	 * Sets the name to appear in the dialog name text field
	 * 
	 * @param name
	 */
	public void setSDKName(String name)
	{
		flexName = name;
	}

	/**
	 * Sets the location to appear in the dialog location text field
	 * 
	 * @param location
	 */
	public void setSDKLocation(String location)
	{
		flexDirectory = location;
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		displayArea = new Composite(composite, SWT.NONE);
		displayArea.setLayout(new GridLayout(2, false));
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 500;
		displayArea.setLayoutData(data);

		sdkDirectoryLabel = new Label(displayArea, SWT.LEFT);
		sdkDirectoryLabel.setText("PhoneGap SDK directory");

		Composite browseComp = new Composite(displayArea, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		browseComp.setLayout(layout);
		browseComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		sdkDirectoryText = new Text(browseComp, SWT.SINGLE | SWT.BORDER);
		sdkDirectoryText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sdkDirectoryText.setText(flexDirectory);
		sdkDirectoryText.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent e)
			{
				checkDialogForErrors();
			}

		});
		browseSdkDirectory = new Button(browseComp, SWT.PUSH);
		browseSdkDirectory.setText("Browse for the PhoneGap SDK");
		browseSdkDirectory.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				DirectoryDialog dialog = new DirectoryDialog(browseSdkDirectory.getShell());
				dialog.setMessage("Select PhoneGap SDK root");
				dialog.setText("Browsre for folder");
				String dir = dialog.open();
				if (dir != null)
				{
					sdkDirectoryText.setText(dir);
				}
			}

		});

		sdkNameLabel = new Label(displayArea, SWT.LEFT);
		sdkNameLabel.setText("PhoneGap SDK name");

		sdkNameText = new Text(displayArea, SWT.SINGLE | SWT.BORDER);
		sdkNameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		sdkNameText.setText(flexName);
		sdkNameText.addModifyListener(new ModifyListener()
		{

			public void modifyText(ModifyEvent e)
			{
				checkDialogForErrors();
			}

		});

		checkDialogForErrors();

		return composite;
	}

	/**
	 * @see org.eclipse.ui.dialogs.SelectionStatusDialog#computeResult()
	 */
	protected void computeResult()
	{
		Item item = new Item(sdkNameText.getText(), sdkDirectoryText.getText());
		List<Item> list = new ArrayList<Item>();
		list.add(item);
		setResult(list);

	}

	private static IStatus createStatus(final int severity, final String message)
	{
		return new IStatus()
		{

			public boolean matches(int severityMask)
			{
				return severityMask == severity;
			}

			public boolean isOK()
			{
				return false;
			}

			public boolean isMultiStatus()
			{
				return false;
			}

			public int getSeverity()
			{
				return severity;
			}

			public String getPlugin()
			{
				return null;
			}

			public String getMessage()
			{
				return message;
			}

			public Throwable getException()
			{
				return null;
			}

			public int getCode()
			{
				return 0;
			}

			public IStatus[] getChildren()
			{
				return null;
			}

		};
	}

}
