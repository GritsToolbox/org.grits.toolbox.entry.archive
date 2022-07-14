/**
 * 
 */
package org.grits.toolbox.entry.archive.dialog.uploadfile;

import java.io.File;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.datamodel.DataModelHandler;
import org.grits.toolbox.core.datamodel.property.WorkspaceProperty;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.preference.share.SingleChoicePreference;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.model.DocumentType;
import org.grits.toolbox.entry.archive.model.TypicalArchivedFile;
import org.grits.toolbox.entry.archive.preference.KeywordPreference;
import org.grits.toolbox.entry.archive.preference.doctype.DocTypePreference;
import org.grits.toolbox.entry.archive.utility.DocumentTypeLabelComparator;
import org.grits.toolbox.entry.archive.utility.DocumentTypeLabelProvider;

/**
 * 
 *
 */
public class UploadDialog extends TitleAreaDialog
{
	private static Logger logger = Logger.getLogger(UploadDialog.class);

	private Text locationText = null;
	private Text displayNameText = null;
	private ComboViewer personCombo = null;
	private ComboViewer documentTypeCombo = null;
	private ComboViewer documentSubTypeCombo = null;
	private ArchivedFile archivedFile = null;

	protected Set<String> existingNames = new HashSet<String>();
	private String workspacePath = null;
	private File sourceFile = null;

	public UploadDialog(Shell parentShell, HashSet<String> existingNames)
	{
		super(parentShell);
		this.existingNames = existingNames;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle("Upload File");
		setMessage("Upload File to the Archive");
		getShell().setText("Upload");
		getButton(OK).setText("Upload");
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(composite, SWT.BORDER);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		GridLayout layout = new GridLayout(4, false);
		layout.marginTop = 10;
		layout.marginBottom = 40;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		container.setLayout(layout);

		WorkspaceProperty workspaceProperty = 
				(WorkspaceProperty) DataModelHandler.instance().getRoot().getProperty();
		workspacePath = 
				workspaceProperty.getLocation().substring(0,
						workspaceProperty.getLocation().length() - 1);

		locationText =  new Text(container, SWT.BORDER|SWT.BORDER_SOLID);
		GridData textData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		textData.horizontalSpan = 2;
		textData.minimumWidth = 450;
		locationText.setLayoutData(textData);
		locationText.setText("");

		Button browseFileButton = new Button(container, SWT.PUSH);
		browseFileButton.setText(" Browse File ");
		GridData browseButtonGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		browseButtonGridData.horizontalSpan = 1;
		browseFileButton.setLayoutData(browseButtonGridData);

		Button browseFolderButton = new Button(container, SWT.PUSH);
		browseFolderButton.setText(" Browse Folder ");
		GridData browseFolderGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		browseFolderGridData.horizontalSpan = 1;
		browseFolderButton.setLayoutData(browseFolderGridData);

		Label displayLabel = new Label(container, SWT.NONE);
		displayLabel.setText("Archive Name");
		displayNameText = new Text(container, SWT.BORDER);
		displayNameText.setText("");
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		buttonData.horizontalAlignment = SWT.FILL;
		displayNameText.setLayoutData(buttonData);
		displayNameText.addModifyListener(new ModifyListener()
		{

			@Override
			public void modifyText(ModifyEvent e)
			{
				verifyInput();
			}
		});

		browseFileButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				String selected = null;
				FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
				fileDialog.setText("Select File");
				fileDialog.setFileName("");
				selected = fileDialog.open();
				try
				{
					if (selected != null)
					{
						selected = selected.trim();
						logger.debug("Selected file : " + selected);
						locationText.setText(selected);
						File sourceFile = Paths.get(locationText.getText()).toFile();
						displayNameText.setText(sourceFile.getName());
						displayNameText.selectAll();
						displayNameText.setFocus();
					}
				} catch (InvalidPathException ex)
				{
					setErrorMessage("Select a valid file");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		browseFolderButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				String selected = null;
				DirectoryDialog folderDialog = new DirectoryDialog(getShell(), SWT.OPEN);
				folderDialog.setText("Select Folder");
				selected = folderDialog.open();
				try
				{
					if (selected != null)
					{
						logger.debug("Selected folder : " + selected);
						locationText.setText(selected);
						File locationFolder = Paths.get(
								locationText.getText()).toFile();
						displayNameText.setText(locationFolder.getName() + ".zip");
						displayNameText.selectAll();
						displayNameText.setFocus();
					}
				} catch (InvalidPathException ex)
				{
					setErrorMessage("Select a valid folder");
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		personCombo = createComboLine(container, "Person", 3);
		personCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// TODO Auto-generated method stub

			}
		});
		SingleChoicePreference personPreference =
				ProjectPreferenceStore.getSingleChoicePreference(
						ProjectPreferenceStore.Preference.PERSON);
		List<String> personList = new ArrayList<String>();
		personList.addAll(personPreference.getAllValues());
		String[] persons = getSortedArrayFromList(personList);
		personCombo.setContentProvider(new ArrayContentProvider());
		personCombo.setInput(persons);
		selectInCombo(personCombo, personPreference.getDefaultValue());

