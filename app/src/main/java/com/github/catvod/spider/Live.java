package com.github.catvod.spider;

import android.content.Context;
import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;

/**
 * @author zhixc
 * 直播
 */
public class Live extends Spider {

    private static String myExtend;

    // 请求头部设置
    protected HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "okhttp/3.12.0");
        return headers;
    }

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        try {
            myExtend = extend;
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();

            String[] split = myExtend.split("#");
            for (String s : split) {
                int midIndex = s.indexOf("$");
                String typeName = s.substring(0, midIndex);
                String typeId = s.substring(midIndex + 1);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type_id", typeId);
                jsonObject.put("type_name", typeName);
                classes.put(jsonObject);
            }

            result.put("class", classes);

            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            JSONObject result = new JSONObject();
            JSONArray jSONArray = new JSONArray();

            if (!pg.equals("1")) return "";

            int limit = 0;
            if (tid.endsWith(".txt")) {
                String content = OkHttpUtil.string(tid, getHeaders());

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
                            JSONObject vod = new JSONObject();
                            vod.put("vod_id", vodId.substring(0, vodId.length() - 1));
                            vod.put("vod_name", vodName);
                            vod.put("vod_pic", ""); // 暂时无图片
                            vod.put("vod_remarks", "");
                            jSONArray.put(vod);
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
                    JSONObject vod = new JSONObject();
                    vod.put("vod_id", vodId.substring(0, vodId.length() - 1));
                    vod.put("vod_name", vodName);
                    vod.put("vod_pic", ""); // 暂时无图片
                    vod.put("vod_remarks", "");
                    jSONArray.put(vod);
                }
            }

            if (tid.endsWith(".m3u")) {
                String content = OkHttpUtil.string(tid, getHeaders());
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
                        JSONObject vod = new JSONObject();
                        vod.put("vod_id", url);
                        vod.put("vod_name", vodName);
                        vod.put("vod_pic", pic);
                        vod.put("vod_remarks", remark);
                        jSONArray.put(vod);
                        limit++;
                    }
                }
            }

            result.put("page", Integer.parseInt(pg));
//            result.put("pagecount", Integer.MAX_VALUE);
            result.put("pagecount", 1); // 共一页，真的没有了。
            result.put("limit", limit); // 每页多少条
//            result.put("total", Integer.MAX_VALUE);
            result.put("total", limit); // 总的记录数
            result.put("list", jSONArray);
            return result.toString();

        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }


    @Override
    public String detailContent(List<String> ids) {
        try {
            JSONObject result = new JSONObject();
            JSONObject info = new JSONObject();
            JSONArray list_info = new JSONArray();

            String s = ids.get(0);
            String vod_play_url = s.replace(",", "$");
            String vod_play_from = "选台";  // 线路 / 播放源标题

            // 影片名称、图片等赋值
            info.put("vod_id", ids.get(0));
            info.put("vod_name", "直播");
            info.put("vod_pic", "");

            info.put("vod_play_from", vod_play_from);
            info.put("vod_play_url", vod_play_url);

            list_info.put(info);
            result.put("list", list_info);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            JSONObject result = new JSONObject();
            result.put("parse", 0);
            result.put("header", getHeaders());
            result.put("playUrl", "");
            result.put("url", id);
            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }
}
