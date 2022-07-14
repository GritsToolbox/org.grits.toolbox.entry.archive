/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.dialog.uploadfile.DownloadFileDialog;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.FileArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedFile;

/**
 * 
 *
 */
public class DownloadFile extends Action
{
	private static Logger logger = Logger.getLogger(DownloadFile.class);

	private FileArchivePage page = null;

	public DownloadFile(FileArchivePage fileArchivePage)
	{
		page = fileArchivePage;
		this.setText("Download");
		this.setToolTipText("Download the File");
		this.setImageDescriptor(ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.DOWNLOAD_FILE_ICON));
	}

	@Override
	public void run()
	{
		logger.debug("Start - Downloading  Archive File");
		TreeViewer treeViewer = page.getLeftSideTreeViewer();
		if(!treeViewer.getSelection().isEmpty())
		{
			ArchivedFile selectedArchivedFile = (ArchivedFile) 
					((TreeSelection) treeViewer.getSelection()).getFirstElement();
			{
				DownloadFileDialog downloadDialog = new DownloadFileDialog(
						page, selectedArchivedFile);
				if(downloadDialog.open() == Window.OK)
				{
					treeViewer.setSelection(treeViewer.getSelection());
					logger.debug("Downloaded successfully : " 
							+ selectedArchivedFile.getTypicalArchivedFile().getFileName());
				}
			}
		}
		else
		{
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "No File Selected",
					"Please select a file to download.");
		}
	}
}
