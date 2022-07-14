/**
 * 
 */
package org.grits.toolbox.entry.archive.command;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.core.datamodel.io.ProjectFileHandler;
import org.grits.toolbox.core.datamodel.property.ProjectProperty;
import org.grits.toolbox.core.datamodel.property.PropertyDataFile;
import org.grits.toolbox.core.datamodel.util.DataModelSearch;
import org.grits.toolbox.core.service.IGritsDataModelService;
import org.grits.toolbox.core.service.IGritsUIService;
import org.grits.toolbox.core.utilShare.ErrorUtils;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.dialog.newentry.NewArchiveDialog;
import org.grits.toolbox.entry.archive.model.Archive;
import org.grits.toolbox.entry.archive.property.ArchiveProperty;

/**
 * 
 *
 */
public class NewArchive {

	//log4J Logger
	private static final Logger logger = Logger.getLogger(NewArchive.class);

	@Inject private static IGritsDataModelService gritsDataModelService = null;
	@Inject static IGritsUIService gritsUIService = null;

	@Execute
	public void execute(@Named(IServiceConstants.ACTIVE_SELECTION) Object object,
			IEventBroker eventBroker, @Named (IServiceConstants.ACTIVE_SHELL) Shell shell)
	{
		logger.info("- START COMMAND : Create a new Archive. ");
		try
		{
			Entry selectedEntry = null;
			if(object instanceof Entry)
			{
				selectedEntry = (Entry) object;
			}
			else if (object instanceof StructuredSelection)
			{
				if(((StructuredSelection) object).getFirstElement() instanceof Entry)
				{
					selectedEntry = (Entry) ((StructuredSelection) object).getFirstElement();
				}
			}
			// try getting the last selection from the data model
			if(selectedEntry == null
					&& gritsDataModelService.getLastSelection() != null
					&& gritsDataModelService.getLastSelection().getFirstElement() instanceof Entry)
			{
				selectedEntry = (Entry) gritsDataModelService.getLastSelection().getFirstElement();
			}
			
			createNewArchiveEntry(eventBroker, shell, selectedEntry);
		} catch (Exception ex)
		{
			logger.fatal("Error creating a new Archive : " + ex.getMessage(), ex);
			ErrorUtils.createErrorMessageBox(Display.getCurrent().getActiveShell(), 
					"Unable to create an Archive. ");
		}
		logger.info("- END   COMMAND : Create a new Archive. ");
	}

	private void createNewArchiveEntry(IEventBroker eventBroker, Shell shell, Entry parent) 
	{
		NewArchiveDialog dialog = new NewArchiveDialog(shell);
		dialog.setParentEntry(parent);
		if (dialog.open() == Window.OK) 
		{
			Entry parentEntry = dialog.getParentEntry();
			Entry selectedProjectEntry = DataModelSearch.findParentByType(parentEntry, ProjectProperty.TYPE);
			File archiveFolder = NewArchive.getArchiveDirectory(selectedProjectEntry);
			// create a unique folder name inside the folder
			String newArchivefolderName = NewArchive.generateFolderName(archiveFolder.list());
			try {
				Entry archiveEntry = NewArchive.createEntry(parentEntry, newArchivefolderName);
				try {
					gritsDataModelService.addEntry(parentEntry, archiveEntry);
					ProjectFileHandler.saveProject(selectedProjectEntry);
					
				} catch (IOException e) {
					archiveEntry.delete();
					logger.error(e.getMessage(),e);
					ErrorUtils.createErrorMessageBox(shell, e.getMessage(),e);
					logger.error("Something went wrong while saving project entry \n" + e.getMessage(),e);
					logger.fatal("Closing project entry \""
							+ selectedProjectEntry.getDisplayName() + "\"");
					gritsDataModelService.closeProject(selectedProjectEntry);
				}
				if(archiveEntry != null) {
					(new File(archiveFolder + File.separator + newArchivefolderName)).mkdir();
					Archive archive = new Archive();
					// serialize the archive group object to xml
					ByteArrayOutputStream os = new ByteArrayOutputStream();
					JAXBContext context = JAXBContext.newInstance(Archive.class);
					Marshaller marshaller = context.createMarshaller();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
					marshaller.marshal(archive, os);

					//write the serialized data to the folder
					FileWriter fileWriter = new FileWriter(archiveFolder.getAbsolutePath() 
							+ File.separator + newArchivefolderName 
							+ File.separator + IConfig.ARCHIVE_FILE_NAME);
					fileWriter.write(os.toString((String) marshaller.getProperty(Marshaller.JAXB_ENCODING)));
					fileWriter.close();
					os.close();

					// post will not work because of synchronization 
					// the selection needs to change before we try to open the part!
					eventBroker.send(IGritsDataModelService.EVENT_SELECT_ENTRY, archiveEntry);
					gritsUIService.openEntryInPart(archiveEntry);
				}

			}	 catch (IOException e)
			{
				logger.error(e.getMessage(),e);
				ErrorUtils.createErrorMessageBox(shell, "Error", e);
			} catch (JAXBException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	public static Entry createEntry(Entry parentEntry, String newArchivefolderName)
	{
		Entry newEntry = new Entry();
		newEntry.setDisplayName(IConfig.ARCHIVE_ENTRY_DISPLAY_NAME);
		ArchiveProperty property = new ArchiveProperty();
		List<PropertyDataFile> dataFiles = new ArrayList<PropertyDataFile>();
		PropertyDataFile propertyDataFile = new PropertyDataFile(
				newArchivefolderName,
				ArchiveProperty.CURRENT_VERSION, 
				IConfig.ARCHIVE_FOLDER_TYPE);
		dataFiles.add(propertyDataFile);
		property.setDataFiles(dataFiles);
		newEntry.setProperty(property);
		return newEntry;
	}

	public static String generateFolderName(String[] existingNames)
	{
		String folderName = "";
		int randomLength = 0;
		do 
		{
			folderName = IConfig.FOLDER_NAME_PREFIX;
			while(randomLength < IConfig.FOLDER_NAME_RANDOM_CHARACTERS_LENGTH) 
			{
				int randomcharacter = (int) (Math.random()*10);
				randomLength++;
				folderName = folderName + randomcharacter;
			}
		}
		while (Arrays.asList(existingNames).contains(folderName));
		return folderName;
	}

	public static File getArchiveDirectory(Entry selectedProjectEntry)
	{
		String workspaceLocation = PropertyHandler.getVariable("workspace_location");
		String projectFolderLocation = workspaceLocation.substring(0, workspaceLocation.length()-1) 
				+ File.separator
				+ selectedProjectEntry.getDisplayName();
		String archiveFolderLocation = projectFolderLocation
				+ File.separator
				+ IConfig.ARCHIVE_FOLDER_NAME;
		File archiveFolder = new File(archiveFolderLocation);
		if(!archiveFolder.exists() || !archiveFolder.isDirectory()) 
		{
			archiveFolder.mkdir();
		}
		return archiveFolder;
	}
}
