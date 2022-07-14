/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedemail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.grits.toolbox.core.preference.share.IGritsPreferenceStore;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.preference.share.SingleChoicePreference;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.config.IConfig;
import org.grits.toolbox.entry.archive.config.ImageRegistry;
import org.grits.toolbox.entry.archive.config.ImageRegistry.ArchiveImage;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.AbstractArchivePage;
import org.grits.toolbox.entry.archive.editor.pages.archivedemail.actions.AddArchivedEmail;
import org.grits.toolbox.entry.archive.editor.pages.archivedemail.actions.AddReplyEmail;
import org.grits.toolbox.entry.archive.editor.pages.archivedemail.actions.DeleteAll;
import org.grits.toolbox.entry.archive.editor.pages.archivedemail.actions.DeleteEmail;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.sort.ArchivedFileColumnComparatorListener;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.sort.ArchivedFileViewerComparator;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.sort.SortFileNames;
import org.grits.toolbox.entry.archive.model.ArchivedBasicEmail;
import org.grits.toolbox.entry.archive.model.ArchivedEmail;
import org.grits.toolbox.entry.archive.model.ArchivedEmailTopic;

/**
 * 
 *
 */
public class EmailArchivePage extends AbstractArchivePage 
{
	private Logger logger = Logger.getLogger(EmailArchivePage.class);
	public static final String ID = "1";
	public static final String PAGE_TITLE = "Email  ";

	private static final Image EMAIL_ICON = ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.ADD_EMAIL_ICON).createImage();
	private static final Image REPLY_ICON = ImageRegistry.getImageDescriptor(Activator.PLUGIN_ID, ArchiveImage.REPLY_EMAIL_ICON).createImage();

	private ArchivedBasicEmail selectedArchivedEmail = null;

	private ArrayList<Action> leftSectionActions = null;

	private Composite rightSideComposite = null;
	private ComboViewer fromPersonCombo = null;
	private Text subjectText = null;
	private CDateTime cDateTime = null;
	private Text emailContentText = null;
	private Text notesText = null;
	private Label lastEditedText = null;
	private boolean oldSelection = true;

	private SortFileNames sortByName;
	@Inject EMenuService menuService;
	
	@Inject
	public EmailArchivePage(ArchiveEntryEditor archiveEntryEditor, CTabItem cTabItem)
	{
		super(archiveEntryEditor, cTabItem);
	}

	public Boolean isOldSelection()
	{
		return this.oldSelection;
	}

	private ArchiveEntryEditor getArchiveEntryEditor()
	{
		return ((ArchiveEntryEditor) this.getEditor());
	}

	public ArchivedBasicEmail getSelectedArchivedEmail()
	{
		return selectedArchivedEmail;
	}

	@Override
	public void createRestOftheContent()
	{
		logger.debug("- START : Retrieving all ArchivedBasicEmail for the Archive Editor.");
		try
		{
			rightSideComposite = new Composite(getRightSectionOfPage(), SWT.NONE);
			GridLayout rightSideCompositeLayout = new GridLayout();
			rightSideCompositeLayout.verticalSpacing = 15;
			rightSideCompositeLayout.horizontalSpacing = 10;
			rightSideCompositeLayout.numColumns = 4;
			rightSideCompositeLayout.makeColumnsEqualWidth = false;
			rightSideComposite.setLayout(rightSideCompositeLayout);

			final int MAX_RIGHT_SIDE_WIDTH = 520;

			fromPersonCombo = createComboLine("From", 3);
			fromPersonCombo.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					if(selectedArchivedEmail != null 
							&& fromPersonCombo.getCombo().getSelectionIndex() >= 0)
					{
						String person = fromPersonCombo.getCombo().getItem(
								fromPersonCombo.getCombo().getSelectionIndex());
						if(!Objects.equals(person, selectedArchivedEmail.getFromPerson()))
						{
							selectedArchivedEmail.setFromPerson(person);
							pageEdited();
							refreshPage();
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
			fromPersonCombo.setContentProvider(new ArrayContentProvider());
			fromPersonCombo.setInput(persons);

			createLabel("Date");
			cDateTime = new CDateTime(rightSideComposite, CDT.BORDER | CDT.DROP_DOWN);
			GridData textGridData = new GridData();
			textGridData.grabExcessHorizontalSpace = true;
			textGridData.horizontalAlignment = SWT.FILL;
			textGridData.minimumWidth = 100;
			textGridData.horizontalSpan = 3;
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
					if(selectedArchivedEmail != null)
					{
						Date date = cDateTime.getSelection();
						if(!Objects.equals(date, 
								selectedArchivedEmail.getEmailDate()))
						{
							selectedArchivedEmail.setEmailDate(date);
							pageEdited();
							refreshPage();
						}
					}
				}
			});

			subjectText = createLineForLabel(
					"Subject", 10000, SWT.BORDER|SWT.WRAP, MAX_RIGHT_SIDE_WIDTH, -1, 3);
			subjectText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent e)
				{
					if(selectedArchivedEmail != null)
					{
						String subject = subjectText.getText().trim();
						if(!Objects.equals(subject, 
								selectedArchivedEmail.getEmailSubject()))
						{
							selectedArchivedEmail.setEmailSubject(subject);
							pageEdited();
							refreshPage();
						}
					}
				}
			});
			subjectText.setEnabled(false);

			emailContentText = createLineForLabel("Email", 10000, 
					SWT.BORDER|SWT.V_SCROLL|SWT.WRAP, MAX_RIGHT_SIDE_WIDTH , 400, 3);
			emailContentText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent e)
				{
					if(selectedArchivedEmail != null)
					{
						String description = emailContentText.getText().trim();
						if(!Objects.equals(description, 
								selectedArchivedEmail.getEmailContent()))
						{
							selectedArchivedEmail.setEmailContent(description);
							pageEdited();
						}
					}
				}
			});

			notesText = createLineForLabel("Notes", 10000, 
					SWT.BORDER|SWT.V_SCROLL|SWT.WRAP, MAX_RIGHT_SIDE_WIDTH , 60, 3);
			notesText.addModifyListener(new ModifyListener()
			{
				@Override
				public void modifyText(ModifyEvent e)
				{
					if(selectedArchivedEmail != null)
					{
						String notes = notesText.getText().trim();
						if(!Objects.equals(notes, 
								selectedArchivedEmail.getNotes()))
						{
							selectedArchivedEmail.setNotes(notes);
							pageEdited();
						}
					}
				}
			});

			lastEditedText = new Label(rightSideComposite, SWT.NONE);
			lastEditedText.setText("Last Edited on ");
			GridData labelGridData = new GridData();
			labelGridData.horizontalSpan = 2;
			labelGridData.verticalAlignment = SWT.BEGINNING;
			lastEditedText.setLayoutData(labelGridData);

			rightSideComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			getRightSectionOfPage().setClient(rightSideComposite);
			clearAll();

