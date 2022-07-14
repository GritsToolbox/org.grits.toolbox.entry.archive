package org.grits.toolbox.entry.archive.preference.doctype;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.grits.toolbox.entry.archive.model.DocumentType;

public class DocumentTypeComparator extends ViewerComparator
{
	@Override
	public int compare(Viewer viewer, Object o1, Object o2)
	{
		int comparision = 0;
		String string1 = o1.toString();
		String string2 = o2.toString();
		if(o1 instanceof DocumentType)
		{
			string1 = ((DocumentType) o1).getLabel();
		}
		if(o2 instanceof DocumentType)
		{
			string2 = ((DocumentType) o2).getLabel();
		}
		comparision = string1.compareToIgnoreCase(string2);
		return comparision;
	}
}
