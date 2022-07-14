/**
 * 
 */
package org.grits.toolbox.entry.archive.preference.doctype;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;
import org.grits.toolbox.entry.archive.model.DocumentType;

/**
 * 
 *
 */
public class DocumentTypeFontLabelProvider implements ITableLabelProvider,
ITableFontProvider
{
	public static final String DEFAULT_STRING = " (default)";
	private TreeViewer treeViewer = null;
	private Font boldFont = null;

	public DocumentTypeFontLabelProvider(TreeViewer treeViewer)
	{
		this.treeViewer  = treeViewer;
		// bold font for current font
		Font currentFont = Display.getCurrent().getSystemFont();
		FontData fontData = currentFont.getFontData()[0];
		boldFont = new Font(Display.getCurrent(),
				fontData.getName(), fontData.getHeight(), SWT.BOLD);
	}

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
	public Font getFont(Object element, int columnIndex)
	{
		if(element instanceof DocumentType && columnIndex == 0)
		{
			return ((DocumentType) element).isMakeDefault() ? boldFont : null;
		}
		if(element instanceof String && columnIndex == 1)
		{
			Font font = null;
			for(TreeItem item : treeViewer.getTree().getItems())
			{
				if(item.getData() instanceof DocumentType)
				{
					if(((DocumentType) item.getData()).getSelectedSubType() == element)
					{
						font = boldFont;
						break;
					}
				}
			}
			return font;
		}
		return null;
	}

	@Override
	public Image getColumnImage(Object element, int columnIndex)
	{
		return null;
	}

	@Override
	public String getColumnText(Object element, int columnIndex)
	{
		if(element instanceof DocumentType && columnIndex == 0)
		{
			DocumentType documentType = (DocumentType) element;
			return documentType.isMakeDefault() ? 
					documentType.getLabel() + DEFAULT_STRING : documentType.getLabel();
		}
		if(element instanceof String && columnIndex == 1)
		{
			String suffix = "";
			for(TreeItem item : treeViewer.getTree().getItems())
			{
				if(item.getData() instanceof DocumentType)
				{
					if(((DocumentType) item.getData()).getSelectedSubType() == element)
					{
						suffix = DEFAULT_STRING;
						break;
					}
				}
			}
			return element + suffix;
		}
		return null;
	}
}
