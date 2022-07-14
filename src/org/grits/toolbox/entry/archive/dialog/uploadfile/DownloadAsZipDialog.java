/**
 * 
 */
package org.grits.toolbox.entry.archive.dialog.uploadfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.grits.toolbox.core.utilShare.CheckboxTableViewer;
import org.grits.toolbox.core.utilShare.provider.GenericListContentProvider;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.editor.pages.archivedfile.FileArchivePage;
import org.grits.toolbox.entry.archive.model.ArchivedFile;
import org.grits.toolbox.entry.archive.utility.UtilityZip;

/**
 * 
 *
 */
public class DownloadAsZipDialog extends TitleAreaDialog
{
	private static Logger logger = Logger.getLogger(DownloadAsZipDialog.class);

	private static final long MAX_DOWNLOAD_FILE_SIZE = 2L*1024L*1024L*1024L;

	private FileArchivePage fileArchivePage = null;
	protected Set<String> existingNames = null;

	private Text locationText = null;
	private Button overwrite = null;
	private CheckboxTableViewer checkboxTableViewer = null;

	public DownloadAsZipDialog(FileArchivePage fileArchivePage)
	{
		super(Display.getCurrent().getActiveShell());
		this.fileArchivePage = fileArchivePage;
	}

	@Override
	public void create()
	{
		super.create();
		setTitle("Download File");
		setMessage("Download File From the Archive");
		getShell().setText("Download");
		getButton(OK).setText("Download");
	}

	@SuppressWarnings("unchecked")
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

		locationText =  new Text(container, SWT.BORDER|SWT.BORDER_SOLID);
		GridData textData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		textData.horizontalSpan = 3;
		textData.minimumWidth = 450;
		locationText.setLayoutData(textData);

		Button browseFileButton = new Button(container, SWT.PUSH);
		browseFileButton.setText(" Browse Location ");
		GridData browseButtonGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
		browseButtonGridData.horizontalSpan = 1;
		browseFileButton.setLayoutData(browseButtonGridData);

		Label label = new Label(container, SWT.READ_ONLY);
		GridData labelData = new GridData(SWT.LEFT);
		labelData.horizontalSpan = 3;
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

		Label selectFilesLabel = new Label(container, SWT.NONE);
		selectFilesLabel.setText("Select Files");
		GridData fileLabelData = new GridData(SWT.LEFT);
		fileLabelData.horizontalSpan = 4;
		selectFilesLabel.setLayoutData(fileLabelData);
		Table table = new Table(container, 
				SWT.FILL | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | 
				SWT.FULL_SELECTION);
		checkboxTableViewer = new CheckboxTableViewer(table, "Select", false);

		TableViewerColumn tableColumn2 = new TableViewerColumn(checkboxTableViewer, SWT.FILL, 1);
		tableColumn2.getColumn().setText("Files");
		tableColumn2.getColumn().setWidth(400);
		GridData tableData = new GridData();
		tableData.horizontalSpan = 4;
		tableData.horizontalAlignment = SWT.FILL;
		tableData.heightHint = 200;
		checkboxTableViewer.getTable().setLayoutData(tableData);

		checkboxTableViewer.getTable().setHeaderVisible(true);
		checkboxTableViewer.getTable().setLinesVisible(true);
		checkboxTableViewer.setContentProvider(new GenericListContentProvider());
		checkboxTableViewer.setLabelProvider(new ITableLabelProvider()
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
				if(element instanceof ArchivedFile && columnIndex == 1)
				{
					return ((ArchivedFile) element).getTypicalArchivedFile().getFileName();
				}
				return null;
			}

