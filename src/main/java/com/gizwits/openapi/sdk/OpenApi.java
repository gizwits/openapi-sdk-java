package com.gizwits.openapi.sdk;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenApi
{
    private static class User
    {
        private String wechatOpenId;
        private String gizwitsAppId;
        private String gizwitsUserToken;
        private long gizwitsUserTokenExpiredAt;

        public String getWechatOpenId() { return this.wechatOpenId; }

        public void setWechatOpenId(String value) { this.wechatOpenId = value; }

        public String getGizwitsAppId() { return this.gizwitsAppId; }

        public void setGizwitsAppId(String value) { this.gizwitsAppId = value; }

        public String getGizwitsUserToken() { return this.gizwitsUserToken; }

        public void setGizwitsUserToken(String value) { this.gizwitsUserToken = value; }

        public long getGizwitsUserTokenExpiredAt() { return this.gizwitsUserTokenExpiredAt; }

        public void setGizwitsUserTokenExpiredAt(long value) { this.gizwitsUserTokenExpiredAt = value; }
    }

    private static Map<String, User> users = new HashMap<String, User>();
    private static String gizwitsBaseApiUrl = "https://api.gizwits.com";
    private static final int gizwitsUserTokenExpiredTime = 500; // in seconds

    private static String getUserToken(String wechatOpenId, String gizwitsAppId) throws GizwitsException
    {
        User user = users.get(wechatOpenId);
        if (user == null)
        {
            user = createUser(wechatOpenId, gizwitsAppId);
        }
        if (System.currentTimeMillis() / 1000 + gizwitsUserTokenExpiredTime >= user.getGizwitsUserTokenExpiredAt())
        {
            updateUserToken(user);
            purgeExpiredCache();
        }
        return user.getGizwitsUserToken();
    }

    private synchronized static User createUser(String wechatOpenId, String gizwitsAppId)
    {
        User user = users.get(wechatOpenId);
        if (user == null)
        {
            user = new User();
            user.setWechatOpenId(wechatOpenId);
            user.setGizwitsAppId(gizwitsAppId);
            user.setGizwitsUserTokenExpiredAt(0);
            users.put(wechatOpenId, user);
        }
        return user;
    }

    private static void updateUserToken(User user) throws GizwitsException
    {
        synchronized (user)
        {
            if (System.currentTimeMillis() / 1000 + gizwitsUserTokenExpiredTime >= user.getGizwitsUserTokenExpiredAt())
            {
                String wechatOpenId = user.getWechatOpenId();
                String gizwitsAppId = user.getGizwitsAppId();

                String reqBody = new JSONObject()
                        .put("phone_id", wechatOpenId)
                        .put("lang", "en")
                        .toString();

                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json");
                headers.put("X-Gizwits-Application-Id", gizwitsAppId);

                String url = gizwitsBaseApiUrl + "/app/users";
                Map<String, Object> res = HttpRequest.send(url, HttpRequest.Method.POST, headers, null, reqBody);
                if (res == null)
                {
                    throw new GizwitsException("Internal error");
                }
                int resCode = (Integer)res.get("code");
                if (resCode != 201)
                {
                    throw new GizwitsException("Gizwits open api fault");
                }
                JSONObject resBody = new JSONObject(res.get("body").toString());
                user.setGizwitsUserToken(resBody.getString("token"));
                user.setGizwitsUserTokenExpiredAt(resBody.getLong("expire_at"));
            }
        }
    }

    private static boolean getBoundDevices(String gizwitsAppId, String gizwitsUserToken, int limit, int skip, List<DeviceInfo> result) throws GizwitsException
    {
        boolean more;

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Gizwits-Application-Id", gizwitsAppId);
        headers.put("X-Gizwits-User-token", gizwitsUserToken);

        String query = "show_disabled=1&limit=" + String.valueOf(limit) + "&skip=" + String.valueOf(skip);
        String url = gizwitsBaseApiUrl + "/app/bindings";
        Map<String, Object> res = HttpRequest.send(url, HttpRequest.Method.GET, headers, query, null);
        if (res == null)
        {
            throw new GizwitsException("Internal error");
        }
        int resCode = (Integer)res.get("code");
        if (resCode != 200)
        {
            throw new GizwitsException("Gizwits open api fault");
        }
        JSONObject resJson = new JSONObject(res.get("body").toString());
        JSONArray devicesJson = resJson.getJSONArray("devices");

        for(int i = 0; i < devicesJson.length(); i++)
        {
            JSONObject deviceJson = devicesJson.getJSONObject(i);
            DeviceInfo device = new DeviceInfo();
            device.setMac(deviceJson.getString("mac"));
            device.setDid(deviceJson.getString("did"));
            device.setIsOnline(deviceJson.getBoolean("is_online"));
            device.setAlias(deviceJson.getString("dev_alias"));
            result.add(device);
        }
        more = devicesJson.length() == limit;
        return more;
    }

    /**
     * 获取当前使用的机智云openapi域名
     * @return openapi域名
     */
    public static String getGizwitsBaseApiUrl()
    {
        return gizwitsBaseApiUrl;
    }

    /**
     * 设置机智云openapi域名，默认值为"https://api.gizwits.com"
     * @param url openapi域名
     */
    public static void setGizwitsBaseApiUrl(String url)
    {
        gizwitsBaseApiUrl = url;
    }

    /**
     * 用户绑定设备
     *
     * @param wechatOpenId         微信用户Id
     * @param gizwitsAppId         机智云平台应用标识
     * @param gizwitsProductKey    机智云平台产品标识
     * @param gizwitsProductSecret 机智云平台产品密钥
     * @param mac                  设备mac地址
     * @param deviceAlias          设备别名
     * @param deviceRemark         设备批注
     * @return 绑定成功返回DeviceInfo对象
     * @throws GizwitsException 抛出操作异常原因
     */
    public static DeviceInfo bindDevice(String wechatOpenId, String gizwitsAppId, String gizwitsProductKey, String gizwitsProductSecret, String mac, String deviceAlias, String deviceRemark) throws GizwitsException
    {
        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String gizwitsSignature = HttpRequest.md5((gizwitsProductSecret + timestamp).toLowerCase());
        String gizwitsUserToken = getUserToken(wechatOpenId, gizwitsAppId);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Gizwits-Application-Id", gizwitsAppId);
        headers.put("X-Gizwits-User-token", gizwitsUserToken);
        headers.put("X-Gizwits-Timestamp", timestamp);
        headers.put("X-Gizwits-Signature", gizwitsSignature);

        String reqBody = new JSONObject()
                .put("product_key", gizwitsProductKey)
                .put("mac", mac)
                .put("dev_alias", deviceAlias)
                .put("remark", deviceRemark)
                .toString();

        String url = gizwitsBaseApiUrl + "/app/bind_mac";
        Map<String, Object> res = HttpRequest.send(url, HttpRequest.Method.POST, headers, null, reqBody);
        if (res == null)
        {
            throw new GizwitsException("Internal error");
        }
        int resCode = (Integer)res.get("code");
        if (resCode != 201 && resCode != 200)
        {
            throw new GizwitsException("Gizwits open api fault");
        }
        JSONObject deviceJson = new JSONObject(res.get("body").toString());
        DeviceInfo device = new DeviceInfo();
        device.setMac(deviceJson.getString("mac"));
        device.setDid(deviceJson.getString("did"));
        device.setIsOnline(deviceJson.getBoolean("is_online"));
        device.setAlias(deviceJson.getString("dev_alias"));
        return device;
    }

    /**
     * 获取用户绑定的设备信息
     *
     * @param wechatOpenId 微信用户Id
     * @param gizwitsAppId 机智云平台应用标识
     * @return 返回wechatDevice的信息列表
     * @throws GizwitsException 抛出操作异常原因
     */
    public static List<DeviceInfo> getBoundDevices(String wechatOpenId, String gizwitsAppId) throws GizwitsException
    {
        List<DeviceInfo> devices = new ArrayList<DeviceInfo>();

        int limit = 20;
        int skip = 0;

        String gizwitsUserToken = getUserToken(wechatOpenId, gizwitsAppId);
        while(getBoundDevices(gizwitsAppId, gizwitsUserToken, limit, skip, devices))
        {
            skip += limit;
        }
        return devices;
    }

    /**
     * 获取设备的在线状态
     *
     * @param wechatOpenId      微信用户Id
     * @param gizwitsAppId      机智云平台应用标识
     * @param gizwitsDid        设备在机智云平台的注册Id
     * @return true表示设备在线；false表示设备不在线
     * @throws GizwitsException 抛出操作异常原因
     */
    public static boolean getDeviceOnlineStatus(String wechatOpenId, String gizwitsAppId, String gizwitsDid) throws GizwitsException
    {
        Map<String, String> result = null;

        String gizwitsUserToken = getUserToken(wechatOpenId, gizwitsAppId);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Gizwits-Application-Id", gizwitsAppId);
        headers.put("X-Gizwits-User-token", gizwitsUserToken);

        String url = gizwitsBaseApiUrl + "/app/devices/" + gizwitsDid;
        Map<String, Object> res = HttpRequest.send(url, HttpRequest.Method.GET, headers, null, null);
        if (res == null)
        {
            throw new GizwitsException("Internal error");
        }
        int resCode = (Integer)res.get("code");
        if (resCode != 200)
        {
            throw new GizwitsException("Gizwits open api fault");
        }
        JSONObject resJson = new JSONObject(res.get("body").toString());
        return resJson.getBoolean("is_online");
    }

    /**
     * 用户解绑设备
     *
     * @param wechatOpenId         微信用户Id
     * @param gizwitsAppId         机智云平台应用标识
     * @param gizwitsDid           设备在机智云平台的注册Id
     * @return true表示解绑成功；false表示解绑失败
     * @throws GizwitsException 抛出操作异常原因
     */
    public static boolean unbindDevice(String wechatOpenId, String gizwitsAppId, String gizwitsDid) throws GizwitsException
    {
        String gizwitsUserToken = getUserToken(wechatOpenId, gizwitsAppId);

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/json");
        headers.put("X-Gizwits-Application-Id", gizwitsAppId);
        headers.put("X-Gizwits-User-token", gizwitsUserToken);

        String reqBody = new JSONObject()
                .put("devices", new JSONArray()
                        .put(new JSONObject()
                                .put("did", gizwitsDid)
                        )
                )
                .toString();

        String url = gizwitsBaseApiUrl + "/app/bindings";
        Map<String, Object> res = HttpRequest.send(url, HttpRequest.Method.DELETE, headers, null, reqBody);
        if (res == null)
        {
            throw new GizwitsException("Internal error");
        }
        int resCode = (Integer)res.get("code");
        if (resCode != 200)
        {
            throw new GizwitsException("Gizwits open api fault");
        }
        JSONObject resJson = new JSONObject(res.get("body").toString());
        JSONArray successDids = resJson.getJSONArray("success");
        return successDids.length() > 0;
    }

    /**
     * 清理缓存数据
     */
    public static void purgeExpiredCache()
    {
        for (Map.Entry<String, User> map : users.entrySet())
        {
            User user = map.getValue();
            if (System.currentTimeMillis() / 1000 + gizwitsUserTokenExpiredTime >= user.getGizwitsUserTokenExpiredAt())
            {
                users.remove(map.getKey());
            }
        }
    }
}