		documentTypeCombo = createComboLine(container, "Document Type", 3);
		documentTypeCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				if(!((StructuredSelection) documentTypeCombo.getSelection()).isEmpty())
				{
					DocumentType selectedDocumentType = (DocumentType) ((StructuredSelection) 
							documentTypeCombo.getSelection())
							.getFirstElement();
					String[] documentSubTypes = getSortedArrayFromList(
							new ArrayList<String>(selectedDocumentType.getSubTypes()));
					documentSubTypeCombo.setInput(documentSubTypes);
					selectInCombo(documentSubTypeCombo, selectedDocumentType.getSelectedSubType());
				}
			}
		});
		DocTypePreference.loadPreferences();
		DocumentType[] documentTypes = getSortedDocumentFromList(
				new ArrayList<DocumentType>(DocTypePreference.ALL_DOCUMENT_TYPES));
		documentTypeCombo.setContentProvider(new ArrayContentProvider());
		documentTypeCombo.setLabelProvider(new DocumentTypeLabelProvider());
		documentTypeCombo.setInput(documentTypes);
		if(DocTypePreference.DEFAULT_DOCUMENT_TYPE != null)
		{
			//			selectInCombo(documentTypeCombo, DocTypePreference.DEFAULT_DOCUMENT_TYPE.getLabel());
			int i = 0;
			int selectionIndex = 0;
			for(String docType : documentTypeCombo.getCombo().getItems())
			{
				selectionIndex = docType.equals(
						DocTypePreference.DEFAULT_DOCUMENT_TYPE.getLabel()) 
						? i : selectionIndex;
				i++;
			}
			documentTypeCombo.getCombo().select(selectionIndex);
		}

		documentSubTypeCombo = createComboLine(container, "Sub Type", 3);
		documentSubTypeCombo.addSelectionChangedListener(new ISelectionChangedListener()
		{
			@Override
			public void selectionChanged(SelectionChangedEvent event)
			{
				// TODO Auto-generated method stub
			}
		});
		String[] documentSubTypes = null;
		documentSubTypeCombo.setContentProvider(new ArrayContentProvider());
		if(DocTypePreference.DEFAULT_DOCUMENT_TYPE != null)
		{
			documentSubTypes = getSortedArrayFromList(
					new ArrayList<String>(DocTypePreference.DEFAULT_DOCUMENT_TYPE.getSubTypes()));
			documentSubTypeCombo.setInput(documentSubTypes);
			selectInCombo(documentSubTypeCombo, DocTypePreference.DEFAULT_DOCUMENT_TYPE.getSelectedSubType());
		}

		browseFileButton.setFocus();
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}

	protected void verifyInput()
	{
		setErrorMessage(null);
		String errorMessage = null;
		String archiveName = displayNameText.getText().trim();
		String location = locationText.getText().trim();
		if(archiveName.isEmpty())
		{
			errorMessage = "Archive Name cannot be empty. Please add an archive name";
		}
		else if(location.isEmpty())
		{
			errorMessage = "Invalid File/Folder path.";
		}
		else
		{
			File locationFile = null;
			try
			{
				locationFile = Paths.get(location).toFile();
			} catch (Exception ex)
			{
				errorMessage = "Invalid File/Folder path.";
			}
			if(errorMessage == null && locationFile != null)
			{
				logger.debug("parent file" + locationFile.getParentFile());
				if(locationFile.isDirectory())
				{
					if(locationFile.getAbsolutePath().equals(workspacePath)
							|| locationFile.getAbsolutePath().contains(workspacePath + File.separator))
					{
						errorMessage = "Cannot upload workspace content as archive."
								+ " Please select another folder.";
					}
				}
				//				else if(locationFile.getParentFile() != null 
				//						&& locationFile.getParentFile().getAbsolutePath().equals(archiveFolder.getAbsolutePath()))
				//				{
				//					errorMessage = "Cannot upload archive content again."
				//							+ " Please select another file.";
				//				}
				//				logger.debug(locationFile.getParentFile().getAbsolutePath());
				if(errorMessage == null && existingNames.contains(archiveName))
				{
					errorMessage = "This name already exists in the archive. "
							+ "Please choose a unique archive name.";
				}
			}
		}
		setErrorMessage(errorMessage);
	}

	private void selectInCombo(ComboViewer comboViewer,
			String selectedValue)
	{
		String[] arrayOfValues = comboViewer.getCombo().getItems();
		if(arrayOfValues.length > 0 && selectedValue != null)
		{
			int i = 0;
			int selectionIndex = -1;
			for(String position : arrayOfValues)
			{
				if(selectedValue != null 
						&& position.equals(selectedValue))
				{
					selectionIndex = i;
				}
				i++;
			}
			comboViewer.setInput(arrayOfValues);
			if(selectionIndex >= 0)
				comboViewer.getCombo().select(selectionIndex);
			//			comboViewer.setSelection(comboViewer.getSelection());
		}
	}

	private ComboViewer createComboLine(Composite composite, String label, int horizontalSpan)
	{
		Label labelLabel = new Label(composite, SWT.NONE);
		GridData labelGridData = new GridData();
		labelGridData.verticalAlignment = SWT.BEGINNING;
		labelLabel.setLayoutData(labelGridData);
		labelLabel.setText(label);

		ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
		GridData comboGridData = new GridData();
		comboGridData.grabExcessHorizontalSpace = true;
		comboGridData.horizontalAlignment = SWT.FILL;
		comboGridData.horizontalSpan = horizontalSpan;
		comboViewer.getCombo().setLayoutData(comboGridData);
		return comboViewer;
	}

	private String[] getSortedArrayFromList(List<String> listOfValue)
	{
		Collections.sort(listOfValue);
		String[] arrayOfValue = new String[listOfValue.size()];
		int i = 0;
		for(String value : listOfValue)
		{
			arrayOfValue[i] = value;
			i++;
		}
		return arrayOfValue;
	}

	private DocumentType[] getSortedDocumentFromList(List<DocumentType> listOfValue)
	{
		Collections.sort(listOfValue, new DocumentTypeLabelComparator());
		DocumentType[] arrayOfValue = new DocumentType[listOfValue.size()];
		int i = 0;
		for(DocumentType value : listOfValue)
		{
			arrayOfValue[i] = value;
			i++;
		}
		return arrayOfValue;
	}

	@Override
	public void setErrorMessage(String newErrorMessage)
	{
		super.setErrorMessage(newErrorMessage);
		getButton(OK).setEnabled(newErrorMessage == null);
	}

	@Override
	protected void okPressed()
	{
		String location = locationText.getText().trim();
		if(!location.isEmpty())
		{
			String archiveName = displayNameText.getText().trim();
			if(!archiveName.isEmpty())
			{
				if(personCombo.getCombo().getItems().length == 0 
						|| personCombo.getCombo().getSelectionIndex() >= 0)
				{
					try
					{
						sourceFile = Paths.get(location).toFile();
					} catch (InvalidPathException ex)
					{
						logger.error(ex.getMessage(), ex);
						setErrorMessage("Select a valid file");
					}
					if(sourceFile.isDirectory())
					{
						if(sourceFile.getAbsolutePath().equals(workspacePath)
								|| sourceFile.getAbsolutePath().contains(workspacePath + File.separator))
						{
							setErrorMessage("Cannot upload workspace content as archive."
									+ " Please select another folder.");
						}
						else if (archiveName.equals(".zip") || !archiveName.endsWith(".zip"))
						{
							archiveName = archiveName + ".zip";
						}
					}
					archivedFile = new ArchivedFile();
					TypicalArchivedFile typicalArchivedFile = new TypicalArchivedFile();
					typicalArchivedFile.setFileName(archiveName);

					if(!documentTypeCombo.getSelection().isEmpty())
					{
						DocumentType selectedDocType = (DocumentType) documentTypeCombo.getElementAt(
								documentTypeCombo.getCombo().getSelectionIndex());
						typicalArchivedFile.setDocumentType(selectedDocType.clone());
						if(!documentSubTypeCombo.getSelection().isEmpty())
							typicalArchivedFile.getDocumentType().setSelectedSubType(
									documentSubTypeCombo.getCombo().getText());
					}
					//    			fileType = fileFullName.substring(fileName.lastIndexOf(".") + 1);

					archivedFile.setPerson(personCombo.getCombo().getText());

					archivedFile.setTypicalArchivedFile(typicalArchivedFile);
					Date currentDate = new Date();
					archivedFile.setSetDate(currentDate);
					archivedFile.setModifiedDate(currentDate);
					KeywordPreference.loadPreferences();
					archivedFile.setKeywords(new HashSet<String>(KeywordPreference.DEFAULT_KEYWORDS));
					super.okPressed();	
				}
				else
				{
					setErrorMessage("Select the person");
				}
			}
			else
			{
				setErrorMessage("Archive Name cannot be empty");
			}
		}
		else
		{
			setErrorMessage("Select a valid file");
		}
	}

	public File getSourceFile()
	{
		return sourceFile;
	}

	public ArchivedFile getArchivedFile()
	{
		return archivedFile;
	}
}
