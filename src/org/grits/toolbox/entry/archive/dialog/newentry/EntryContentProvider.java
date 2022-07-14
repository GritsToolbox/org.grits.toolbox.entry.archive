package org.grits.toolbox.entry.archive.dialog.newentry;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.grits.toolbox.core.datamodel.Entry;

public class EntryContentProvider implements ITreeContentProvider {

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent)
	{
		return this.getChildren(parent);
	}

	public Object getParent(Object child)
	{
		if (child instanceof Entry) {
			return ((Entry)child).getParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent)
	{
		if (parent instanceof Entry) {
			Entry[] children = new Entry[((Entry)parent).getChildren().size()];
			int i = 0;
			for(Entry ent : ((Entry)parent).getChildren())
			{
				children[i] = ent;
				i++;
			}
			return children;
		}
		return new Entry[0];
	}

	public boolean hasChildren(Object parent) 
	{
		if (parent instanceof Entry)
			return ((Entry)parent).hasChildren();
		return false;
	}

}
