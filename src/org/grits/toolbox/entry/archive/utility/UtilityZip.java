package org.grits.toolbox.entry.archive.utility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.grits.toolbox.entry.archive.dialog.uploadfile.FileCopyProgressDialog;

/**
 * 
 * 
 * 
 */
public class UtilityZip
{
	private static final long MAX_FOLDER_SIZE = 5*1024*1024*1024;
	private static Logger logger = Logger.getLogger(UtilityZip.class);
	public static final int bufferSize = 10*1024*1024;

	/**
	 * zips the entire folder
	 * @param folderToCopy folder that is to be zipped
	 * @param parentFolder folder where the zip would be created
	 * @param zipFileName name of the zip file
	 * @return the zipped file
	 * @throws Exception
	 */
	public static File createZippedFolder(File folderToCopy, File parentFolder,
			String zipFileName) throws Exception
			{
		File zippedFile = null;
		if (folderToCopy.length() < MAX_FOLDER_SIZE)
		{
			logger.debug("Copying folder whose size is : " + folderToCopy.length());
			try
			{
				zippedFile = new File(parentFolder, zipFileName);
				FileOutputStream fos = new FileOutputStream(zippedFile);
				ZipOutputStream zipOutputStream = new ZipOutputStream(fos);
				FileCopyProgressDialog progressDialog = 
						new FileCopyProgressDialog(folderToCopy);
				progressDialog.openProgressBar();
				addFiles(folderToCopy, "", zipOutputStream, progressDialog);
				zipOutputStream.flush();
				zipOutputStream.close();
				fos.close();
				progressDialog.closeProgressBar();
			} catch (Exception ex)
			{
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
		else
			throw new Exception("Folder is too large to be compressed");
		return zippedFile;
			}

	private static void addFiles(File folderToCopy, String relativeZipPath,
			ZipOutputStream zipOutputStream, FileCopyProgressDialog progressDialog) throws Exception
			{
		String relativePath = null;
		for (File childFile : folderToCopy.listFiles())
		{
			if (childFile.exists())
			{
				relativePath = relativeZipPath == null || relativeZipPath.isEmpty() ?
						childFile.getName() 
						: relativeZipPath + File.separator + childFile.getName();
						if (childFile.isFile())
						{
							addFile(childFile, relativePath, zipOutputStream, progressDialog);
						}
						else if (childFile.isDirectory())
						{
							addDirectory(childFile, relativePath, zipOutputStream, progressDialog);
						}
			}
		}
			}

	/**
	 * adds file to a zip
	 * @param childFile
	 * @param relativePath path relative to the zip file
	 * e.g. if the file is to be directly inside zip, write the name of the file
	 * @param zipOutputStream
	 * @param progressDialog
	 * @throws Exception
	 */
	public static void addFile(File childFile, String relativePath,
			ZipOutputStream zipOutputStream, FileCopyProgressDialog progressDialog) throws Exception
			{
		if(childFile.isFile())
		{
			try
			{
				ZipEntry zipEntry = new ZipEntry(relativePath);
				zipOutputStream.putNextEntry(zipEntry);
				FileInputStream fis = new FileInputStream(childFile);
				byte[] buffer = new byte[bufferSize];
				int len;
				while ((len = fis.read(buffer)) > 0)
				{
					zipOutputStream.write(buffer, 0, len);
				}
				fis.close();
				zipOutputStream.closeEntry();
				if(progressDialog != null) progressDialog.updateProgressBar(childFile.length());
			} catch (Exception ex)
			{
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
			}

	/**
	 * adds directory to a zip
	 * @param childFile
	 * @param relativePath path relative to the zip file
	 * e.g. if the directly is to be directly inside zip, write the name of the directory
	 * @param zipOutputStream
	 * @param progressDialog
	 * @throws Exception
	 */
	public static void addDirectory(File childFile, String relativePath,
			ZipOutputStream zipOutputStream, FileCopyProgressDialog progressDialog) throws Exception
			{
		if(childFile.isDirectory())
		{
			try
			{
				ZipEntry zipEntry = new ZipEntry(relativePath + File.separator);
				zipOutputStream.putNextEntry(zipEntry);
				addFiles(childFile, relativePath, zipOutputStream, progressDialog);
				zipOutputStream.closeEntry();
			} catch (Exception ex)
			{
				logger.error(ex.getMessage(), ex);
				throw ex;
			}
		}
			}

	/**
	 * copies file while showing progress bar
	 * @param sourceFile file that has to be copied
	 * @param destinationFile file destination
	 * @throws Exception
	 */
	public static void copyFile(File sourceFile, File destinationFile) throws Exception
	{
		InputStream input = null;
		OutputStream output = null;
		try
		{
			input = new FileInputStream(sourceFile);
			output = new FileOutputStream(destinationFile);
			byte[] buf = new byte[bufferSize];
			int bytesRead = 0;
			FileCopyProgressDialog progressDialog = 
					new FileCopyProgressDialog(sourceFile);
			progressDialog.openProgressBar();
			int counter = 0;
			while((bytesRead = input.read(buf)) > 0)
			{
				output.write(buf, 0, bytesRead);
				if(counter%5 == 0)
				{
					progressDialog.updateProgressBar(bytesRead);
				}
				counter++;
			}
			input.close();
			output.close();
			progressDialog.closeProgressBar();
		} catch (Exception ex)
		{
			if(input != null)
				input.close();
			if(output != null)
				output.close();
			logger.error(ex.getMessage(), ex);
			throw ex;
		}
	}

	public static void copyDirectory(File sourceFile , File destinationFile) throws IOException 
	{
		if (sourceFile.isDirectory())
		{
			if (!destinationFile.exists())
			{
				destinationFile.mkdir();
			}

			for (String child : sourceFile.list())
			{
				copyDirectory(new File(sourceFile, child),
						new File(destinationFile, child));
			}
		}
		else
		{
			InputStream in = null;
			OutputStream out = null;
			try
			{
				in = new FileInputStream(sourceFile);
				out = new FileOutputStream(destinationFile);

				byte[] buf = new byte[bufferSize];
				int len;
				while ((len = in.read(buf)) > 0)
				{
					out.write(buf, 0, len);
				}
			} finally
			{
				IOUtils.closeQuietly(in);
				IOUtils.closeQuietly(out);
			}
		}
	}
}