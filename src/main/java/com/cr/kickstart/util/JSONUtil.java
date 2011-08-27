package com.cr.kickstart.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

/**
 * Author: chrismicali
 */
public class JSONUtil {

    public static JSONObject merge(JSONObject dst, JSONObject src) throws JSONException {
        Iterator iter = src.keys();
        while(iter.hasNext()) {
            String key = (String)iter.next();
            dst.put(key, src.get(key));
        }
        return dst;
    }
}
