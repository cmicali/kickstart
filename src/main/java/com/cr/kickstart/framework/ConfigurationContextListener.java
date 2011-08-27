package com.cr.kickstart.framework;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Author: chris
 */
public class ConfigurationContextListener implements ServletContextListener {

    public void contextInitialized(ServletContextEvent servletContextEvent) {
        ServerConfig.init(servletContextEvent.getServletContext());
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        ServerConfig.destroy(servletContextEvent.getServletContext());
    }
}