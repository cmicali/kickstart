package com.cr.kickstart.framework;

import org.apache.commons.lang.time.DurationFormatUtils;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Author: chris
 */
public class ServerConfig {

    protected static final String ATTR_CONFIG = "serverconfig.config";
    protected static final String ATTR_STARTUP_TIME = "serverconfig.startup_time";

    public static String getUptimeString(ServletContext context) {

        Long startTime = (Long)context.getAttribute(ATTR_STARTUP_TIME);
        long ago = startTime.longValue();
        long now = System.currentTimeMillis();
        return String.format("%s", DurationFormatUtils.formatDurationWords(now - ago, true, true));
    }

    public static void init(ServletContext context) {
        context.setAttribute(ATTR_STARTUP_TIME, new Long(System.currentTimeMillis()));
        ServerConfig.loadPropertiesFile(context);
    }

    public static void destroy(ServletContext context) {

    }

    public static synchronized Properties loadPropertiesFile(ServletContext context) {
        Properties p = null;
        InputStream is = null;
        try {
            is = ServerConfig.class.getClassLoader().getResourceAsStream("serverconfig.properties");
            p = new Properties();
            p.load(is);
        }
        catch(IOException ex) {
            
        }
        finally {
            try { if (is != null) { is.close(); } } catch(Exception ex) {}
        }
        context.setAttribute(ATTR_CONFIG, p);
        return p;
    }

    public static void clearPropertiesFile(ServletContext context) {
        context.removeAttribute(ATTR_CONFIG);
    }

    protected static Properties getPropertiesFile(ServletContext context) {
        return (Properties)context.getAttribute(ServerConfig.ATTR_CONFIG);
    }

    public static String getProperty(String propertyName, ServletContext context) {
        Properties p = getPropertiesFile(context);
        if (p == null) {
            p = loadPropertiesFile(context);
        }
        return p.getProperty(propertyName);
    }

    public static String getProperty(String propertyName, String defaultValue, ServletContext context) {
        Properties p = getPropertiesFile(context);
        if (p == null) {
            p = loadPropertiesFile(context);
        }
        return p.getProperty(propertyName, defaultValue);
    }

}
