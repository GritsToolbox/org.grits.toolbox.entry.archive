/**
 * 
 */
package org.grits.toolbox.entry.archive.model;


import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * 
 *
 */
@XmlRootElement(name = "archivedFile")
@XmlType(propOrder={"person", "setDate", "modifiedDate", "typicalArchivedFile", "description", "keywords"})
public class ArchivedFile
{
	private String person = null;
	private Date setDate = null;
	private Date modifiedDate = null;
	private String description = null;
	private Set<String> keywords = new HashSet<String>();
	private TypicalArchivedFile typicalArchivedFile = null;

	/**
	 * @return the person
	 */
	@XmlAttribute(name = "person", required= true)
	public String getPerson() {
		return person;
	}

	/**
	 * @param person the person to set
	 */
	public void setPerson(String person) {
		this.person = person;
	}

	/**
	 * @return the setDate
	 */
	@XmlAttribute(name = "setDate", required= true)
	public Date getSetDate() {
		return setDate;
	}

	/**
	 * @param setDate the setDate to set
	 */
	public void setSetDate(Date setDate) {
		this.setDate = setDate;
	}

	/**
	 * @return the modifiedDate
	 */
	@XmlAttribute(name = "modifiedDate", required= true)
	public Date getModifiedDate() {
		return modifiedDate;
	}

	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	/**
	 * @return the description
	 */
	@XmlElement(name = "description", required= true)
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the keywords
	 */
	@XmlJavaTypeAdapter(KeywordsAdapter.class)
	@XmlElement(name = "keywords")
	public Set<String> getKeywords() {
		return keywords;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(Set<String> keywords) {
		this.keywords = keywords;
	}

	/**
	 * @return the typicalArchivedFile
	 */
	@XmlElement(name = "typicalArchivedFile", required= true)
	public TypicalArchivedFile getTypicalArchivedFile() {
		return typicalArchivedFile;
	}

	/**
	 * @param typicalArchivedFile the typicalArchivedFile to set
	 */
	public void setTypicalArchivedFile(TypicalArchivedFile typicalArchivedFile) {
		this.typicalArchivedFile = typicalArchivedFile;
	}
}
