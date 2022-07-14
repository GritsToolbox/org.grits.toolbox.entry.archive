/**
 * 
 */
package org.grits.toolbox.entry.archive.io;

import java.io.IOException;

import org.grits.toolbox.core.datamodel.property.Property;
import org.grits.toolbox.entry.archive.property.ArchiveProperty;
import org.jdom.Element;

/**
 * 
 *
 */
public class ReaderVersion1
{
	public static Property read(Element propertyElement, ArchiveProperty property) throws IOException
	{
		return property;
	}

}
