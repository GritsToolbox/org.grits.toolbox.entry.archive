/**
 * 
 */
package org.grits.toolbox.entry.archive.editor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.grits.toolbox.core.dataShare.IGritsConstants;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.Entry;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.editor.pages.AbstractArchivePage;
import org.grits.toolbox.entry.archive.editor.pages.archivedemail.EmailArchivePage;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.FileArchivePage;
import org.grits.toolbox.entry.archive.model.Archive;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.property.ArchiveProperty;

/**
 * 
 *
 */
public class ArchiveEntryEditor {
	private static final Logger logger = Logger.getLogger(ArchiveEntryEditor.class);
	public static final String DATE_FORMAT = "MM/dd/yyyy";
	//private boolean dirtyFlag = false;
	private Entry archiveEntry = null;
	private File archiveFolder = null;
	private HashMap<String, String> renamedArchives = new HashMap<String, String>();

	private Archive archive = null;
	private File archiveXmlFile = null;
	
	@Inject MDirtyable dirtyFlag;
	private CTabFolder cTabFolder;
	
	private Map<CTabItem, AbstractArchivePage> cTabItemToPartTabMap =
			new HashMap<CTabItem, AbstractArchivePage>();
	private FileArchivePage filePage;
	private EmailArchivePage emailPage;

	public Map<CTabItem, AbstractArchivePage> getcTabItemToPartTabMap() {
		return cTabItemToPartTabMap;
	}
	
	public HashMap<String, String> getRenamedArchives()
	{
		return renamedArchives;
	}
	
	@Inject
	public ArchiveEntryEditor(@Named(IServiceConstants.ACTIVE_SELECTION) Entry entry) throws Exception {
		
		if (entry == null) 
			return;
		
		this.archiveEntry = entry;
		archiveEntry.setDisplayName(archiveEntry.getParent().getDisplayName() + "- " + IConfig.ARCHIVE_ENTRY_DISPLAY_NAME);
		archiveFolder = ArchiveProperty.getFolderForArchiveEntry(archiveEntry);
		if(archiveFolder != null && archiveFolder.exists() && archiveFolder.isDirectory())
		{
			archiveXmlFile = this.loadArchiveXmlFile();
			if(archiveXmlFile != null && archiveXmlFile.exists() && archiveXmlFile.isFile())
			{
				this.archive = this.loadArchive();
				verifyArchiveFiles();
			}
			else
			{
				throw new Exception("Could not find archive xml file.");
			}
		}
		logger.debug("- END : Getting Input for Archive Entry Editor...");
	}

