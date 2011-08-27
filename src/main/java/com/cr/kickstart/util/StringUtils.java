package com.cr.kickstart.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Author: chris
 */
public class StringUtils {

    public static String getBytesAsString(byte[] data) {
        return getBytesAsString(data, "UTF8");
    }

    public static String getBytesAsString(byte[] data, String encoding) {
        String result = "";
        try {
            if (data != null) {
                result = new String(data, encoding);
            }
        }
        catch(UnsupportedEncodingException uee) {
            // Try converting with no encoding
            result = new String(data);
        }
        return result;
    }

    public static String encodeURLParameter(String parameterValue) {
        try {
            return URLEncoder.encode(parameterValue, "UTF-8");
        }
        catch(Exception x) {
        }
        return null;
    }
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }

    public static boolean isNotEmpty(String s) {
        return s!=null && s.length() > 0;
    }

    public static String urlEncode(String s) {
        try {
            s = URLEncoder.encode(s, "UTF8");
        }
        catch(Exception ex) {
            
        }
        return s;
    }
}
