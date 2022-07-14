/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedemail.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.dialog.email.AddEmailDialog;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.archivedemail.EmailArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedBasicEmail;

/**
 * 
 *
 */
public class AddArchivedEmail extends Action
{
	private static Logger logger = Logger.getLogger(AddArchivedEmail.class);
	private EmailArchivePage page = null;

	public AddArchivedEmail(EmailArchivePage emailArchivePage)
	{
		page = emailArchivePage;
		this.setText("Add an email");
		this.setToolTipText("Add a new email");
		this.setImageDescriptor(ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.ADD_EMAIL_ICON));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		logger.debug("Start - Uploading  Archive Email");
		ArchiveEntryEditor archiveEntryEditor = (ArchiveEntryEditor) page.getEditor();
		TreeViewer treeViewer = page.getLeftSideTreeViewer();
		List<ArchivedBasicEmail> inputArchivedEmails = (List<ArchivedBasicEmail>) treeViewer.getInput();

		try
		{
			AddEmailDialog addEmailDialog = new AddEmailDialog(Display.getCurrent().getActiveShell(), null);
			if(addEmailDialog.open() == Window.OK)
			{
				ArchivedBasicEmail newArchivedEmail = addEmailDialog.getArchivedEmail();
				if(newArchivedEmail != null)
				{
					try
					{
						inputArchivedEmails.add(newArchivedEmail);
						archiveEntryEditor.markDirty();
					} catch (Exception ex)
					{
						logger.error("Error uploading email to archive.\n" + ex.getMessage(), ex);
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Upload Error", 
								"The email could not be uploaded to GRITS. "
										+ "Please report this error to the development team");
					}
				}
				treeViewer.refresh();
				treeViewer.setSelection(new StructuredSelection(newArchivedEmail));
			}
		} catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Upload Error", 
					"The email could not be uploaded to GRITS. "
							+ "Please report this error to the development team");
		}

		logger.debug("End - Uploading  Archive Email");
	}
}
