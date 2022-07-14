/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions;

import java.io.File;
import java.nio.file.Paths;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.FileArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.utility.UniqueNameValidator;
import org.grits.toolbox.entry.archive.utility.UtilityFileName;
import org.grits.toolbox.entry.archive.utility.UtilityZip;

/**
 * 
 *
 */
public class UpdateArchivedFile extends Action
{
	private static Logger logger = Logger.getLogger(UpdateArchivedFile.class);
	private FileArchivePage page = null;

	public UpdateArchivedFile(FileArchivePage fileArchivePage)
	{
		page = fileArchivePage;
		this.setText("Update");
		this.setToolTipText("Update the File");
		this.setImageDescriptor(ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.UPDATE_ICON));
	}

	@SuppressWarnings({"unchecked" })
	@Override
	public void run()
	{
		logger.debug("Start - Updating  Archive File");
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
					String previousName = selectedArchivedFile.getTypicalArchivedFile().getFileName();
					HashSet<String> existingNames = UtilityFileName.getExistingNames
							(inputArchivedFiles, archiveFolder);
					existingNames.remove(previousName);
					FileDialog updateDialog = new FileDialog(Display.getCurrent().getActiveShell(), SWT.OPEN);
					String location = null;
					if((location = updateDialog.open()) != null)
					{
						File sourceFile = Paths.get(location).toFile();
						if(sourceFile.exists())
						{
							String newName = sourceFile.getName();
							boolean uniqueName = !existingNames.contains(newName);
							boolean quitUpdate = false;
							while(!uniqueName && !quitUpdate)
							{
								InputDialog renameDialog = new InputDialog(Display.getCurrent().getActiveShell(), "RenameArchivedFile", 
										"Please give a unique name to this archive",
										newName, 
										new UniqueNameValidator(existingNames));
								if(renameDialog.open() == Window.OK)
								{
									newName = renameDialog.getValue();
								}
								else
								{
									quitUpdate = MessageDialog.openQuestion(
											Display.getCurrent().getActiveShell(), "Name not Unique", 
											"Do you want to quit updating the archive file?");
								}
								uniqueName = !existingNames.contains(newName);
							}
							if(uniqueName)
							{
								File oldFile = new File(archiveFolder, previousName);
								File newFile = new File(archiveFolder, newName);
								UtilityZip.copyFile(sourceFile, newFile);
								Date modifiedDate = archiveEntryEditor.renameArchive(previousName, newName);
								selectedArchivedFile.setModifiedDate(modifiedDate);
								selectedArchivedFile.getTypicalArchivedFile().setFileName(newName);
								treeViewer.refresh();
								treeViewer.setSelection(treeViewer.getSelection());
								if(!newName.equals(previousName))
									oldFile.delete();
								logger.info("Updated successfully : " 
										+ selectedArchivedFile.getTypicalArchivedFile().getFileName());
							}
						}
						else
						{

							logger.debug("Updated unsuccessful : " 
									+ selectedArchivedFile.getTypicalArchivedFile().getFileName());
							MessageDialog.openError(Display.getCurrent().getActiveShell(), 
									"UpdateArchivedFile Unsuccessful", 
									"The source file is not valid.");
						}
					}
				} catch (Exception ex)
				{
					logger.error(ex.getMessage(), ex);
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "UpdateArchivedFile Unsuccessful", 
							"The archive file could not be updated.");
				}
			}
		}
		else
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "No File Selected",
					"Please select a file to update.");
		}
	}
}
