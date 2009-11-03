package com.phonegap.preferences;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;

import com.phonegap.PhoneGapPlugin;

/**
 * @author Kevin Sawicki (ksawicki@aptana.com)
 */
public class SDKPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

	private CheckboxTableViewer sdkTableViewer;
	private Table sdkTable;
	private TableColumn nameColumn;
	private TableColumn locationColumn;
	private Label installedSDKs;
	private Button add;
	private Button edit;
	private Button remove;
	private Composite displayArea;
	private List<Item> sdks;
	private IPreferenceStore store;
	private Link runtimeLink;
	private Link sdkLink;
	private Item defaultSDK;
	private SelectionAdapter linkSelector = new SelectionAdapter()
	{

		public void widgetSelected(SelectionEvent e)
		{
			try
			{
				URL url = new URL(e.text);
				IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
				support.getExternalBrowser().openURL(url);
			}
			catch (PartInitException e1)
			{
				// Do nothing
			}
			catch (MalformedURLException e1)
			{
				// Do nothing
			}
		}

	};

	private ITableLabelProvider labelProvider = new ITableLabelProvider()
	{

		public void removeListener(ILabelProviderListener listener)
		{

		}

		public boolean isLabelProperty(Object element, String property)
		{
			return false;
		}

		public void dispose()
		{

		}

		public void addListener(ILabelProviderListener listener)
		{

		}

		public String getColumnText(Object element, int columnIndex)
		{
			if (element instanceof Item)
			{
				if (columnIndex == 0)
				{
					return ((Item) element).getName();
				}
				else if (columnIndex == 1)
				{
					if (element.equals(defaultSDK))
					{
						return ((Item) element).getName();
					}
					return ((Item) element).getLocation();
				}
			}
			return ""; //$NON-NLS-1$
		}

		public Image getColumnImage(Object element, int columnIndex)
		{
			if (columnIndex == 0)
			{
				return PhoneGapPlugin.getImage("icons/classpath.gif"); //$NON-NLS-1$
			}
			else
			{
				return null;
			}
		}

	};

	private IStructuredContentProvider contentProvider = new IStructuredContentProvider()
	{

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
		{

		}

		public void dispose()
		{

		}

		public Object[] getElements(Object inputElement)
		{
			return sdks.toArray();
		}

	};

	private void buildTableItems()
	{
		if (store != null)
		{
			String names = store.getString(PhoneGapPlugin.RUNTIME_NAMES_PREFERENCE);
			String[] sdkPrefNames = names.split(PhoneGapPlugin.PREFERENCE_DELIMITER);
			String locations = store.getString(PhoneGapPlugin.RUNTIME_LOCATIONS_PREFERENCE);
			String[] sdkPrefLocations = locations.split(PhoneGapPlugin.PREFERENCE_DELIMITER);
			if (sdkPrefNames.length == sdkPrefLocations.length)
			{
				for (int i = 0; i < sdkPrefNames.length; i++)
				{
					if (sdkPrefNames[i].length() > 0 && sdkPrefLocations[i].length() > 0)
					{
						Item item = new Item(sdkPrefNames[i], sdkPrefLocations[i]);
						sdks.add(item);
					}
				}
			}
		}
	}

	private void validatePreferencePage()
	{
		Object[] checked = sdkTableViewer.getCheckedElements();
		if (checked == null || checked.length == 0)
		{
			setErrorMessage("Select default");
			setValid(false);
		}
		else
		{
			setErrorMessage(null);
			setValid(true);
		}
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent)
	{
		sdks = new ArrayList<Item>();
		if (PhoneGapPlugin.getEmbeddedSDKLocation() != null)
		{
			defaultSDK = new Item(PhoneGapPlugin.getEmbeddedSDKName(), PhoneGapPlugin.getEmbeddedSDKLocation());
			sdks.add(defaultSDK);
		}

		buildTableItems();
		displayArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 4;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		displayArea.setLayout(layout);
		displayArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite links = new Composite(displayArea, SWT.NONE);
		layout = new GridLayout(1, true);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		links.setLayout(layout);
		sdkLink = new Link(links, SWT.NONE);
		sdkLink.setText("Download SDK");
		sdkLink.addSelectionListener(linkSelector);
		runtimeLink = new Link(links, SWT.NONE);
		runtimeLink.setText("Download AIR runtime");
		runtimeLink.addSelectionListener(linkSelector);

		installedSDKs = new Label(displayArea, SWT.WRAP | SWT.LEFT);
		installedSDKs.setText("Installed SDKs");

		Composite center = new Composite(displayArea, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 5;
		layout.marginWidth = 0;
		center.setLayout(layout);
		center.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		sdkTable = new Table(center, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.CHECK);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = 450;
		sdkTable.setLayoutData(data);
		sdkTable.setLinesVisible(true);
		sdkTable.setHeaderVisible(true);

		sdkTableViewer = new CheckboxTableViewer(sdkTable);
		sdkTableViewer.setLabelProvider(labelProvider);
		sdkTableViewer.setContentProvider(contentProvider);
		sdkTableViewer.addCheckStateListener(new ICheckStateListener()
		{

			public void checkStateChanged(CheckStateChangedEvent event)
			{
				validatePreferencePage();

				// Ensure only one default checked
				for (int i = 0; i < sdks.size(); i++)
				{
					Item curr = (Item) sdks.get(i);
					if (!curr.equals(event.getElement()))
					{
						sdkTableViewer.setChecked(curr, false);
					}
				}
			}

		});
		sdkTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{

			public void selectionChanged(SelectionChangedEvent event)
			{
				IStructuredSelection selection = (IStructuredSelection) sdkTableViewer.getSelection();
				edit.setEnabled(selection != null && !selection.isEmpty()
						&& !selection.getFirstElement().equals(defaultSDK));
				remove.setEnabled(selection != null && !selection.isEmpty()
						&& !selection.getFirstElement().equals(defaultSDK));
			}

		});

		nameColumn = new TableColumn(sdkTable, SWT.LEFT);
		nameColumn.setText("Name");
		nameColumn.setWidth(150);

		locationColumn = new TableColumn(sdkTable, SWT.LEFT);
		locationColumn.setText("Location");
		locationColumn.setWidth(300);

		Composite buttons = new Composite(center, SWT.NONE);
		buttons.setLayout(new GridLayout(1, true));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		add = new Button(buttons, SWT.PUSH);
		add.setText("Add");
		add.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		add.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				SDKDialog dialog = new SDKDialog(add.getShell());
				dialog.setTitle("Add PhoneGap SDK");
				List<String> names = new ArrayList<String>();
				for (int i = 0; i < sdks.size(); i++)
				{
					names.add(((Item) sdks.get(i)).getName());
				}
				dialog.setSDKNames(names);
				int rc = dialog.open();
				if (rc == SDKDialog.OK)
				{
					Item item = (Item) dialog.getFirstResult();
					sdks.add(item);
					sdkTableViewer.refresh();
					Object[] elements = sdkTableViewer.getCheckedElements();
					if (elements == null || elements.length == 0)
					{
						sdkTableViewer.setChecked(item, true);
					}
				}
				validatePreferencePage();
			}

		});

		edit = new Button(buttons, SWT.PUSH);
		edit.setText("Edit");
		edit.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		edit.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				Object selection = ((IStructuredSelection) sdkTableViewer.getSelection()).getFirstElement();
				if (selection != null && selection instanceof Item)
				{
					Item curr = (Item) selection;
					SDKDialog dialog = new SDKDialog(edit.getShell());
					dialog.setTitle("Edit PhoneGap SDK");
					List<String> names = new ArrayList<String>();
					for (int i = 0; i < sdks.size(); i++)
					{
						Item sdkItem = (Item) sdks.get(i);
						if (!sdkItem.equals(curr))
						{
							names.add(sdkItem.getName());
						}
					}
					dialog.setSDKNames(names);
					dialog.setSDKName(curr.getName());
					dialog.setSDKLocation(curr.getLocation());
					int rc = dialog.open();
					if (rc == SDKDialog.OK)
					{
						Item item = (Item) dialog.getFirstResult();
						curr.setName(item.getName());
						curr.setLocation(item.getLocation());
						sdkTableViewer.refresh();
					}
					validatePreferencePage();
				}
			}

		});

		remove = new Button(buttons, SWT.PUSH);
		remove.setText("Remove");
		remove.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		remove.addSelectionListener(new SelectionAdapter()
		{

			public void widgetSelected(SelectionEvent e)
			{
				Object selection = ((IStructuredSelection) sdkTableViewer.getSelection()).getFirstElement();
				if (selection != null && selection instanceof Item)
				{
					sdks.remove(selection);
					sdkTableViewer.refresh();
					validatePreferencePage();
				}
			}

		});

		sdkTableViewer.setInput(sdks);
		if (store != null)
		{
			String defaultName = store.getString(PhoneGapPlugin.RUNTIME_NAME_DEFAULT_PREFERENCE);
			String defaultLocation = store.getString(PhoneGapPlugin.RUNTIME_LOCATION_DEFAULT_PREFERENCE);
			for (int i = 0; i < sdks.size(); i++)
			{
				Item curr = (Item) sdks.get(i);
				if (curr.getName().equals(defaultName) && curr.getLocation().equals(defaultLocation))
				{
					sdkTableViewer.setChecked(curr, true);
				}
			}
		}

		if (defaultSDK != null
				&& (sdkTableViewer.getCheckedElements() == null || sdkTableViewer.getCheckedElements().length == 0))
		{
			sdkTableViewer.setChecked(defaultSDK, true);
		}

		return displayArea;
	}

	/**
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	public boolean performOk()
	{
		String runtimeNames = ""; //$NON-NLS-1$
		String runtimeLocations = ""; //$NON-NLS-1$
		for (int i = 0; i < sdks.size(); i++)
		{
			Item item = (Item) sdks.get(i);
			if (!item.equals(defaultSDK))
			{
				runtimeNames += item.getName() + PhoneGapPlugin.PREFERENCE_DELIMITER;
				runtimeLocations += item.getLocation() + PhoneGapPlugin.PREFERENCE_DELIMITER;
			}
		}

		store.setValue(PhoneGapPlugin.RUNTIME_NAMES_PREFERENCE, runtimeNames);
		store.setValue(PhoneGapPlugin.RUNTIME_LOCATIONS_PREFERENCE, runtimeLocations);

		Object[] checked = sdkTableViewer.getCheckedElements();
		if (checked != null && checked.length == 1)
		{
			if (checked[0] instanceof Item)
			{
				Item item = (Item) checked[0];
				store.setValue(PhoneGapPlugin.RUNTIME_LOCATION_DEFAULT_PREFERENCE, item.getLocation());
				store.setValue(PhoneGapPlugin.RUNTIME_NAME_DEFAULT_PREFERENCE, item.getName());
			}
		}

		return super.performOk();
	}

	/**
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench)
	{
		setDescription("PhoneGap SDK description"
				+ "default message");
		store = PhoneGapPlugin.getDefault().getPreferenceStore();
	}

}