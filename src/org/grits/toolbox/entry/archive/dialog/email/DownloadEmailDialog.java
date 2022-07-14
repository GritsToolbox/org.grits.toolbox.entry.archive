/**
 * 
 */
package org.grits.toolbox.entry.archive.dialog.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.archivedemail.EmailArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedBasicEmail;

/**
 * 
 *
 */
public class DownloadEmailDialog extends TitleAreaDialog
{
	private static Logger logger = Logger.getLogger(DownloadEmailDialog.class);

	private EmailArchivePage emailArchivePage = null;

	private ArchivedBasicEmail selectedArchivedEmail = null;
	private Set<String> existingNames = null;

	private Text locationText = null;
	private Button overwrite = null;

	public DownloadEmailDialog(EmailArchivePage emailArchivePage, ArchivedBasicEmail inputArchivedEmail)
	{
		super(Display.getCurrent().getActiveShell());
		this.emailArchivePage  = emailArchivePage;
		this.selectedArchivedEmail  = inputArchivedEmail;
		this.existingNames  = new HashSet<String>();
	}

	@Override
	public void create()
	{
		super.create();
		setTitle("Download File");
		setMessage("Download File From the Archive");
		getShell().setText("Download");
		getButton(OK).setText("Download");
		getShell().setSize(700, 360);
	}

	@Override
	protected Control createDialogArea(Composite parent)
	{
		Composite composite = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(composite, SWT.BORDER);
		container.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false));
		GridLayout layout = new GridLayout(5, false);
		layout.marginTop = 10;
		layout.marginBottom = 20;
		layout.marginLeft = 10;
		layout.marginRight = 10;
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		container.setLayout(layout);

		locationText =  new Text(container, SWT.BORDER|SWT.BORDER_SOLID);
		GridData textData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		textData.horizontalSpan = 4;
		textData.minimumWidth = 300;
		locationText.setLayoutData(textData);

		Button browseFolderButton = new Button(container, SWT.PUSH);
		browseFolderButton.setText(" Download Location ");
		GridData browseButtonGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		browseButtonGridData.horizontalSpan = 1;
		browseFolderButton.setLayoutData(browseButtonGridData);

		Font currentFont = Display.getCurrent().getSystemFont();
		FontData fontData= currentFont.getFontData()[0];
		Font boldFont = new Font(Display.getCurrent(),fontData.getName(), fontData.getHeight(), SWT.BOLD);

		Label label = new Label(container, SWT.READ_ONLY);
		GridData labelData = new GridData(SWT.LEFT);
		labelData.horizontalSpan = 4;
		label.setLayoutData(labelData);

		overwrite = new Button(container, SWT.CHECK);
		overwrite.setText("Overwrite Existing");
		overwrite.setSelection(false);
		overwrite.addSelectionListener(new SelectionListener()
		{

			@Override
			public void widgetSelected(SelectionEvent e)
			{
				verifyInput();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{
				verifyInput();
			}
		});

		label = new Label(container, SWT.READ_ONLY);
		label.setText("Selected Archive     ");

		Label selectEmailsLabel = new Label(container, SWT.READ_ONLY);
		GridData emailLabelData = new GridData(SWT.LEFT);
		emailLabelData.horizontalSpan = 1;
		selectEmailsLabel.setLayoutData(emailLabelData);
		selectEmailsLabel.setText(selectedArchivedEmail.getDisplayTitle());
		selectEmailsLabel.setFont(boldFont);

		browseFolderButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				setErrorMessage(null);
				String selected = null;
				FileDialog emailDialog = new FileDialog(getShell(), SWT.SAVE);
				emailDialog.setFileName(selectedArchivedEmail.getDisplayTitle());
				emailDialog.setOverwrite(true);
				emailDialog.setText("Save Location");
				selected = emailDialog.open();
				if (selected != null)
				{
					try
					{
						existingNames = new HashSet<String>(Arrays.asList(new File(selected).getParentFile().list()));
						locationText.setText(selected);
					} catch (Exception ex)
					{
						logger.error(ex.getMessage(), ex);
						setErrorMessage("Please select a valid location to download.");
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e)
			{

			}
		});

		locationText.setText("");
		locationText.addModifyListener(new ModifyListener()
		{
			@Override
			public void modifyText(ModifyEvent e)
			{
					verifyInput();
			}
		});
		browseFolderButton.setFocus();
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}

	protected void verifyInput()
	{
		setErrorMessage(null);
		String location = locationText.getText().trim();
		if(location.isEmpty())
		{
			setErrorMessage("Please select a valid location to download.");
		}
		else
		{
			try
			{
				File file = Paths.get(location).toFile();
				if(file != null)
				{
					String downloadEmailName = file.getName();
					File parentFolder = file.getParentFile();
					if(parentFolder != null && parentFolder.exists())
					{
						if(downloadEmailName == null || downloadEmailName.isEmpty())
						{
							setErrorMessage("The email name cannot be empty. "
									+ "Please choose a valid name for the download email.");
						}
						else if(existingNames.contains(downloadEmailName) && !overwrite.getSelection())
						{
							setErrorMessage("The email name already exists. "
									+ "You need to check overwrite to overwrite "
									+ "the existing email in the selected folder.");
						}
					}
					else
					{
						setErrorMessage("The selected email parent directory does not exist."
								+ " Please select a valid directory to download.");
					}
				}
				else
				{
					setErrorMessage("Please select a valid location to download.");
				}
			} catch (InvalidPathException ex)
			{
				setErrorMessage("Select a valid location");
			}
		}
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
		try
		{
			File downloadFolder = null;
			String downloadName = null;
			try
			{
				File file = Paths.get(location).toFile();
				downloadFolder = file.getParentFile();
				downloadName = file.getName();
			} catch (Exception ex)
			{
				setErrorMessage("Select a valid folder location.");
			}
			if(downloadFolder != null && downloadFolder.isDirectory())
			{
				File archiveEmail = new File(((ArchiveEntryEditor) emailArchivePage.getEditor())
						.getArchiveFolder(), selectedArchivedEmail.getDisplayTitle());
				FileInputStream fis = new FileInputStream(archiveEmail);
				File downloadEmail = new File(downloadFolder, downloadName);
				FileOutputStream emailOutputStream = new FileOutputStream(downloadEmail);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = fis.read(buffer)) > 0)
				{
					emailOutputStream.write(buffer, 0, len);
				}
				emailOutputStream.close();
				fis.close();
				super.okPressed();
			}
			else
			{
				setErrorMessage("Select a valid folder location.");
			}
		} catch (FileNotFoundException e)
		{
			logger.error("Error downloading " 
					+ selectedArchivedEmail.getDisplayTitle() + "\n" + e.getMessage(), e);
			MessageDialog.openWarning(getShell(), "Download Problem", 
					"The selected email could not be downloaded from the archive \"" 
							+ selectedArchivedEmail.getDisplayTitle()
							+ "\".\n" + e.getMessage());
			super.cancelPressed();

		} catch (Exception e)
		{
			logger.error("Error downloading " 
					+ selectedArchivedEmail.getDisplayTitle() + "\n" + e.getMessage(), e);
			MessageDialog.openWarning(getShell(), "Download Problem", 
					"The selected email could not be downloaded.\n\"" 
							+ selectedArchivedEmail.getDisplayTitle() + "\"");
			super.cancelPressed();
		}
	}
}
