package org.grits.toolbox.entry.archive.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

@XmlSeeAlso({ArchivedEmailTopic.class, ArchivedBasicEmail.class})
@XmlType(propOrder= {"TO_PERSON", "emailDate", "emailSubject"})
public abstract class ArchivedEmail
{
	public static final DateFormat DATE_FORMAT0 = new SimpleDateFormat();
	public static final DateFormat DATE_FORMAT1 = new SimpleDateFormat("MM/dd/yyyy");
	@XmlAttribute(name = "toPerson", required= true)
	public static String TO_PERSON = "";

	protected Date emailDate = null;
	protected String emailSubject = null;

	/**
	 * @return the emailDate
	 */
	@XmlAttribute(name = "emailDate", required= true)
	public Date getEmailDate()
	{
		return emailDate;
	}

	/**
	 * @param emailDate the emailDate to set
	 */
	public void setEmailDate(Date emailDate)
	{
		this.emailDate = emailDate;
	}

	/**
	 * @return the emailSubject
	 */
	@XmlElement(name = "emailSubject", required= true)
	public String getEmailSubject()
	{
		return emailSubject;
	}

	/**
	 * @param emailSubject the emailSubject to set
	 */
	public void setEmailSubject(String emailSubject)
	{
		this.emailSubject = emailSubject;
	}

	public String getDisplayTitle()
	{
		return emailSubject == null ? "" : emailSubject;
	}

	protected ArchivedEmail getACopy(ArchivedEmail archivedEmail)
	{
		Date date = emailDate == null ? null : new Date(emailDate.getTime());
		archivedEmail.setEmailDate(date);
		archivedEmail.setEmailSubject(emailSubject);
		return archivedEmail;
	}
}
