package com.gizwits.openapi.sdk;

public class DeviceInfo
{
    /**
     * 设备mac地址
     */
    private String mac;

    /**
     * 设备在机智云平台的注册id
     */
    private String did;

    /**
     * 设备在线状态
     */
    private boolean isOnline;

    /**
     * 设备别名
     */
    private String alias;

    /**
     * 获取设备mac地址
     * @return 设备mac地址
     */
    public String getMac() { return this.mac; }

    /**
     * 设置设备mac地址
     * @param value 设备mac地址
     */
    public void setMac(String value) { this.mac = value; }

    /**
     * 获取设备did
     * @return 设备did
     */
    public String getDid() { return this.did; }

    /**
     * 设置设备did
     * @param value 设备did
     */
    public void setDid(String value) { this.did = value; }

    /**
     * 获取设备在线状态
     * @return true：设备在线；false：设备不在线
     */
    public boolean getIsOnline() { return this.isOnline; }

    /**
     * 设置设备在线状态
     * @param value true：设备在线；false：设备不在线
     */
    public void setIsOnline(boolean value) { this.isOnline = value; }

    /**
     * 获取设备别名
     * @return 设备别名
     */
    public String getAlias() { return this.alias; }

    /**
     * 设置设备别名
     * @param value 设备别名
     */
    public void setAlias(String value) { this.alias = value; }
}
