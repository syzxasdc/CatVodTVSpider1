package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
import com.github.catvod.utils.okhttp.OkHttpUtil;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;


/**
 * @author zhixc
 * 新飘花电影网
 */
public class BTPiaoHua extends Spider {

    private final String siteUrl = "https://www.xpiaohua.com";

    protected HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36");
        return headers;
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();

            JSONObject actionMovie = new JSONObject();
            actionMovie.put("type_id", "/dongzuo/");
            actionMovie.put("type_name", "动作片");

            JSONObject comedy = new JSONObject();
            comedy.put("type_id", "/xiju/");
            comedy.put("type_name", "喜剧片");

            JSONObject romanticMovie = new JSONObject();
            romanticMovie.put("type_id", "/aiqing/");
            romanticMovie.put("type_name", "爱情片");

            JSONObject scientificMovie = new JSONObject();
            scientificMovie.put("type_id", "/kehuan/");
            scientificMovie.put("type_name", "科幻片");

            JSONObject featureMovie = new JSONObject();
            featureMovie.put("type_id", "/juqing/");
            featureMovie.put("type_name", "剧情片");

            JSONObject suspenseMovie = new JSONObject();
            suspenseMovie.put("type_id", "/xuanyi/");
            suspenseMovie.put("type_name", "悬疑片");

            JSONObject warMovie = new JSONObject();
            warMovie.put("type_id", "/zhanzheng/");
            warMovie.put("type_name", "战争片");

            JSONObject horrorMovie = new JSONObject();
            horrorMovie.put("type_id", "/kongbu/");
            horrorMovie.put("type_name", "恐怖片");

            JSONObject disasterMovie = new JSONObject();
            disasterMovie.put("type_id", "/zainan/");
            disasterMovie.put("type_name", "灾难片");

            JSONObject anime = new JSONObject();
            anime.put("type_id", "/dongman/");
            anime.put("type_name", "动漫");

            JSONObject documentary = new JSONObject();
            documentary.put("type_id", "/jilu/");
            documentary.put("type_name", "纪录片");

            classes.put(actionMovie);
            classes.put(comedy);
            classes.put(romanticMovie);
            classes.put(scientificMovie);
            classes.put(featureMovie);
            classes.put(suspenseMovie);
            classes.put(warMovie);
            classes.put(horrorMovie);
            classes.put(disasterMovie);
            classes.put(anime);
            classes.put(documentary);

            result.put("class", classes);
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

            // 第一页
            // https://www.xpiaohua.com/column/xiju/
            // 第二页
            // https://www.xpiaohua.com/column/xiju/list_2.html
            String cateUrl = siteUrl + "/column" + tid;
            if (!pg.equals("1")) {
                cateUrl += "/list_" + pg + ".html";
            }

            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")
                    .get()
                    .url(cateUrl)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            byte[] bytes = response.body().bytes();
            response.close();
            String content = new String(bytes, "gb2312");
            Document document = Jsoup.parse(content);
            Elements listElements = document.select("#list").select("dl");
            for (Element item : listElements) {
                String id = item.select("strong").select("a").attr("href");
                String title = item.select("strong").text();
                String cover = item.select("img").attr("src");
                JSONObject vod = new JSONObject();
                vod.put("vod_id", id);
                vod.put("vod_name", title);
                vod.put("vod_pic", cover);
                vod.put("vod_remarks", "");
                jSONArray.put(vod);
            }

            result.put("page", Integer.parseInt(pg));
            result.put("pagecount", Integer.MAX_VALUE);
            result.put("limit", listElements.size());
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
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36")
                    .get()
                    .url(detailUrl)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            byte[] bytes = response.body().bytes();
            response.close();
            String content = new String(bytes, "gb2312");
            Document doc = Jsoup.parse(content);
            Element source = doc.select("table").get(0);

            StringBuilder vod_play_url = new StringBuilder(); // 线路/播放源 里面的各集的播放页面链接
            String vod_play_from = "";  // 线路 / 播放源标题
            vod_play_from = vod_play_from + "magnet" + "$$$";
            Elements aElemntArray = source.select("a");
            for (int j = 0; j < aElemntArray.size(); j++) {
                if (!vod_play_url.toString().equals("")) {
                    // 如果已经有一条磁力链接了，那么退出for循环
                    // 因为多条磁力链接，TVBox 似乎不会识别播放
                    break;
                }
                String href = aElemntArray.get(j).attr("href");
                String[] split = href.split("&dn=");
                String title = split[1];
                if (!href.startsWith("magnet")) continue;
                vod_play_url.append(title).append("$").append(href);
                boolean notLastEpisode = j < aElemntArray.size() - 1; // 不是最后一集的标志
                vod_play_url.append(notLastEpisode ? "#" : "$$$");
            }


            // 影片标题
            String title = doc.select("h3").text();

            // 图片
            String pic = doc.select("#showinfo")
                    .select("img")
                    .attr("src");

            // 影片名称、图片等赋值
            info.put("vod_id", ids.get(0));
            info.put("vod_name", title);
            info.put("vod_pic", pic);

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
            // https://www.xpiaohua.com/plus/search.php?q=我&searchtype.x=0&searchtype.y=0
            // 需要 GBK 编码
            String url = siteUrl + "/plus/search.php?q=" + URLEncoder.encode(key, "GBK") + "&searchtype.x=0&searchtype.y=0";
            String searchPageString = OkHttpUtil.string(url, getHeaders());
            Document searchPage = Jsoup.parse(searchPageString);
            JSONObject result = new JSONObject();
            JSONArray videoInfo = new JSONArray();
            Elements list = searchPage.select("#list").select("dl");
            for (Element item : list) {
                String id = item.select("strong").select("a").attr("href");
                String title = item.select("strong").text();
                String cover = item.select("img").attr("src");
                JSONObject v = new JSONObject();
                v.put("vod_id", id);
                v.put("vod_name", title);
                v.put("vod_pic", cover);
                v.put("vod_remarks", "");
                videoInfo.put(v);
            }
            result.put("list", videoInfo);
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
//            result.put("parse", 1);
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
