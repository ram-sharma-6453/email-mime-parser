package com.blueglacier.configuration;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.FilenameUtils;

import java.util.Properties;

public class AppConfig {

	private volatile static AppConfig objectInstance = null;

	private Configuration appConfig;
	private Properties charSetMap = null;

	private AppConfig() {
		try {
			this.appConfig = new XMLConfiguration(this.getClass().getClassLoader().getResource("conf/emailParserConfig.xml"));
		} catch (ConfigurationException e) {
			throw new RuntimeException(e);		
		}
	}

	public static AppConfig getInstance() {
		if (objectInstance == null) {
			synchronized (AppConfig.class) {
				if (objectInstance == null) {
					objectInstance = new AppConfig();
				}
			}
		}
		return objectInstance;
	}

	public Properties getCharSetMap() {
		if (charSetMap == null) {
			synchronized (AppConfig.class) {
				if (charSetMap == null) {
					charSetMap = new Properties();
					String[] arrStr = appConfig.getStringArray("appSettings.charSetFallback");
					for (int i = 0; i < arrStr.length; i++) {
						String temp = arrStr[i];
						String parentCharSet = temp.substring(temp.indexOf(':') + 1, temp.length());
						temp = temp.substring(0, temp.indexOf(':'));
						String[] arrChild = temp.split(",");
						for (int j = 0; j < arrChild.length; j++) {							
							charSetMap.setProperty(arrChild[j].toLowerCase(), parentCharSet);
						}
					}
				}
			}
		}
		return charSetMap;
	}

	public boolean isImageFormat(String fileName) {
		if (fileName != null && !fileName.isEmpty()) {
			String[] arrImageFormat = appConfig.getStringArray("appSettings.imageFileFormats");
			return FilenameUtils.isExtension(fileName.toLowerCase(), arrImageFormat);
		}
		return false;
	}	
}
