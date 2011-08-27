package com.cr.kickstart.framework;

import com.cr.kickstart.data.PersistenceManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by IntelliJ IDEA.
 * User: chris
 * Date: 4/23/11
 * Time: 4:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class PersistenceServletContextListener implements ServletContextListener {
    public void contextInitialized(ServletContextEvent servletContextEvent) {
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        PersistenceManager.getInstance().closeEntityManagerFactory();
    }
}