/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.preference.share.SingleChoicePreference;
import org.grits.toolbox.core.utilShare.SelectKeywordsDialog;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.AbstractArchivePage;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.DeleteArchivedFile;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.DownloadFile;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.DownloadFilesAsZip;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.RenameArchivedFile;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.UpdateArchivedFile;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.UploadArchivedFile;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.sort.ArchivedFileColumnComparatorListener;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.sort.ArchivedFileViewerComparator;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.sort.SortFileNames;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.model.DocumentType;
import org.grits.toolbox.entry.archive.preference.KeywordPreference;
import org.grits.toolbox.entry.archive.preference.doctype.DocTypePreference;
import org.grits.toolbox.entry.archive.utility.DocumentTypeLabelComparator;
import org.grits.toolbox.entry.archive.utility.DocumentTypeLabelProvider;
import org.grits.toolbox.entry.archive.utility.UtilityFileName;

/**
 * 
 *
 */
public class FileArchivePage extends AbstractArchivePage
{
	private Logger logger = Logger.getLogger(FileArchivePage.class);

	public static final HashMap<String, Image> FILE_ICON_MAP = new HashMap<String, Image>();

	public static final String ID = "0";
	public static final String PAGE_TITLE = "File   ";

	private static final Image UNKNOWN_ICON = ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.UNKNOWN_ICON).createImage();

	private ArchivedFile selectedArchivedFile = null;

	private ArrayList<Action> leftSectionActions = null;

	private Composite rightSideComposite = null;
	private Text descriptionText = null;
	private ComboViewer documentTypeCombo = null;
	private ComboViewer documentSubTypeCombo = null;
	private ComboViewer personCombo = null;
	private CDateTime cDateTime = null;
	private Text dateModifiedText = null;
	private Text keywordsText = null;
	private Button changeKeywordButton = null;
	private boolean oldSelection = true;

	private SelectKeywordsDialog selectKeywordsDialog = null;

	private SortFileNames sortByName;

	@Inject private EMenuService menuService;

	public Boolean isOldSelection()
	{
		return this.oldSelection;
	}

	@Inject
	public FileArchivePage(ArchiveEntryEditor archiveEntryEditor, CTabItem cTabItem)
	{
		super(archiveEntryEditor, cTabItem);
	}

	private ArchiveEntryEditor getArchiveEntryEditor()
	{
		return ((ArchiveEntryEditor) this.getEditor());
	}

	public ArchivedFile getSelectedArchivedFile()
	{
		return selectedArchivedFile;
	}

	@Override
	public void createRestOftheContent()
	{
		logger.debug("- START : Creating remaining part for the ArchivedFile page.");
		try
		{
			rightSideComposite = new Composite(getRightSectionOfPage(), SWT.NONE);
			GridLayout rightSideCompositeLayout = new GridLayout();
			rightSideCompositeLayout.verticalSpacing = 30;
			rightSideCompositeLayout.horizontalSpacing = 10;
			rightSideCompositeLayout.numColumns = 4;
			rightSideCompositeLayout.makeColumnsEqualWidth = false;
			rightSideComposite.setLayout(rightSideCompositeLayout);

			documentTypeCombo = createComboLine("Document Type", 1);
			DocTypePreference.loadPreferences();
			DocumentType[] documentTypes = getSortedDocumentFromList(
					new ArrayList<DocumentType>(DocTypePreference.ALL_DOCUMENT_TYPES));
			documentTypeCombo.setContentProvider(new ArrayContentProvider());
			documentTypeCombo.setLabelProvider(new DocumentTypeLabelProvider());
			documentTypeCombo.setInput(documentTypes);
			if(DocTypePreference.DEFAULT_DOCUMENT_TYPE != null)
			{
				resetDocumentType(DocTypePreference.DEFAULT_DOCUMENT_TYPE.clone());
			}

			documentSubTypeCombo = createComboLine("     Sub Type", 1);
			documentSubTypeCombo.setContentProvider(new ArrayContentProvider());
			String[] documentSubTypes;
			if(DocTypePreference.DEFAULT_DOCUMENT_TYPE != null)
			{
				documentSubTypes = getSortedArrayFromList(
						new ArrayList<String>(DocTypePreference.DEFAULT_DOCUMENT_TYPE.getSubTypes()));
				documentSubTypeCombo.setInput(documentSubTypes);
				resetDocumentSubType(DocTypePreference.DEFAULT_DOCUMENT_TYPE, 
						DocTypePreference.DEFAULT_DOCUMENT_TYPE.getSelectedSubType());
			}

			documentTypeCombo.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					if(selectedArchivedFile != null
							&& documentTypeCombo.getCombo().getSelectionIndex() >= 0)
					{
						DocumentType documentType = (DocumentType) documentTypeCombo.getElementAt(
								documentTypeCombo.getCombo().getSelectionIndex());

						if(documentTypeCombo.getCombo().getSelectionIndex() == 0)
						{
							// only reset if it was not this no selection
							if(selectedArchivedFile.getTypicalArchivedFile().getDocumentType() != null)
							{
								selectedArchivedFile.getTypicalArchivedFile().setDocumentType(null);
								// keep the latest subtypes of document
								documentSubTypeCombo.setInput(getSortedArrayFromList(
										new ArrayList<String>(documentType.getSubTypes())));
								resetDocumentSubType(documentType, IConfig.NO_SELECTION_IN_COMBO);
								pageEdited();
							}
						}
						else if(selectedArchivedFile.getTypicalArchivedFile().getDocumentType() == null
								|| !Objects.equals(selectedArchivedFile.getTypicalArchivedFile()
										.getDocumentType().getLabel(), documentType.getLabel()))
						{
							selectedArchivedFile.getTypicalArchivedFile().setDocumentType(documentType.clone());
							// keep the latest subtypes of document
							documentSubTypeCombo.setInput(getSortedArrayFromList(
									new ArrayList<String>(documentType.getSubTypes())));
							resetDocumentSubType(documentType, documentType.getSelectedSubType());
							pageEdited();
						}
					}
				}
			});

			documentSubTypeCombo.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					if(selectedArchivedFile != null
							&& documentSubTypeCombo.getCombo().getSelectionIndex() >= 0)
					{
						String documentSubType = documentSubTypeCombo.getCombo().getItem(
								documentSubTypeCombo.getCombo().getSelectionIndex());

						// only set subtype when current document type is not null
						// and previous subtype is different than current selection
						if(selectedArchivedFile.getTypicalArchivedFile().getDocumentType() != null
								&& !Objects.equals(selectedArchivedFile.getTypicalArchivedFile()
										.getDocumentType().getSelectedSubType(), documentSubType))
						{
							selectedArchivedFile.getTypicalArchivedFile()
							.getDocumentType().setSelectedSubType(documentSubType);
							pageEdited();
						}
					}
				}
			});

			personCombo = createComboLine("Person", 3);
			personCombo.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					if(selectedArchivedFile != null 
							&& personCombo.getCombo().getSelectionIndex() >= 0)
					{
						String person = personCombo.getCombo().getItem(
								personCombo.getCombo().getSelectionIndex());
						if(!Objects.equals(person, selectedArchivedFile.getPerson()))
						{
							selectedArchivedFile.setPerson(person);
							pageEdited();
						}
					}
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

			createLabel("Date");
			cDateTime = new CDateTime(rightSideComposite, CDT.BORDER | CDT.DROP_DOWN);
			cDateTime.setPattern(ArchiveEntryEditor.DATE_FORMAT);
			GridData textGridData = new GridData();
			textGridData.grabExcessHorizontalSpace = true;
			textGridData.horizontalAlignment = SWT.FILL;
			textGridData.minimumWidth = 100;
			textGridData.horizontalSpan = 1;
			cDateTime.setLayoutData(textGridData);
			cDateTime.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					changeDate();
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{
					changeDate();
				}
				
				private void changeDate()
				{
					if(selectedArchivedFile != null)
					{
						Date date = cDateTime.getSelection();
						if(!Objects.equals(date, 
								selectedArchivedFile.getSetDate()))
						{
							selectedArchivedFile.setSetDate(date);
							pageEdited();
						}
					}
				}
			});

			dateModifiedText = createLineForLabel("Last Modified", 
					PropertyHandler.LABEL_TEXT_LIMIT, SWT.BORDER, 100, -1, 1);
			dateModifiedText.setEnabled(false);

			final int MAX_RIGHT_SIDE_WIDTH = 580;

			descriptionText = createLineForLabel("Description", 10000, 
					SWT.BORDER|SWT.V_SCROLL|SWT.WRAP, MAX_RIGHT_SIDE_WIDTH , 250, 3);
			descriptionText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent e)
				{
					if(selectedArchivedFile != null)
					{
						String description = descriptionText.getText().trim();
						if(!Objects.equals(description, 
								selectedArchivedFile.getDescription()))
						{
							selectedArchivedFile.setDescription(description);
							pageEdited();
						}
					}
				}
			});

			keywordsText = createLineForLabel(
					"Keywords", 10000, SWT.BORDER|SWT.WRAP, MAX_RIGHT_SIDE_WIDTH, 100, 3);
			keywordsText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent e)
				{
					pageEdited();
				}
			});
			keywordsText.setEnabled(false);

			changeKeywordButton = new Button(rightSideComposite, SWT.PUSH);
			changeKeywordButton.setText("Modify Keywords");
			changeKeywordButton.setEnabled(false);
			GridData buttonData = new GridData();
			buttonData.widthHint = 200;
			buttonData.horizontalAlignment = SWT.CENTER;
			buttonData.horizontalSpan = 4;
			changeKeywordButton.setLayoutData(buttonData);
			KeywordPreference.loadPreferences();
			List<String> keywords = new ArrayList<String>();
			keywords.addAll(KeywordPreference.DEFAULT_KEYWORDS);
			keywords.addAll(KeywordPreference.OTHER_KEYWORDS);
			Collections.sort(keywords, String.CASE_INSENSITIVE_ORDER);
