package com.github.catvod.spider;

import android.content.Context;
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
import java.util.HashMap;
import java.util.List;

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
                String content = getWebContent(myExtend);
                JSONArray jsonArray = new JSONArray(content);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject liveObj = jsonArray.getJSONObject(i);
                    String name = liveObj.optString("name");
                    String url = liveObj.optString("url");
                    JSONObject obj = new JSONObject()
                            .put("type_id", url)
                            .put("type_name", name);
                    classes.put(obj);
                }
                JSONObject result = new JSONObject()
                        .put("class", classes);
                return result.toString();
            }

            String[] split = myExtend.split("#");
            for (String s : split) {
                int midIndex = s.indexOf("$");
                String typeName = s.substring(0, midIndex);
                String typeId = s.substring(midIndex);
                JSONObject obj = new JSONObject()
                        .put("type_id", typeId)
                        .put("type_name", typeName);
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
            String URL = "";
            String diyPic = "";
            if (!tid.startsWith("$")) {
                URL = tid;
                int midIndex = tid.indexOf("&&&");
                if (midIndex != -1) {
                    URL = tid.substring(0, midIndex);
                    diyPic = tid.substring(midIndex + 3);
                }
            } else {
                URL = tid.substring(1);
            }
            String content = getWebContent(URL);
            ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
            JSONArray videos = new JSONArray();
            if (content.contains("#genre#")) {
                // 是 txt 格式的直播，调用 txt 直播处理方法
                setTxtLive(bufferedReader, videos, diyPic);
            } else if (content.contains("#EXTM3U")) {
                // 是 m3u 格式的直播，调用 m3u 直播处理方法
                setM3ULive(bufferedReader, videos, diyPic);
            }
            JSONObject result = new JSONObject()
                    .put("pagecount", 1)
                    .put("list", videos);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
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
                    String vodName = "";
                    String pic = "";
                    String remark = "";

                    int tvgLogoIndex = line.indexOf("tvg-logo=");
                    int tvgNameIndex = line.indexOf("tvg-name=");
                    int tvgNameLength = "tvg-name=".length();
                    if (tvgLogoIndex != -1 && tvgNameIndex != -1) {
                        String tvgName = line.substring(tvgNameIndex + tvgNameLength, tvgLogoIndex);
                        vodName = tvgName.trim().replaceAll("\"", "");
                    }

                    vodName = vodName.equals("") ? line.substring(line.lastIndexOf(",") + 1) : vodName;

                    int groupTitleIndex = line.indexOf("group-title=");
                    int tvgLogoLength = "tvg-logo=".length();
                    if (groupTitleIndex != -1 && tvgLogoIndex != -1) {
                        String tvgLogo = line.substring(tvgLogoIndex + tvgLogoLength, groupTitleIndex);
                        pic = tvgLogo.trim().replaceAll("\"", "");
                    }

                    int groupTitleLength = "group-title=".length();
                    if (groupTitleIndex != -1) {
                        String groupTitle = line.substring(groupTitleIndex + groupTitleLength);
                        remark = groupTitle.trim().replaceAll("\"", "");
                    }

                    // 再读取一行，就是对应的 url 链接了
                    String url = bufferedReader.readLine();
                    String vid = vodName + "$" + url;
                    pic = !diyPic.equals("") ? diyPic : pic; // 如果有自定义图片，那么以自定义图片为主。
                    JSONObject videoInfoObj = new JSONObject()
                            .put("sub", vid)
                            .put("pic", pic);
                    JSONObject vod = new JSONObject()
                            .put("vod_id", videoInfoObj.toString())
                            .put("vod_name", vodName)
                            .put("vod_pic", pic)
                            .put("vod_remarks", remark);
                    videos.put(vod);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ######## 处理txt 格式的直播
    private void setTxtLive(BufferedReader bufferedReader, JSONArray videos, String diyPic) {
        try {
            String line;
            StringBuilder sb = new StringBuilder(); // 初始值为空字符串
            String vodName = "";
            while ((line = bufferedReader.readLine()) != null) {
                if (line.equals("")) continue; // 空行不管，进入下一次循环
                if (line.contains(",#genre#")) {
                    // 是直播分类
                    // 将数据存起来
                    if (!sb.toString().equals("")) {
                        String substring = sb.substring(0, sb.length() - 1);
                        JSONObject videoInfoObj = new JSONObject()
                                .put("sub", substring)
                                .put("pic", diyPic);
                        JSONObject vod = new JSONObject()
                                .put("vod_id", videoInfoObj.toString())
                                .put("vod_name", vodName)
                                .put("vod_pic", diyPic)
                                .put("vod_remarks", "");
                        videos.put(vod);
                        // 清空 sb 参数
                        sb.delete(0, sb.length());
                        // sb.setLength(0); // 另一种清空方式
                    }
                    vodName = line.substring(0, line.indexOf(","));
                    continue;
                }
                // 否则就算是一行直播链接代码
                sb.append(line).append("#");
            }
            // 循环结束后，最后一次的直播内容需要再写一次
            if (!sb.toString().equals("")) {
                String substring = sb.substring(0, sb.length() - 1);
                JSONObject videoInfoObj = new JSONObject()
                        .put("sub", substring)
                        .put("pic", diyPic);
                JSONObject vod = new JSONObject()
                        .put("vod_id", videoInfoObj.toString())
                        .put("vod_name", vodName)
                        .put("vod_pic", diyPic)
                        .put("vod_remarks", "");
                videos.put(vod);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String detailContent(List<String> ids) {
        try {
            JSONObject videoInfoObj = new JSONObject(ids.get(0));
            String s = videoInfoObj.getString("sub");
            String pic = videoInfoObj.getString("pic");
            String vod_play_url = s.replace(",", "$");
            String vod_play_from = "选台";  // 线路 / 播放源标题

            String description = "";
            String[] split = vod_play_url.split("\\$");
            String name = "电视直播";
            if (split.length == 2) {
                name = split[0];
                description = "播放地址：" + vod_play_url;
            }
            JSONObject info = new JSONObject()
                    .put("vod_id", ids.get(0))
                    .put("vod_name", name) // 影片名称
                    .put("vod_pic", pic) // 图片/影片封面
                    .put("type_name", "电视直播")// 年份
                    .put("vod_year", "") // 年份
                    .put("vod_area", "") // 地区
                    .put("vod_remarks", "") // 备注
                    .put("vod_actor", "") // 主演
                    .put("vod_director", "") // 导演
                    .put("vod_content", description) // 简介
                    .put("vod_play_from", vod_play_from)
                    .put("vod_play_url", vod_play_url);

            JSONArray listInfo = new JSONArray()
                    .put(info);
            JSONObject result = new JSONObject()
                    .put("list", listInfo);
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
