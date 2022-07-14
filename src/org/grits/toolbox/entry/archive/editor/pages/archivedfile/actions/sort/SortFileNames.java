/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.sort;

import org.apache.log4j.Logger;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.grits.toolbox.core.img.ImageShare;

/**
 * 
 *
 */
public class SortFileNames extends Action
{
  private Logger logger = Logger.getLogger(SortFileNames.class);
    protected TreeViewer treeViewer = null;

    public SortFileNames(TreeViewer treeViewer)
    {
        super();
        super.setToolTipText("Sort in Alphanumeric Ascending order");
        this.treeViewer  = treeViewer;
        treeViewer.getTree().setSortColumn(treeViewer.getTree().getColumn(0));
        boolean ascending = treeViewer.getTree().getSortDirection() == SWT.UP;
        setAscendingDescendingIcon(ascending);
        int direction = ascending ? SWT.UP : SWT.DOWN;
        treeViewer.getTree().setSortDirection(direction);
    }
    
    public void setAscendingDescendingIcon(boolean ascending)
    {
        if(ascending)
        {
            super.setImageDescriptor(ImageShare.SORT_ICON_ASCEND);
        }
        else
        {
            super.setImageDescriptor(ImageShare.SORT_ICON_DESCEND);
        }
    }

    public void run()
    {
		logger.debug("Start - Sorting left side table of Archive Entry Editor");
        ArchivedFileViewerComparator archivedFileViewerComparator = 
        		(ArchivedFileViewerComparator) treeViewer.getComparator();
        boolean ascending = treeViewer.getTree().getSortDirection() == SWT.UP;
        int newDirection = ascending ? SWT.DOWN : SWT.UP;
        treeViewer.getTree().setSortDirection(newDirection);
    	treeViewer.getTree().setSortColumn(treeViewer.getTree().getColumn(0));
        setAscendingDescendingIcon(ascending);
        archivedFileViewerComparator.setColumn(0);
        archivedFileViewerComparator.setAscending(!ascending);
        treeViewer.refresh();
        logger.debug("End - Sorting left side table of Archive Entry Editor");
    }
}
