/**
 * 
 */
package org.grits.toolbox.entry.archive.config;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.entry.archive.Activator;

/**
 * 
 *
 */
public interface IConfig 
{
	public static final String ARCHIVE_ENTRY_DISPLAY_NAME = "Archive";
	public static final String ARCHIVE_FOLDER_TYPE = "archive_folder";

	public static final String ARCHIVE_FOLDER_NAME = "Archive";
	public static final String FOLDER_NAME_PREFIX = "Archive_";
	public static final int FOLDER_NAME_RANDOM_CHARACTERS_LENGTH = 5;

	public static final String ARCHIVE_FILE_NAME = "archive.xml";

	public static final String NO_SELECTION_IN_COMBO = "  --  No Selection -- ";

	public static final List<String> ARCHIVABLE_PLUGINS = 
			Arrays.asList(ProjectProperty.TYPE, "org.grits.toolbox.property.sample");
}
