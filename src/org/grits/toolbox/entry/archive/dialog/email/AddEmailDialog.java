/**
 * 
 */
package org.grits.toolbox.entry.archive.dialog.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.preference.share.ProjectPreferenceStore;
import org.grits.toolbox.core.preference.share.SingleChoicePreference;
import org.grits.toolbox.entry.archive.model.ArchivedBasicEmail;

/**
 * 
 *
 */
public class AddEmailDialog extends TitleAreaDialog
{
	private static Logger logger = Logger.getLogger(AddEmailDialog.class);

	private ComboViewer personCombo = null;
	private Text emailSubjectText = null;
	private CDateTime cDateTime = null;
	private ArchivedBasicEmail archivedEmail = null;

	private String emailSubject = null;

	public AddEmailDialog(Shell parentShell, String emailSubject)
	{
		super(parentShell);
		this.emailSubject  = emailSubject;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle("Add Email");
		setMessage("Add Email to the Archive");
		getShell().setText("Add Email");
		getButton(OK).setText("Add");
		if(emailSubject != null)
			emailSubjectText.setText(emailSubject);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		logger.debug("Creating add email dialog");
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

		personCombo = createComboLine(container, "From", 3);
		SingleChoicePreference personPreference =
				ProjectPreferenceStore.getSingleChoicePreference(
						ProjectPreferenceStore.Preference.PERSON);
		List<String> personList = new ArrayList<String>();
		personList.addAll(personPreference.getAllValues());
		String[] persons = getSortedArrayFromList(personList);
		personCombo.setContentProvider(new ArrayContentProvider());
		personCombo.setInput(persons);
		selectInCombo(personCombo, personPreference.getDefaultValue());

		Label displayLabel = new Label(container, SWT.NONE);
		displayLabel.setText("Subject");
		emailSubjectText = new Text(container, SWT.BORDER);
		emailSubjectText.setText("");
		GridData buttonData = new GridData();
		buttonData.horizontalSpan = 3;
		buttonData.horizontalAlignment = SWT.FILL;
		emailSubjectText.setLayoutData(buttonData);

		createLabel(container, "Email Date");
		cDateTime = new CDateTime(container, CDT.BORDER | CDT.DROP_DOWN);
		GridData textGridData = new GridData();
		textGridData.grabExcessHorizontalSpace = true;
		textGridData.horizontalAlignment = SWT.FILL;
		textGridData.minimumWidth = 100;
		textGridData.horizontalSpan = 3;
		cDateTime.setLayoutData(textGridData);
		cDateTime.setSelection(new Date());

		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
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
		}
	}

	private ComboViewer createComboLine(Composite composite, String label, int horizontalSpan)
	{
		createLabel(composite, label);
		return createComboViewer(composite, horizontalSpan);
	}

	private ComboViewer createComboViewer(Composite composite,
			int horizontalSpan)
	{
		ComboViewer comboViewer = new ComboViewer(composite, SWT.READ_ONLY);
		GridData comboGridData = new GridData();
		comboGridData.grabExcessHorizontalSpace = true;
		comboGridData.horizontalAlignment = SWT.FILL;
		comboGridData.horizontalSpan = horizontalSpan;
		comboViewer.getCombo().setLayoutData(comboGridData);
		return comboViewer;
	}

	private void createLabel(Composite composite, String label)
	{
		Label labelLabel = new Label(composite, SWT.NONE);
		GridData labelGridData = new GridData();
		labelGridData.verticalAlignment = SWT.BEGINNING;
		labelLabel.setLayoutData(labelGridData);
		labelLabel.setText(label);
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

	@Override
	public void setErrorMessage(String newErrorMessage)
	{
		super.setErrorMessage(newErrorMessage);
		getButton(OK).setEnabled(newErrorMessage == null);
	}

	@Override
	protected void okPressed()
	{
		if(personCombo.getCombo().getItems().length == 0 
				|| personCombo.getCombo().getSelectionIndex() >= 0)
		{
			archivedEmail = new ArchivedBasicEmail();
			archivedEmail.setFromPerson(personCombo.getCombo().getText());
			archivedEmail.setEmailSubject(emailSubjectText.getText());
			archivedEmail.setEmailDate(cDateTime.getSelection());
			archivedEmail.setModifiedDate(new Date());
			super.okPressed();	
		}
		else
		{
			setErrorMessage("Select the person");
		}
	}

	public ArchivedBasicEmail getArchivedEmail()
	{
		return archivedEmail;
	}
}
