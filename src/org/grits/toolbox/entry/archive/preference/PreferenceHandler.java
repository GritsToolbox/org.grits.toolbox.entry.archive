package org.grits.toolbox.entry.archive.preference;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.grits.toolbox.entry.archive.Activator;
import org.grits.toolbox.entry.archive.model.DocumentType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class PreferenceHandler
{
	private static final Logger logger = Logger.getLogger(PreferenceHandler.class);

	public static HashMap<String, Boolean> getDocumentPreferenceValues(String fileName)
	{
		HashMap<String, Boolean> preferenceValues = new HashMap<String, Boolean>();
		try
		{
			URL resourceURL = FileLocator.toFileURL(
					Platform.getBundle(Activator.PLUGIN_ID).getResource("preference"));
			String fileLocation = resourceURL.getPath() + File.separator + fileName;
			File preferenceFile = new File(fileLocation);
			if(preferenceFile.exists())
			{
				try
				{
					SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(preferenceFile);
					if(document != null)
					{
						Element defaultPreferences = document.getRootElement();
						if (defaultPreferences != null)
						{
							List<?> children = defaultPreferences.getChildren("preference");
							String name = null;
							boolean selected = false;
							for(Object child : children)
							{
								name = ((Element) child).getAttributeValue("name");
								if(name != null)
								{
									selected = ((Element) child).getAttributeValue("selected") == null
											? false : ((Element) child).getAttributeValue("selected").equals("true");
									preferenceValues.put(name, selected);
								}
							}
						}
					}
				} catch (JDOMException e)
				{
					logger.error(e.getMessage(), e);
				} catch (IOException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
			else
			{
				logger.error("File not found for default preference : " + fileLocation);
			}
		} catch (IOException ex)
		{
			logger.error(ex.getMessage(), ex);
		}
		return preferenceValues;
	}

	public static HashMap<String, Boolean> getPreferenceValues(String fileName)
	{
		HashMap<String, Boolean> preferenceValues = new HashMap<String, Boolean>();
		try
		{
			URL resourceURL = FileLocator.toFileURL(
					Platform.getBundle(Activator.PLUGIN_ID).getResource("preference"));
			String fileLocation = resourceURL.getPath() + File.separator + fileName;
			File preferenceFile = new File(fileLocation);
			if(preferenceFile.exists())
			{
				try
				{
					SAXBuilder builder = new SAXBuilder();
					Document document = builder.build(preferenceFile);
					if(document != null)
					{
						Element defaultPreferences = document.getRootElement();
						if (defaultPreferences != null)
						{
							List<?> children = defaultPreferences.getChildren("preference");
							String name = null;
							boolean selected = false;
							for(Object child : children)
							{
								name = ((Element) child).getAttributeValue("name");
								if(name != null)
								{
									selected = ((Element) child).getAttributeValue("selected") == null
											? false : ((Element) child).getAttributeValue("selected").equals("true");
									preferenceValues.put(name, selected);
								}
							}
						}
					}
				} catch (JDOMException e)
				{
					logger.error(e.getMessage(), e);
				} catch (IOException e)
				{
					logger.error(e.getMessage(), e);
				}
			}
			else
			{
				logger.error("File not found for default preference : " + fileLocation);
			}
		} catch (IOException ex)
		{
			logger.error(ex.getMessage(), ex);
		}
		return preferenceValues;
	}

	public static List<DocumentType> getDocumentTypesFromXml(File preferenceFile)
	{
		List<DocumentType> documentTypes = new ArrayList<DocumentType>();
		if(preferenceFile.exists())
		{
			try
			{
				SAXBuilder builder = new SAXBuilder();
				Document document = builder.build(preferenceFile);
				if(document != null)
				{
					Element defaultPreferences = document.getRootElement();
					if (defaultPreferences != null)
					{
						List<?> children = defaultPreferences.getChildren("documentType");
						DocumentType documentType = null;
						String label = null;
						boolean selected = false;
						Set<String> subTypes = null;
						String subType = null;
						Element documentTypeElement = null;
						List<?> subTypeElements = null;
						Element subTypeElement = null;
						for(Object child : children)
						{
							documentTypeElement = (Element) child;
							label = documentTypeElement.getAttributeValue("name");
							selected = ((Element) child).getAttributeValue("selected") == null
									? false : ((Element) child).getAttributeValue("selected").equals("true");
							documentType = new DocumentType();
							documentType.setLabel(label);
							documentType.setMakeDefault(selected);
							subTypes = new HashSet<String>();
							subTypeElements = documentTypeElement.getChildren("subType");
							if(subTypeElements != null)
							{
								for(Object subTypeEle : subTypeElements)
								{
									subTypeElement = (Element) subTypeEle;
									subType = subTypeElement.getAttributeValue("name");
									selected = ((Element) child).getAttributeValue("selected") == null
											? false : ((Element) child).getAttributeValue("selected").equals("true");
									if(selected)
									{
										documentType.setSelectedSubType(subType);
									}
									subTypes.add(subType);
								}
							}
							documentType.setSubTypes(subTypes);
							documentTypes.add(documentType);
						}
					}
				}
			} catch (Exception ex)
			{
				logger.error(ex.getMessage(), ex);
			}

		}
		return documentTypes;
	}
}
