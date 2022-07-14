/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.grits.toolbox.entry.archive.editor.ArchiveEntryEditor;
import org.grits.toolbox.entry.archive.utility.GenericListTreeContentProvider;

/**
 * 
 *
 */
public abstract class AbstractArchivePage {
	protected static final String NO_SELECTION_TITLE = "No Selection";

	public static final String PAGE_TITLE = null;

	private Logger logger = Logger.getLogger(AbstractArchivePage.class);

	protected Section leftSectionOfPage = null;
	private Section rightSectionOfPage = null;
	protected TreeViewer leftSideTreeViewer = null;

	protected int leftSideSelection = 0;

	private Composite rightComposite;

	protected Font boldFont = null;

	private CTabItem cTabItem;

	private ArchiveEntryEditor editor;

	//	private TreeViewerColumn columnViewer0 = null;
	//	private TreeViewerColumn columnViewer1 = null;

	@Inject
	public AbstractArchivePage(ArchiveEntryEditor editor, CTabItem cTabItem)
	{
		this.cTabItem = cTabItem;
		this.editor = editor;
		Font currentFont = Display.getCurrent().getSystemFont();
		FontData fontData= currentFont.getFontData()[0];
		boldFont   = new Font(Display.getCurrent(),fontData.getName(), fontData.getHeight(), SWT.BOLD);
	}

	public ArchiveEntryEditor getEditor() {
		return editor;
	}
	
	/**
	 * @return the leftSectionOfPage
	 */
	public Section getLeftSectionOfPage()
	{
		return leftSectionOfPage;
	}

	public Composite getRightComposite()
	{
		return rightComposite;
	}

	/**
	 * @return the rightSectionOfPage
	 */
	public Section getRightSectionOfPage()
	{
		return rightSectionOfPage;
	}

	public TreeViewer getLeftSideTreeViewer()
	{
		return leftSideTreeViewer;
	}

