/**
 * 
 */
package org.grits.toolbox.entry.archive.model;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */

@XmlRootElement(name = "archivedEmailTopic")
@XmlType(propOrder={"replyEmails"})
public class ArchivedEmailTopic extends ArchivedEmail
{
	private ArrayList<ArchivedBasicEmail> replyEmails = new ArrayList<ArchivedBasicEmail>();
	/**
	 * @return the replyEmails
	 */
	@XmlElement(name = "replyEmails", required= true)
	public ArrayList<ArchivedBasicEmail> getReplyEmails()
	{
		return replyEmails;
	}
	/**
	 * @param replyEmails the replyEmails to set
	 */
	public void setReplyEmails(ArrayList<ArchivedBasicEmail> replyEmails)
	{
		this.replyEmails = replyEmails;
	}

	/**
	 * @return the replyEmails
	 */
	public boolean addReplyEmail(ArchivedBasicEmail replyEmail)
	{
		return replyEmails.add(replyEmail);
	}

	public ArchivedBasicEmail getlastReplyEmail()
	{
		ArchivedBasicEmail lastReplyEmail = null;
		for(ArchivedBasicEmail replyEmail : replyEmails)
		{
			lastReplyEmail = replyEmail;
		}
		return lastReplyEmail;
	}

	public String getDisplayTitle()
	{
		ArchivedBasicEmail lastReplyEmail = getlastReplyEmail();
		return lastReplyEmail == null ? emailSubject : lastReplyEmail.getEmailSubject();
	}

	public ArchivedEmailTopic getACopy()
	{
		ArchivedEmailTopic copiedEmailTopic = new ArchivedEmailTopic();
		super.getACopy(copiedEmailTopic);
		for(ArchivedBasicEmail email : replyEmails)
		{
			copiedEmailTopic.addReplyEmail(email.getACopy());
		}
		return copiedEmailTopic;
	}
}
