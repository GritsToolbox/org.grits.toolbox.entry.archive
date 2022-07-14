/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.dialog.uploadfile.DownloadAsZipDialog;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.FileArchivePage;

/**
 * 
 *
 */
public class DownloadFilesAsZip extends Action
{
	private static Logger logger = Logger.getLogger(DownloadFilesAsZip.class);

	private FileArchivePage page = null;

	public DownloadFilesAsZip(FileArchivePage fileArchivePage)
	{
		page = fileArchivePage;
		this.setText("Download Files as Zip");
		this.setToolTipText("Download Files as Zip");
		this.setImageDescriptor(ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.DOWNLOAD_ICON));
	}

	@Override
	public void run()
	{
		logger.debug("Start - Downloading  Archive File");
		TreeViewer treeViewer = page.getLeftSideTreeViewer();
		DownloadAsZipDialog downloadAsZipDialog = new DownloadAsZipDialog(page);
		if(downloadAsZipDialog.open() == Window.OK)
		{
			treeViewer.setSelection(treeViewer.getSelection());
			logger.debug("Downloaded successfully selected files." );
		}
	}
}
