package com.gizwits.openapi.sdk;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest
{

    public enum Method
    {
        POST, GET, DELETE, PUT
    }

    public static Map<String, Object> send(String url, Method method, Map<String, String> headers, String query, String body)
    {
        PrintWriter out = null;
        BufferedReader in = null;
        Map<String, Object> result = null;
        try
        {
            URL urlObj = new URL(query == null ? url : url + "?" + query);
            HttpURLConnection conn = (HttpURLConnection)urlObj.openConnection();
            conn.setRequestMethod(method.name());
            conn.setDoOutput(true);

            // add headers
            if (headers != null)
            {
                for (String key : headers.keySet())
                {
                    conn.setRequestProperty(key, headers.get(key));
                }
            }

            // add body
            if (body != null)
            {
                conn.setDoInput(true);
                out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                out.print(body);
                out.flush();
            }

            // get response
            int code = conn.getResponseCode();
            result = new HashMap<String, Object>();
            result.put("code", code);
            InputStream is = null;
            try
            {
                is = conn.getInputStream();
            }
            catch (IOException e)
            {
                is = conn.getErrorStream();
            }
            if (is != null)
            {
                in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                String resBody = "";
                String line;
                while ((line = in.readLine()) != null) {
                    resBody += line;
                }
                result.put("body", resBody);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if(out!=null) out.close();
                if(in!=null) in.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * Generate 32bit lower-case MD5 String for the input String.
     * @param inputStr Any String will accept
     * @return 32bit lower-case MD5 String, or get "" when error occur
     */
    public static String md5(String inputStr)
    {
        MessageDigest digestInstance = null;
        String md5Str;
        try
        {
            digestInstance = MessageDigest.getInstance("md5");
            md5Str = bytesToString(digestInstance.digest(inputStr.getBytes("UTF-8")));
        }
        catch (Exception e)
        {
            md5Str = "";
        }
        return md5Str.toLowerCase();
    }

    private static String bytesToString(byte[] bytes)
    {
        StringBuilder sb= new StringBuilder();

        for(byte i: bytes)
        {
            String byteStr = Integer.toHexString(0xff & i).toLowerCase();
            if (byteStr.length() == 1)
            {
                sb.append("0" + byteStr);
            }
            else
            {
                sb.append(byteStr);
            }
        }

        return sb.toString();
    }
}
