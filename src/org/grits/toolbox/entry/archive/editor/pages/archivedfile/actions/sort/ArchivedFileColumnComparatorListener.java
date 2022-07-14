/**
 * 
 */
package org.grits.toolbox.entry.archive.editor.pages.archivedfile.actions.sort;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * 
 *
 */
public class ArchivedFileColumnComparatorListener implements SelectionListener 
{
    private TreeViewer treeViewer = null;
    private ArchivedFileViewerComparator archivedFileViewerComparator;

    public ArchivedFileColumnComparatorListener(TreeViewer treeViewer)
    {
        this.treeViewer = treeViewer;
        this.archivedFileViewerComparator = 
                (ArchivedFileViewerComparator) treeViewer.getComparator();
    }

    @Override
    public void widgetSelected(SelectionEvent e)
    {
        TreeColumn treeColumn = (TreeColumn) e.getSource();
        Tree tree = treeColumn.getParent();
        boolean ascending = tree.getSortDirection() == SWT.UP;
        int newDirection = ascending ? SWT.DOWN : SWT.UP;

        int column = tree.indexOf(treeColumn);
        tree.setSortDirection(newDirection);
        tree.setSortColumn(tree.getColumn(column));
        archivedFileViewerComparator.setColumn(column);
        archivedFileViewerComparator.setAscending(!ascending);
        treeViewer.refresh();
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e)
    {
        
    }
}