//			selectKeywordsDialog = new SelectKeywordsDialog(Display.getCurrent().getActiveShell(), keywords);
			changeKeywordButton.addSelectionListener(new SelectionListener()
			{
				@Override
				public void widgetSelected(SelectionEvent e)
				{
					if(selectedArchivedFile != null)
					{
						selectKeywords();
					}
				}

				@Override
				public void widgetDefaultSelected(SelectionEvent e)
				{

					if(selectKeywordsDialog.open() == Window.OK)
					{
						selectKeywords();
					}
				}

				private void selectKeywords()
				{
					Set<String> allKeywords = new HashSet<String>();
					KeywordPreference.loadPreferences();
					allKeywords.addAll(KeywordPreference.DEFAULT_KEYWORDS);
					allKeywords.addAll(KeywordPreference.OTHER_KEYWORDS);
//					Collections.sort(keywords, String.CASE_INSENSITIVE_ORDER);
					selectKeywordsDialog = new SelectKeywordsDialog(Display.getCurrent().getActiveShell(), allKeywords); 
					selectKeywordsDialog.setSelectedKeyWords(selectedArchivedFile.getKeywords());
					if(selectKeywordsDialog.open() == Window.OK)
					{
						selectedArchivedFile.setKeywords(selectKeywordsDialog.getSelectedKeywords());
						keywordsText.setText(getCSKeywords(selectKeywordsDialog.getSelectedKeywords()));
					}
				}
			});

			rightSideComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			getRightSectionOfPage().setClient(rightSideComposite);
			clearAll();
		} catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
		}
		logger.debug("- END   : Creating remaining part for the ArchivedFile page.");
	}

	public void pageEdited()
	{
		if(selectedArchivedFile != null && oldSelection)
		{
			selectedArchivedFile.setModifiedDate(new Date());
			getArchiveEntryEditor().markDirty();
		}
	}

	protected String getCSKeywords(Set<String> keywordSet)
	{
		String keywords = "";
		String separator = ", ";
		for(String keyword : keywordSet)
		{
			if(!keyword.isEmpty())
				keywords += keyword + separator;
		}
		if(!keywords.isEmpty())
			keywords = keywords.substring(0, keywords.length() - separator.length());
		return keywords;
	}

	private DocumentType[] getSortedDocumentFromList(List<DocumentType> arrayList)
	{
		Collections.sort(arrayList, new DocumentTypeLabelComparator());
		DocumentType[] arrayOfValue = new DocumentType[arrayList.size() + 1];
		DocumentType documentType = new DocumentType();
		documentType.setLabel(IConfig.NO_SELECTION_IN_COMBO);
		documentType.setSubTypes(new HashSet<>(Arrays.asList(IConfig.NO_SELECTION_IN_COMBO)));
		arrayOfValue[0] = documentType;
		int i = 1;
		for(DocumentType value : arrayList)
		{
			arrayOfValue[i] = value;
			i++;
		}
		return arrayOfValue;
	}

	private String[] getSortedArrayFromList(List<String> listOfValues)
	{
		Collections.sort(listOfValues);
		String[] arrayOfValue = new String[listOfValues.size()];
		int i = 0;
		if(!listOfValues.contains(IConfig.NO_SELECTION_IN_COMBO))
		{
			arrayOfValue = new String[listOfValues.size()+ 1];
			arrayOfValue[0] = IConfig.NO_SELECTION_IN_COMBO;
			i = 1;
		}
		for(String value : listOfValues)
		{
			arrayOfValue[i] = value;
			i++;
		}
		return arrayOfValue;
	}

	private ComboViewer createComboLine(String label, int horizontalSpan)
	{
		Label labelLabel = new Label(rightSideComposite, SWT.NONE);
		labelLabel.setText(label);
		labelLabel.setFont(this.boldFont);
		GridData labelGridData = new GridData();
		labelGridData.verticalAlignment = SWT.BEGINNING;
		labelLabel.setLayoutData(labelGridData);

		ComboViewer comboViewer = new ComboViewer(rightSideComposite, SWT.READ_ONLY);
		GridData comboGridData = new GridData();
		comboGridData.grabExcessHorizontalSpace = true;
		comboGridData.horizontalAlignment = SWT.FILL;
		comboGridData.horizontalSpan = horizontalSpan;
		comboViewer.getCombo().setLayoutData(comboGridData);
		return comboViewer;
	}

	protected Text createLineForLabel(String label, int textLimit, int style, 
			int minimumTextWidth, int heightHint, int horizontalSpan)
	{
		createLabel(label);

		Text labelText = new Text(rightSideComposite, style);
		labelText.setTextLimit(textLimit);
		GridData textGridData = new GridData();
		textGridData.grabExcessHorizontalSpace = true;
		textGridData.horizontalAlignment = SWT.FILL;
		textGridData.minimumWidth = minimumTextWidth;
		if(heightHint > 0)
			textGridData.heightHint = heightHint;
		textGridData.horizontalSpan = horizontalSpan;
		labelText.setLayoutData(textGridData);
		return labelText;
	}

	private void createLabel(String label)
	{
		Label labelLabel = new Label(rightSideComposite, SWT.NONE);
		labelLabel.setText(label);
		labelLabel.setFont(this.boldFont);
		GridData labelGridData = new GridData();
		labelGridData.verticalAlignment = SWT.BEGINNING;
		labelLabel.setLayoutData(labelGridData);
	}

	@Override
	protected List<Action> getLeftSectionActions()
	{
		leftSectionActions = new ArrayList<Action>();
		leftSectionActions.add(0, new UploadArchivedFile(this));
		leftSectionActions.add(1, new UpdateArchivedFile(this));
		leftSectionActions.add(2, new RenameArchivedFile(this));
		leftSectionActions.add(3, new DownloadFile(this));
		leftSectionActions.add(4, new DownloadFilesAsZip(this));
		leftSectionActions.add(5, new DeleteArchivedFile(this));
		sortByName = new SortFileNames(this.leftSideTreeViewer);
		leftSectionActions.add(6, sortByName);
		createContextMenu();
		return leftSectionActions;
	}

	protected void createContextMenu()
	{
		if(this.leftSideTreeViewer != null)
		{
			MenuManager contextMenu = new MenuManager("#ViewerMenu"); //$NON-NLS-1$
			contextMenu.setRemoveAllWhenShown(true);
			contextMenu.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager mgr)
				{
					if(leftSideTreeViewer.getSelection() != null 
							&& !leftSideTreeViewer.getSelection().isEmpty())
					{
						fillContextMenu(mgr);
					}
				}
			});

			Menu menu = contextMenu.createContextMenu(this.leftSideTreeViewer.getTree());
			this.leftSideTreeViewer.getTree().setMenu(menu);
			menuService.registerContextMenu(this.leftSideTreeViewer, "#ViewerMenu");
		}
	}

	protected void fillContextMenu(IMenuManager contextMenu)
	{
		contextMenu.add(leftSectionActions.get(3));
		contextMenu.add(new Separator());
		contextMenu.add(leftSectionActions.get(1));
		contextMenu.add(leftSectionActions.get(2));
		contextMenu.add(leftSectionActions.get(5));
	}

	@Override
	public void setSelection(Object selectedObject)
	{
		String selectionTitle = AbstractArchivePage.NO_SELECTION_TITLE;
		if(selectedObject instanceof ArchivedFile)
		{
			oldSelection = false;
			selectedArchivedFile = (ArchivedFile) selectedObject;
			selectionTitle = "Selection : " + selectedArchivedFile.getTypicalArchivedFile().getFileName();
			resetPerson(selectedArchivedFile.getPerson());
			if(selectedArchivedFile.getTypicalArchivedFile().getDocumentType() != null)
			{
				resetDocumentType(selectedArchivedFile.getTypicalArchivedFile().getDocumentType());
				String[] documentSubTypes = getSortedArrayFromList(
						new ArrayList<String>(selectedArchivedFile.getTypicalArchivedFile()
								.getDocumentType().getSubTypes()));
				documentSubTypeCombo.setInput(documentSubTypes);
				if(selectedArchivedFile.getTypicalArchivedFile().getDocumentType().getSelectedSubType() != null)
				{
					resetDocumentSubType(selectedArchivedFile.getTypicalArchivedFile().getDocumentType(), 
							selectedArchivedFile.getTypicalArchivedFile().getDocumentType().getSelectedSubType());
				}
			}
			cDateTime.setSelection(selectedArchivedFile.getSetDate());

			String value = selectedArchivedFile.getModifiedDate() == null ? ""
					: selectedArchivedFile.getModifiedDate().toString();
			dateModifiedText.setText(value);

			value = selectedArchivedFile.getDescription() == null 
					|| selectedArchivedFile.getDescription().isEmpty() ? ""
							: selectedArchivedFile.getDescription();
			descriptionText.setText(value);

			value = selectedArchivedFile.getKeywords() == null 
					|| selectedArchivedFile.getKeywords().isEmpty() ? ""
							: getCSKeywords(selectedArchivedFile.getKeywords());
			keywordsText.setText(value);

			changeKeywordButton.setEnabled(true);

			makeControlsEditable(true);
			oldSelection = true;
		}
		else
		{
			clearAll();
		}
		getRightSectionOfPage().setText(selectionTitle);
	}

	private void clearAll()
	{
		oldSelection = false;
		makeControlsEditable(false);
		selectedArchivedFile = null;
		documentTypeCombo.getCombo().select(0);
		documentSubTypeCombo.getCombo().select(0);
		personCombo.getCombo().select(0);
		cDateTime.setSelection(null);
		dateModifiedText.setText("");
		descriptionText.setText("");
		keywordsText.setText("");
	}

	private void makeControlsEditable(boolean editable)
	{
		documentTypeCombo.getCombo().setEnabled(editable);
		documentSubTypeCombo.getCombo().setEnabled(editable);
		personCombo.getCombo().setEnabled(editable);
		cDateTime.setEnabled(editable);
		//		dateModifiedText.setEnabled(false);
		descriptionText.setEnabled(editable);
		//		keywordsText.setEnabled(false);
		changeKeywordButton.setEnabled(editable);
	}

	private void resetCombo(ComboViewer comboViewer, String selectedValue) 
	{
		int selectionIndex = 0;
		if(selectedValue != null)
		{
			comboViewer.getCombo().clearSelection();
			String[] arrayOfValues = comboViewer.getCombo().getItems();
			int i = 0;
			for(String value : arrayOfValues)
			{
				if(value.equals(selectedValue))
				{
					selectionIndex = i;
					break;
				}
				i++;
			}
		}
		comboViewer.getCombo().select(selectionIndex);
	}

	private void resetPerson(String selectedPerson) 
	{
		if(selectedPerson != null)
		{
			boolean found = false;
			for(String docName : personCombo.getCombo().getItems())
			{
				if(docName.equals(selectedPerson))
				{
					found = true; break;
				}
			}
			if(!found)
			{
				personCombo.add(selectedPerson);
			}
		}
		resetCombo(personCombo, selectedPerson);
	}

	private void resetDocumentType(DocumentType documentType)
	{
		String documentTypeLabel = null;
		if(documentType != null)
		{
			boolean found = false;
			for(String docName : documentTypeCombo.getCombo().getItems())
			{
				if(docName.equals(documentType.getLabel()))
				{
					found = true; break;
				}
			}
			if(!found)
			{
				documentTypeCombo.add(documentType.clone());
			}
			documentTypeLabel = documentType.getLabel();
		}
		resetCombo(documentTypeCombo, documentTypeLabel);
	}

	private void resetDocumentSubType(DocumentType documentType,
			String selectedSubType)
	{
		if(documentType != null && selectedSubType != null)
		{
			boolean found = false;
			for(String docSubtype : documentSubTypeCombo.getCombo().getItems())
			{
				if(docSubtype.equals(selectedSubType))
				{
					found = true; break;
				}
			}
			if(!found)
			{
				documentSubTypeCombo.add(selectedSubType);
				documentType.addSubType(selectedSubType);
			}
		}
		resetCombo(documentSubTypeCombo, selectedSubType);
	}

	protected List<?> getInput()
	{
		return getArchiveEntryEditor().getArchive().getArchivedFiles();
	}

	@Override
	public void refreshPage()
	{
		ArchivedFile currentSelection = this.selectedArchivedFile;
		clearAll();
		setSelection(currentSelection);
	}

	@Override
	protected void setUpColumns(TreeViewer treeViewer)
	{
		final File archiveFolder = getArchiveEntryEditor().getArchiveFolder();

		treeViewer.setComparator(new ArchivedFileViewerComparator(getArchiveEntryEditor()));
		TreeViewerColumn columnViewer0 = new TreeViewerColumn(treeViewer, SWT.LEFT);
		columnViewer0.getColumn().setWidth(200);
		columnViewer0.getColumn().setText("Name");
		columnViewer0.setLabelProvider(new ColumnLabelProvider()
		{

			@Override
			public String getText(Object element)
			{
				if(element instanceof ArchivedFile)
				{
					return ((ArchivedFile) element).getTypicalArchivedFile().getFileName();
				}
				return null;
			}

			@Override
			public Image getImage(Object element)
			{
				if(element instanceof ArchivedFile)
				{
					return getFileIcon((ArchivedFile) element);

				}
				return null;
			}

		});
		columnViewer0.getColumn().addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				sortByName.run();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				sortByName.run();
			}
		});
		TreeViewerColumn columnViewer1 = new TreeViewerColumn(treeViewer, SWT.RIGHT);
		columnViewer1.getColumn().setWidth(100);
		columnViewer1.getColumn().setText("Size");
		columnViewer1.setLabelProvider(new ColumnLabelProvider()
		{

			@Override
			public String getText(Object element)
			{
				if(element instanceof ArchivedFile)
				{
					ArchivedFile archivedFile = ((ArchivedFile) element);
					return archivedFile.getTypicalArchivedFile() == null ?
							null : UtilityFileName.getFileSize(
									new File(archiveFolder, 
											archivedFile.getTypicalArchivedFile().getFileName()));
				}
				return null;
			}

			@Override
			public Image getImage(Object element)
			{
				return null;
			}

		});
		columnViewer1.getColumn().addSelectionListener(new ArchivedFileColumnComparatorListener(treeViewer));
	}

	protected Image getFileIcon(ArchivedFile archivedFile)
	{
		Image icon = UNKNOWN_ICON;
		try
		{
			File file = new File(getArchiveEntryEditor().getArchiveFolder(), 
					archivedFile.getTypicalArchivedFile().getFileName());
			if(file != null && file.getName() != null && file.getName().contains("."))
			{
				String fileEnding = file.getName().substring(file.getName().lastIndexOf('.'));
				if(FILE_ICON_MAP.containsKey(fileEnding))
				{
					icon = FILE_ICON_MAP.get(fileEnding);
				}
				else
				{
					Program fileProgram = Program.findProgram(fileEnding);
					ImageData iconData = null;
					if(fileProgram != null 
							&& ((iconData = fileProgram.getImageData().scaledTo(16, 16)) != null))
					{
						icon = new Image(Display.getCurrent(), iconData);
						FILE_ICON_MAP.put(fileEnding, icon);
					}
				}
			}
		} catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		return icon;
	}

	@Inject @Optional
	public void preferenceUpdated(@UIEventTopic(IGritsPreferenceStore.EVENT_TOPIC_PREF_VALUE_CHANGED) String preferenceName)
	{
		if(documentTypeCombo != null 
				&& preferenceName.equals(DocTypePreference.PREFERENCE_NAME_ALL))
		{
			DocTypePreference.loadPreferences();
			DocumentType selectedDocumentType = documentTypeCombo.getSelection().isEmpty() ? null :
				(DocumentType) ((StructuredSelection) documentTypeCombo.getSelection()).getFirstElement();
			String subTypeSelection = null;
			if(selectedDocumentType != null)
			{
				subTypeSelection = documentSubTypeCombo.getSelection().isEmpty() ? null :
					(String) ((StructuredSelection) documentSubTypeCombo.getSelection()).getFirstElement();
			}

			documentTypeCombo.setInput(getSortedDocumentFromList(
					new ArrayList<DocumentType>(DocTypePreference.ALL_DOCUMENT_TYPES)));

			for(DocumentType docType : DocTypePreference.ALL_DOCUMENT_TYPES)
			{
				if(Objects.equals(selectedDocumentType.getLabel(), docType.getLabel()))
				{
					selectedDocumentType = docType;
					break;
				}
			}
			resetDocumentType(selectedDocumentType);

			if(selectedDocumentType != null)
			{
				documentSubTypeCombo.setInput(getSortedArrayFromList(
						new ArrayList<String>(selectedDocumentType.getSubTypes())));
			}
			resetDocumentSubType(selectedDocumentType, subTypeSelection);
		}
		if(personCombo != null 
				&& preferenceName.equals(ProjectPreferenceStore.Preference.PERSON.getPreferenceName()))
		{
			String personSelection = personCombo.getSelection().isEmpty() ? null 
					: (String)((StructuredSelection) personCombo.getSelection()).getFirstElement();
			SingleChoicePreference personPreference =
					ProjectPreferenceStore.getSingleChoicePreference(
							ProjectPreferenceStore.Preference.PERSON);
			List<String> personList = new ArrayList<String>();
			personList.addAll(personPreference.getAllValues());
			personCombo.setInput(getSortedArrayFromList(personList));
			resetPerson(personSelection);
		}
		if(preferenceName.equals(KeywordPreference.PREFERENCE_NAME_ALL))
		{
			logger.debug("keywords updated");
			KeywordPreference.loadPreferences();
//			List<String> keywords = new ArrayList<String>();
//			keywords.addAll(KeywordPreference.DEFAULT_KEYWORDS);
//			keywords.addAll(KeywordPreference.OTHER_KEYWORDS);
//			Collections.sort(keywords, String.CASE_INSENSITIVE_ORDER);
//			selectKeywordsDialog = new SelectKeywordsDialog(Display.getCurrent().getActiveShell(), keywords);
		}
	}


	@Override
	protected String getTitle() {
		return PAGE_TITLE;
	}

}
