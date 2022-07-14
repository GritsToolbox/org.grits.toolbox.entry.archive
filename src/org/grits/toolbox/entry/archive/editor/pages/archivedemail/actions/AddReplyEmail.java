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
import org.grits.toolbox.entry.archive.model.ArchivedEmail;
import org.grits.toolbox.entry.archive.model.ArchivedEmailTopic;

/**
 * 
 *
 */
public class AddReplyEmail extends Action
{
	private static Logger logger = Logger.getLogger(AddReplyEmail.class);
	private EmailArchivePage page = null;

	public AddReplyEmail(EmailArchivePage emailArchivePage)
	{
		page = emailArchivePage;
		this.setText("Reply email");
		this.setToolTipText("Add a reply email");
		this.setImageDescriptor(ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.REPLY_EMAIL_ICON));
	}

	@SuppressWarnings({ "unchecked" })
	@Override
	public void run()
	{
		logger.debug("Start - Adding reply Email");
		ArchiveEntryEditor archiveEntryEditor = (ArchiveEntryEditor) page.getEditor();
		TreeViewer treeViewer = page.getLeftSideTreeViewer();
		if(!treeViewer.getSelection().isEmpty())
		{
			ArchivedEmail selectedEmail = (ArchivedEmail) ((StructuredSelection) 
					treeViewer.getSelection()).getFirstElement();
			List<ArchivedEmail> inputArchivedEmails = (List<ArchivedEmail>) treeViewer.getInput();
			try
			{
				ArchivedEmailTopic selectedParent = null;
				if(selectedEmail instanceof ArchivedEmailTopic)
				{
					selectedParent = (ArchivedEmailTopic) selectedEmail;
				}
				else if(((ArchivedBasicEmail) selectedEmail).getArchivedEmailTopic() == null)
				{
					selectedParent = changeBasicToTopic((ArchivedBasicEmail) selectedEmail);
					inputArchivedEmails.remove(selectedEmail);
					inputArchivedEmails.add(selectedParent);
				}
				else
				{
					selectedParent = ((ArchivedBasicEmail) selectedEmail).getArchivedEmailTopic();
				}
				if(selectedParent != null)
				{
					AddEmailDialog addEmailDialog = 
							new AddEmailDialog(Display.getCurrent().getActiveShell(), selectedParent.getEmailSubject());
					if(addEmailDialog.open() == Window.OK)
					{
						ArchivedBasicEmail newArchivedEmail = addEmailDialog.getArchivedEmail();
						selectedParent.addReplyEmail(newArchivedEmail);
						treeViewer.refresh();
						treeViewer.setSelection(new StructuredSelection(newArchivedEmail));
						archiveEntryEditor.markDirty();
					}
				}
			} catch (Exception ex)
			{
				logger.error(ex.getMessage(), ex);
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Upload Error", 
						"The email reply could not be added to GRITS. "
								+ "Please report this error to the development team");
			}

			logger.debug("End - Adding reply Email");
		}
	}

	private ArchivedEmailTopic changeBasicToTopic(ArchivedBasicEmail selectedEmail)
	{
		ArchivedEmailTopic archivedEmailTopic = new ArchivedEmailTopic();
		archivedEmailTopic.addReplyEmail(selectedEmail);
		archivedEmailTopic.setEmailSubject(selectedEmail.getEmailSubject());
		archivedEmailTopic.setEmailDate(selectedEmail.getEmailDate());
		return archivedEmailTopic;
	}
}
