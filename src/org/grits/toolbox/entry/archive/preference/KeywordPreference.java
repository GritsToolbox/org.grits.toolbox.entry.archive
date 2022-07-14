/**
 * 
 */
package org.grits.toolbox.entry.archive.preference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.grits.toolbox.core.datamodel.UnsupportedVersionException;
import org.grits.toolbox.core.preference.PreferenceHandler;
import org.grits.toolbox.core.preference.project.UtilityPreferenceValue;
import org.grits.toolbox.core.preference.share.PreferenceEntity;
import org.grits.toolbox.core.preference.share.PreferenceReader;
import org.grits.toolbox.core.preference.share.PreferenceWriter;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.utilShare.UtilityPreference;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.core.utilShare.sort.CheckboxStringSorter;
import org.jdom.Element;

/**
 * 
 *
 */
public class KeywordPreference extends PreferencePage {

	private static final Logger logger = Logger.getLogger(KeywordPreference.class);

	public static final String PREFERENCE_PAGE_ID = 
			"org.grits.toolbox.preference.archive.keyword";
	public static final String PREFERENCE_NAME_ALL = KeywordPreference.class.getName() + ".all";
	private static final String DEFAULT_PREFERENCE_FILE = "keyword_preference.xml";
	private static final String NEW_KEYWORD_PREFIX = "New Keyword ";
	private static int newKeywordCount = 0;

	public static Set<String> DEFAULT_KEYWORDS = null;
	public static Set<String> OTHER_KEYWORDS = null;

	private CheckboxTableViewer checkboxTableViewer = null;
	private TableViewerColumn tableColumn2 = null;

	public static void loadPreferences()
	{
		DEFAULT_KEYWORDS = new HashSet<String>();
		OTHER_KEYWORDS = new HashSet<String>();
		newKeywordCount = 0;
		boolean loaded = false;
		try
		{
			try
			{
				PreferenceEntity preferenceEntity = 
						PreferenceReader.getPreferenceByName(PREFERENCE_NAME_ALL);
				if(preferenceEntity != null)
				{
					addToSet(UtilityPreferenceValue.getPreferenceValuesWithSelection(preferenceEntity));
					loaded = true;
				}
				else loaded = loadValuesPreVersioning(); // preference variable name has changed
			} catch (UnsupportedVersionException uEx)
			{
				loaded = loadValuesPreVersioning();
			}
			if(!loaded)
			{
				addToSet(PreferenceHandler.getPreferenceValues(DEFAULT_PREFERENCE_FILE));
				saveValues();
			}
			updateKeywordCount();
		} catch (Exception ex)
		{
			logger.error("Error getting the Preference variable for Default Keywords", ex);
		}
	}

	private static boolean loadValuesPreVersioning()
	{
		boolean loaded = false;
		String previousPrefName = KeywordPreference.class.getName() + ".default";
		Element prefElement = 
				PreferenceReader.getPreferenceElement(previousPrefName);
		if(prefElement == null)
			prefElement = PreferenceReader.getPreferenceElement("class " + previousPrefName);
		if(prefElement != null)
		{
			addToSet(UtilityPreferenceValue.getPreversioningValues(prefElement), true);
			saveValues();
			PreferenceWriter.deletePreference(previousPrefName);
			loaded = true;
		}

		previousPrefName = KeywordPreference.class.getName() + ".other";
		prefElement = 
				PreferenceReader.getPreferenceElement(previousPrefName);
		if(prefElement == null)
			prefElement = PreferenceReader.getPreferenceElement("class " + previousPrefName);
		if(prefElement != null)
		{
			addToSet(UtilityPreferenceValue.getPreversioningValues(prefElement), false);
			saveValues();
			PreferenceWriter.deletePreference(previousPrefName);
			loaded = true;
		}
		return loaded;
	}

	private static void addToSet(Map<String, Boolean> preferenceValuesWithSelection)
	{
		for(String keyword : preferenceValuesWithSelection.keySet())
		{
			if(preferenceValuesWithSelection.get(keyword))
			{
				DEFAULT_KEYWORDS.add(keyword);
			}
			else
			{
				OTHER_KEYWORDS.add(keyword);
			}
		}
	}

