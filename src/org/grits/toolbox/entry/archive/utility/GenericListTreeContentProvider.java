/**
 * 
 */
package org.grits.toolbox.entry.archive.utility;

import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.entry.archive.model.ArchivedBasicEmail;
import org.grits.toolbox.entry.archive.model.ArchivedEmailTopic;

/**
 * 
 *
 */
public class GenericListTreeContentProvider implements ITreeContentProvider
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
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement)
	{
		List<?> inputList = null;
		if(parentElement instanceof List)
		{
			inputList = ((List<?>) parentElement);
		}
		else if(parentElement instanceof ArchivedEmailTopic)
		{
			inputList = ((ArchivedEmailTopic) parentElement).getReplyEmails();
			if(inputList != null)
			{
				Object[] array = new Object[inputList.size()];
				int i = inputList.size() - 1;
				for(Object input : inputList)
				{
					array[i--] = input;
					((ArchivedBasicEmail) input).setArchivedEmailTopic((ArchivedEmailTopic) parentElement);
				}
				return array;
			}
		}
		if(inputList != null)
		{
			Object[] array = new Object[inputList.size()];
			int i = 0;
			for(Object input : inputList)
			{
				array[i++] = input;
			}
			return array;
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
		return element instanceof ArchivedEmailTopic;
	}

}
