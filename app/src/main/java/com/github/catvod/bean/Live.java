package com.github.catvod.bean;

/**
 * 直播实体类
 * @author zhixc
 */
public class Live {
    private String name;
    private String url;
    private String group;

    public Live(String name, String url, String group) {
        this.name = name;
        this.url = url;
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getGroup() {
        return group;
    }
}