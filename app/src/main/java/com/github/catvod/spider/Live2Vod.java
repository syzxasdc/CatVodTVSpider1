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
    
    private final String userAgent = "okhttp/3.12.0";

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        try {
            myExtend = extend;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONArray classes = new JSONArray();
            String[] split = myExtend.split("#");
            for (String s : split) {
                int midIndex = s.indexOf("$");
                String typeName = s.substring(0, midIndex);
                String typeId = s.substring(midIndex + 1);
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
        String content =  response.body().string();
        response.close();
        return content;
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            if (!pg.equals("1")) return "";

            int limit = 0;
            JSONArray videos = new JSONArray();
            if (tid.endsWith(".txt")) {
                String content = getWebContent(tid);

                ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuilder vodId = new StringBuilder(); // 初始值为空字符串
                String vodName = "";
                while ((line = bufferedReader.readLine()) != null) {
                    if (line.equals("")) continue; // 空行不管，进入下一次循环
                    if (line.contains(",#genre#")) {
                        // 是直播分类
                        // 将数据存起来
                        if (!vodId.toString().equals("")) {
                            JSONObject vod = new JSONObject()
                                    .put("vod_id", vodId.substring(0, vodId.length() - 1))
                                    .put("vod_name", vodName)
                                    .put("vod_pic", "") // 暂时无图片
                                    .put("vod_remarks", "");
                            videos.put(vod);
                            // 清空 vodId 参数
                            vodId.delete(0, vodId.length());
                            // vodId.setLength(0); // 另一种清空方式
                        }
                        vodName = line.substring(0, line.indexOf(","));
                        limit++;
                        continue;
                    }
                    // 否则就算是一行直播链接代码
                    vodId.append(line).append("#");
                }
                // 循环结束后，最后一次的直播内容需要再写一次
                if (!vodId.toString().equals("")) {
                    JSONObject vod = new JSONObject()
                            .put("vod_id", vodId.substring(0, vodId.length() - 1))
                            .put("vod_name", vodName)
                            .put("vod_pic", "") // 暂时无图片
                            .put("vod_remarks", "");
                    videos.put(vod);
                }
            }

            if (tid.endsWith(".m3u")) {
                String content = getWebContent(tid);
                ByteArrayInputStream is = new ByteArrayInputStream(content.getBytes());
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
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
                        JSONObject vod = new JSONObject()
                                .put("vod_id", vid)
                                .put("vod_name", vodName)
                                .put("vod_pic", pic)
                                .put("vod_remarks", remark);
                        videos.put(vod);
                        limit++;
                    }
                }
            }

            JSONObject result = new JSONObject()
                    .put("page", Integer.parseInt(pg))
                    .put("pagecount", 1) // 共一页，真的没有了。
                    .put("limit", limit) // 每页多少条
                    .put("total", limit) // 总的记录数
                    .put("list", videos);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String detailContent(List<String> ids) {
        try {
            String s = ids.get(0);
            String vod_play_url = s.replace(",", "$");
            String vod_play_from = "选台";  // 线路 / 播放源标题

            String[] split = vod_play_url.split("\\$");
            String name = "电视直播";
            if (split.length == 2) {
                name = split[0];
            }
            JSONObject info = new JSONObject()
                    .put("vod_id", ids.get(0))
                    .put("vod_name", name)
                    .put("vod_pic", "")
                    .put("type_name", "电视直播")
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
