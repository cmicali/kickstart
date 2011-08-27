package com.cr.kickstart.stripes;

import net.sourceforge.stripes.config.Configuration;
import net.sourceforge.stripes.exception.ExceptionHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Author: cmicali
 */
public class SiteExceptionHandler implements ExceptionHandler {
    /** Doesn't have to do anything... */
    public void init(Configuration configuration) throws Exception { }

    /** Do something a bit more complicated that just going to a view. */
    public void handle(Throwable throwable,
                       HttpServletRequest request,
                       HttpServletResponse response) throws ServletException, IOException {

        if (hasCause(throwable, com.mchange.v2.resourcepool.CannotAcquireResourceException.class)) {
            request.setAttribute("error_message", "Unable to connect to database");
            request.getRequestDispatcher("/WEB-INF/jsp/error.jsp").forward(request, response);
        }
        else {
            // TODO: Show generic error page
            throw new ServletException(throwable);
        }
    }

    private boolean hasCause(Throwable throwable, Class clazz) {
        if (clazz.isInstance(throwable)) {
            return true;
        }
        if (throwable.getCause() != null && throwable.getCause() != throwable) {
            return hasCause(throwable.getCause(), clazz);
        }
        return false;
    }


}