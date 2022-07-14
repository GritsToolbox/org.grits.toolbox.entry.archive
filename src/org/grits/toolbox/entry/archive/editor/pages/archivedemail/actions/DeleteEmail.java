/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedemail.actions;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.archivedemail.EmailArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedBasicEmail;
import org.grits.toolbox.entry.archive.model.ArchivedEmail;
import org.grits.toolbox.entry.archive.model.ArchivedEmailTopic;

/**
 * 
 *
 */
public class DeleteEmail extends Action
{
	private Logger logger = Logger.getLogger(DeleteEmail.class);
	private EmailArchivePage page = null;

	public DeleteEmail(EmailArchivePage fileArchivePage)
	{
		page = fileArchivePage;
		this.setText("Delete");
		this.setToolTipText("Delete Email");
		this.setImageDescriptor(ImageShare.DELETE_ICON);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		TreeViewer treeViewer = page.getLeftSideTreeViewer();
		TreeSelection treeSelection = ((TreeSelection) treeViewer.getSelection());
		if(treeSelection.isEmpty())
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "No email Selected",
					"Please select an email to delete.");
		}
		else if(treeSelection.getFirstElement() instanceof ArchivedEmail)
		{
			int selectionIndex = treeViewer.getTree().indexOf((TreeItem) treeViewer.getTree().getSelection()[0]);
			List<ArchivedEmail> inputArchivedEmails = (List<ArchivedEmail>) treeViewer.getInput();
			ArchivedEmail archivedEmailToDelete = (ArchivedEmail) treeSelection.getFirstElement();
			boolean delete = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Delete Archive", 
					"Are you sure you want to delete this email \"" 
							+ archivedEmailToDelete.getDisplayTitle() + "\" from the archive?");
			if(delete)
			{
				try
				{
					if(inputArchivedEmails.contains(archivedEmailToDelete))
					{
						inputArchivedEmails.remove(archivedEmailToDelete);
					}
					else if(archivedEmailToDelete instanceof ArchivedBasicEmail)
					{
						ArchivedBasicEmail basicEmailtoDelete = (ArchivedBasicEmail) archivedEmailToDelete;
						ArchivedEmailTopic selectedTopic = basicEmailtoDelete.getArchivedEmailTopic();
						if(selectedTopic.getReplyEmails().size() == 1)
						{
							inputArchivedEmails.remove(selectedTopic);
						}
						else if(selectedTopic.getReplyEmails().size() > 1)
						{
							selectedTopic.getReplyEmails().remove(basicEmailtoDelete);
						}
					}
				} catch (Exception ex)
				{
					logger.error("Error removing email from archive.\n" + ex.getMessage(), ex);
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "Delete Error", 
							"Error removing email from archive. "
									+ "Please report this error to the development team");
				}

				treeViewer.refresh();
				selectionIndex = Math.max(0, selectionIndex - 1);
				if(selectionIndex >= 0 && selectionIndex < inputArchivedEmails.size())
				{
					treeViewer.getTree().select(treeViewer.getTree().getItem(selectionIndex));
					treeViewer.setSelection(treeViewer.getSelection());
				}
				else
					page.setSelection(null);
				((ArchiveEntryEditor) page.getEditor()).markDirty();
			}
		}

		logger.debug("End - Deleting  Archived Email");
	}
}
