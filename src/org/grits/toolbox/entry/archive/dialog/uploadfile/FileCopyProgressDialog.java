/**
 * 
 */
package org.grits.toolbox.entry.archive.dialog.uploadfile;

import java.io.File;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 *
 */
public class FileCopyProgressDialog
{
	private static Logger logger = Logger.getLogger(FileCopyProgressDialog.class);
	public ProgressBar progressBar = null;
	public long totalBytes = 0;
	private long totalByteCopied = 0;
	private int dotCounter = 0;

	public FileCopyProgressDialog(File fileToArchive)
	{
		if(fileToArchive != null && fileToArchive.exists())
		{
			init();
			totalBytes = getFileSize(fileToArchive);
		}
	}

	private long getFileSize(File file)
	{
		if(file.isFile())
			return file.length();
		else if(file.isDirectory())
		{
			long totalBytes = 0;
			for(File childFile : file.listFiles())
			{
				totalBytes += childFile.isFile() 
						? childFile.length() : getFileSize(childFile);
			}
			return totalBytes;
		}
		else return 0;
	}

	public void init()
	{
		Shell shell = new Shell(Display.getCurrent().getActiveShell());
		shell.setLocation(600, 400);
		progressBar  = new ProgressBar(shell, 
				SWT.HORIZONTAL|SWT.SMOOTH);
		progressBar.getShell().setSize(330, 80);
		progressBar.getShell().setText("File Copy (Progress " + 0 + "%)");
		progressBar.setMinimum(0);
		progressBar.setMaximum(100);
		progressBar.setSelection(0);
		progressBar.setBounds(10, 10, 300, 20);
		logger.debug(totalBytes);
	}
	
	public void openProgressBar()
	{
		logger.debug("Opening progress bar");
		progressBar.getShell().open();
	}

	public void updateProgressBar(long byteIncrement)
	{
		totalByteCopied += byteIncrement;
		progressBar.getDisplay().syncExec(new Runnable()
		{

			@Override
			public void run() {
				if (!progressBar.isDisposed())
				{
					String copyDot = "";
					for(int i = 0; i < 4; i++) copyDot += i <= (dotCounter%3) ? "." : " ";
					int copyPercentage = (int) (100*((float) totalByteCopied/totalBytes));
					progressBar.getShell().setText("File Copy (Copying" + copyDot + 
							+ copyPercentage + "%)");
					dotCounter++;
					progressBar.setSelection(copyPercentage);
//					logger.debug(copyPercentage + "%");
				}
			}
		});
	}

	public void closeProgressBar()
	{
		logger.debug("Disposing progress bar");
		progressBar.getDisplay().asyncExec(new Runnable() {
			@Override
			public void run()
			{
				if(!progressBar.getShell().isDisposed())
				{
					progressBar.getShell().dispose();
				}
			}
		});
	}
}
