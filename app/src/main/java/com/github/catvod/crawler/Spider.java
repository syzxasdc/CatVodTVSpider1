package com.github.catvod.crawler;

import android.content.Context;

import java.util.HashMap;
import java.util.List;

public abstract class Spider {

    public void init(Context context) throws Exception {
    }

    /**
     * @param extend 配置文件的 ext 参数
     */
    public void init(Context context, String extend) throws Exception {
        init(context);
    }

    /**
     * 首页数据内容
     *
     * @param filter 
     * @return 返回值
     */
    public String homeContent(boolean filter) throws Exception {
        return "";
    }

    /**
     * 首页最近更新数据 如果上面的homeContent中不包含首页最近更新视频的数据 可以使用这个接口返回
     *
     * @return 返回值
     */
    public String homeVideoContent() throws Exception {
        return "";
    }

    /**
     * 分类数据
     *
     * @param tid    影片分类id值，来自 homeContent 里面的 type_id 值
     * @param pg     第几页
     * @param filter 
     * @param extend 用户已经选择的二级筛选数据
     * @return 返回值
     */
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        return "";
    }

    /**
     * 详情数据
     *
     * @param ids ids.get(0) 来源于 categoryContent() 或 searchContent() 的 vod_id
     * @return 返回值
     */
    public String detailContent(List<String> ids) throws Exception {
        return "";
    }

    /**
     * 搜索数据内容
     *
     * @param key   关键字/词
     * @param quick
     * @return 返回值
     */
    public String searchContent(String key, boolean quick) throws Exception {
        return "";
    }

    /**
     * 搜索数据内容，支持分页，这是 FongMi 的影视TV 2.0.5 之后便可以支持了
     * @param key   关键字/词
     * @param quick 
     * @param pg    页码
     * @return  返回值
     * @throws Exception   异常
     */
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        return "";
    }

    /**
     * 播放信息
     *
     * @param flag 来源于 detailContent() 里面的 vod_play_from
     * @param id   播放页链接或直链 来源于 detailContent 里面的 vod_play_url 里面的值
     * @return 返回值
     */
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        return "";
    }

    /**
     * webview解析时使用 可自定义判断当前加载的 url 是否是视频
     *
     * @param url url链接
     * @return 返回值
     */
    public boolean isVideoFormat(String url) throws Exception {
        return false;
    }

    /**
     * 是否手动检测 webview 中加载的url
     *
     * @return 返回值
     */
    public boolean manualVideoCheck() throws Exception {
        return false;
    }
}
