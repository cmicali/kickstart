package com.cr.kickstart.stripes;

import net.sourceforge.stripes.util.StringUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SecurityFilter implements Filter {

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (request.getSession().getAttribute("user") != null) {
            filterChain.doFilter(request, response);
        } else if (isPublicResource(request)) {
            filterChain.doFilter(request, response);
        } else {
            // Redirect the user to the login page, noting where they were coming from
            String targetUrl = StringUtil.urlEncode(request.getServletPath());

            response.sendRedirect(
                    request.getContextPath() + "/bugzooky/Login.jsp?targetUrl=" + targetUrl);
        }
    }

    /**
     * Method that checks the request to see if it is for a publicly accessible resource
     */
    protected boolean isPublicResource(HttpServletRequest request) {
        return true;
        /*
        String resource = request.getServletPath();

        return publicUrls.contains(request.getServletPath())
                || (!resource.endsWith(".jsp") && !resource.endsWith(".action"));
                */
    }

    /**
     * Does nothing.
     */
    public void destroy() {
    }
}
