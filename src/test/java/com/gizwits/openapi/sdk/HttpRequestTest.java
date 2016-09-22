package com.gizwits.openapi.sdk;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class HttpRequestTest
{
    @Test
    public void testSend()
    {
        String url = "http://api.gizwits.com/dev/devices/jLAYMCwHrVxwZM3T2Ph6Gr";
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/text");
        HttpRequest.Method method = HttpRequest.Method.GET;
        String query = null;
        String body = null;
        Map<String, Object> res = HttpRequest.send(url, method, headers, query, body);
        assertEquals(200, res.get("code"));
    }

    @Test
    public void testMD5()
    {
        String inputStr = "abcdefg";
        String md5Str = "7ac66c0f148de9519b8bd264312c4d64";
        assertEquals(md5Str, HttpRequest.md5(inputStr));

        assertEquals("", HttpRequest.md5(null));
    }
}
