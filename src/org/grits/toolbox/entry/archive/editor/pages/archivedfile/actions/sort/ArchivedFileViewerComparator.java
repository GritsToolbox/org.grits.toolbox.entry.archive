/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.sort;

import java.io.File;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.model.ArchivedBasicEmail;
import org.grits.toolbox.entry.archive.model.ArchivedEmail;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.utility.UtilityFileName;

/**
 * 
 *
 */
public class ArchivedFileViewerComparator  extends ViewerComparator
{
	private int column;
	private boolean ascending;
	private ArchiveEntryEditor archiveEntryEditor = null;
	
	public ArchivedFileViewerComparator(ArchiveEntryEditor archiveEntryEditor)
	{
		this.archiveEntryEditor  = archiveEntryEditor;
	}

	/**
	 * meant for trees with a label provider
	 */
	public int compare(Viewer viewer, Object object1, Object object2)
	{
		int comparision = 0;
		if(viewer instanceof TreeViewer)
		{
			if(object1 != null && object2 != null)
			{
				if(viewer instanceof TreeViewer && column >= 0)
				{
					if(object1 instanceof ArchivedFile && object2 instanceof ArchivedFile)
					{
					ArchivedFile archivedFile1 = ((ArchivedFile) object1);
					ArchivedFile archivedFile2 = ((ArchivedFile) object2);
					if(column == 0)
					{
						String text1 = archivedFile1.getTypicalArchivedFile() != null 
								&& archivedFile1.getTypicalArchivedFile().getFileName() != null ? 
										archivedFile1.getTypicalArchivedFile().getFileName() : "";

						String text2 = archivedFile2.getTypicalArchivedFile() != null 
												&& archivedFile2.getTypicalArchivedFile().getFileName() != null ? 
												archivedFile2.getTypicalArchivedFile().getFileName() : "";

						comparision = text1.compareToIgnoreCase(text2);
					}
					else if(column == 1)
					{
						String fileLocation1 = archiveEntryEditor.getArchiveFolder()
								+ File.separator + archivedFile1.getTypicalArchivedFile().getFileName();

						String fileLocation2 = archiveEntryEditor.getArchiveFolder()
								+ File.separator + archivedFile2.getTypicalArchivedFile().getFileName();

						String size1 = UtilityFileName.getFileSize(new File(fileLocation1));
						String size2 = UtilityFileName.getFileSize(new File(fileLocation2));

						comparision = this.compareSize(size1, size2);
					}
					}
					else if(object1 instanceof ArchivedEmail && object2 instanceof ArchivedEmail)
					{
						if(((!(object1 instanceof ArchivedBasicEmail)) 
								|| (((ArchivedBasicEmail) object1).getArchivedEmailTopic() == null))
								&& ((!(object2 instanceof ArchivedBasicEmail))
										|| (((ArchivedBasicEmail) object2).getArchivedEmailTopic() == null)))
						{
							ArchivedEmail archivedEmail1 = ((ArchivedEmail) object1);
							ArchivedEmail archivedEmail2 = ((ArchivedEmail) object2);
							if(column == 0)
							{
								String text1 = archivedEmail1.getDisplayTitle() == null ? "" : archivedEmail1.getDisplayTitle();
								String text2 = archivedEmail2.getDisplayTitle() == null ? "" : archivedEmail2.getDisplayTitle();
								comparision = text1.compareToIgnoreCase(text2);
							}
							else if(column == 1)
							{
								comparision = archivedEmail1.getEmailDate() != null 
										&& archivedEmail2.getEmailDate() != null 
										? archivedEmail1.getEmailDate().compareTo(archivedEmail2.getEmailDate()) : 0;
							}
						}
					}
				}
			}
		}

		comparision = ascending ? comparision : -comparision;
		return comparision;
	}

	private int compareSize(String size1, String size2)
	{
		int comparision = 0;
		String[] sizeArray1 = size1.split(" ");
		String[] sizeArray2 = size2.split(" ");
		if(sizeArray1.length == 2 && sizeArray2.length == 2)
		{
			if(sizeArray1[1].equals(sizeArray2[1]))
			{
				comparision = compareFloatValue(sizeArray1[0], sizeArray2[0]);
			}
			else
			{
				comparision = getSuffixRank(sizeArray1[1]) - getSuffixRank(sizeArray2[1]);
			}

		}
		return comparision;
	}

	private int getSuffixRank(String suffix)
	{
		switch (suffix)
		{
		case "KB":
			return 1;
		case "MB":
			return 2;
		case "GB":
			return 3;
		default:
			return 0;
		}
	}

	private int compareFloatValue(String text1, String text2)
	{
		try
		{
			float intValue1 = Float.parseFloat(text1);
			float intValue2 = Float.parseFloat(text2);
			return (int) Math.signum(intValue1 - intValue2);
		} catch (NumberFormatException ex)
		{
			return 0;
		}
	}

	public void setColumn(int column)
	{
		this.column = column;
	}

	public void setAscending(boolean ascending)
	{
		this.ascending = ascending;
	}

	public boolean getAscending()
	{
		return this.ascending;
	}
}
