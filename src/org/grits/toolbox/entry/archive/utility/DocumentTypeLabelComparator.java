package org.grits.toolbox.entry.archive.utility;

import java.util.Comparator;

import org.grits.toolbox.entry.archive.model.DocumentType;

/**
 * 
 * 
 *
 */
public class DocumentTypeLabelComparator implements
		Comparator<DocumentType>
{
	@Override
	public int compare(DocumentType d1, DocumentType d2)
	{
		if(d1 == null || d1.getLabel() == null)
		{
			return d2 == null || d2.getLabel() == null ? 0 : -1;
		}
		else if(d2 == null || d2.getLabel() == null)
		{
			return 1;
		}
		else
		{
			return d1.getLabel().compareToIgnoreCase(d2.getLabel());
		}
	}
}
