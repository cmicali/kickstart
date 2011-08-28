package com.cr.kickstart.framework;

import com.cr.kickstart.data.PersistenceManager;
import com.cr.kickstart.util.JSONUtil;
import com.cr.kickstart.util.StringUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.persistence.EntityManager;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * Author: chrismicali
 */
public abstract class APIEndpoint {

    protected static final String CONTENT_TYPE_JSON = "application/json";

    // Status codes

    protected static final String STATUS_OK = "ok";
    protected static final String STATUS_ERROR = "error";
    protected static final String STATUS_OBJECT_NOT_FOUND = "object_not_found";
	protected static final String STATUS_INVALID_RECIPIENT = "invalid_recipient";
	protected static final String STATUS_INVALID_SENDER = "invalid_sender";
	protected static final String STATUS_INVALID_REQUEST = "invalid_request";

    // Parameter field types

    protected static final String FIELD_REQUIRED = "required";
    protected static final String FIELD_OPTIONAL = "optional";

    protected static final String FIELD_TYPE_STRING = "string";
    protected static final String FIELD_TYPE_INT = "int";
    protected static final String FIELD_TYPE_LONG = "long";
    protected static final String FIELD_TYPE_FLOAT = "float";
    protected static final String FIELD_TYPE_BOOLEAN = "boolean";
    protected static final String FIELD_TYPE_FILE = "file";
    protected static final String FIELD_TYPE_INT_ARRAY = "int_array";
    protected static final String FIELD_TYPE_LONG_ARRAY = "long_array";

    // Common parameters

    protected static final String PARAM_LIMIT = "limit";
    protected static final String PARAM_OFFSET = "offset";

    // Timing

    private static final String TIMING_START_TIME = "timing_start_time";
    private static final String TIMING_HASH = "timing_hash";

    private final boolean isServletTimingEnabled = true;

    void doGet(HttpServletRequest req, HttpServletResponse resp, ServletContext servletContext) throws ServletException, IOException {
        startServletTiming(req);
		try {
			JSONObject jo = getJSONObjectFromRequestParameters(req);
			doRequest(jo, req, resp, servletContext);
		}
		catch(JSONException jex) {
			try {
				endRequest(STATUS_INVALID_REQUEST, req, resp);
			}
            catch(Exception ex) {
			}
		}
        finally {
            onServletComplete();
        }
	}

