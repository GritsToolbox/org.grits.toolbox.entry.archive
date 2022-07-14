/**
 * 
 */
package org.grits.toolbox.entry.archive.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.model.Archive;
import org.grits.toolbox.entry.archive.property.ArchiveProperty;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * 
 *
 */
public class ReaderVersion0
{
	public static final String PROJECT_XML_ARCHIVE_ELEMENT = "archive";
	public static final String PROJECT_XML_FOLDER_ATTRIBUTE = "folder";

	public static Property read(Element propertyElement, ArchiveProperty archiveProperty) throws IOException
	{
		Element archiveElement = propertyElement.getChild(PROJECT_XML_ARCHIVE_ELEMENT);
		if (archiveElement == null )
		{
			throw new IOException("Archive property misses element \"" + PROJECT_XML_ARCHIVE_ELEMENT + "\"");
		}

		Attribute folderAttribute = archiveElement.getAttribute(PROJECT_XML_FOLDER_ATTRIBUTE);
		if(folderAttribute != null)
		{
			List<PropertyDataFile> dataFiles = new ArrayList<PropertyDataFile>();
			PropertyDataFile propertyDataFile = new PropertyDataFile(
					folderAttribute.getValue(), 
					Archive.CURRENT_VERSION,
					IConfig.ARCHIVE_FOLDER_TYPE);

			dataFiles.add(propertyDataFile);
			archiveProperty.setDataFiles(dataFiles);
			return archiveProperty;
		}
		else 
			throw new IOException("Archive property misses folder attribute.");
	}

}
