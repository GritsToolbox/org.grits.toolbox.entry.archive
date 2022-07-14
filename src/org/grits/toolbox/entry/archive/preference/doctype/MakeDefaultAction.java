/**
 * 
 */
package org.grits.toolbox.entry.archive.preference.doctype;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.TreeItem;
import org.grits.toolbox.entry.archive.model.DocumentType;

/**
 * 
 *
 */
public class MakeDefaultAction extends Action
{
	private TreeViewer treeViewer = null;

	public MakeDefaultAction(TreeViewer treeViewer)
	{
		this.treeViewer  = treeViewer;
	}

	@Override
	public void run()
	{
		TreeSelection treeSelection = (TreeSelection) treeViewer.getSelection();
		if(!treeSelection.isEmpty())
		{
			// default document type
			if(treeSelection.getFirstElement() instanceof DocumentType)
			{
				DocumentType selectedDocumentType = 
						(DocumentType) treeSelection.getFirstElement();
				DocumentType documentType = null;
				for(TreeItem item : treeViewer.getTree().getItems())
				{
					documentType = (DocumentType) item.getData();
					// make document type default if it is this selection
					documentType.setMakeDefault(
							documentType == selectedDocumentType);
				}
			}
			// default document subtype
			else if(treeSelection.getFirstElement() instanceof String)
			{
				TreePath[] paths = treeSelection.getPaths();
				// make this selected subtype default for document type
				((DocumentType) paths[0].getSegment(0)).setSelectedSubType(
						(String) treeSelection.getFirstElement());
			}
			treeViewer.refresh();
		}
	}
}