	void doPost(HttpServletRequest request, HttpServletResponse response, ServletContext servletContext) throws ServletException,
			IOException {
        startServletTiming(request);
        JSONObject jo;
		try {
            if (CONTENT_TYPE_JSON.equals(request.getContentType())) {

                BufferedReader reader = request.getReader();
                StringBuilder sb = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    sb.append(line);
                    sb.append("\n");
                    line = reader.readLine();
                }
                reader.close();
                String data = sb.toString();

                jo = new JSONObject(data);
            }
            jo = getJSONObjectFromRequestParameters(request);
            if (ServletFileUpload.isMultipartContent(request)) {
                try {
                    // Create a factory for disk-based file items
                    FileItemFactory factory = new DiskFileItemFactory();

                    // Create a new file upload handler
                    ServletFileUpload upload = new ServletFileUpload(factory);
                    // Parse the request
                    List<FileItem> items = upload.parseRequest(request);
                    JSONArray arr = new JSONArray();
                    for(FileItem f : items) {
                        if (f.isFormField()) {
                            jo.put(f.getFieldName(), f.getString());
                        }
                        else if (CONTENT_TYPE_JSON.equals(f.getContentType())) {
                            JSONObject njo = new JSONObject(StringUtils.getBytesAsString(f.get()));
                            JSONUtil.merge(jo, njo);
                        }
                        else {
                            jo.put(f.getFieldName(), f);
                        }
                    }
                }
                catch(Exception ex) {
                    endRequest(STATUS_ERROR, request, response);
                    return;
                }
            }
            doRequest(jo, request, response, servletContext);
		}
		catch (JSONException jex) {
			try {
				endRequest(STATUS_INVALID_REQUEST, request, response);
			} catch(Exception ex) {

			}
		}
        finally {
            onServletComplete();
        }
	}

    ////////////////////////
    // Request parameter methods

    private JSONObject getJSONObjectFromRequestParameters(HttpServletRequest req) throws JSONException {
        return getJSONObjectFromRequestParameters(req, new JSONObject());
    }

    private JSONObject getJSONObjectFromRequestParameters(HttpServletRequest req, JSONObject jo) throws JSONException {
        Enumeration e = req.getParameterNames();
        while(e.hasMoreElements()) {
            String key = e.nextElement().toString();
            String param = req.getParameter(key);
            if (param.length() > 0) {
                jo.put(key, param);
            }
        }
        return jo;
    }

    private static final ThreadLocal<EntityManager> entityManager = new ThreadLocal<EntityManager>();

    protected EntityManager getEntityManager(ServletContext servletContext) {
        return getEntityManager(true, servletContext);
    }

    protected EntityManager getEntityManager(boolean loadIfRequired, ServletContext servletContext) {
        EntityManager em = entityManager.get();
        if (em == null && loadIfRequired) {
            em = PersistenceManager.getInstance().getEntityManager(servletContext);
            em.getTransaction().begin();
            entityManager.set(em);
        }
        return em;
    }

    private void closeEntityManager() {
        EntityManager em = entityManager.get();
        if (em != null) {
            if (em.isOpen()) {
                em.close();
            }
            entityManager.set(null);
        }
    }

	private void doRequest(JSONObject jo, HttpServletRequest request, HttpServletResponse response, ServletContext servletContext)
	throws ServletException, IOException, JSONException
	{

        if (jo.has("response_delay")) {
            long delay = jo.getInt("response_delay");
            if (isServletTimingEnabled) {
                startTiming("response_delay", request);
            }
            try { Thread.sleep(delay); } catch(InterruptedException ex) {}
            if (isServletTimingEnabled) {
                stopTiming("response_delay", request);
            }
        }

		if (validateRequest(jo)) {
            try {
			    handleRequest(jo, request, response, servletContext);
            }
            finally {
                EntityManager em = getEntityManager(true, servletContext);
                if (em != null) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    closeEntityManager();
                }
            }
		}
		else {
			endRequest(STATUS_INVALID_REQUEST, request, response);
		}
	}

	protected void endRequest(String statusCode, HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
		JSONObject jo = new JSONObject();
		endRequest(statusCode, jo, request, response);
	}

	protected void endRequest(String statusCode, JSONObject jo, HttpServletRequest request, HttpServletResponse response) throws IOException, JSONException {
        JSONObject fjo = new JSONObject();
        JSONObject requestjo = new JSONObject();

        if (!STATUS_OK.equals(statusCode)) {
            requestjo.put("error_type", statusCode);
            statusCode = STATUS_ERROR;
            if (getErrorMessage() != null) {
                requestjo.put("error_detail", getErrorMessage());
            }
        }
        requestjo.put("status", statusCode);

        fjo.put("response", jo);
        stopServletTiming(requestjo, request);
        fjo.put("request", requestjo);
		response.setContentType("application/json");
		fjo.write(response.getWriter());
	}

    protected String[] getRequestFields() {
        return null;
    }

    String errorMessage = null;

    protected void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    protected String getErrorMessage() {
        return errorMessage;
    }

	protected boolean validateRequest(JSONObject requestObject) throws JSONException{
        String[] built_ins = new String[] {
                PARAM_LIMIT, FIELD_TYPE_INT, FIELD_OPTIONAL,
                PARAM_OFFSET, FIELD_TYPE_INT, FIELD_OPTIONAL,
        };

        return validateRequest(built_ins, requestObject) &&
               validateRequest(getRequestFields(), requestObject);
    }

    protected final boolean validateRequest(String[] fields, JSONObject requestObject) throws JSONException{
        if (fields != null) {
            for(int i = 0; i < fields.length; i+=3) {
                String s = fields[i];
                String type = fields[i+1];
                boolean required = FIELD_REQUIRED.equals(fields[i+2]);
                if (!requestObject.has(s)) {
                    if (required) {
                        setErrorMessage("Required parameter missing: " + s);
                        return false;
                    }
                    else {
                        break;
                    }
                }
                if (FIELD_TYPE_FILE.equals(type)) {
                    Object o = requestObject.get(s);
                    if (o == null || (!(o instanceof FileItem))) {
                        setErrorMessage("Required parameter missing: " + s);
                        return false;
                    }
                    break;
                }

                String strVal = requestObject.getString(s);
                if (required && (strVal.length() == 0)) {
                    setErrorMessage("Required parameter missing: " + s);
                    return false;
                }


                try {
                    if (FIELD_TYPE_STRING.equals(type)) {
                        // TODO: Additional checks (invalid chars, SQL injection, etc)
                    }
                    else if (FIELD_TYPE_INT.equals(type)) {
                        int x = Integer.parseInt(strVal);
                    }
                    else if (FIELD_TYPE_LONG.equals(type)) {
                        long x = Long.parseLong(strVal);
                    }
                    else if (FIELD_TYPE_FLOAT.equals(type)) {
                        float x = Float.parseFloat(strVal);
                    }
                    else if (FIELD_TYPE_BOOLEAN.equals(type)) {
                        Boolean x = Boolean.parseBoolean(strVal);
                    }
                    else if (FIELD_TYPE_INT_ARRAY.equals(type)) {
                        if (strVal.length() > 0) {
                            if (strVal.contains(",")) {
                                String[] parts = strVal.split(",");
                                for(String part : parts) {
                                    int x = Integer.parseInt(part);
                                }
                            }
                            else {
                                int x = Integer.parseInt(strVal);
                            }
                        }
                    }
                    else if (FIELD_TYPE_LONG_ARRAY.equals(type)) {
                        if (strVal.length() > 0) {
                            if (strVal.contains(",")) {
                                String[] parts = strVal.split(",");
                                for(String part : parts) {
                                    long x = Long.parseLong(strVal);
                                }
                            }
                            else {
                                long x = Long.parseLong(strVal);
                            }
                        }
                    }
                    else {
                        setErrorMessage("Unknown field type: " + type);
                        return false;
                    }
                }
                catch(Exception ex) {
                    setErrorMessage("Invalid parameter format: " + s);
                    return false;
                }
            }
        }
        return true;
	}

    protected void onServletComplete() {
        errorMessage = null;
    }

	protected abstract void handleRequest(JSONObject params, HttpServletRequest request,
			HttpServletResponse response, ServletContext servletContext) throws ServletException, IOException, JSONException;


    private void startServletTiming(HttpServletRequest request) {
        if (isServletTimingEnabled) {
            request.setAttribute(TIMING_START_TIME, new Long(System.currentTimeMillis()));
        }
    }

    private void stopServletTiming(JSONObject response, HttpServletRequest request) throws JSONException {
        if (isServletTimingEnabled) {
            long timingEndTime = System.currentTimeMillis();
            long timingStartTime = ((Long)request.getAttribute(TIMING_START_TIME)).longValue();
            long processingTime = timingEndTime - timingStartTime;
            JSONObject jo = new JSONObject();
            jo.put("total", processingTime + "ms");
            Hashtable<String, Long> timingHash = (Hashtable<String, Long>)request.getAttribute(TIMING_HASH);
            if (timingHash != null) {
                for(String key : timingHash.keySet()) {
                    long length = timingHash.get(key);
                    jo.put(key, length + "ms");
                    processingTime -= length;
                }
                jo.put("other", processingTime + "ms");
             }
            response.put("timing", jo);
            request.removeAttribute(TIMING_HASH);
            request.removeAttribute(TIMING_START_TIME);
        }
    }

    protected void startTiming(String category, HttpServletRequest request) {
        if (isServletTimingEnabled) {
            Hashtable<String, Long> timingHash = (Hashtable<String, Long>)request.getAttribute(TIMING_HASH);
            if (timingHash == null) {
                timingHash = new Hashtable<String, Long>();
                request.setAttribute(TIMING_HASH, timingHash);
            }
            long startTime = System.currentTimeMillis();
            if (timingHash.containsKey(category)) {
                startTime -= timingHash.get(category);
            }
            timingHash.put(category, startTime);
        }
    }

    protected void stopTiming(String category, HttpServletRequest request) {
        if (isServletTimingEnabled) {
            Hashtable<String, Long> timingHash = (Hashtable<String, Long>)request.getAttribute(TIMING_HASH);
            if (timingHash.containsKey(category)) {
                Long startTime = timingHash.get(category);
                long elapsed = System.currentTimeMillis() - startTime;
                timingHash.put(category, elapsed);
            }
        }
    }

    protected static boolean isContentTypeSupportedForPhoto(String contentType) {
        return ("image/jpeg".equals(contentType) || "image/png".equals(contentType));

    }

    protected boolean isPhotoValid(FileItem photo, HttpServletRequest request, HttpServletResponse response) throws JSONException, IOException {
        String type = photo.getContentType();
        if (!isContentTypeSupportedForPhoto(type)) {
            setErrorMessage("Invalid image format: " + type+ " - Supported formats: image/jpeg,image/png");
            endRequest(STATUS_INVALID_REQUEST, request, response);
            return false;
        }
        return true;
    }


    protected int getOffset(JSONObject requestObject) throws JSONException {
        if (requestObject.has(PARAM_OFFSET)) {
            return requestObject.getInt(PARAM_OFFSET);
        }
        return 0;
    }

    protected int getDefaultLimit() {
        return 20;
    }

    protected int getLimit(JSONObject requestObject) throws JSONException {
        return getLimit(requestObject, getDefaultLimit());
    }

    protected int getLimit(JSONObject requestObject, int defaultLimit) throws JSONException {
        if (requestObject.has(PARAM_LIMIT)) {
            return requestObject.getInt(PARAM_LIMIT);
        }
        return defaultLimit;
    }

    public String[] getArrayParameter(String paramValue) {
        if (paramValue.contains(","))
            return paramValue.split(",");
        else
            return new String[] { paramValue };
    }

}
