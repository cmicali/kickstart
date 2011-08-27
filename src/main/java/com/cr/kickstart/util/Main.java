package com.cr.kickstart.util;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.webapp.WebAppContext;

/**
 * Author: chrismicali
 */
public class Main {

    public static void main(String[] args) throws Exception{
        Server server = new Server(Integer.valueOf(System.getenv("PORT")));
        WebAppContext app = new WebAppContext();
        app.setContextPath("/");
        app.setWar("src/main/webapp");
        server.setHandler(app);
        server.start();
        server.join();
    }
}
