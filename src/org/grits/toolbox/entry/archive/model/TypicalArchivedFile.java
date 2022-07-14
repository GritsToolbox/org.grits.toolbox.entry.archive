/**
 * 
 */
package org.grits.toolbox.entry.archive.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * 
 *
 */
@XmlRootElement(name = "typicalArchivedFile")
@XmlType(propOrder={"fileName", "fileType", "documentType"})
public class TypicalArchivedFile
{
	private String fileName = null;
	private String fileType = null;
	private DocumentType documentType = null;

	/**
	 * @return the fileName
	 */
	@XmlAttribute(name = "fileName", required= true)
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the fileType
	 */
	@XmlAttribute(name = "fileType", required= false)
	public String getFileType() {
		return fileType;
	}

	/**
	 * @param fileType the fileType to set
	 */
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	/**
	 * @return the documentType
	 */
	@XmlElement(name = "documentType", required= true)
	public DocumentType getDocumentType()
	{
		return documentType;
	}

	/**
	 * @param documentType the documentType to set
	 */
	public void setDocumentType(DocumentType documentType)
	{
		this.documentType = documentType;
	}
}
