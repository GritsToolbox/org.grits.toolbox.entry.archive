/**
 * 
 */
package org.grits.toolbox.entry.archive.model;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlRootElement(name = "documentType")
@XmlType(propOrder={"label", "makeDefault", "selectedSubType", "subTypes"})
public class DocumentType
{
	private String label = null;
	private boolean makeDefault = false;
	private String selectedSubType = null;
	private Set<String> subTypes = new HashSet<String>();

	private boolean initialized = false;

	/**
	 * @return the label
	 */
	@XmlAttribute(name = "label", required= true)
	public String getLabel()
	{
		return label;
	}

	/**
	 * @param label the label to set
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}

	/**
	 * @return the makeDefault
	 */
	@XmlAttribute(name = "makeDefault", required= true)
	public boolean isMakeDefault()
	{
		return makeDefault;
	}

	/**
	 * @param makeDefault the makeDefault to set
	 */
	public void setMakeDefault(boolean defaultSelection)
	{
		this.makeDefault = defaultSelection;
	}

	/**
	 * @return the selectedSubType
	 */
	@XmlAttribute(name = "selectedSubType", required= false)
	public String getSelectedSubType()
	{
		return selectedSubType;
	}

	/**
	 * @param selectedSubType the selectedSubType to set
	 */
	public void setSelectedSubType(String selectedSubType)
	{
		// select the string object whose value is same as the passed value
		for(String subType : subTypes)
		{
			if(subType.equals(selectedSubType))
			{
				this.selectedSubType = subType;
				break;
			}
		}

		// in case the selected subType string was not present in the list
		// save the value with the given value, ideally is an exception
		if(!Objects.equals(this.selectedSubType, selectedSubType))
			this.selectedSubType = selectedSubType;
	}

	/**
	 * @return the subTypes
	 */
	@XmlElement(name = "subTypes", required= false)
	public Set<String> getSubTypes()
	{
		// for jaxb initialization
		if(!initialized && !subTypes.isEmpty())
		{
			createNewStringObjects();
			initialized = true;
		}
		return subTypes;
	}

	private void createNewStringObjects()
	{
		Set<String> localSubTypes = new HashSet<String>();
		String newString = null;
		for(String ele : subTypes)
		{
			newString = new String(ele);
			localSubTypes.add(newString);
			if(selectedSubType != null && selectedSubType.equals(ele))
			{
				this.selectedSubType = newString;
			}
		}
		subTypes = localSubTypes;
	}

	/**
	 * @param subTypes the subTypes to set
	 */
	public void setSubTypes(Set<String> subTypes)
	{
		this.subTypes = subTypes;
		createNewStringObjects();
	}

	/**
	 * 
	 * @param subtype
	 * @return
	 */
	public boolean addSubType(String subtype)
	{
		return subTypes.add(new String(subtype));
	}

	public DocumentType clone()
	{
		DocumentType docType = new DocumentType();
		docType.setLabel(label);
		docType.setMakeDefault(makeDefault);
		docType.setSubTypes(new HashSet<String>());
		String newSubType = null;
		for(String subType : subTypes)
		{
			newSubType = new String(subType);
			docType.getSubTypes().add(newSubType);
			if(selectedSubType != null && selectedSubType.equals(subType))
			{
				docType.setSelectedSubType(newSubType);
			}
		}
		return docType;
	}
}
