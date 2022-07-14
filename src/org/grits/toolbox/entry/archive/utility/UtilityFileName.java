/**
 * 
 */
package org.grits.toolbox.entry.archive.utility;

import java.io.File;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.grits.toolbox.entry.archive.model.ArchivedFile;

/**
 * 
 *
 */
public class UtilityFileName
{
	private static Logger logger = Logger.getLogger(UtilityFileName.class);

	/**
	 * generates a unique file name from the suggested name by adding _N where N is any number
	 * @param parentFolder the folder in which unique name is to be found
	 * @param suggestedName the name to which suffix is added to make it unique
	 * @return the unique name of the file (does not include path information)
	 */
	public static String generateUniqueFileName(File parentFolder, String suggestedName)
	{
		boolean unique = false;
		String uniqueName = null;
		int i = 0;
		do
		{
			String[] nameParts = suggestedName.split("\\.");
			if(nameParts.length > 0)
			{
				appendSuffix(nameParts, "_" + i);
				uniqueName = "";
				for(String namePart : nameParts)
				{
					if(namePart != null)
						uniqueName += namePart + ".";
				}
				uniqueName = uniqueName.substring(0, uniqueName.length()-1);
				unique = checkUnique(parentFolder, uniqueName);
				i++;
			}
			else
				break;
		}
		while(!unique);

		return uniqueName;
	}

	/**
	 * appends suffix to first non-empty part
	 * @param nameParts array of strings that could be a part of file name
	 * @param suffix appending the suffix
	 */
	private static void appendSuffix(String[] nameParts, String suffix)
	{
		int i = 0;
		boolean appended = false;
		while(i < nameParts.length && !appended)
		{
			if(!nameParts[i].isEmpty())
			{
				nameParts[i] = nameParts[i] + suffix;
				appended = true;
			}
			i++;
		}
	}

	/**
	 * checks if the name of the file is unique in the given folder
	 * @param parentFolder the folder in which the file is to unique
	 * @param fileName name of the file that is to be checked
	 * @return false if not unique else true
	 */
	public static boolean checkUnique(File parentFolder, String fileName)
	{
		boolean unique = true;
		for(File childFile : parentFolder.listFiles())
		{
			logger.debug("Matching " + fileName + " with : " + childFile.getName());
			if(childFile.getName().equals(fileName))
			{
				unique = false;
				break;
			}
		}
		return unique;
	}

	/**
	 * names of the existing archived files or files that are inside the archive folders
	 * includes archive names in the archive xml file 
	 * along with the names of the file that are inside the archive folder
	 * @param inputArchivedFiles list of archive files in the archive xml
	 * @param archiveFolder folder containing the archives
	 * @return list of existing names
	 * @throws Exception
	 */
	public static HashSet<String> getExistingNames(
			List<ArchivedFile> inputArchivedFiles, File archiveFolder) throws Exception
			{
		try
		{
			HashSet<String> existingNames = new HashSet<String>();
			if(inputArchivedFiles != null)
			{
				for(ArchivedFile archiveFile : inputArchivedFiles)
				{
					existingNames.add(archiveFile.getTypicalArchivedFile().getFileName());
				}
			}
			if(archiveFolder != null && archiveFolder.isDirectory())
			{
				for(File archive : archiveFolder.listFiles())
				{
					existingNames.add(archive.getName());
				}
			}
			return existingNames;
		} catch (Exception ex)
		{
			throw ex;
		}
			}

	/**
	 * returns the size of the file
	 * @param file whose size is to be determined
	 * @return the string with "KB", "MB" or other appropriate value
	 */
	public static String getFileSize(File file)
	{
		String fileSize = "0 KB";
		if(file != null)
		{
			DecimalFormat df = new DecimalFormat("#.##");
			if(file != null && file.exists())
			{
				long size = file.length();
				if(size < 1024f)
				{
					fileSize = df.format(size) + " B";
				}
				else if(size < 1024f*1024f)
				{
					fileSize = df.format(size / 1024f) + " KB";
				}
				else if(size < 1024f*1024f*1024f)
				{
					fileSize = df.format(size / (1024f*1024f)) + " MB";			
				}
				else
				{
					fileSize = df.format(size / (1024f*1024f*1024f)) + " GB";
				}
			}
		}
		return fileSize;
	}
}
