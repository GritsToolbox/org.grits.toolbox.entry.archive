/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.FileArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.utility.UniqueNameValidator;
import org.grits.toolbox.entry.archive.utility.UtilityFileName;

/**
 * 
 *
 */
public class RenameArchivedFile extends Action
{
	private static Logger logger = Logger.getLogger(RenameArchivedFile.class);
	private FileArchivePage page = null;

	public RenameArchivedFile(FileArchivePage fileArchivePage)
	{
		page = fileArchivePage;
		this.setText("Rename");
		this.setToolTipText("Rename the File");
		this.setImageDescriptor(ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.RENAME_ICON));
	}

	@SuppressWarnings({"unchecked" })
	@Override
	public void run()
	{
		logger.debug("Start - Renaming  Archive File");
		TreeViewer treeViewer = page.getLeftSideTreeViewer();
		if(!treeViewer.getSelection().isEmpty())
		{
			ArchivedFile selectedArchivedFile = (ArchivedFile) 
					((TreeSelection) treeViewer.getSelection()).getFirstElement();
			{
				List<ArchivedFile> inputArchivedFiles = (List<ArchivedFile>) treeViewer.getInput();
				ArchiveEntryEditor archiveEntryEditor = (ArchiveEntryEditor) page.getEditor();
				File archiveFolder = archiveEntryEditor.getArchiveFolder();
				try
				{
					HashSet<String> existingNames = UtilityFileName.getExistingNames
							(inputArchivedFiles, archiveFolder);
					existingNames.remove(selectedArchivedFile.getTypicalArchivedFile().getFileName());
					InputDialog renameDialog = new InputDialog(Display.getCurrent().getActiveShell(), "RenameArchivedFile", 
							"Please give a unique name to this archive",
							selectedArchivedFile.getTypicalArchivedFile().getFileName(), 
							new UniqueNameValidator(existingNames));
					if(renameDialog.open() == Window.OK)
					{
						String previousName = selectedArchivedFile.getTypicalArchivedFile().getFileName();
						String newName = renameDialog.getValue();
						if((new File(archiveFolder, previousName))
								.renameTo(new File(archiveFolder, newName)))
						{
							Date modifiedDate = archiveEntryEditor.renameArchive(previousName, newName);
							selectedArchivedFile.setModifiedDate(modifiedDate);
							selectedArchivedFile.getTypicalArchivedFile().setFileName(newName);
							treeViewer.refresh();
							treeViewer.setSelection(treeViewer.getSelection());
							logger.debug("Renamed successfully : " 
									+ selectedArchivedFile.getTypicalArchivedFile().getFileName());
						}
						else
						{
							MessageDialog.openError(Display.getCurrent().getActiveShell(), "RenameArchivedFile Unsuccessful", 
									"The archive file could not be renamed.");
						}
					}
				} catch (Exception ex)
				{
					logger.error(ex.getMessage(), ex);
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "RenameArchivedFile Unsuccessful", 
							"The archive file could not be renamed.");
				}
			}
		}
		else
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "No File Selected",
					"Please select a file to rename.");
		}
	}
}
