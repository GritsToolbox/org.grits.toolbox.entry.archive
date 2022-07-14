/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.grits.toolbox.core.img.ImageShare;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.FileArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedFile;

/**
 * 
 *
 */
public class DeleteArchivedFile extends Action
{
    private Logger logger = Logger.getLogger(DeleteArchivedFile.class);
	// https://www.iconfinder.com/icons/282471/cross_delete_remove_icon#size=16
	public static final ImageDescriptor DELETE_ICON = ImageShare.DELETE_ICON;
	private FileArchivePage page = null;

	public DeleteArchivedFile(FileArchivePage fileArchivePage)
	{
		page = fileArchivePage;
		this.setText("Delete");
		this.setToolTipText("Delete the File");
		ImageDescriptor imageDesc = DELETE_ICON;
		Image image = imageDesc.createImage();
		this.setImageDescriptor(ImageDescriptor.createFromImage(image));
	}

	@Override
	public void run()
	{
//		logger.debug("Start - Deleting  Archived File");
		TreeViewer treeViewer = page.getLeftSideTreeViewer();
		TreeSelection treeSelection = ((TreeSelection) treeViewer.getSelection());
		if(treeSelection.isEmpty())
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "No File Selected",
						"Please select a file to delete.");
		}
		else if(treeSelection.getFirstElement() instanceof ArchivedFile)
		{
			int selectionIndex = treeViewer.getTree().indexOf((TreeItem) treeViewer.getTree().getSelection()[0]);
			List<?> inputArchivedFiles = (List<?>) treeViewer.getInput();
			ArchivedFile archivedFileToDelete = (ArchivedFile) treeSelection.getFirstElement();
			boolean delete = MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), "Delete Archive", 
					"Are you sure you want to delete this file \"" 
							+ archivedFileToDelete.getTypicalArchivedFile().getFileName() + "\" from the archive?");
			ArchiveEntryEditor archiveEntryEditor = (ArchiveEntryEditor) page.getEditor();
			if(delete)
			{
				boolean deleted = false;
				try
				{
					File archiveFolder = (archiveEntryEditor).getArchiveFolder();
					File fileArchive = new File(archiveFolder, 
							archivedFileToDelete.getTypicalArchivedFile().getFileName());
					(new FileInputStream(fileArchive)).close();
					logger.debug("Deleting File from the archive : \n" + fileArchive.getName());
					Files.delete(fileArchive.toPath());
					deleted = true;
				} catch (FileNotFoundException ex)
				{
					logger.error("The file was not found in the archive.\n" + ex.getMessage(), ex);
					deleted = true;
				} catch (NoSuchFileException ex)
				{
					logger.error("The file was not found in the archive.\n" + ex.getMessage(), ex);
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Delete Error", 
							"The file was not found in the archive.");
					deleted = true;
				} catch (DirectoryNotEmptyException ex)
				{
					logger.error("Error deleting non empty folder.\n" + ex.getMessage(), ex);
					MessageDialog.openWarning(Display.getCurrent().getActiveShell(), "Delete Error", 
							"The folder is not empty and could not be deleted from archive.");
				} catch (Exception ex)
				{
					logger.error("Error deleting file to archive.\n" + ex.getMessage(), ex);
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "Delete Error", 
							"The file could not be deleted from GRITS. "
									+ "Please report this error to the development team");
				}
				if(deleted)
				{
					try
					{
					inputArchivedFiles.remove(archivedFileToDelete);
					archiveEntryEditor.removeArchivedFile(archivedFileToDelete);} catch (Exception ex)
					{
						logger.error("Error removing entry from archive.\n" + ex.getMessage(), ex);
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Delete Error", 
								"Error removing entry from archive. "
										+ "Please report this error to the development team");
					}
				}
				treeViewer.refresh();
				selectionIndex = Math.max(0, selectionIndex - 1);
				if(selectionIndex >= 0 && selectionIndex < inputArchivedFiles.size())
				{
					treeViewer.getTree().select(treeViewer.getTree().getItem(selectionIndex));
					treeViewer.setSelection(treeViewer.getSelection());
				}
				else
					page.setSelection(null);
			}
		}

		logger.debug("End - Deleting  Archived File");
	}
}
