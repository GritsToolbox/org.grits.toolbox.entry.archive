package org.grits.toolbox.entry.archive.dialog.newentry;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.grits.toolbox.core.datamodel.Entry;

public class EntryLabelProvider extends LabelProvider{
	
	public String getText(Object obj) 
	{
		if(obj instanceof Entry)
		{
			return ((Entry)obj).getDisplayName();
		}
		return null;
	}
	public Image getImage(Object obj) 
	{
		if(obj instanceof Entry)
		{
			Entry entry = (Entry)obj;
			return entry.getProperty().getImage().createImage();
		}
		return null;
	}
}
