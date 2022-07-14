package org.grits.toolbox.entry.archive.config;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;

public class ImageRegistry {
	private static Logger logger = Logger.getLogger(ImageRegistry.class);
	private static final String IMAGE_PATH = "icons" + File.separator;
	private static Map<ArchiveImage, ImageDescriptor> imageCache = new HashMap<ArchiveImage, ImageDescriptor>();

	public static ImageDescriptor getImageDescriptor(String pluginId, ArchiveImage image)
	{
		logger.info("Get image from ms plugin : " + image);

		ImageDescriptor imageDescriptor = null;
		if(image != null)
		{
			imageDescriptor = imageCache.get(image);
			if(imageDescriptor == null)
			{
				logger.info("ImageDescriptor not found in cache");
				URL fullPathString = FileLocator.find(
						Platform.getBundle(pluginId), new Path(IMAGE_PATH + image.iconName), null);

				logger.info("Loading image from url : " + fullPathString);
				if(fullPathString != null)
				{
					imageDescriptor = ImageDescriptor.createFromURL(fullPathString);
					imageCache.put(image, imageDescriptor);
				}
			}
		}
		else
			logger.error("Cannot load image from ms plugin (image name is null)");

		return imageDescriptor;
	}


	/**
	 ***********************************
	 *			Icons
	 ***********************************
	 */
	public enum ArchiveImage
	{
		// Yannick Lung ​http://yannicklung.com
		// https://www.iconfinder.com/icons/314782/folder_upload_icon
		BOOKS_ICON("bookcase.png"),
		UPLOAD_FOLDER_ICON("upload2.png"),
		// Maxim Basinski ​https://www.iconfinder.com/vasabii
		// https://www.iconfinder.com/icons/473800/clipboard_document_file_notepad_plan_schedule_write_icon
		UPDATE_ICON("update.png"),
		// Yannick Lung ​http://yannicklung.com
		// ​https://www.iconfinder.com/icons/314420/document_download_icon
		DOWNLOAD_FILE_ICON("download_file.png"),
		// Yannick Lung ​http://yannicklung.com
		// ​https://www.iconfinder.com/icons/314781/download_folder_icon
		DOWNLOAD_ICON("download.png"),
		// Pavel InFeRnODeMoN ​http://www.kde-look.org/usermanager/search.php?username=InFeRnODeMoN GNU GPL license
		// ​http://findicons.com/icon/234317/empty?id=234552
		UNKNOWN_ICON("unknown.png"),
		// Sergio Sanchez Lopez ​http://www.kde-look.org/usermanager/search.php?username=Sephiroth6779 GNU GPL license
		// ​https://www.iconfinder.com/icons/7546/a_adobe_font_letter_type_icon
		RENAME_ICON("rename.png"),
		// Mark James http://www.famfamfam.com Creative Commons Attribution (by)
		// http://findicons.com/icon/159899/email_add
		ADD_EMAIL_ICON("add_email.png"),
		// Tango Desktop Project http://tango.freedesktop.org Public Domain license
		// http://findicons.com/icon/115501/mail_reply_sender
		REPLY_EMAIL_ICON("reply_email.png"),
		// W3 Creative Lab http://www.w3creativelab.com Creative Commons (Attribution 3.0 Unported)
		// https://www.iconfinder.com/icons/370086/bin_delete_empty_out_recycle_remove_trash_icon
		DELETE_ALL_ICON("delete_all_emails.png");
		
		private String iconName = null;
		
		private ArchiveImage(String iconName)
		{
			this.iconName  = iconName;
		}
	}
}
