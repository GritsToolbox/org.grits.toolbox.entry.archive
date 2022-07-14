/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.dialog.uploadfile.UploadDialog;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.FileArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.utility.UtilityFileName;
import org.grits.toolbox.entry.archive.utility.UtilityZip;

/**
 * 
 *
 */
public class UploadArchivedFile extends Action
{
	private static Logger logger = Logger.getLogger(UploadArchivedFile.class);
	// https://www.iconfinder.com/icons/314462/folder_upload_icon#size=16 (upload.png)
	private FileArchivePage page = null;

	public UploadArchivedFile(FileArchivePage fileArchivePage)
	{
		page = fileArchivePage;
		this.setText("Upload a File");
		this.setToolTipText("Upload a File");
		this.setImageDescriptor(ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.UPLOAD_FOLDER_ICON));
	}

	@SuppressWarnings("unchecked")
	@Override
	public void run()
	{
		logger.debug("Start - Uploading  Archive File");
		ArchiveEntryEditor archiveEntryEditor = (ArchiveEntryEditor) page.getEditor();
		TreeViewer treeViewer = page.getLeftSideTreeViewer();
		List<ArchivedFile> inputArchivedFiles = (List<ArchivedFile>) treeViewer.getInput();

		File archiveFolder = archiveEntryEditor.getArchiveFolder();
		try
		{
			UploadDialog uploadDialog = new UploadDialog(Display.getCurrent().getActiveShell(),
					UtilityFileName.getExistingNames(inputArchivedFiles, archiveFolder));
			if(uploadDialog.open() == Window.OK)
			{

				ArchivedFile newArchivedFile = uploadDialog.getArchivedFile();
				final File fileToArchive = uploadDialog.getSourceFile();
				if(archiveFolder.exists() && fileToArchive.exists())
				{
					try
					{
						String archiveFileName = newArchivedFile.getTypicalArchivedFile().getFileName();
						if(!UtilityFileName.checkUnique(archiveFolder, archiveFileName))
						{
							archiveFileName = UtilityFileName.generateUniqueFileName(archiveFolder, archiveFileName);
						}

						File archiveFile = null;
						if(fileToArchive.isDirectory())
						{
							try
							{
								if(fileToArchive.getAbsolutePath().contains(archiveFolder.getAbsolutePath()))
								{	MessageDialog.openError(Display.getCurrent().getActiveShell(), "Upload Error", 
										"You cannot upload the archive folder or its content.\n");
								}
								else
								{
									archiveFile = UtilityZip.createZippedFolder(fileToArchive, archiveFolder, archiveFileName);
								}
							} catch (Exception ex)
							{
								logger.error("Folder upload not supported yet.\n" + ex.getMessage(), ex);
								MessageDialog.openError(Display.getCurrent().getActiveShell(), "Upload Error", 
										"Error uploading folder.\n" + ex.getMessage());
							}
						}
						else
						{
							archiveFile = new File(archiveFolder, archiveFileName);
							UtilityZip.copyFile(fileToArchive, archiveFile);
						}
						if(archiveFile != null)
						{
							newArchivedFile.getTypicalArchivedFile().setFileName(archiveFileName);
							inputArchivedFiles.add(newArchivedFile);
							treeViewer.setInput(inputArchivedFiles);
							archiveEntryEditor.saveModel();
						}

					} catch (Exception ex)
					{
						logger.error("Error uploading file to archive.\n" + ex.getMessage(), ex);
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Upload Error", 
								"The file could not be uploaded to GRITS. "
										+ "Please report this error to the development team");
					}
				}

				page.refreshPage();
				int i = 0;
				int selectionIndex = 0;
				for(ArchivedFile archivedFile : inputArchivedFiles)
				{
					if(archivedFile.getTypicalArchivedFile().getFileName().equals(
							newArchivedFile.getTypicalArchivedFile().getFileName()))
					{
						selectionIndex = i;
					}
					i++;
				}
				TreeItem selectionItem = treeViewer.getTree().getItem(selectionIndex);
				treeViewer.getTree().select(selectionItem);
				treeViewer.setSelection(treeViewer.getSelection());
			}
		} catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Upload Error", 
					"The file could not be uploaded to GRITS. "
							+ "Please report this error to the development team");
		}

		logger.debug("End - Uploading  Archive File");
	}
}
