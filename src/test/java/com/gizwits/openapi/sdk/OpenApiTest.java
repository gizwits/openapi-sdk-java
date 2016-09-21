package com.gizwits.openapi.sdk;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class OpenApiTest
{
    private final String wechatOpenId = "wx001";
    private final String gizwitsAppId = "60d9d45a420b4f539434adb127fe5e5e";
    private final String gizwitsProductKey = "62a66585588d4b3a96073540f4f9c2f0";
    private final String gizwitsProductSecret = "cddacb17faab42deae053812f45bebe4";
    private final String wechatDeviceId = "wx2016032201";
    private final String gizwitsDid = "g7UZR9LbNFZfJWFACocJmV";
    private final String deviceAlias = "JunitDevice";
    private final String deviceRemark = "";

    @Test
    public void testBindDeviceSuccess() throws GizwitsException
    {
        DeviceInfo deviceInfo = OpenApi.bindDevice(wechatOpenId, gizwitsAppId, gizwitsProductKey, gizwitsProductSecret, wechatDeviceId, deviceAlias, deviceRemark);
        Assert.assertEquals(gizwitsDid, deviceInfo.getDid());
        Assert.assertEquals(wechatDeviceId, deviceInfo.getMac());
        Assert.assertEquals(deviceAlias, deviceInfo.getAlias());
    }

    @Test(expected = GizwitsException.class)
    public void testBindDeviceFail() throws GizwitsException
    {
        String gizwitsAppId = "";
        OpenApi.bindDevice(wechatOpenId, gizwitsAppId, gizwitsProductKey, gizwitsProductSecret, wechatDeviceId, deviceAlias, deviceRemark);
    }

    @Test
    public void testGetBoundDevicesSuccess() throws GizwitsException
    {
        List<DeviceInfo> devices = OpenApi.getBoundDevices(wechatOpenId, gizwitsAppId);
        DeviceInfo device = devices.get(0);
        Assert.assertEquals(wechatDeviceId, device.getMac());
        Assert.assertEquals(deviceAlias, device.getAlias());
    }

    @Test
    public void testGetDeviceOnlineStatusSuccess() throws GizwitsException
    {
        DeviceInfo deviceInfo = OpenApi.bindDevice(wechatOpenId, gizwitsAppId, gizwitsProductKey, gizwitsProductSecret, wechatDeviceId, deviceAlias, deviceRemark);
        boolean onlineStatus = OpenApi.getDeviceOnlineStatus(wechatOpenId, gizwitsAppId, deviceInfo.getDid());
        Assert.assertFalse(onlineStatus);
    }

    @Test
    public void testUnbindDeviceSuccess() throws GizwitsException
    {
        boolean result = OpenApi.unbindDevice(wechatOpenId, gizwitsAppId, gizwitsDid);
        Assert.assertTrue(result);
    }

}
