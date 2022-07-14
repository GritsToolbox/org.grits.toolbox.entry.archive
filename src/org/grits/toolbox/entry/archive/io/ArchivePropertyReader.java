package org.grits.toolbox.entry.archive.io;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.datamodel.io.PropertyReader;
import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.archive.property.ArchiveProperty;
import org.jdom.Element;

/**
 * 
 * 
 *
 */
public class ArchivePropertyReader extends PropertyReader
{
	private static final Logger logger = Logger.getLogger(ArchivePropertyReader.class);

	@Override
	public Property read(Element propertyElement) throws IOException, UnsupportedVersionException
	{
		ArchiveProperty archiveProperty = new ArchiveProperty();

		PropertyReader.addGenericInfo(propertyElement, archiveProperty);

		if(archiveProperty.getVersion() == null)
		{
			logger.debug("Loading pre-versioning archive.");
			return ReaderVersion0.read(propertyElement, archiveProperty);
		}
		else if(archiveProperty.getVersion().equals("1.0"))
		{
			logger.debug("Loading archive version \"1.0\".");
			return ReaderVersion1.read(propertyElement, archiveProperty);
		}
		else 
			throw new UnsupportedVersionException(
					"This archive version is currently not supported.", archiveProperty.getVersion());
	}
}
