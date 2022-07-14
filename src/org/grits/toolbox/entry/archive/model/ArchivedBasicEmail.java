/**
 * 
 */
package org.grits.toolbox.entry.archive.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlRootElement(name = "archivedBasicEmail")
@XmlType(propOrder={"fromPerson", "modifiedDate",
		"emailContent","notes"})
public class ArchivedBasicEmail extends ArchivedEmail
{
	private String fromPerson = null;
	private String emailContent = null;
	private String notes = null;
	private Date modifiedDate = null;
	private ArchivedEmailTopic archivedEmailTopic = null;

	/**
	 * @return the fromPerson
	 */
	@XmlAttribute(name = "fromPerson", required= true)
	public String getFromPerson()
	{
		return fromPerson;
	}
	/**
	 * @param fromPerson the fromPerson to set
	 */
	public void setFromPerson(String fromPerson)
	{
		this.fromPerson = fromPerson;
	}
	/**
	 * @return the emailContent
	 */
	@XmlElement(name = "emailContent", required= true)
	public String getEmailContent()
	{
		return emailContent;
	}
	/**
	 * @param emailContent the emailContent to set
	 */
	public void setEmailContent(String emailContent)
	{
		this.emailContent = emailContent;
	}
	/**
	 * @return the notes
	 */
	@XmlElement(name = "notes", required= false)
	public String getNotes()
	{
		return notes;
	}
	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes)
	{
		this.notes = notes;
	}
	/**
	 * @return the modifiedDate
	 */
	@XmlAttribute(name = "modifiedDate", required= true)
	public Date getModifiedDate()
	{
		return modifiedDate;
	}
	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate)
	{
		this.modifiedDate = modifiedDate;
	}

	/**
	 * @return the archivedEmailTopic
	 */
	@XmlTransient
	public ArchivedEmailTopic getArchivedEmailTopic()
	{
		return archivedEmailTopic;
	}

	/**
	 * @param archivedEmailTopic the archivedEmailTopic to set
	 */
	public void setArchivedEmailTopic(ArchivedEmailTopic archivedEmailTopic)
	{
		this.archivedEmailTopic = archivedEmailTopic;
	}

	@XmlTransient
	public String getDisplayTitle()
	{
		String rowTitle = fromPerson == null ? "" : fromPerson;
		if(archivedEmailTopic == null)
		{
			rowTitle += emailSubject == null ? "" : " ( " + emailSubject + " )";
		}
		return rowTitle;
	}

	@XmlTransient
	public ArchivedBasicEmail getACopy()
	{
		ArchivedBasicEmail copiedArchivedEmail = new ArchivedBasicEmail();
		super.getACopy(copiedArchivedEmail);
		copiedArchivedEmail.setFromPerson(fromPerson);
		copiedArchivedEmail.setEmailContent(emailContent);
		Date date = modifiedDate == null ? null : new Date(modifiedDate.getTime());
		copiedArchivedEmail.setModifiedDate(date);
		copiedArchivedEmail.setNotes(notes);
		copiedArchivedEmail.setArchivedEmailTopic(archivedEmailTopic);
		return copiedArchivedEmail;
	}
}
