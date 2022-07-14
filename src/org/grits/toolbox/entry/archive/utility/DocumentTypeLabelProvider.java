package org.grits.toolbox.entry.archive.utility;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.entry.archive.model.DocumentType;

public class DocumentTypeLabelProvider implements ILabelProvider
{

	@Override
	public void addListener(ILabelProviderListener listener)
	{
		
	}

	@Override
	public void dispose()
	{
		
	}

	@Override
	public boolean isLabelProperty(Object element, String property)
	{
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener)
	{
		
	}

	@Override
	public Image getImage(Object element)
	{
		return null;
	}

	@Override
	public String getText(Object element)
	{
		if(element instanceof DocumentType)
		{
			return ((DocumentType) element).getLabel();
		}
		return null;
	}
}
