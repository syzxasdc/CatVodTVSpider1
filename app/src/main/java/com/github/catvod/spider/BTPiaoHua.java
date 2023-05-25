package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
import com.github.catvod.net.SSLSocketFactoryCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;


/**
 * @author zhixc
 * 新飘花电影网
 */
public class BTPiaoHua extends Spider {

    private final String siteUrl = "https://www.xpiaohua.com";

    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject actionMovie = new JSONObject()
                    .put("type_id", "/dongzuo/")
                    .put("type_name", "动作片");

            JSONObject comedy = new JSONObject()
                    .put("type_id", "/xiju/")
                    .put("type_name", "喜剧片");

            JSONObject romanticMovie = new JSONObject()
                    .put("type_id", "/aiqing/")
                    .put("type_name", "爱情片");

            JSONObject scientificMovie = new JSONObject()
                    .put("type_id", "/kehuan/")
                    .put("type_name", "科幻片");

            JSONObject featureMovie = new JSONObject()
                    .put("type_id", "/juqing/")
                    .put("type_name", "剧情片");

            JSONObject suspenseMovie = new JSONObject()
                    .put("type_id", "/xuanyi/")
                    .put("type_name", "悬疑片");

            JSONObject warMovie = new JSONObject()
                    .put("type_id", "/zhanzheng/")
                    .put("type_name", "战争片");

            JSONObject horrorMovie = new JSONObject()
                    .put("type_id", "/kongbu/")
                    .put("type_name", "恐怖片");

            JSONObject disasterMovie = new JSONObject()
                    .put("type_id", "/zainan/")
                    .put("type_name", "灾难片");

            JSONObject anime = new JSONObject()
                    .put("type_id", "/dongman/")
                    .put("type_name", "动漫");

            JSONObject documentary = new JSONObject()
                    .put("type_id", "/jilu/")
                    .put("type_name", "纪录片");

            JSONArray classes = new JSONArray()
                    .put(actionMovie)
                    .put(comedy)
                    .put(romanticMovie)
                    .put(scientificMovie)
                    .put(featureMovie)
                    .put(suspenseMovie)
                    .put(warMovie)
                    .put(horrorMovie)
                    .put(disasterMovie)
                    .put(anime)
                    .put(documentary);

            JSONObject result = new JSONObject()
                    .put("class", classes);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            // 第一页
            // https://www.xpiaohua.com/column/xiju/
            // 第二页
            // https://www.xpiaohua.com/column/xiju/list_2.html
            String cateUrl = siteUrl + "/column" + tid;
            if (!pg.equals("1")) {
                cateUrl += "/list_" + pg + ".html";
            }

            String content = getWebContent(cateUrl);
            JSONArray videos = new JSONArray();
            Elements listElements = Jsoup.parse(content).select("#list").select("dl");
            for (Element item : listElements) {
                String vid = item.select("strong").select("a").attr("href");
                String name = item.select("strong").text();
                String pic = item.select("img").attr("src");
                JSONObject vod = new JSONObject()
                        .put("vod_id", vid)
                        .put("vod_name", name)
                        .put("vod_pic", pic)
                        .put("vod_remarks", "");
                videos.put(vod);
            }

            JSONObject result = new JSONObject()
                    .put("page", Integer.parseInt(pg))
                    .put("pagecount", Integer.MAX_VALUE)
                    .put("limit", listElements.size())
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
                .addHeader("User-Agent", userAgent)
                .get()
                .url(targetUrl)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(new SSLSocketFactoryCompat(), SSLSocketFactoryCompat.trustAllCert)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.body() == null) return "";
        byte[] bytes = response.body().bytes();
        response.close();
        return new String(bytes, "gb2312");
    }

    @Override
    public String detailContent(List<String> ids) {
        try {
            String detailUrl = ids.get(0);
            String content = getWebContent(detailUrl);
            Document doc = Jsoup.parse(content);
            Element source = doc.select("table").get(0);

            StringBuilder vod_play_url = new StringBuilder(); // 线路/播放源 里面的各集的播放页面链接
            String vod_play_from = "";  // 线路 / 播放源标题
            vod_play_from = vod_play_from + "magnet" + "$$$";
            Elements aList = source.select("a");
            for (int j = 0; j < aList.size(); j++) {
                if (!vod_play_url.toString().equals("")) {
                    // 如果已经有一条磁力链接了，那么退出for循环
                    // 因为多条磁力链接，TVBox 似乎不会识别播放
                    break;
                }
                String href = aList.get(j).attr("href");
                String[] split = href.split("&dn=");
                String title = split[1];
                if (!href.startsWith("magnet")) continue;
                vod_play_url.append(title).append("$").append(href);
                boolean notLastEpisode = j < aList.size() - 1; // 不是最后一集的标志
                vod_play_url.append(notLastEpisode ? "#" : "$$$");
            }

            // 影片标题
            String title = doc.select("h3").text();

            // 图片
            String pic = doc.select("#showinfo")
                    .select("img")
                    .attr("src");

            // 影片名称、图片等赋值
            JSONObject vod = new JSONObject()
                    .put("vod_id", ids.get(0))
                    .put("vod_name", title)
                    .put("vod_pic", pic)
                    .put("vod_play_from", vod_play_from)
                    .put("vod_play_url", vod_play_url.toString());

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
            // https://www.xpiaohua.com/plus/search.php?q=我&searchtype.x=0&searchtype.y=0
            // 需要 GBK 编码
            String searchUrl = siteUrl + "/plus/search.php?q=" + URLEncoder.encode(key, "GBK") + "&searchtype.x=0&searchtype.y=0";
            String pageSource = getWebContent(searchUrl);
            Document searchPage = Jsoup.parse(pageSource);
            JSONObject result = new JSONObject();
            JSONArray videos = new JSONArray();
            Elements list = searchPage.select("#list").select("dl");
            for (Element item : list) {
                String vid = item.select("strong").select("a").attr("href");
                String name = item.select("strong").text();
                String pic = item.select("img").attr("src");
                JSONObject vod = new JSONObject()
                        .put("vod_id", vid)
                        .put("vod_name", name)
                        .put("vod_pic", pic)
                        .put("vod_remarks", "");
                videos.put(vod);
            }
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
            JSONObject result = new JSONObject()
                    .put("parse", 0)
                    .put("header", "")
                    .put("playUrl", "")
                    .put("url", id);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
