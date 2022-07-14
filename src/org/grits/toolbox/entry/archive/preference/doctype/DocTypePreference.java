/**
 *
 */
package org.grits.toolbox.entry.archive.preference.doctype;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TreeItem;
import org.grits.toolbox.core.dataShare.PropertyHandler;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.project.UtilityPreferenceValue;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.core.utilShare.UtilityPreference;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.model.DocumentType;
import org.grits.toolbox.entry.archive.preference.PreferenceHandler;
import org.jdom.Element;

/**
 *
 *
 */
public class DocTypePreference extends PreferencePage
{
	
	private static final Logger			logger						= Logger
			.getLogger(DocTypePreference.class.getName());
	
	public static final String			PREFERENCE_NAME_ALL			= DocTypePreference.class.getName() + ".all";
	private static final String			NEW_DOCUMENT_TYPE_PREFIX	= "New DocumentType ";
	private static final String			DEFAULT_PREFERENCE_FILE		= "docType_preference.xml";
	
	private static int					newDocumentTypeCount		= 0;
	
	public static List<DocumentType>	ALL_DOCUMENT_TYPES			= null;
	public static DocumentType			DEFAULT_DOCUMENT_TYPE		= null;
	
	private TreeViewer					treeViewer					= null;
	private MakeDefaultAction			makeDefaultAction			= null;
	
