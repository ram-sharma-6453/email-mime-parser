package tech.blueglacier.configuration;

import org.apache.commons.io.FilenameUtils;

import java.util.Properties;

public class AppConfig {

    private volatile static AppConfig objectInstance = null;

    private Properties charSetMap = null;

    private AppConfig() {
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

                    // Adding overriding character set for Chinese character set
                    charSetMap.setProperty("iso-ir-58", "GB18030");
                    charSetMap.setProperty("chinese", "GB18030");
                    charSetMap.setProperty("gbk", "GB18030");
                    charSetMap.setProperty("cn-gb", "GB18030");
                    charSetMap.setProperty("csgb2312", "GB18030");
                    charSetMap.setProperty("csiso58gb231280", "GB18030");
                    charSetMap.setProperty("euc-cn", "GB18030");
                    charSetMap.setProperty("euc_cn", "GB18030");
                    charSetMap.setProperty("euccn", "GB18030");
                    charSetMap.setProperty("gb2312", "GB18030");
                    charSetMap.setProperty("gb_2312-80", "GB18030");
                    charSetMap.setProperty("x-EUC-CN", "GB18030");
                    charSetMap.setProperty("gb2312-1980", "GB18030");
                    charSetMap.setProperty("gb2312-80", "GB18030");

                    //  Adding overriding character set for Korean character sets
                    charSetMap.setProperty("5601", "EUC-KR");
                    charSetMap.setProperty("ksc5601-1987", "EUC-KR");
                    charSetMap.setProperty("ksc5601_1987", "EUC-KR");
                    charSetMap.setProperty("euckr", "EUC-KR");
                    charSetMap.setProperty("ksc5601", "EUC-KR");
                    charSetMap.setProperty("ksc_5601", "EUC-KR");
                    charSetMap.setProperty("euc_kr", "EUC-KR");
                    charSetMap.setProperty("csEUCKR", "EUC-KR");
                    charSetMap.setProperty("ks_c_5601-1987", "EUC-KR");

                    // Adding overriding character set for Thai character sets
                    charSetMap.setProperty("ms-874", "TIS-620");
                    charSetMap.setProperty("ms874", "TIS-620");
                    charSetMap.setProperty("windows-874", "TIS-620");
                    charSetMap.setProperty("cp874", "TIS-620");
                    charSetMap.setProperty("874", "TIS-620");
                    charSetMap.setProperty("cs874", "TIS-620");
                    charSetMap.setProperty("ibm874", "TIS-620");

                    // Adding overriding character set for Japanese character sets
                    charSetMap.setProperty("cp932", "cp943");
                }
            }
        }
        return charSetMap;

    }

    public boolean isImageFormat(String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            String[] arrImageFormat = new String[]{"jpeg", "jpg", "jpe", "jfif", "png", "tiff", "tif", "bmp", "gif", "dib", "icb", "mng", "epsf", "xbm", "pbm", "pcx", "pgm", "xpm", "ppm", "rgb"};
            return FilenameUtils.isExtension(fileName.toLowerCase(), arrImageFormat);
        }
        return false;
    }
}
