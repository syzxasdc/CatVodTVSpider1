package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;
import com.github.catvod.bean.Live;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.SSLSocketFactoryCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Collator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhixc
 * 电视直播(爬虫版)
 */
public class Live2Vod extends Spider {

    private String myExtend;

    private final String userAgent = "okhttp/3.12.11";

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        myExtend = extend;
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONArray classes = new JSONArray();
            // 如果是远程配置文件的话，尝试发起请求查询
            if (!myExtend.contains("$")) {
                String html = getWebContent(myExtend);
                JSONArray jsonArray = new JSONArray(html);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject liveObj = jsonArray.getJSONObject(i);
                    String name = liveObj.optString("name");
                    String url = liveObj.optString("url");
                    String group = liveObj.optString("group");
                    String diyPic = "";
                    if (url.contains("&&&")) {
                        String[] split = url.split("&&&");
                        url = split[0];
                        diyPic = split.length > 1 ? split[1] : "";
                    }
                    JSONObject typeIdObj = new JSONObject()
                            .put("url", url)
                            .put("pic", diyPic)
                            .put("group", group);
                    JSONObject obj = new JSONObject()
                            .put("type_id", typeIdObj.toString())
                            .put("type_name", name);
                    classes.put(obj);
                }
                JSONObject result = new JSONObject()
                        .put("class", classes);
                return result.toString();
            }

            String sub = myExtend;
            String diyPic = "";
            if (myExtend.contains("&&&")) {
                String[] split = myExtend.split("&&&");
                sub = split[0];
                diyPic = split.length > 1 ? split[1] : "";
            }
            String[] split = sub.split("#");
            for (String s : split) {
                String[] split2 = s.split("\\$");
                String name = split2[0];
                String url = split2[1];
                String pic = url.contains(".txt") ? diyPic : "";
                JSONObject typeIdObj = new JSONObject()
                        .put("url", url)
                        .put("pic", pic);
                JSONObject obj = new JSONObject()
                        .put("type_id", typeIdObj.toString())
                        .put("type_name", name);
                classes.put(obj);
            }
            JSONObject result = new JSONObject()
                    .put("class", classes);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getWebContent(String targetUrl) throws IOException {
        Request request = new Request.Builder()
                .url(targetUrl)
                .get()
                .addHeader("User-Agent", userAgent)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(new SSLSocketFactoryCompat(), SSLSocketFactoryCompat.trustAllCert)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.body() == null) return "";
        String content = response.body().string();
        response.close();
        return content;
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            if (!pg.equals("1")) return "";
            JSONObject typeIdObj = new JSONObject(tid);
            String URL = typeIdObj.optString("url");
            String diyPic = typeIdObj.optString("pic");
            String group = typeIdObj.optString("group");
            String content = getWebContent(URL);
            ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            JSONArray videos = new JSONArray();
            if (content.contains("#genre#")) {
                // 是 txt 格式的直播，调用 txt 直播处理方法
                setTxtLive(bufferedReader, videos, diyPic);
            }
            if (content.contains("#EXTM3U")) {
                // 是 m3u 格式的直播，调用 m3u 直播处理方法
                if (group.equals("1")) {
                    setM3ULiveGroup(bufferedReader, videos, diyPic); // 要分组
                } else {
                    setM3ULive(bufferedReader, videos, diyPic);
                }
            }
            // 倒序关闭流
            bufferedReader.close();
            is.close();
            JSONObject result = new JSONObject()
                    .put("pagecount", 1)
                    .put("list", videos);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 字符串截取方法
     *
     * @param prefixMark 前缀字符串
     * @param suffixMark 后缀字符串
     * @param originStr  原始字符串
     * @return 返回的是 从原始字符串中截取 前缀字符串 和 后缀字符串中间的字符串
     */
    private String subStr(String prefixMark, String suffixMark, String originStr) {
        try {
            int i = originStr.indexOf(prefixMark);
            int j = originStr.indexOf(suffixMark, i);
            int len = prefixMark.length();
            return originStr.substring(i + len, j);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("-------------->  截取产生异常了");
        }
        return "";
    }

    // ######## 处理 m3u 格式的直播
    private void setM3ULive(BufferedReader bufferedReader, JSONArray videos, String diyPic) {
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals("")) continue;
                if (line.contains("#EXTM3U")) continue;
                if (line.contains("#EXTINF")) {
                    String name = subStr("tvg-name=\"", "\" tvg-logo=", line).trim();
                    String pic = subStr("tvg-logo=\"", "\" group-title=", line);
                    if (!diyPic.equals("")) {
                        pic = diyPic; // 如果有自定义图片，那么以自定义图片为主。
                    }
                    String remark = subStr("group-title=\"", "\",", line);
                    if (name.equals("")) {
                        name = line.substring(line.lastIndexOf(",") + 1);
                    }
                    // 再读取一行，就是对应的 url 链接了
                    String url = bufferedReader.readLine().trim();
                    String sub = name + "$" + url;
                    JSONObject videoInfoObj = new JSONObject()
                            .put("sub", sub)
                            .put("pic", pic);
                    JSONObject vod = new JSONObject()
                            .put("vod_id", videoInfoObj.toString())
                            .put("vod_name", name)
                            .put("vod_pic", pic)
                            .put("vod_remarks", remark);
                    videos.put(vod);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ######## 处理 m3u 格式的直播，并进行分组
    private void setM3ULiveGroup(BufferedReader bufferedReader, JSONArray videos, String diyPic) {
        try {
            List<Live> liveList = new ArrayList<>();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals("")) continue;
                if (line.contains("#EXTM3U")) continue;
                if (line.contains("#EXTINF")) {
                    String name = subStr("tvg-name=\"", "\" tvg-logo=", line);
                    String groupTitle = subStr("group-title=\"", "\",", line);
                    if (name.equals("")) {
                        name = line.substring(line.lastIndexOf(",") + 1);
                    }
                    // 再读取一行，就是对应的 url 链接了
                    String url = bufferedReader.readLine().trim();
                    liveList.add(new Live(name, url, groupTitle));
                }
            }
            // 文件流读取完毕后，进行分组
            Map<String, List<Live>> collect = liveList.stream()
                    .collect(Collectors.groupingBy(Live::getGroup, LinkedHashMap::new, Collectors.toList()));

            collect.forEach((group, lives) -> {
                List<String> vodItems = new ArrayList<>();
                for (Live it : lives) {
                    vodItems.add(it.getName() + "$" + it.getUrl());
                }
                String sub = TextUtils.join("#", vodItems);
                try {
                    JSONObject videoInfoObj = new JSONObject()
                            .put("sub", sub)
                            .put("pic", diyPic);
                    JSONObject vod = new JSONObject()
                            .put("vod_id", videoInfoObj.toString())
                            .put("vod_name", group)
                            .put("vod_pic", diyPic)
                            .put("vod_remarks", "");
                    videos.put(vod);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ######## 处理txt 格式的直播
    private void setTxtLive(BufferedReader bufferedReader, JSONArray videos, String diyPic) {
        try {
            Map<String, String> map = new LinkedHashMap<>();
            List<String> vodItems = new ArrayList<>();
            String group = "";
            int count = 0; // 计数
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals("")) continue; // 空行不管，进入下一次循环
                if (line.contains(",#genre#")) {
                    // 是直播分类
                    count++;
                    if (count > 1) {
                        // count 大于 1 时，可以将直播数据存储起来
                        List<String> sortedItems = getSortedItems(vodItems);
                        map.put(group, TextUtils.join("#", sortedItems));
                        vodItems.clear(); // 重置 vodItems
                    }
                    group = line.substring(0, line.indexOf(","));
                    continue;
                }
                // 到了这里 line 是一行直播链接代码
                vodItems.add(line.replace(",", "$"));
            }
            // 将最后一次的数据存到 map 集合里面
            if (vodItems.size() > 0) {
                List<String> sortedItems = getSortedItems(vodItems);
                map.put(group, TextUtils.join("#", sortedItems));
            }
            for (String key : map.keySet()) {
                String value = map.get(key);
                JSONObject videoInfoObj = new JSONObject()
                        .put("sub", value)
                        .put("pic", diyPic);
                JSONObject vod = new JSONObject()
                        .put("vod_id", videoInfoObj.toString())
                        .put("vod_name", key)
                        .put("vod_pic", diyPic)
                        .put("vod_remarks", "");
                videos.put(vod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<String> getSortedItems(List<String> vodItems) {
        return vodItems.stream().sorted((o1, o2) -> {
                    Collator collator = Collator.getInstance(Locale.CHINA);
                    return collator.compare(o1, o2);
                })
                .collect(Collectors.toList());
    }

    @Override
    public String detailContent(List<String> ids) {
        try {
            JSONObject videoInfoObj = new JSONObject(ids.get(0));
            String vod_play_url = videoInfoObj.getString("sub");
            String pic = videoInfoObj.getString("pic");
            String vod_play_from = "选台";  // 线路 / 播放源标题

            String description = "";
            String[] split = vod_play_url.split("\\$");
            String name = "电视直播";
            if (split.length == 2) {
                name = split[0];
                description = "播放地址：" + split[1];
            }
            JSONObject vodInfo = new JSONObject()
                    .put("vod_id", ids.get(0))
                    .put("vod_name", name) // 影片名称
                    .put("vod_pic", pic) // 图片/影片封面
                    .put("type_name", "电视直播")// 年份
                    .put("vod_year", "") // 年份
                    .put("vod_area", "") // 地区
                    .put("vod_remarks", "") // 备注
                    .put("vod_actor", "") // 主演
                    .put("vod_director", "") // 导演
                    .put("vod_content", description); // 简介

            if (vod_play_url.length() > 0) {
                vodInfo.put("vod_play_from", vod_play_from)
                        .put("vod_play_url", vod_play_url);
            }

            JSONArray jsonArray = new JSONArray()
                    .put(vodInfo);
            JSONObject result = new JSONObject()
                    .put("list", jsonArray);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            HashMap<String, String> header = new HashMap<>();
            header.put("User-Agent", userAgent);
            JSONObject result = new JSONObject()
                    .put("parse", 0) // 直播链接都是可以直接播放的，所以直连就行
                    .put("header", header)
                    .put("playUrl", "")
                    .put("url", id);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
