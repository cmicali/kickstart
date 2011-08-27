package com.cr.kickstart.framework;

import com.cr.kickstart.framework.annotations.APIEndpointBinding;
import com.cr.kickstart.util.ClasspathUtil;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

public class APIEndpointDispatcherServlet extends HttpServlet {

    public static org.slf4j.Logger log = LoggerFactory.getLogger(APIEndpointDispatcherServlet.class);

    private HashMap<String, APIEndpoint> serviceMap;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String packagePrefix = config.getInitParameter("WebServices.Packages");
        String baseUri = config.getInitParameter("BaseUri");
        serviceMap = new HashMap<String, APIEndpoint>();
        try {
            Set<Class> classes = ClasspathUtil.findClasses(getClass().getClassLoader(), packagePrefix);
            for(Class c : classes) {
                try {
                    boolean isService = APIEndpoint.class.isAssignableFrom(c);
                    boolean hasAnnotation = c.isAnnotationPresent(APIEndpointBinding.class);
                    if (isService && hasAnnotation) {
                        APIEndpointBinding s = (APIEndpointBinding)c.getAnnotation(APIEndpointBinding.class);
                        String url = baseUri + s.value();
                        serviceMap.put(url, (APIEndpoint)c.newInstance());
                    }
                    else if (isService) {
                        throw new ServletException("Class extends APIEndpoint but has no binding: " + c.getName());
                    }
                    else if (hasAnnotation) {
                        throw new ServletException("Class has WebServiceBinding but does not extend APIEndpoint: " + c.getName());
                    }
                }
                catch(Exception ex) {
                }
            }
        }
        catch(ClassNotFoundException ex) {
        }
    }

    private APIEndpoint getService(HttpServletRequest req) {
        String uri = req.getRequestURI();
        return serviceMap.get(uri);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        APIEndpoint ws = getService(req);
        if (ws != null) {
            ws.doGet(req, resp, getServletContext());
        }
        else {
            handleNotFound(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        APIEndpoint ws = getService(req);
        if (ws != null) {
            ws.doPost(req, resp, getServletContext());
        }
        else {
            handleNotFound(req, resp);
        }
    }

    protected void handleNotFound(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.sendError(HttpServletResponse.SC_NOT_FOUND, req.getRequestURI());
    }

}
