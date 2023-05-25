package com.github.catvod.spider;

import android.content.Context;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.SSLSocketFactoryCompat;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * @author zhixc
 * 6V电影网（新版页面）
 */
public class SixV extends Spider {

    private String siteUrl;

    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        try {
            if (extend.endsWith("/")) {
                extend = extend.substring(0, extend.lastIndexOf("/"));
            }
            siteUrl = extend;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject movie = new JSONObject()
                    .put("type_id", "my_tid_movie")
                    .put("type_name", "电影");

            JSONArray classes = new JSONArray()
                    .put(movie);
            // filter 二级筛选 start
            String f = "{\"my_tid_movie\": [{\"key\": \"class\", \"name\": \"类型\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"喜剧片\", \"v\": \"xijupian\"}, {\"n\": \"动作片\", \"v\": \"dongzuopian\"}, {\"n\": \"爱情片\", \"v\": \"aiqingpian\"}, {\"n\": \"科幻片\", \"v\": \"kehuanpian\"}, {\"n\": \"恐怖片\", \"v\": \"kongbupian\"}, {\"n\": \"剧情片\", \"v\": \"juqingpian\"}, {\"n\": \"战争片\", \"v\": \"zhanzhengpian\"}, {\"n\": \"纪录片\", \"v\": \"jilupian\"}, {\"n\": \"动画片\", \"v\": \"donghuapian\"}]}]}";
            JSONObject filterConfig = new JSONObject(f);
            // filter 二级筛选 end
            JSONObject result = new JSONObject()
                    .put("class", classes)
                    .put("filters", filterConfig);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
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
            String content = getWebContent(cateUrl);

            Elements lis = Jsoup.parse(content)
                    .select("#post_container")
                    .select("[class=zoom]");
            JSONArray videos = new JSONArray();
            for (Element li : lis) {
                String vid = siteUrl + li.attr("href");
                String name = li.attr("title");
                String pic = li.select("img").attr("src");
                String remark = "";

                JSONObject vod = new JSONObject()
                        .put("vod_id", vid)
                        .put("vod_name", name)
                        .put("vod_pic", pic)
                        .put("vod_remarks", remark);
                videos.put(vod);
            }
            JSONObject result = new JSONObject()
                    .put("page", Integer.parseInt(pg))
                    .put("pagecount", Integer.MAX_VALUE)
                    .put("limit", lis.size())
                    .put("total", Integer.MAX_VALUE)
                    .put("list", videos);
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
    public String detailContent(List<String> ids) {
        try {
            String detailUrl = ids.get(0);
            String content = getWebContent(detailUrl);
            Elements sourceList = Jsoup.parse(content)
                    .select("#post_content");

            // 磁力链接只能选择一条，多了，TVBox就无法识别播放了。
            // 另外磁力链接的播放，即使返回到首页，不在播放页面了，磁力依旧在后台继续下载
            // 以上这些问题估计只能考 TVBox 的作者去解决了。

            StringBuilder vodPlayUrl = new StringBuilder(); // 线路/播放源 里面的各集的播放页面链接
            StringBuilder vodPlayFrom = new StringBuilder();  // 线路 / 播放源标题
            for (Element source : sourceList) {
                vodPlayFrom.append("magnet").append("$$$");
                Elements aList = source.select("table").select("a");
                for (int j = 0; j < aList.size(); j++) {
                    // 如果已经有一条磁力链接了，那么退出for循环
                    // 因为多条磁力链接，TVBox 似乎不会识别播放
                    if (!vodPlayUrl.toString().equals("")) break;

                    String href = aList.get(j).attr("href");
                    String text = aList.get(j).text();
                    if (!href.startsWith("magnet")) continue;
                    vodPlayUrl.append(text).append("$").append(href);
                    boolean notLastEpisode = j < aList.size() - 1;
                    vodPlayUrl.append(notLastEpisode ? "#" : "$$$");
                }
            }

            // 影片标题
            String title = Jsoup.parse(content)
                    .select(".article_container")
                    .get(0)
                    .getElementsByTag("h1")
                    .text();

            // 图片
            String pic = Jsoup.parse(content)
                    .select("#post_content")
                    .select("img")
                    .attr("src");
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

            // 影片名称、图片等赋值
            JSONObject vod = new JSONObject()
                    .put("vod_id", ids.get(0))
                    .put("vod_name", title)
                    .put("vod_pic", pic)
                    .put("type_name", typeName)
                    .put("vod_year", year)
                    .put("vod_area", area)
                    .put("vod_remarks", remark)
                    .put("vod_actor", actor)
                    .put("vod_director", director)
                    .put("vod_content", brief)
                    .put("vod_play_from", vodPlayFrom.toString())
                    .put("vod_play_url", vodPlayUrl.toString());

            JSONArray jsonArray = new JSONArray()
                    .put(vod);
            JSONObject result = new JSONObject()
                    .put("list", jsonArray);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String searchUrl = siteUrl + "/e/search/index.php";
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
                    .url(searchUrl)
                    .addHeader("user-agent", userAgent)
                    .post(formBody)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .sslSocketFactory(new SSLSocketFactoryCompat(), SSLSocketFactoryCompat.trustAllCert)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.body() == null) return "";
            String content = response.body().string();
            response.close(); // 关闭响应资源
            Document doc = Jsoup.parse(content);
            Elements list = doc.select("#post_container")
                    .select("[class=zoom]");
            JSONArray videos = new JSONArray();
            for (Element item : list) {
                String vid = siteUrl + item.attr("href");
                String name = item.attr("title").replaceAll("</?[^>]+>", "");
                String pic = item.select("img").attr("src");
                JSONObject vod = new JSONObject()
                        .put("vod_id", vid)
                        .put("vod_name", name)
                        .put("vod_pic", pic)
                        .put("vod_remarks", "");
                videos.put(vod);
            }

            JSONObject result = new JSONObject()
                    .put("list", videos);
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