	private static void addToSet(Set<String> preferenceXmlValues, boolean selected)
	{
		Set<String> projectKeywords = 
				selected ? DEFAULT_KEYWORDS : OTHER_KEYWORDS;
		for(String keyword : preferenceXmlValues)
		{
			projectKeywords.add(keyword);
		}
	}

	private static void updateKeywordCount()
	{
		Set<String> keywords = new HashSet<String>();
		keywords.addAll(DEFAULT_KEYWORDS);
		keywords.addAll(OTHER_KEYWORDS);

		for(String action : keywords)
		{
			if(action.startsWith(NEW_KEYWORD_PREFIX))
			{
				String lastPart = action.substring(NEW_KEYWORD_PREFIX.length());
				try
				{
					int num = Integer.parseInt(lastPart);
					newKeywordCount = Math.max(newKeywordCount, num);
				} catch (NumberFormatException ex)
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
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		container.setLayout(layout);

		Label label = new Label(container, SWT.None);
		label.setText("Keywords");
		label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING, 
				GridData.VERTICAL_ALIGN_BEGINNING, true, false, 2, 1));

		checkboxTableViewer = createKeywordsTableViewer(container);
		initializeValues(OTHER_KEYWORDS, DEFAULT_KEYWORDS);

		Button addButton = new Button(container, SWT.None);
		addButton.setText("Add");
		GridData addButtonData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		addButtonData.widthHint = 80;
		addButtonData.verticalSpan = 1;
		addButtonData.grabExcessHorizontalSpace = true;
		addButtonData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		addButtonData.grabExcessVerticalSpace = true;
		addButton.setLayoutData(addButtonData);
		addButton.addSelectionListener(new SelectionListener()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				List<String> keywords = 
						(List<String>) checkboxTableViewer.getInput();
				String newKeyword = NEW_KEYWORD_PREFIX + (++newKeywordCount);
				keywords.add(newKeyword);
				checkboxTableViewer.refresh();

				// edit the added element
				int addingIndex = 0;
				int i = 0;
				for(String k : keywords)
				{
					if(k.equals(newKeyword))
					{
						addingIndex = i;
					}
					i++;
				}
				tableColumn2.getViewer().editElement(
						checkboxTableViewer.getElementAt(addingIndex), 1);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		Button removeButton = new Button(container, SWT.None);
		removeButton.setText("Remove");
		GridData removeButtonData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		removeButtonData.widthHint = 80;
		removeButtonData.verticalSpan = 1;
		removeButtonData.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		addButtonData.grabExcessVerticalSpace = true;
		removeButton.setLayoutData(removeButtonData);
		removeButton.addSelectionListener(new SelectionListener()
		{
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				List<String> keywords = 
						(List<String>) checkboxTableViewer.getInput();
				int selectionIndex = checkboxTableViewer.getTable().getSelectionIndex();
				if(selectionIndex >= 0 
						&& selectionIndex < keywords.size())
				{
					String selectedKeyword = 
							(String) checkboxTableViewer.getElementAt(selectionIndex);
					Object[] defKeywords = checkboxTableViewer.getCheckedElements();
					keywords.remove(selectedKeyword);
					checkboxTableViewer.refresh();
					if(keywords.size() > 0)
					{
						selectionIndex = Math.max(selectionIndex - 1, 0);
						checkboxTableViewer.getTable().select(selectionIndex);
						checkboxTableViewer.setSelection(checkboxTableViewer.getSelection());
					}
					checkboxTableViewer.setCheckedElements(defKeywords);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		return container;
	}

	private void initializeValues(Set<String> otherKeywords,
			Set<String> defaultKeywords)
	{
		List<String> keywords = new ArrayList<String>();
		keywords.addAll(otherKeywords);
		keywords.addAll(defaultKeywords);
		Collections.sort(keywords, String.CASE_INSENSITIVE_ORDER);
		checkboxTableViewer.setInput(keywords);
		String[] defKeywords = new String[defaultKeywords.size()];
		int i = 0;
		for(String keyword : defaultKeywords)
		{
			defKeywords[i++] = keyword;
		}
		checkboxTableViewer.setCheckedElements(defKeywords);
		checkboxTableViewer.refresh();
	}

	private CheckboxTableViewer createKeywordsTableViewer(Composite container)
	{
		Table keywordsTable = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		GridData tableLayouData = new GridData(GridData.FILL_BOTH);
		tableLayouData.horizontalSpan = 2;
		tableLayouData.verticalSpan = 1;
		tableLayouData.minimumHeight = 260;
		tableLayouData.heightHint = 300;
		keywordsTable.setLayoutData(tableLayouData);
		CheckboxTableViewer tableViewer = new CheckboxTableViewer(keywordsTable, "Default");

		tableColumn2 = new TableViewerColumn(tableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Keywords");
		tableColumn2.getColumn().setWidth(300);
		tableColumn2.setEditingSupport(new EditingSupport(tableViewer)
		{
			TextCellEditor textEditor = null;

			@SuppressWarnings("unchecked")
			@Override
			protected void setValue(Object element, Object value)
			{
				if(element instanceof String 
						&& value instanceof String)
				{
					List<String> input = (List<String>) checkboxTableViewer.getInput();
					for(String ele : input)
					{
						if(ele.equals(element))
						{
							input.set(input.indexOf(ele), ((String) value).trim());
							break;
						}
					}
					checkboxTableViewer.setInput(input);
					checkboxTableViewer.refresh();
				}
			}

			@Override
			protected Object getValue(Object element)
			{
				return element;
			}

			@Override
			protected CellEditor getCellEditor(Object element)
			{
				if(element instanceof String)
				{
					if(textEditor == null)
					{
						textEditor = new TextCellEditor(checkboxTableViewer.getTable());
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

		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.setContentProvider(new GenericListContentProvider());
		tableViewer.setLabelProvider(new ITableLabelProvider()
		{
			@Override
			public void removeListener(ILabelProviderListener listener)
			{

			}

			@Override
			public boolean isLabelProperty(Object element, String property)
			{
				return false;
			}

			@Override
			public void dispose()
			{

			}

			@Override
			public void addListener(ILabelProviderListener listener)
			{

			}

			@Override
			public String getColumnText(Object element, int columnIndex)
			{
				if(columnIndex > 0 
						&& element instanceof String)
				{
					return (String) element;
				}
				return null;
			}

			@Override
			public Image getColumnImage(Object element, int columnIndex)
			{
				return null;
			}
		});

		tableColumn2.getColumn().addSelectionListener(
				new CheckboxStringSorter(tableViewer));

		return tableViewer;
	}

	@Override
	protected void performDefaults()
	{
		HashMap<String, Boolean> keywordsMap = 
				PreferenceHandler.getPreferenceValues(DEFAULT_PREFERENCE_FILE);
		HashSet<String> otherKeywords = new HashSet<String>();
		Set<String> defaultKeywords = new HashSet<String>();
		for(String keyword : keywordsMap.keySet())
		{
			if(keywordsMap.get(keyword))
			{
				defaultKeywords.add(keyword);
			}
			else
			{
				otherKeywords.add(keyword);
			}
		}
		initializeValues(otherKeywords, defaultKeywords);
		super.performDefaults();
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
		if(invalidMessage == null)
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
		List<String> tableInput = (List<String>) checkboxTableViewer.getInput();
		Set<String> uniqueKeywords = new HashSet<String>();
		uniqueKeywords.addAll(tableInput);
		invalidMessage = uniqueKeywords.size() == tableInput.size() ?
				invalidMessage : "Non-unique Keywords";
		return invalidMessage;
	}

	@SuppressWarnings("unchecked")
	private void save()
	{
		List<String> tableInput = (List<String>) checkboxTableViewer.getInput();
		DEFAULT_KEYWORDS = new HashSet<String>();
		OTHER_KEYWORDS = new HashSet<String>();
		for(String keyword : tableInput)
		{
			if(checkboxTableViewer.getChecked(keyword))
			{
				DEFAULT_KEYWORDS.add(keyword);
			}
			else
			{
				OTHER_KEYWORDS.add(keyword);
			}
		}
		saveValues();
		UtilityPreference.updateUIs(
				Arrays.asList(PREFERENCE_NAME_ALL));
	}

	public static boolean saveValues() 
	{
		PreferenceEntity preferenceEntity = new PreferenceEntity(PREFERENCE_NAME_ALL);
		preferenceEntity.setValue(
				UtilityPreferenceValue.getPreferenceValueString(DEFAULT_KEYWORDS, OTHER_KEYWORDS));
		return PreferenceWriter.savePreference(preferenceEntity);
	}
}
