/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedemail.actions;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.archivedemail.EmailArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedEmail;

/**
 * 
 *
 */
public class DeleteAll extends Action
{
	private static Logger logger = Logger.getLogger(DeleteAll.class);

	private EmailArchivePage page = null;

	public DeleteAll(EmailArchivePage fileArchivePage)
	{
		page = fileArchivePage;
		this.setText("Delete All");
		this.setToolTipText("Delete all Emails");
		this.setImageDescriptor(ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.DELETE_ALL_ICON));
	}

	@Override
	public void run()
	{
		logger.debug("Start - Removing all Emails");
		ArchiveEntryEditor archiveEntryEditor = (ArchiveEntryEditor) page.getEditor();
		archiveEntryEditor.getArchive().setArchivedEmails(new ArrayList<ArchivedEmail>());
		page.getLeftSideTreeViewer().setInput(archiveEntryEditor.getArchive().getArchivedEmails());
		((ArchiveEntryEditor) page.getEditor()).markDirty();
		page.refreshPage();
		page.getLeftSideTreeViewer().refresh();
		logger.debug("Start - Removing all Emails");
	}
}