//			ArchivedEmail.TO_PERSON = "Some Person";//ArchiveEmailPreference.TO_PERSON;
		} catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
		}
		logger.debug("- END   : Retrieving all ArchivedBasicEmail for the Archive Editor.");
	}

	public void pageEdited()
	{
		if(selectedArchivedEmail != null && oldSelection)
		{
			selectedArchivedEmail.setModifiedDate(new Date());
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
		leftSectionActions.add(0, new AddArchivedEmail(this));
		leftSectionActions.add(1, new AddReplyEmail(this));
		leftSectionActions.add(2, new DeleteEmail(this));
		leftSectionActions.add(3, new DeleteAll(this));
		sortByName = new SortFileNames(this.leftSideTreeViewer);
		leftSectionActions.add(4, sortByName);
		createContextMenu();
		return leftSectionActions;
	}

	protected void createContextMenu()
	{
		if(this.leftSideTreeViewer != null)
		{
			MenuManager contextMenu = new MenuManager("#ViewerMenu-email");
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
			menuService.registerContextMenu(this.leftSideTreeViewer, "#ViewerMenu-email");
		}
	}

	protected void fillContextMenu(IMenuManager contextMenu)
	{
		contextMenu.add(leftSectionActions.get(0));
		contextMenu.add(new Separator("additions"));
		contextMenu.add(leftSectionActions.get(1));
		contextMenu.add(leftSectionActions.get(2));
		contextMenu.add(leftSectionActions.get(3));
	}

	@Override
	public void setSelection(Object selectedObject)
	{
		String selectionTitle = AbstractArchivePage.NO_SELECTION_TITLE;
		if(selectedObject instanceof ArchivedBasicEmail || selectedObject instanceof ArchivedEmailTopic)
		{
			oldSelection = false;
			selectedArchivedEmail = selectedObject instanceof ArchivedEmailTopic ? 
					((ArchivedEmailTopic) selectedObject).getlastReplyEmail() : (ArchivedBasicEmail) selectedObject;
			selectionTitle = "Selection : " + selectedArchivedEmail.getDisplayTitle();
			resetPerson(selectedArchivedEmail.getFromPerson());

			cDateTime.setSelection(selectedArchivedEmail.getEmailDate());

			String value = selectedArchivedEmail.getEmailSubject() == null ? ""
					: selectedArchivedEmail.getEmailSubject();
			subjectText.setText(value);

			value = selectedArchivedEmail.getEmailContent() == null ? ""
					: selectedArchivedEmail.getEmailContent();
			emailContentText.setText(value);

			value = selectedArchivedEmail.getNotes() == null ? ""
					: selectedArchivedEmail.getNotes();
			notesText.setText(value);

			value = selectedArchivedEmail.getModifiedDate() == null ? ""
					: selectedArchivedEmail.getModifiedDate().toString();
			lastEditedText.setText("Last Edited on " + value);

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
		selectedArchivedEmail = null;
		lastEditedText.setText("Last Edited on ");
		emailContentText.setText("");
		notesText.setText("");
		subjectText.setText("");
		fromPersonCombo.getCombo().select(0);
		cDateTime.setSelection(null);
	}

	private void makeControlsEditable(boolean editable)
	{
		fromPersonCombo.getCombo().setEnabled(editable);
		cDateTime.setEnabled(editable);
		subjectText.setEnabled(editable);
		//		lastEditedText.setEnabled(false);
		emailContentText.setEnabled(editable);
		notesText.setEnabled(editable);
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
			for(String docName : fromPersonCombo.getCombo().getItems())
			{
				if(docName.equals(selectedPerson))
				{
					found = true; break;
				}
			}
			if(!found)
			{
				fromPersonCombo.add(selectedPerson);
			}
		}
		resetCombo(fromPersonCombo, selectedPerson);
	}

	@Override
	protected List<?> getInput() 
	{
		return getArchiveEntryEditor().getArchive().getArchivedEmails();
	}

	@Override
	public void refreshPage()
	{
		TreeSelection selection = (TreeSelection) leftSideTreeViewer.getSelection();
		getLeftSideTreeViewer().refresh();
		setSelection(selection.getFirstElement());
	}

	@Override
	protected void setUpColumns(TreeViewer treeViewer)
	{
		treeViewer.setComparator(new ArchivedFileViewerComparator(getArchiveEntryEditor()));
		TreeViewerColumn columnViewer0 = new TreeViewerColumn(treeViewer, SWT.LEFT);
		columnViewer0.getColumn().setWidth(170);
		columnViewer0.getColumn().setText("Email");
		columnViewer0.setLabelProvider(new ColumnLabelProvider()
		{

			@Override
			public String getText(Object element)
			{
				if(element instanceof ArchivedEmail)
				{
					return ((ArchivedEmail) element).getDisplayTitle();
				}
				return null;
			}

			@Override
			public Image getImage(Object element)
			{
				if(element instanceof ArchivedBasicEmail)
				{
					return getEmailIcon((ArchivedBasicEmail) element);
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
		TreeViewerColumn columnViewer1 = new TreeViewerColumn(treeViewer, SWT.LEFT);
		columnViewer1.getColumn().setWidth(150);
		columnViewer1.getColumn().setText("Date");
		columnViewer1.setLabelProvider(new ColumnLabelProvider()
		{

			@Override
			public String getText(Object element)
			{
				if(element instanceof ArchivedBasicEmail 
						&& ((ArchivedBasicEmail) element).getArchivedEmailTopic() != null)
				{
					ArchivedEmail archivedBasicEmail = (ArchivedBasicEmail) element;
					return archivedBasicEmail == null ? " " 
							: " " + ArchivedEmail.DATE_FORMAT1.format(archivedBasicEmail.getEmailDate());
				}
				if(element instanceof ArchivedEmail)
				{
					ArchivedEmail archivedEmail = ((ArchivedEmail) element);
					String dateValue = archivedEmail.getEmailDate() == null ? " " 
							: " " + ArchivedEmail.DATE_FORMAT0.format(archivedEmail.getEmailDate());
					return dateValue;
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

	protected Image getEmailIcon(ArchivedEmail archivedEmail)
	{
		Image icon = null;
		try
		{
			if(archivedEmail instanceof ArchivedBasicEmail)
			{
				icon = ((ArchivedBasicEmail) archivedEmail).getArchivedEmailTopic() == null 
						? EMAIL_ICON : REPLY_ICON;
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
		if(fromPersonCombo != null 
				&& preferenceName.equals(ProjectPreferenceStore.Preference.PERSON.getPreferenceName()))
		{
			String personSelection = fromPersonCombo.getSelection().isEmpty() ? null 
					: (String)((StructuredSelection) fromPersonCombo.getSelection()).getFirstElement();
			SingleChoicePreference personPreference =
					ProjectPreferenceStore.getSingleChoicePreference(
							ProjectPreferenceStore.Preference.PERSON);
			List<String> personList = new ArrayList<String>();
			personList.addAll(personPreference.getAllValues());
			fromPersonCombo.setInput(getSortedArrayFromList(personList));
			resetPerson(personSelection);
		}
	}

	@Override
	protected String getTitle() {
		return PAGE_TITLE;
	}
}