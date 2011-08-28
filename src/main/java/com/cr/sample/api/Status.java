package com.cr.sample.api;

import com.cr.kickstart.framework.APIEndpoint;
import com.cr.kickstart.framework.ServerConfig;
import com.cr.kickstart.framework.annotations.APIEndpointBinding;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Author: chrismicali
 */
@APIEndpointBinding("/api/status")
public class Status extends APIEndpoint {

    @Override
    protected void handleRequest(JSONObject reqParams, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException, JSONException {
        JSONObject jo = new JSONObject();
        boolean isUp = true;

        jo.put("system_uptime", ServerConfig.getUptimeString(servletContext));
        jo.put("system_status", isUp ? "ok" : "error");

        endRequest(STATUS_OK, jo, request, response);
    }

}