			@Override
			public Image getColumnImage(Object element, int columnIndex)
			{
				return null;
			}
		});

		checkboxTableViewer.setInput((List<ArchivedFile>) fileArchivePage.getLeftSideTreeViewer().getInput());
		checkboxTableViewer.refresh();

		browseFileButton.addSelectionListener(new SelectionListener()
		{
			@Override
			public void widgetSelected(SelectionEvent e) 
			{
				setErrorMessage(null);
				String selected = null;
				FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
				fileDialog.setOverwrite(true);
				fileDialog.setText("Save Location");
				fileDialog.setFilterExtensions(new String[]{".zip"});
				fileDialog.setFileName("");
				selected = fileDialog.open();
				try
				{
					if (selected != null)
					{
						locationText.setText(selected);
					}
				} catch (InvalidPathException ex)
				{
					setErrorMessage("Select a valid location");
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
				try
				{
					File sourceFile = Paths.get(locationText.getText().trim()).toFile();
					if(sourceFile != null && sourceFile.getParentFile() != null)
					{
						existingNames = new HashSet<String>(Arrays.asList(sourceFile.getParentFile().list()));
						verifyInput();
					}
					else
					{
						setErrorMessage("Please select a valid location to download.");
					}
				} catch (Exception ex)
				{
					logger.error(ex.getMessage(), ex);
					setErrorMessage("Please select a valid location to download.");
				}
			}
		});
		browseFileButton.setFocus();
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
				File file = new File(location);
				if(!file.isDirectory() && file.getParentFile() != null)
				{
					File parentDirectory = file.getParentFile();
					if(parentDirectory == null || !parentDirectory.exists())
					{
						setErrorMessage("The selected directory does not exist."
								+ " Please select a valid directory to download.");
					}
					String downloadFileName = file.getName();
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
		if(!locationText.getText().isEmpty())
		{
			try
			{
				if(!locationText.getText().endsWith(".zip"))
				{
					locationText.setText(locationText.getText() + ".zip");
				}
				Paths.get(locationText.getText()).toFile();
				ArchivedFile archivedFile = null;
				File downloadFile = null;
				FileOutputStream fos;
				try
				{
					fos = new FileOutputStream(new File(locationText.getText()));
					ZipOutputStream  zipOutputStream = new ZipOutputStream(fos);
					String notDownloadedMessage = "";
					for(Object checkedElement : checkboxTableViewer.getCheckedElements())
					{
						if(checkedElement instanceof ArchivedFile)
						{
							try
							{
								archivedFile = (ArchivedFile) checkedElement;
								downloadFile = new File(((ArchiveEntryEditor) fileArchivePage.getEditor())
										.getArchiveFolder(), archivedFile.getTypicalArchivedFile().getFileName());
								if(downloadFile.isDirectory())
								{
									UtilityZip.addDirectory(downloadFile, downloadFile.getName(), zipOutputStream, null);
								}
								else if(downloadFile.isFile())
								{
									if(downloadFile.length() < MAX_DOWNLOAD_FILE_SIZE)
									{
										UtilityZip.addFile(downloadFile, downloadFile.getName(), zipOutputStream, null);
									}
									else
									{
										notDownloadedMessage += notDownloadedMessage.length() > 200 
												? "" : downloadFile.getName() + "'s size (" 
												+ (downloadFile.length()/(1024.0f*1024.0f*1024.0f))+ "GB) is greater than " 
												+ MAX_DOWNLOAD_FILE_SIZE + " GB.\n";
									}
								}
								else
									logger.debug(downloadFile.getName()
											+ " file type not known and could not be zipped.");
							} catch (Exception e)
							{
								logger.error("Error downloading " + downloadFile.getName());
							}
						}
					}
					zipOutputStream.close();
					fos.close();
					if(!notDownloadedMessage.isEmpty())
					{
						if(notDownloadedMessage.length() > 200)
						{
							notDownloadedMessage += ".....";
						}
						MessageDialog.openWarning(getShell(), "Download Problems", 
								"These folders could not be downloaded. Try downloading them separately :\n\n" + notDownloadedMessage);
					}
					super.okPressed();
				} catch (IOException e)
				{
					logger.error(e.getMessage(), e);
				}
			} catch (InvalidPathException ex)
			{
				logger.error(ex.getMessage(), ex);
				setErrorMessage("Select a valid location");
			}
		}
		else
		{
			setErrorMessage("Select a valid location");
		}
	}
}