	public static void loadPreferences()
	{
		ALL_DOCUMENT_TYPES = new ArrayList<DocumentType>();
		DEFAULT_DOCUMENT_TYPE = null;
		newDocumentTypeCount = 0;
		boolean loaded = false;
		try
		{
			try
			{
				PreferenceEntity preferenceEntity = PreferenceReader.getPreferenceByName(PREFERENCE_NAME_ALL);
				if (preferenceEntity != null)
				{
					addToSet(UtilityPreferenceValue.getPreferenceValuesWithSelection(preferenceEntity));
					loaded = true;
				}
				else
					loaded = loadValuesPreVersioning(); // preference variable
														// name has changed
			}
			catch (UnsupportedVersionException uEx)
			{
				loaded = loadValuesPreVersioning();
			}
			if (!loaded)
			{
				File preferenceFile = new File(
						(FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getResource("preference")))
								.getPath() + File.separator + DEFAULT_PREFERENCE_FILE);
				ALL_DOCUMENT_TYPES = PreferenceHandler.getDocumentTypesFromXml(preferenceFile);
				for (DocumentType docType : ALL_DOCUMENT_TYPES)
				{
					if (docType.isMakeDefault())
					{
						DEFAULT_DOCUMENT_TYPE = docType;
						break;
					}
				}
				saveValues();
			}
			if (!ALL_DOCUMENT_TYPES.isEmpty() && DEFAULT_DOCUMENT_TYPE == null)
			{
				DEFAULT_DOCUMENT_TYPE = ALL_DOCUMENT_TYPES.iterator().next();
				DEFAULT_DOCUMENT_TYPE.setMakeDefault(true);
				saveValues();
			}
			updateDocumentTypeCount();
		}
		catch (Exception ex)
		{
			logger.error("Error getting the Preference variable for DocumentType", ex);
		}
	}
	
	private static boolean loadValuesPreVersioning()
	{
		boolean loaded = false;
		String prevPrefName = PREFERENCE_NAME_ALL;
		Element preferenceElement = PreferenceReader.getPreferenceElement(prevPrefName);
		if (preferenceElement == null)
		{
			prevPrefName = "class " + prevPrefName;
			preferenceElement = PreferenceReader.getPreferenceElement(prevPrefName);
		}
		if (preferenceElement != null)
		{
			DocumentType documentType = null;
			for (String preferenceValueString : UtilityPreferenceValue.getPreversioningValues(preferenceElement))
			{
				documentType = getDocumentTypeFromXml(preferenceValueString);
				if (documentType != null)
				{
					ALL_DOCUMENT_TYPES.add(documentType);
				}
			}
			DEFAULT_DOCUMENT_TYPE = getDocumentTypeFromXml(
					UtilityPreferenceValue.getPreversioningSelected(preferenceElement));
			PreferenceWriter.deletePreference(prevPrefName);
			saveValues();
			loaded = true;
		}
		return loaded;
	}
	
	private static void addToSet(Map<String, Boolean> preferenceValuesWithSelection)
	{
		DocumentType documentType = null;
		for (String keyword : preferenceValuesWithSelection.keySet())
		{
			documentType = getDocumentTypeFromXml(keyword);
			if (documentType != null)
			{
				if (preferenceValuesWithSelection.get(keyword))
				{
					DEFAULT_DOCUMENT_TYPE = documentType;
				}
				ALL_DOCUMENT_TYPES.add(documentType);
			}
		}
	}
	
	private static DocumentType getDocumentTypeFromXml(String docTypePreference)
	{
		DocumentType documentType = null;
		try
		{
			JAXBContext context = JAXBContext.newInstance(DocumentType.class);
			Unmarshaller unmarshaller = context.createUnmarshaller();
			documentType = (DocumentType) unmarshaller.unmarshal(new StringReader(docTypePreference));
		}
		catch (JAXBException e)
		{
			logger.error("The object could not be read from xml." + e.getMessage(), e);
		}
		return documentType;
	}
	
	private static void updateDocumentTypeCount()
	{
		for (DocumentType documentType : ALL_DOCUMENT_TYPES)
		{
			if (documentType.getLabel().startsWith(NEW_DOCUMENT_TYPE_PREFIX))
			{
				String lastPart = documentType.getLabel().substring(NEW_DOCUMENT_TYPE_PREFIX.length());
				try
				{
					int num = Integer.parseInt(lastPart);
					newDocumentTypeCount = Math.max(newDocumentTypeCount, num);
				}
				catch (NumberFormatException ex)
				{
					
				}
			}
		}
	}
	
	@Override
	protected Control createContents(Composite parent)
	{
		loadPreferences();
		Composite container = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginRight = 8;
		layout.verticalSpacing = 15;
		layout.horizontalSpacing = 10;
		layout.numColumns = 4;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);
		
		Label label = new Label(container, SWT.None);
		label.setText("DocumentTypes");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, GridData.VERTICAL_ALIGN_BEGINNING, true,
				false, 4, 1));
		
		treeViewer = createDocumentTypesTreeViewer(container);
		
		List<DocumentType> documentTypes = new ArrayList<DocumentType>();
		documentTypes.addAll(ALL_DOCUMENT_TYPES);
		DocumentType defaultDocumentType = null;
		
		for (DocumentType docType : documentTypes)
		{
			if (DEFAULT_DOCUMENT_TYPE.getLabel().equals(docType.getLabel()))
			{
				defaultDocumentType = docType;
			}
			else
			{
				docType.setMakeDefault(false);
			}
		}
		treeViewer.setComparator(new DocumentTypeComparator());
		treeViewer.setInput(documentTypes);
		treeViewer.refresh();
		
		treeViewer.expandToLevel(defaultDocumentType, TreeViewer.ALL_LEVELS);
		
		makeDefaultAction = new MakeDefaultAction(treeViewer);
		treeViewer.addDoubleClickListener(new IDoubleClickListener()
		{
			@Override
			public void doubleClick(DoubleClickEvent event)
			{
				makeDefaultAction.run();
			}
		});
		
		Button addButton = createButton(container, "Add");
		addButton.addSelectionListener(new SelectionListener()
		{
			
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				List<DocumentType> documentTypes = (List<DocumentType>) treeViewer.getInput();
				DocumentType newDocumentType = new DocumentType();
				newDocumentType.setLabel(NEW_DOCUMENT_TYPE_PREFIX + (++newDocumentTypeCount));
				documentTypes.add(newDocumentType);
				treeViewer.refresh();
				for (TreeItem treeItem : treeViewer.getTree().getItems())
				{
					if (matchDocumentType((DocumentType) treeItem.getData(), newDocumentType))
					{
						treeViewer.setSelection(new StructuredSelection(newDocumentType));
						break;
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
		
		Button addSubType = createButton(container, "Add SubType");
		addSubType.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				TreeSelection treeSelection = (TreeSelection) treeViewer.getSelection();
				if (!treeSelection.isEmpty())
				{
					DocumentType selectedDocumentType = null;
					if (treeSelection.getFirstElement() instanceof DocumentType)
					{
						selectedDocumentType = (DocumentType) treeSelection.getFirstElement();
					}
					else
					{
						TreePath[] paths = treeSelection.getPaths();
						selectedDocumentType = (DocumentType) paths[0].getSegment(0);
					}
					if (selectedDocumentType != null)
					{
						int suffix = selectedDocumentType.getSubTypes().size();
						String newSubType = "SubType " + suffix;
						while (selectedDocumentType.getSubTypes().contains(newSubType))
						{
							newSubType = "SubType " + ++suffix;
						}
						selectedDocumentType.addSubType(newSubType);
						if (selectedDocumentType.getSubTypes().size() == 1)
						{
							selectedDocumentType
									.setSelectedSubType(selectedDocumentType.getSubTypes().iterator().next());
						}
						treeViewer.refresh();
						treeViewer.expandToLevel(selectedDocumentType, TreeViewer.ALL_LEVELS);
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
		
		Button removeButton = createButton(container, "Remove");
		removeButton.addSelectionListener(new SelectionListener()
		{
			
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				List<DocumentType> documentTypes = (List<DocumentType>) treeViewer.getInput();
				TreeSelection treeSelection = (TreeSelection) treeViewer.getSelection();
				int selectionIndex = -1;
				DocumentType selectedDocumentType = null;
				if (!treeSelection.isEmpty() && treeSelection.getFirstElement() instanceof DocumentType)
				{
					selectedDocumentType = (DocumentType) treeSelection.getFirstElement();
					selectionIndex = Math.max(0, getTreeItemIndex(selectedDocumentType) - 1);
					documentTypes.remove(selectedDocumentType);
				}
				else
					if (treeSelection.getFirstElement() instanceof String)
					{
						TreePath[] paths = treeSelection.getPaths();
						selectedDocumentType = (DocumentType) paths[0].getSegment(0);
						selectionIndex = getTreeItemIndex(selectedDocumentType);
						
						logger.debug(selectedDocumentType.getLabel());
						
						selectedDocumentType.getSubTypes().remove(treeSelection.getFirstElement());
						if (selectedDocumentType.getSubTypes().size() == 1
								|| selectedDocumentType.getSelectedSubType() == treeSelection.getFirstElement())
						{
							selectedDocumentType
									.setSelectedSubType(selectedDocumentType.getSubTypes().iterator().next());
						}
					}
				treeViewer.refresh();
				if (documentTypes.contains(selectedDocumentType))
				{
					for (TreeItem treeItem : treeViewer.getTree().getItems())
					{
						if (matchDocumentType((DocumentType) treeItem.getData(), selectedDocumentType))
						{
							treeViewer.setSelection(new StructuredSelection(selectedDocumentType));
							break;
						}
					}
				}
				else
					if (selectionIndex >= 0 && selectionIndex < documentTypes.size())
					{
						treeViewer.setSelection(
								new StructuredSelection(treeViewer.getTree().getItems()[selectionIndex].getData()));
					}
			}
			
			private int getTreeItemIndex(DocumentType selectedDocumentType)
			{
				int selectionIndex = -1;
				int i = 0;
				for (TreeItem item : treeViewer.getTree().getItems())
				{
					if (item.getData().equals(selectedDocumentType))
					{
						selectionIndex = i;
						break;
					}
					i++;
				}
				return selectionIndex;
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				
			}
		});
		
		return container;
	}
	
	protected boolean matchDocumentType(DocumentType documentType, DocumentType selectedDocumentType)
	{
		boolean match = documentType.getLabel().equals(selectedDocumentType.getLabel());
		return match;
	}
	
	private Button createButton(Composite container, String label)
	{
		Button button = new Button(container, SWT.None);
		button.setText(label);
		GridData buttonData = new GridData();
		buttonData.widthHint = 120;
		buttonData.verticalSpan = 1;
		buttonData.horizontalSpan = 1;
		buttonData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		buttonData.grabExcessVerticalSpace = true;
		button.setLayoutData(buttonData);
		return button;
	}
	
	private TreeViewer createDocumentTypesTreeViewer(Composite container)
	{
		TreeViewer treeViewer = new TreeViewer(container,
				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		
		TreeViewerColumn treeColumn1 = new TreeViewerColumn(treeViewer, SWT.FILL, 0);
		treeColumn1.getColumn().setText("DocumentTypes");
		treeColumn1.getColumn().setWidth(220);
		
		treeColumn1.setEditingSupport(new EditingSupport(treeViewer)
		{
			TextCellEditor textEditor = null;
			
			@Override
			protected void setValue(Object element, Object value)
			{
				if (element instanceof DocumentType && value instanceof String)
				{
					String newValue = ((String) value).trim();
					if (!newValue.isEmpty())
					{
						String oldValue = ((DocumentType) element).getLabel();
						((DocumentType) element).setLabel(newValue);
						if (validateInput() != null)
						{
							((DocumentType) element).setLabel(oldValue);
						}
						DocTypePreference.this.treeViewer.refresh();
					}
				}
			}
			
			@Override
			protected Object getValue(Object element)
			{
				if (element instanceof DocumentType)
					return ((DocumentType) element).getLabel();
				return null;
			}
			
			@Override
			protected CellEditor getCellEditor(Object element)
			{
				if (element instanceof DocumentType)
				{
					if (textEditor == null)
					{
						textEditor = new TextCellEditor(DocTypePreference.this.treeViewer.getTree());
					}
					return textEditor;
				}
				return null;
			}
			
			@Override
			protected boolean canEdit(Object element)
			{
				return element instanceof DocumentType;
			}
		});
		
		TreeViewerColumn treeColumn2 = new TreeViewerColumn(treeViewer, SWT.FILL, 1);
		treeColumn2.getColumn().setText("SubType");
		treeColumn2.getColumn().setWidth(200);
		
		treeColumn2.setEditingSupport(new EditingSupport(treeViewer)
		{
			TextCellEditor	textEditor		= null;
			TreeSelection	treeSelection	= null;
			
			@Override
			protected void setValue(Object element, Object value)
			{
				if (element instanceof String && value instanceof String)
				{
					String newValue = ((String) value).trim();
					TreePath[] paths = treeSelection.getPaths();
					DocumentType selectedDocumentType = (DocumentType) paths[0].getSegment(0);
					
					logger.debug(selectedDocumentType.getLabel());
					
					if (!newValue.isEmpty())
					{
						selectedDocumentType.getSubTypes().remove(element);
						selectedDocumentType.addSubType(newValue);
						if (selectedDocumentType.getSubTypes().size() == 1)
						{
							selectedDocumentType
									.setSelectedSubType(selectedDocumentType.getSubTypes().iterator().next());
						}
					}
					DocTypePreference.this.treeViewer.refresh();
				}
			}
			
			@Override
			protected Object getValue(Object element)
			{
				if (element instanceof String)
					return element;
				return null;
			}
			
			@Override
			protected CellEditor getCellEditor(Object element)
			{
				treeSelection = (TreeSelection) DocTypePreference.this.treeViewer.getSelection();
				if (element instanceof String)
				{
					if (textEditor == null)
					{
						textEditor = new TextCellEditor(DocTypePreference.this.treeViewer.getTree());
					}
					return textEditor;
				}
				return null;
			}
			
			@Override
			protected boolean canEdit(Object element)
			{
				return element instanceof String;
			}
		});
		
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.getTree().setLinesVisible(true);
		
		treeViewer.setLabelProvider(new DocumentTypeFontLabelProvider(treeViewer));
		treeViewer.setContentProvider(new DocumentTypeContentProvider());
		
		GridData gridDataTree = new GridData(GridData.FILL_BOTH);
		gridDataTree.grabExcessHorizontalSpace = true;
		gridDataTree.grabExcessVerticalSpace = true;
		gridDataTree.horizontalSpan = 4;
		gridDataTree.verticalSpan = 1;
		gridDataTree.minimumHeight = 250;
		gridDataTree.heightHint = 400;
		gridDataTree.minimumWidth = 300;
		gridDataTree.widthHint = 400;
		treeViewer.getTree().setLayoutData(gridDataTree);
		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.getTree().setLinesVisible(true);
		return treeViewer;
	}
	
	@Override
	protected void performDefaults()
	{
		try
		{
			File preferenceFile = new File(
					(FileLocator.toFileURL(Platform.getBundle(Activator.PLUGIN_ID).getResource("preference"))).getPath()
							+ File.separator + DEFAULT_PREFERENCE_FILE);
			List<DocumentType> allDocumentTypes = PreferenceHandler.getDocumentTypesFromXml(preferenceFile);
			DocumentType defaultDocumentType = null;
			for (DocumentType docType : allDocumentTypes)
			{
				if (docType.isMakeDefault())
				{
					defaultDocumentType = docType;
					break;
				}
			}
			initializeValues(allDocumentTypes, defaultDocumentType);
			super.performDefaults();
		}
		catch (IOException ex)
		{
			logger.fatal(ex.getMessage(), ex);
		}
	}
	
	private void initializeValues(List<DocumentType> allDocumentTypes, DocumentType defaultDocumentType)
	{
		treeViewer.setInput(allDocumentTypes);
		treeViewer.refresh();
	}
	
	@Override
	protected void performApply()
	{
		performOk();
	}
	
	@Override
	public boolean performOk()
	{
		String invalidMessage = validateInput();
		setErrorMessage(invalidMessage);
		if (invalidMessage == null)
		{
			save();
			return true;
		}
		else
		{
			return false;
		}
	}
	
	@SuppressWarnings("unchecked")
	private String validateInput()
	{
		String invalidMessage = null;
		List<DocumentType> treeInput = (List<DocumentType>) treeViewer.getInput();
		boolean nonUnique = false;
		int i = 0;
		int j = 0;
		DocumentType element1 = null;
		DocumentType element2 = null;
		outerLoop: while (i < treeInput.size())
		{
			if ((element1 = treeInput.get(i)) != null)
			{
				j = i + 1;
				while (j < treeInput.size())
				{
					if ((element2 = treeInput.get(j)) != null)
					{
						if (matchDocumentType(element1, element2))
						{
							nonUnique = true;
							break outerLoop;
						}
					}
					j++;
				}
			}
			i++;
		}
		invalidMessage = nonUnique ? "Non-unique DocumentTypes" : invalidMessage;
		return invalidMessage;
	}
	
	@SuppressWarnings("unchecked")
	private void save()
	{
		List<DocumentType> treeInput = (List<DocumentType>) treeViewer.getInput();
		ALL_DOCUMENT_TYPES = new ArrayList<DocumentType>();
		DEFAULT_DOCUMENT_TYPE = null;
		for (DocumentType documentType : treeInput)
		{
			if (documentType.isMakeDefault())
			{
				DEFAULT_DOCUMENT_TYPE = documentType;
			}
			ALL_DOCUMENT_TYPES.add(documentType);
		}
		saveValues();
		
		UtilityPreference.updateEditors(Arrays.asList(PREFERENCE_NAME_ALL));
	}
	
	private static boolean saveValues()
	{
		PreferenceEntity preferenceEntity = new PreferenceEntity(PREFERENCE_NAME_ALL);
		preferenceEntity.setValue(UtilityPreferenceValue.getPreferenceValueString(
				getStringSetFromList(ALL_DOCUMENT_TYPES), getXmlString(DEFAULT_DOCUMENT_TYPE)));
		return PreferenceWriter.savePreference(preferenceEntity);
	}
	
	private static String getXmlString(DocumentType documentType)
	{
		String documentTypeXml = null;
		if (documentType != null)
		{
			try
			{
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				JAXBContext context = JAXBContext.newInstance(DocumentType.class);
				Marshaller marshaller = context.createMarshaller();
				marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				marshaller.setProperty(Marshaller.JAXB_ENCODING, PropertyHandler.GRITS_CHARACTER_ENCODING);
				marshaller.marshal(documentType, os);
				documentTypeXml = os.toString();
			}
			catch (JAXBException e)
			{
				logger.error("The DocumentType could not be serialized as xml." + e.getMessage(), e);
			}
			catch (Exception e)
			{
				logger.fatal("The DocumentType could not be serialized as xml." + e.getMessage(), e);
			}
		}
		return documentTypeXml;
	}
	
	private static Set<String> getStringSetFromList(List<DocumentType> documentTypes)
	{
		Set<String> valueSet = new HashSet<String>();
		String xmlString = null;
		for (DocumentType element : documentTypes)
		{
			xmlString = getXmlString(element);
			if (xmlString != null)
				valueSet.add(xmlString);
		}
		return valueSet;
	}
}