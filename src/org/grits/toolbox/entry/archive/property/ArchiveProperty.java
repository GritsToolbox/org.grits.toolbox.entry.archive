/**
 * 
 */
package org.grits.toolbox.entry.archive.property;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.utilShare.DeleteUtils;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.command.NewArchive;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.io.ArchivePropertyWriter;
import org.grits.toolbox.entry.archive.utility.UtilityZip;

/**
 * 
 *
 */
public class ArchiveProperty extends Property 
{
    private static Logger logger = Logger.getLogger(ArchiveProperty.class);
    public static final String CURRENT_VERSION = "1.0";
    public static final String TYPE = "org.grits.toolbox.property.archive";
    protected static PropertyWriter writer = new ArchivePropertyWriter();
    private int viewerRank = 500;

    public ArchiveProperty()
    {
        super();
    }

	public PropertyDataFile getArchiveFile()
	{
		PropertyDataFile archiveFile = null;
		for(PropertyDataFile dataFile : dataFiles)
		{
			if(IConfig.ARCHIVE_FOLDER_TYPE.equals(dataFile.getType()))
			{
				archiveFile = dataFile;
				break;
			}
		}
		return archiveFile;
	}
    
    public void setArchiveFile(PropertyDataFile propertyDataFile)
    {
		dataFiles = Arrays.asList(propertyDataFile);
	}
	
	@Override
    public String getType() {
        return ArchiveProperty.TYPE;
    }

    @Override
    public PropertyWriter getWriter() {
        return ArchiveProperty.writer;
    }

    @Override
    public ImageDescriptor getImage() {
        return ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.BOOKS_ICON);
    }

    @Override
    public void delete(Entry entry) 
    {
    	logger.debug("Deleting Archive Entry");
    	if(entry != null && entry.getProperty().getType().equals(ArchiveProperty.TYPE))
    	{
            try
            {
            	File archiveFolder = getFolderForArchiveEntry(entry);
            	if(archiveFolder != null)
            		DeleteUtils.delete(archiveFolder);
            } catch (IOException e)
            {
                ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot Delete Entry", e);
            	logger.error(e.getMessage(), e);
            }
    	}
    	logger.debug("Deleting Archive Entry");
    }

	@Override
	public Object clone() 
	{
		return null;
	}

	@Override
	public Property getParentProperty() 
	{
		return null;
	}
	
	public static File getFolderForArchiveEntry(Entry entry)
	{
		File archiveFolder = null;
		logger.debug("Start : Retrieving Archive Folder for Entry.");
		if(entry != null && entry.getProperty().getType().equals(ArchiveProperty.TYPE))
		{
			try
			{
				ArchiveProperty archiveProperty = (ArchiveProperty) entry.getProperty();
				String archiveFolderName = archiveProperty.getArchiveFile().getName();
				Entry projectEntry = DataModelSearch.findParentByType(entry, ProjectProperty.TYPE);
				String workspaceLocation = PropertyHandler.getVariable("workspace_location");
				String folderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
						+ File.separator + projectEntry.getDisplayName()
						+ File.separator + IConfig.ARCHIVE_FOLDER_NAME
						+ File.separator + archiveFolderName;
				archiveFolder = new File(folderLocation);
			} catch (Exception e)
			{
				ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), "Cannot retrieve archive folder", e);
				logger.error(e.getMessage(), e);
			}
		}
		logger.debug("End : Retrieving Archive Folder for Entry.");
		return archiveFolder;
	}

	@Override
	public boolean isRenamable()
	{
		return false;
	}
	
	/**
	 * returns the rank of the Archive entry in project explorer view
	 */
	@Override
	public int getViewerRank()
	{
		return this.viewerRank;
	}

	/**
	 * sets the rank of the Archive Entry in project explorer view
	 */
	@Override
	public void setViewerRank(int viewerRank)
	{
		this.viewerRank = viewerRank;
	}

	@Override
	public void makeACopy(Entry currentEntry, Entry destinationEntry)
			throws IOException
	{
		try
		{
			
			File currentArchiveFolder = getFolderForArchiveEntry(currentEntry);
			if(currentArchiveFolder.exists())
			{
				File destinationArchive = NewArchive.getArchiveDirectory(
						DataModelSearch.findParentByType(destinationEntry.getParent(), ProjectProperty.TYPE));
				// create a unique folder name inside the folder
				String newArchiveFolderName = NewArchive.generateFolderName(destinationArchive.list());
				File newArchiveFolder = new File(destinationArchive, newArchiveFolderName);

				UtilityZip.copyDirectory(currentArchiveFolder, newArchiveFolder);

				List<PropertyDataFile> dataFiles = new ArrayList<PropertyDataFile>();
				PropertyDataFile currentArchiveDataFile = getArchiveFile();
				dataFiles.add(new PropertyDataFile(newArchiveFolderName, 
						currentArchiveDataFile.getVersion(), currentArchiveDataFile.getType()));
				ArchiveProperty archiveProperty = new ArchiveProperty();
				archiveProperty.setDataFiles(dataFiles);
				archiveProperty.setRemoved(!exists());
				archiveProperty.setVersion(getVersion());
				archiveProperty.setViewerRank(getViewerRank());
				destinationEntry.setProperty(archiveProperty);
			}
			else throw new FileNotFoundException("Could not find archive file for selected archive \"" 
					+ currentEntry.getDisplayName() + "\" in project \"" 
					+ DataModelSearch.findParentByType(currentEntry, ProjectProperty.TYPE).getDisplayName()
					+ "\"");
		} catch (FileNotFoundException ex)
		{
			throw ex;
		} catch (IOException ex)
		{
			throw new IOException("Error copying archive information.\n" + ex.getMessage(), ex);
		} catch (Exception ex)
		{
			throw new IOException("Error copying archive information.\n" + ex.getMessage(), ex);
		}
	}
}
