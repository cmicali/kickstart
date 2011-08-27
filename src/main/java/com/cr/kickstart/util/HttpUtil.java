package com.cr.kickstart.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class HttpUtil {

    protected static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    public static String fetchUrl(String url) {
        URL u = null;
        InputStream is = null;
        String result = null;
        try {
            log.debug("Fetching URL: {}", url);
            u = new URL(url);
            is = u.openStream();
            return getStringFromInputStream(is);
        }
        catch(Exception ex) {
            log.error("exception fetching url: {}", ex.getMessage());
        }
        finally {
            if (is != null) { try { is.close(); } catch(Exception ex) {} }
        }
        return result;
    }

    public static String getStringFromInputStream(InputStream is) throws IOException {
        byte[] data = readData(is);
        return StringUtils.getBytesAsString(data);
    }

    protected static byte[] readData(InputStream in)
            throws IOException {
        //log.info("In readData in " + in);
    	ByteArrayOutputStream boas = null;
        try {
            boas = new ByteArrayOutputStream();
            transfer(in, boas);
            byte[] data = boas.toByteArray();
            return data;
        }
        finally {
            try { if (boas != null) boas.close(); } catch(Exception ex) {}
        }
    }

    //updated version with less latency
    protected static void transfer(InputStream in, ByteArrayOutputStream out)
    {
        //log.info("in transfer");
        final int chunkSize = 1024;
        byte[] inBuff = new byte[chunkSize];
        int bytesRead = -1;
        DataInputStream ds;
        try {
            //log.info("calling read");
        	boolean dataRemaining = true;
        	int numToRead = in.available();
        	while (dataRemaining) {

        		if(numToRead <= 0) {
        			numToRead = 1;  //if nothing available, read 1 byte (so, waits until there is any data available)
        		}
        		if(numToRead > chunkSize) {
        			numToRead = chunkSize;
        		}

        		bytesRead = in.read(inBuff, 0, numToRead);
        		if(bytesRead < 0) {
        			dataRemaining = false;
        		}
        		else {
        			out.write(inBuff, 0, bytesRead);
        			numToRead = in.available();
        			if(numToRead == 0) {
        				out.flush();  //read all the remaining input, so flush the output
        			}
        		}
            }
        } catch (Exception ex) {}
    }

}