	@PostConstruct
	protected void createFormContent()
	{
		logger.debug("- START : Creating Abstract page.");
		try
		{
			ScrolledComposite scrolledComposite = new ScrolledComposite(cTabItem.getParent(), 
					SWT.H_SCROLL | SWT.V_SCROLL);
			scrolledComposite.setBackground(Display.getCurrent().getSystemColor(
					SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));
			
		/*	ScrolledForm form = managedForm.getForm();
			form.getForm().getHead().setBackground(Display.getCurrent().getSystemColor(
					SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT));

			managedForm.getForm().setText(this.getTitle());
			toolkit = managedForm.getToolkit();*/
			
			Composite fullComposite = new Composite(scrolledComposite, SWT.FILL);
			fullComposite.setLayout(new GridLayout(2, false));

			Composite leftComposite = new Composite(fullComposite, SWT.NONE);
			TableWrapLayout leftCompositeLayout = new TableWrapLayout();
			leftCompositeLayout.numColumns = 1;
			leftCompositeLayout.makeColumnsEqualWidth = true;
			leftComposite.setLayout(leftCompositeLayout);

			leftSectionOfPage = new Section(leftComposite , Section.TITLE_BAR);
			leftSectionOfPage.setLayout(new TableWrapLayout());

			TableWrapData section1LayoutData = new TableWrapData();
			section1LayoutData.colspan = 1;
			section1LayoutData.grabVertical = true;
			leftSectionOfPage.setLayoutData(section1LayoutData);

			GridData leftCompositeData = new GridData();
			leftCompositeData.grabExcessVerticalSpace = true;
			leftCompositeData.horizontalSpan = 1;
			leftCompositeData.verticalSpan = 1;
			leftCompositeData.horizontalAlignment = GridData.BEGINNING;
			leftCompositeData.verticalAlignment = GridData.BEGINNING;
			leftCompositeData.widthHint = 400;
			leftComposite.setLayoutData(leftCompositeData);

			rightComposite = new Composite(fullComposite, SWT.NONE);
			GridLayout rightCompositeLayout = new GridLayout();
			rightCompositeLayout.numColumns = 1;
			rightCompositeLayout.makeColumnsEqualWidth = true;
			rightComposite.setLayout(rightCompositeLayout);

			rightSectionOfPage  = new Section(rightComposite , Section.TITLE_BAR);
			rightSectionOfPage.setText(NO_SELECTION_TITLE);
			rightSectionOfPage.setLayout(new TableWrapLayout());

			GridData rightSectionLayoutData = new GridData();
			rightSectionLayoutData.horizontalSpan = 1;
			rightSectionOfPage.setLayoutData(rightSectionLayoutData);

			GridData rightCompositeData = new GridData();
			rightCompositeData.grabExcessVerticalSpace = true;

			rightCompositeData.grabExcessHorizontalSpace = true;
			rightCompositeData.horizontalSpan = 1;
			rightCompositeData.verticalSpan = 1;
			rightCompositeData.horizontalAlignment = GridData.FILL_HORIZONTAL;
			rightCompositeData.verticalAlignment = GridData.BEGINNING;
			rightComposite.setLayoutData(rightCompositeData);

			leftSectionOfPage.setText(this.getTitle().trim() + "s");
			leftSideTreeViewer = this.createLeftSideTree();
			leftSideTreeViewer.addSelectionChangedListener(new ISelectionChangedListener()
			{
				@Override
				public void selectionChanged(SelectionChangedEvent event)
				{
					if(!leftSideTreeViewer.getSelection().isEmpty())
					{
						Object selectedObject = ((TreeSelection) 
								leftSideTreeViewer.getSelection()).getFirstElement();

						setSelection(selectedObject);
					}
				}
			});

			createToolBar(leftSideTreeViewer);
			this.createRestOftheContent();
			if(leftSideTreeViewer.getTree().getItemCount() > 0 
				&& leftSideTreeViewer.getTree().getItem(0) != null 
						&& leftSideTreeViewer.getTree().getItem(0).getData() != null)
			{
					leftSideTreeViewer.setSelection(new StructuredSelection(
							leftSideTreeViewer.getTree().getItem(0).getData()));
			}
			leftSectionOfPage.setExpanded(true);   
			rightSectionOfPage.setExpanded(true);   
			
			scrolledComposite.setContent(fullComposite);
			scrolledComposite.setMinSize(fullComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
			scrolledComposite.setExpandVertical(true);
			scrolledComposite.setExpandHorizontal(true);
			cTabItem.setControl(scrolledComposite);
		} catch (Exception ex)
		{
			logger.error(ex.getMessage(), ex);
		}
		logger.debug("- END   : Creating Abstract page.");
	}

	protected abstract String getTitle();

	public abstract void setSelection(Object selectedObject);

	protected TreeViewer createLeftSideTree()
	{
		Composite leftSectionComposite = new Composite(leftSectionOfPage, SWT.NONE);
		leftSectionComposite.setLayout(new GridLayout());
		TreeViewer treeViewer = new TreeViewer(new Tree(leftSectionComposite, SWT.BORDER |
				SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL |  SWT.FULL_SELECTION));
		GridData treeViewerData = new GridData();
		treeViewerData.widthHint = 320;
		treeViewerData.heightHint = 680;
		treeViewerData.grabExcessHorizontalSpace = true;
		treeViewer.getTree().setLayoutData(treeViewerData);
		treeViewer.getTree().setVisible(true);
		treeViewer.getTree().setLinesVisible(true);
		leftSectionComposite.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		leftSectionOfPage.setClient(leftSectionComposite);
		ToolBarManager toolBarManager = new ToolBarManager(SWT.BALLOON);
		ToolBar toolbar = toolBarManager.createControl(leftSectionOfPage);
		leftSectionOfPage.setTextClient(toolbar);
		treeViewer.setContentProvider(new GenericListTreeContentProvider());

		setUpColumns(treeViewer);

		treeViewer.getTree().setHeaderVisible(true);
		treeViewer.setInput(getInput());
		return treeViewer;
	}

	protected abstract void setUpColumns(TreeViewer leftSideTreeViewer);

	protected void selectDefault()
	{
		if(leftSideTreeViewer != null && leftSideTreeViewer.getTree().getItemCount() > 0)
		{
			((TreeSelection) leftSideTreeViewer.getSelection()).getFirstElement();
			leftSideTreeViewer.setSelection(leftSideTreeViewer.getSelection());
		}
	}

	public abstract void createRestOftheContent();

	protected void createToolBar(TreeViewer treeViewer)
	{
		logger.debug("- START : Creating Toolbar for the page.");
				try
				{
					ToolBarManager toolBarManager = new ToolBarManager();
					List<Action> leftSideActions = getLeftSectionActions();
					for(Action action : leftSideActions)
					{
						toolBarManager.add(action);
					}

					ToolBar toolbar = toolBarManager.createControl(leftSectionOfPage);
					leftSectionOfPage.setTextClient(toolbar);
				} catch (Exception ex)
				{
					logger.error(ex.getMessage(), ex);
				}
				logger.debug("- END   : Creating Toolbar for the page.");
	}

	protected abstract List<Action> getLeftSectionActions();

	public abstract void refreshPage();

	protected abstract List<?> getInput();
}
