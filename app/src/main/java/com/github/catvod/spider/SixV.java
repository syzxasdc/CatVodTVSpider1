package com.github.catvod.spider;

import android.content.Context;
import com.github.catvod.crawler.Spider;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * @author zhixc
 * 6V电影网（新版页面）
 */
public class SixV extends Spider {

    private String siteUrl;

    protected HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");
        return headers;
    }

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        try {
            if (null != extend && extend.startsWith("http")) {
                if (extend.endsWith("/")) {
                    extend = extend.substring(0, extend.lastIndexOf("/"));
                }
                siteUrl = extend;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();

            JSONObject movie = new JSONObject();
            movie.put("type_id", "my_tid_movie");
            movie.put("type_name", "电影");

            classes.put(movie);
            result.put("class", classes);
            // filter 二级筛选 start
            if (filter) {
                String f = "{\"my_tid_movie\": [{\"key\": \"class\", \"name\": \"类型\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"喜剧片\", \"v\": \"xijupian\"}, {\"n\": \"动作片\", \"v\": \"dongzuopian\"}, {\"n\": \"爱情片\", \"v\": \"aiqingpian\"}, {\"n\": \"科幻片\", \"v\": \"kehuanpian\"}, {\"n\": \"恐怖片\", \"v\": \"kongbupian\"}, {\"n\": \"剧情片\", \"v\": \"juqingpian\"}, {\"n\": \"战争片\", \"v\": \"zhanzhengpian\"}, {\"n\": \"纪录片\", \"v\": \"jilupian\"}, {\"n\": \"动画片\", \"v\": \"donghuapian\"}]}]}";
                JSONObject filterConfig = new JSONObject(f);
                result.put("filters", filterConfig);
            }
            // filter 二级筛选 end
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {

            JSONObject result = new JSONObject();
            JSONArray jSONArray = new JSONArray();

            // 筛选处理 start
            HashMap<String, String> ext = new HashMap<>();
            if (extend != null && extend.size() > 0) {
                ext.putAll(extend);
            }
            String classType = ext.get("class") == null ? "" : ext.get("class");
            // 筛选处理 end

            String cateUrl = siteUrl + "/" + classType;
            if (!pg.equals("1")) {
                cateUrl += "/index_" + pg + ".html";
            }
            String content = OkHttpUtil.string(cateUrl, getHeaders());

            Elements list_el = Jsoup.parse(content)
                    .select("#post_container")
                    .select("[class=zoom]");

            for (int i = 0; i < list_el.size(); i++) {
                JSONObject vod = new JSONObject();
                Element item = list_el.get(i);
                String vid = siteUrl + item.attr("href");
                String name = item.attr("title");
                String pic = item.select("img").attr("src");
                String remark = "";
                vod.put("vod_id", vid);
                vod.put("vod_name", name);
                vod.put("vod_pic", pic);
                vod.put("vod_remarks", remark);
                jSONArray.put(vod);
            }
            result.put("page", Integer.parseInt(pg));
            result.put("pagecount", Integer.MAX_VALUE);
            result.put("limit", list_el.size());
            result.put("total", Integer.MAX_VALUE);
            result.put("list", jSONArray);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String detailContent(List<String> ids) {
        try {
            JSONObject result = new JSONObject();
            JSONObject info = new JSONObject();
            JSONArray list_info = new JSONArray();

            String detailUrl = ids.get(0);
            String content = OkHttpUtil.string(detailUrl, getHeaders());
            Elements sources = Jsoup.parse(content)
                    .select("#post_content");

            // 磁力链接只能选择一条，多了，TVBox就无法识别播放了。
            // 另外磁力链接的播放，即使返回到首页，不在播放页面了，磁力依旧在后台继续下载
            // 以上这些问题估计只能考 TVBox 的作者去解决了。

            StringBuilder vod_play_url = new StringBuilder(); // 线路/播放源 里面的各集的播放页面链接
            String vod_play_from = "";  // 线路 / 播放源标题
            for (int i = 0; i < sources.size(); i++) {
                vod_play_from = vod_play_from + "magnet" + "$$$";

                Elements aElemntArray = sources.get(i).select("table").select("a");
                for (int j = 0; j < aElemntArray.size(); j++) {
                    // 如果已经有一条磁力链接了，那么退出for循环
                    // 因为多条磁力链接，TVBox 似乎不会识别播放
                    if (!vod_play_url.toString().equals("")) break;

                    String href = aElemntArray.get(j).attr("href");
                    String text = aElemntArray.get(j).text();
                    if (!href.startsWith("magnet")) continue;
                    vod_play_url.append(text).append("$").append(href);
                    boolean notLastEpisode = j < aElemntArray.size() - 1;
                    vod_play_url.append(notLastEpisode ? "#" : "$$$");
                }
            }

            // 影片标题
            String title = Jsoup.parse(content)
                    .select(".article_container")
                    .get(0).getElementsByTag("h1").text();

            // 图片
            String pic = Jsoup.parse(content)
                    .select("#post_content")
                    .select("img")
                    .attr("src");

            // 影片名称、图片等赋值
            info.put("vod_id", ids.get(0));
            info.put("vod_name", title);
            info.put("vod_pic", pic);

            // -------------------- 选填部分 start --------------------
            Document document = Jsoup.parse(content);
            List<TextNode> textNodes = document.select("#post_content").get(0).select("p").get(0).textNodes();
            String typeName = "";
            String year = "";
            String area = "";
            String remark = "";
            String actor = "";
            String director = "";
            String brief = "";
            if (textNodes.size() >= 13) {
                typeName = textNodes.get(5).text().substring(6);
                year = textNodes.get(3).text().substring(6);
                area = textNodes.get(4).text().substring(6);
                remark = textNodes.get(9).text().substring(6);
                actor = textNodes.get(13).text().substring(6);
                director = textNodes.get(10).text().substring(6);
            }
            List<TextNode> textNodes2 = document.select("#post_content").get(0).select("p").get(1).textNodes();
            if (textNodes2.size() > 1) {
                brief = textNodes2.get(1).text();
            }

            info.put("type_name", typeName);
            info.put("vod_year", year);
            info.put("vod_area", area);
            info.put("vod_remarks", remark);
            info.put("vod_actor", actor);
            info.put("vod_director", director);
            info.put("vod_content", brief);
            // -------------------- 选填部分 end ---------------------

            info.put("vod_play_from", vod_play_from);
            info.put("vod_play_url", vod_play_url.toString());

            list_info.put(info);
            result.put("list", list_info);

            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String url = siteUrl + "/e/search/index.php";
            JSONArray videos = new JSONArray();

            RequestBody formBody = new FormBody.Builder()
                    .add("show", "title")
                    .add("tempid", "1")
                    .add("tbname", "article")
                    .add("mid", "1")
                    .add("dopost", "search")
                    .add("submit", "")
                    .addEncoded("keyboard", URLEncoder.encode(key, "utf8"))
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .post(formBody)
                    .build();

            OkHttpClient okHttpClient = new OkHttpClient();
            Response response = okHttpClient.newCall(request).execute();
            String content = response.body().string();
            response.close(); // 关闭响应资源
            Document doc = Jsoup.parse(content);
            Elements list_el = doc.select("#post_container")
                    .select("[class=zoom]");
            for (int i = 0; i < list_el.size(); i++) {
                JSONObject vod = new JSONObject();
                Element item = list_el.get(i);
                String vid = siteUrl + item.attr("href");
                String name = item.attr("title").replaceAll("</?[^>]+>", "");
                String pic = item.select("img").attr("src");
                vod.put("vod_id", vid);
                vod.put("vod_name", name);
                vod.put("vod_pic", pic);
                vod.put("vod_remarks", "");
                videos.put(vod);
            }

            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            JSONObject result = new JSONObject();
            result.put("parse", 0);
            result.put("header", "");
            result.put("playUrl", "");
            result.put("url", id);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
