/**
 * 
 */
package org.grits.toolbox.entry.archive.utility;

import java.util.Set;

import org.eclipse.jface.dialogs.IInputValidator;

/**
 * 
 *
 */
public class UniqueNameValidator implements IInputValidator
{

	private Set<String> existingNames = null;

	public UniqueNameValidator(Set<String> existingNames)
	{
		this.existingNames  = existingNames;
	}

	@Override
	public String isValid(String newText)
	{
		String errorMessage = null;
		if(newText == null || newText.isEmpty())
		{
			errorMessage = "The file name cannot be empty. "
					+ "Please choose a valid name for the download file.";
		}
		else if(existingNames.contains(newText))
		{
			errorMessage = "This file name already exists. "
					+ "Please choose a unique file name.";
		}
		return errorMessage;
	}

}
