/**
 * 
 */
package org.grits.toolbox.entry.archive.dialog.uploadfile;

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
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.FileArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedFile;

/**
 * 
 *
 */
public class DownloadFileDialog extends TitleAreaDialog
{
	private static Logger logger = Logger.getLogger(DownloadFileDialog.class);

	private FileArchivePage fileArchivePage = null;

	private ArchivedFile selectedArchivedFile = null;
	private Set<String> existingNames = null;

	private Text locationText = null;
	private Button overwrite = null;

	public DownloadFileDialog(FileArchivePage fileArchivePage, ArchivedFile inputArchivedFile)
	{
		super(Display.getCurrent().getActiveShell());
		this.fileArchivePage  = fileArchivePage;
		this.selectedArchivedFile  = inputArchivedFile;
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
		//		label.setText("Name");
		//		downloadName= new Text(container, SWT.BORDER);
		//		GridData downloadNameData = new GridData();
		//		downloadNameData.horizontalSpan = 3;
		//		downloadNameData.horizontalAlignment = SWT.FILL;
		//		downloadNameData.grabExcessHorizontalSpace = true;
		//		downloadName.setLayoutData(downloadNameData);
		//		downloadName.setText(inputArchivedFile.getTypicalArchivedFile().getFileName());
		//		downloadName.addModifyListener(new ModifyListener()
		//		{
		//			
		//			@Override
		//			public void modifyText(ModifyEvent e)
		//			{
		//				verifyInput();
		//			}
		//		});

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

		Label selectFilesLabel = new Label(container, SWT.READ_ONLY);
		GridData fileLabelData = new GridData(SWT.LEFT);
		fileLabelData.horizontalSpan = 1;
		selectFilesLabel.setLayoutData(fileLabelData);
		selectFilesLabel.setText(selectedArchivedFile.getTypicalArchivedFile().getFileName());
		selectFilesLabel.setFont(boldFont);

		browseFolderButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				setErrorMessage(null);
				String selected = null;
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
				fileDialog.setFileName(
						selectedArchivedFile.getTypicalArchivedFile().getFileName());
				fileDialog.setOverwrite(true);
				fileDialog.setText("Save Location");
				selected = fileDialog.open();
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
					String downloadFileName = file.getName();
					File parentFolder = file.getParentFile();
					if(parentFolder != null && parentFolder.exists())
					{
						if(downloadFileName == null || downloadFileName.isEmpty())
						{
							setErrorMessage("The file name cannot be empty. "
									+ "Please choose a valid name for the download file.");
						}
						else if(existingNames.contains(downloadFileName) && !overwrite.getSelection())
						{
							setErrorMessage("The file name already exists. "
									+ "You need to check overwrite to overwrite "
									+ "the existing file in the selected folder.");
						}
					}
					else
					{
						setErrorMessage("The selected file parent directory does not exist."
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
				File archiveFile = new File(((ArchiveEntryEditor) fileArchivePage.getEditor())
						.getArchiveFolder(), selectedArchivedFile.getTypicalArchivedFile().getFileName());
				FileInputStream fis = new FileInputStream(archiveFile);
				File downloadFile = new File(downloadFolder, downloadName);
				FileOutputStream fileOutputStream = new FileOutputStream(downloadFile);
				byte[] buffer = new byte[1024];
				int len;
				while ((len = fis.read(buffer)) > 0)
				{
					fileOutputStream.write(buffer, 0, len);
				}
				fileOutputStream.close();
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
					+ selectedArchivedFile.getTypicalArchivedFile().getFileName() + "\n" + e.getMessage(), e);
			MessageDialog.openWarning(getShell(), "Download Problem", 
					"The selected file could not be downloaded from the archive \"" 
							+ selectedArchivedFile.getTypicalArchivedFile().getFileName()
							+ "\".\n" + e.getMessage());
			super.cancelPressed();

		} catch (Exception e)
		{
			logger.error("Error downloading " 
					+ selectedArchivedFile.getTypicalArchivedFile().getFileName() + "\n" + e.getMessage(), e);
			MessageDialog.openWarning(getShell(), "Download Problem", 
					"The selected file could not be downloaded.\n\"" 
							+ selectedArchivedFile.getTypicalArchivedFile().getFileName() + "\"");
			super.cancelPressed();
		}
	}
}
