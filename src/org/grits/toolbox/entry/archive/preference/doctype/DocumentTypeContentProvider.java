/**
 * 
 */
package org.grits.toolbox.entry.archive.preference.doctype;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.entry.archive.model.DocumentType;

/**
 * 
 *
 */
public class DocumentTypeContentProvider implements ITreeContentProvider
{

	@Override
	public void dispose()
	{
		
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
	{
		
	}

	@Override
	public Object[] getElements(Object inputElement)
	{
		Object[] elements = null;
		if(inputElement instanceof List<?>)
		{
			elements = new Object[((List<?>) inputElement).size()];
			int  i = 0;
			for(Object child : (List<?>) inputElement)
			{
				elements[i++] = child;
			}
		}
		return elements;
	}

	@Override
	public Object[] getChildren(Object parentElement)
	{
		if(parentElement instanceof DocumentType)
		{
			return getElements(new ArrayList<String>(
					((DocumentType) parentElement).getSubTypes()));
		}
		return null;
	}

	@Override
	public Object getParent(Object element)
	{
		return null;
	}

	@Override
	public boolean hasChildren(Object element)
	{
		if(element instanceof DocumentType)
		{
			return ((DocumentType) element).getSubTypes().size() > 0;
		}
		return false;
	}
}
