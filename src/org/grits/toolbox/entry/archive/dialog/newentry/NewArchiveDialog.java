/**
 * 
 */
package org.grits.toolbox.entry.archive.dialog.newentry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.utilShare.sort.EntryComparator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.property.ArchiveProperty;

/**
 * 
 *
 */
public class NewArchiveDialog extends Dialog 
{
	private Entry parentEntry = null;
	private TreeViewer treeViewer = null;

	public NewArchiveDialog(Shell parentShell) 
	{
		super(parentShell);
	}
	
	@Override
	public void create()
	{
		super.create();
		getButton(OK).setEnabled(false);
		if(treeViewer != null && parentEntry != null)
		{
			treeViewer.setSelection(
					new StructuredSelection(parentEntry));
		}
	}

	public Control createDialogArea(Composite parent) 
	{
		getShell().setText("Select Entry");
		Composite composite = (Composite) super.createDialogArea(parent);//new Composite(parent, SWT.FILL);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		composite.setLayout(new GridLayout());

		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		treeViewer.setContentProvider(new EntryContentProvider());
		treeViewer.setLabelProvider(new EntryLabelProvider());
		treeViewer.setComparator(new EntryComparator());

		GridData treeData = new GridData();
		treeData.minimumHeight = 200;
		treeData.heightHint = 400;
		treeData.widthHint = 400;
		treeData.grabExcessHorizontalSpace = true;
		treeData.grabExcessVerticalSpace = true;
		treeData.horizontalAlignment = GridData.FILL;
		treeViewer.getTree().setLayoutData(treeData);
		treeViewer.setInput(DataModelHandler.instance().getRoot());

		treeViewer.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) 
			{
				getButton(OK).setEnabled(false);
				ISelection selection = event.getSelection();
				if(!selection.isEmpty())
				{
					TreeSelection treeSelection = (TreeSelection) selection;
					if (treeSelection.size() == 1)
					{
						Entry entry = (Entry) treeSelection.getFirstElement();
						boolean canHaveArchive = entry.getProperty() instanceof ProjectProperty ? 
								((ProjectProperty) entry.getProperty()).isOpen() : 
									IConfig.ARCHIVABLE_PLUGINS.contains(entry.getProperty().getType());

						if(canHaveArchive)
						{
							for(Entry child : entry.getChildren())
							{
								if(child.getProperty().getType().equals(ArchiveProperty.TYPE))
								{
									canHaveArchive = false;
									break;
								}
							}
							if(canHaveArchive)
							{
								getButton(OK).setEnabled(true);
								parentEntry = entry;
							}
						}
					}
				}
			}
		});
		treeViewer.getTree().setFocus();

		return composite;
	}

	protected void okPressed() 
	{
		super.okPressed();
	}

	public void cancelPressed() 
	{
		super.cancelPressed();
	}

	public Entry getParentEntry() {
		return parentEntry;
	}

	public void setParentEntry(Entry parentEntry)
	{
		this.parentEntry = parentEntry;
	}

}