	private void verifyArchiveFiles()
	{
		List<ArchivedFile> filesToRemove = new ArrayList<ArchivedFile>();
		for(ArchivedFile archivedFile : archive.getArchivedFiles())
		{
			if(!(new File(archiveFolder, 
					archivedFile.getTypicalArchivedFile().getFileName())).exists())
			{
				filesToRemove.add(archivedFile);
			}
		}
		if(!filesToRemove.isEmpty())
		{
			String archivedFileNames = "(";
			int i = 0;
			int max = 5;
			for(ArchivedFile ftr : filesToRemove)
			{
				if(i < max)
				{
					archivedFileNames += ftr.getTypicalArchivedFile().getFileName() + ", ";
				}
				else if(i == max)
					archivedFileNames += "...";

				i++;
			}
			if(archivedFileNames.endsWith(", "))
			{
				archivedFileNames = archivedFileNames.substring(0, archivedFileNames.length() -2);
			}
			archivedFileNames += ")\n\n";
			boolean remove = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), "Files not Found", 
					"These are the archives that were not found in the system :\n" + archivedFileNames
					+ "Do you want to remove them from the archive list?");
			if(remove)
			{
				for(ArchivedFile ftr : filesToRemove)
				{
					archive.getArchivedFiles().remove(ftr);
				}
				saveModel();
			}
		}
	}

	public Entry getArchiveEntry()
	{
		return archiveEntry;
	}

	public Archive getArchive()
	{
		return archive;
	}
	
	@PostConstruct
	public void postConstruct(Composite parent, final MPart part)
	{
		part.setLabel(archiveEntry.getParent().getDisplayName() + "- " + IConfig.ARCHIVE_ENTRY_DISPLAY_NAME);
		logger.info("Creating tabs");
		parent.setLayout(new FillLayout());
		cTabFolder = new CTabFolder(parent, SWT.NONE);
		cTabFolder.setTabPosition(SWT.BOTTOM);
		cTabFolder.setSimple(false);
		part.getContext().set(CTabFolder.class, cTabFolder);
		part.getContext().set(ArchiveEntryEditor.class, this);
		try
		{
			
			cTabFolder.addSelectionListener(new SelectionListener()
			{

				@Override
				public void widgetSelected(SelectionEvent e)
				{
					changeTab(e);
				}

				private void changeTab(SelectionEvent e)
				{
					int selectionIndex = cTabFolder.getSelectionIndex();

					logger.info("Tab changed to " + selectionIndex);
					selectionIndex = selectionIndex < 0 ? 0 : selectionIndex;
					AbstractArchivePage currentTab = getcTabItemToPartTabMap().get(cTabFolder.getSelection());
					part.getContext().set(AbstractArchivePage.class, currentTab);

					// an added notification for the selected tab for specialized action
					cTabFolder.getSelection().notifyListeners(SWT.Selection, new Event());
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
					changeTab(e);
				}
			});

			addPages(part);
			cTabFolder.setSelection(0);
			cTabFolder.notifyListeners(SWT.Selection, new Event());
		}
		catch (Exception e)
		{
			logger.fatal("Error while adding tabs to the qrtpcr editor.\n" + e.getMessage(), e);
			MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Creating Page", 
					"Some unexpected error occurred while opening the editor. "
							+ "Please contact developers for further information/help.");
			logger.fatal(e.getMessage(), e);
		}

		logger.info("END   : Creating QrtPCRTableEditor. ");
	}

	protected void addPages(MPart part) {
		logger.info("Adding pages");
		CTabItem cTabItem = new CTabItem(cTabFolder, SWT.NONE);
		cTabItem.setText(FileArchivePage.PAGE_TITLE);
		cTabItem.setShowClose(false);
		part.getContext().set(CTabItem.class, cTabItem);
		filePage = ContextInjectionFactory.make(
				FileArchivePage.class, part.getContext());
		getcTabItemToPartTabMap().put(cTabItem, filePage);

		cTabItem = new CTabItem(cTabFolder, SWT.NONE);
		cTabItem.setText(EmailArchivePage.PAGE_TITLE);
		cTabItem.setShowClose(false);
		part.getContext().set(CTabItem.class, cTabItem);
		
		emailPage = ContextInjectionFactory.make (EmailArchivePage.class, part.getContext());
        getcTabItemToPartTabMap().put(cTabItem, emailPage);
	}

	@Persist
	public void doSave(IProgressMonitor monitor)
	{
		verifyArchiveFiles();
		saveModel();
		checkAndResetDirty();
	}

	public void checkAndResetDirty()
	{
		if(dirtyFlag.isDirty())
		{
			this.dirtyFlag.setDirty(false);
			AbstractArchivePage activePage = getcTabItemToPartTabMap().get(cTabFolder.getSelection());
			if(activePage != null) {
				activePage.refreshPage();
			}
		}
	}

	public boolean isDirty()
	{
		return this.dirtyFlag.isDirty() ;
	}

	public void markDirty() 
	{
		if(!dirtyFlag.isDirty())
		{
			this.dirtyFlag.setDirty(true);
		}
	}

	private File loadArchiveXmlFile()
	{
		File archiveXmlFile = null;
		logger.debug("- START : Loading all Archive Xml File from the archive.");
		try
		{
			if(archiveFolder.exists() && archiveFolder.isDirectory())
			{
				for(File file : archiveFolder.listFiles())
				{
					if(file.getName().equals(IConfig.ARCHIVE_FILE_NAME) 
							&& file.isFile())
					{
						archiveXmlFile = file;
						break;
					}
				}
			}
		} catch (Exception ex)
		{
			logger.error(ex);
		}
		logger.debug("- END   : Loading all Archive Xml File from the archive.");
		return archiveXmlFile;
	}

	public Archive loadArchive() throws Exception
	{
		Archive archive = null;
		if(archiveXmlFile != null && archiveXmlFile.exists())
		{
			try
			{
				FileInputStream inputStream = new FileInputStream(archiveXmlFile.getAbsolutePath());
				InputStreamReader reader = new InputStreamReader(inputStream, 
						PropertyHandler.GRITS_CHARACTER_ENCODING);
				try {
					JAXBContext context = JAXBContext.newInstance(Archive.class);
					Unmarshaller unmarshaller = context.createUnmarshaller();
					archive = (Archive) unmarshaller.unmarshal(reader);
				} catch (JAXBException ex) {
					logger.error("Error parsing the archive xml file." + ex.getMessage(), ex);
					throw ex;
				}
			} catch (FileNotFoundException | UnsupportedEncodingException ex)
			{
				logger.error("Error loading the archive from the file location. " + ex.getMessage(), ex);
				throw ex;
			}
		}
		return archive;
	}

	public boolean saveModel()
	{
		return saveModel(archive, archiveXmlFile);
	}

	public static boolean saveModel(Archive archive, File archiveXmlFile)
	{
		boolean updated = false;
		if(archive != null)
		{
			try
			{
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				try
				{
					JAXBContext context = JAXBContext.newInstance(Archive.class);
					Marshaller marshaller = context.createMarshaller();
					marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
					marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
					marshaller.marshal(archive, os);
					try
					{
						FileWriter fileWriter = new FileWriter(archiveXmlFile);
						fileWriter.write(os.toString((String) marshaller.getProperty(Marshaller.JAXB_ENCODING)));
						fileWriter.close();
						os.close();
						updated = true;
					} catch (IOException e) {
						logger.error("The changes made could not be written to the file.", e);
						MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Writing File", 
								"The changes made could not be written to the file.");
					}
				} catch (JAXBException e) {
					logger.error("The changes made could not be serialized as xml.", e);
					MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Parsing File", 
							"The changes made could not be serialized to xml.");
				}
			} catch (Exception e) {
				logger.fatal(e);
				MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error Saving File", 
						"The changes made could not be saved to the file. Please contact the development team.");
			}
		}
		return updated;
	}

	public File getArchiveFolder()
	{
		return archiveFolder ;
	}

	public Date renameArchive(String previousName, String newName) throws Exception
	{
		Date modifiedDate = null;
		Archive tempArchiveModel = loadArchive();
		for(ArchivedFile archiveFile : tempArchiveModel.getArchivedFiles())
		{
			if(archiveFile.getTypicalArchivedFile().getFileName().equals(previousName))
			{
				archiveFile.getTypicalArchivedFile().setFileName(newName);
				archiveFile.setModifiedDate(modifiedDate = new Date());
				break;
			}
		}
		saveModel(tempArchiveModel, archiveXmlFile);
		return modifiedDate;
	}

	public void removeArchivedFile(ArchivedFile archivedFile) throws Exception
	{
		Archive tempArchiveModel = loadArchive();
		ArchivedFile archivedFileToRemove = null;
		for(ArchivedFile thisArchive : tempArchiveModel.getArchivedFiles())
		{
			if(thisArchive.getTypicalArchivedFile().getFileName().equals(
					archivedFile.getTypicalArchivedFile().getFileName()))
			{
				archivedFileToRemove = thisArchive;
				break;
			}
		}
		tempArchiveModel.getArchivedFiles().remove(archivedFileToRemove);
		saveModel(tempArchiveModel, archiveXmlFile);
	}
}