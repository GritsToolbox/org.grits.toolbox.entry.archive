/**
 * 
 */
package org.grits.toolbox.entry.archive.model;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * 
 *
 */
public class KeywordsAdapter extends XmlAdapter<String, Set<String>>
{
	public static final String SEPARATOR = ", ";

	@Override
	public String marshal(Set<String> keywordSet) throws Exception
	{
		String keywords = null;
		if(keywordSet != null)
		{
			keywords = "";
			for(String keyword : keywordSet)
			{
				if(!keyword.isEmpty())
					keywords += keyword + SEPARATOR;
			}
			if(!keywords.isEmpty())
				keywords = keywords.substring(0, keywords.lastIndexOf(SEPARATOR));
		}
		return keywords;
	}

	@Override
	public Set<String> unmarshal(String keywords) throws Exception
	{
		Set<String> keywordSet = null;
		if(keywords != null)
		{
			String[] keywordArray = keywords.split(SEPARATOR);
			keywordSet = new HashSet<String>();
			for(String keyword : keywordArray)
			{
				if(!keyword.isEmpty())
					keywordSet.add(keyword);
			}
		}
		return keywordSet;
	}

}
