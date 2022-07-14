/**
 * 
 */
package org.grits.toolbox.entry.archive.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 *
 */
@XmlRootElement(name = "archive")
public class Archive 
{
	@XmlAttribute(name = "version", required= true)
	public static final String CURRENT_VERSION = "1.0";
	private List<ArchivedFile> archivedFiles = new ArrayList<ArchivedFile>();
	private List<ArchivedEmail> archivedEmails = new ArrayList<ArchivedEmail>();

	@XmlElement(name = "archivedFile", required= true)
	public List<ArchivedFile> getArchivedFiles()
	{
		return archivedFiles;
	}

	public void setArchivedFiles(List<ArchivedFile> archivedFiles)
	{
		this.archivedFiles = archivedFiles;
	}

	/**
	 * @return the archivedEmails
	 */
	@XmlElements({
        @XmlElement(name="archivedBasicEmail", type=ArchivedBasicEmail.class),
        @XmlElement(name="archivedEmailTopic", type=ArchivedEmailTopic.class)})
	public List<ArchivedEmail> getArchivedEmails()
	{
		return archivedEmails;
	}

	/**
	 * @param archivedEmails the archivedEmails to set
	 */
	public void setArchivedEmails(List<ArchivedEmail> archivedEmails)
	{
		this.archivedEmails = archivedEmails;
	}

}
