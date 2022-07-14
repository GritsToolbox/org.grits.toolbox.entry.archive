/**
 * 
 */
package org.grits.toolbox.entry.archive.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.grits.toolbox.core.datamodel.io.PropertyWriter;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.property.ArchiveProperty;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * 
 *
 */
public class ArchivePropertyWriter implements PropertyWriter {

	@Override
	public void write(Property property, Element propertyElement)
			throws IOException 
	{
		if(property instanceof ArchiveProperty)
		{
			ArchiveProperty archiveProperty = (ArchiveProperty) property;
			if(archiveProperty.getArchiveFile() != null 
					&& archiveProperty.getArchiveFile().getName() != null)
			{
				Element fileElement = new Element("file");
				List<Attribute> attributes = new ArrayList<Attribute>();
				attributes.add(new Attribute("name", archiveProperty.getArchiveFile().getName()));
				attributes.add(new Attribute("version", ArchiveProperty.CURRENT_VERSION));
				attributes.add(new Attribute("type", IConfig.ARCHIVE_FOLDER_TYPE));
				fileElement.setAttributes(attributes);
				propertyElement.setContent(fileElement);
			}
			else
				throw new IOException("Archive property could not be added as "
						+ "its archive file is missing.");
		}
		else
		{
			throw new IOException("This property is not a Archive Property");
		}
	}

}
