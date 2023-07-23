package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author zhixc
 * 新飘花电影网
 */
public class BTPiaoHua extends Spider {

    private final String siteURL = "https://www.xpiaohua.com";

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
            String cateURL = siteURL + "/column" + tid;
            if (!pg.equals("1")) {
                cateURL += "/list_" + pg + ".html";
            }
            String html = getWebContent(cateURL);
            JSONArray videos = new JSONArray();
            Elements items = Jsoup.parse(html).select("#list dl");
            for (Element item : items) {
                String vid = item.select("strong a").attr("href");
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
                    .put("pagecount", 999)
                    .put("list", videos);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getWebContent(String targetURL) throws IOException {
        Request request = new Request.Builder()
                .addHeader("User-Agent", userAgent)
                .get()
                .url(targetURL)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(new SSLSocketFactoryCompat(), SSLSocketFactoryCompat.trustAllCert)
                .hostnameVerifier((hostname, session) -> true)
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
            String detailURL = ids.get(0);
            String html = getWebContent(detailURL);
            Document doc = Jsoup.parse(html);
            String vod_play_url = "";
            String vod_play_from = "magnet";
            Elements aList = doc.select("table").get(0).select("a");
            for (Element element : aList) {
                if (!vod_play_url.equals("")) {
                    // 如果已经有一条磁力链接了，那么退出for循环
                    // 因为多条磁力链接，TVBox 似乎不会识别播放
                    break;
                }
                String episodeURL = element.attr("href");
                String[] split = episodeURL.split("&dn=");
                String episodeName = split[1];
                if (!episodeURL.startsWith("magnet")) continue;
                vod_play_url = episodeName + "$" + episodeURL;
            }

            String name = doc.select("h3").text();
            String pic = doc.select("#showinfo img").attr("src");
            String typeName = getStrByRegex(Pattern.compile("◎类　　别　(.*?)<br"), html);
            String year = getStrByRegex(Pattern.compile("◎年　　代　(.*?)<br"), html);
            String area = getStrByRegex(Pattern.compile("◎产　　地　(.*?)<br"), html);
            String remark = getStrByRegex(Pattern.compile("◎上映日期　(.*?)<br"), html);
            String actor = getActorStr(html);
            String director = getDirectorStr(Pattern.compile("◎导　　演　(.*?)<br"), html);
            String description = getDescription(Pattern.compile("◎简　　介(.*?)◎", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), html);
            JSONObject vodInfo = new JSONObject()
                    .put("vod_id", ids.get(0))
                    .put("vod_name", name)
                    .put("vod_pic", pic)
                    .put("type_name", typeName)
                    .put("vod_year", year)
                    .put("vod_area", area)
                    .put("vod_remarks", remark)
                    .put("vod_actor", actor)
                    .put("vod_director", director)
                    .put("vod_content", description);
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

    private String getDescription(Pattern pattern, String html) {
        return getStrByRegex(pattern, html).replaceAll("</?[^>]+>", "");
    }

    private String getDirectorStr(Pattern pattern, String html) {
        return getStrByRegex(pattern, html)
                .replaceAll("&middot;", "·");
    }

    private String getActorStr(String html) {
        Pattern p1 = Pattern.compile("◎演　　员　(.*?)◎");
        Pattern p2 = Pattern.compile("◎主　　演　(.*?)◎");
        String actor =  getStrByRegex(p1, html).equals("") ? getStrByRegex(p2, html) : "";
        return actor.replaceAll("</?[^>]+>", "")
                .replaceAll("　　　　　", "")
                .replaceAll("&middot;", "·");
    }

    private String getStrByRegex(Pattern pattern, String html){
        try {
            Matcher matcher = pattern.matcher(html);
            if (matcher.find()) return matcher.group(1);
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String searchURL = siteURL + "/plus/search.php?q=" + URLEncoder.encode(key, "GBK") + "&searchtype.x=0&searchtype.y=0";
            String html = getWebContent(searchURL);
            JSONArray videos = new JSONArray();
            Elements items = Jsoup.parse(html).select("#list dl");
            for (Element item : items) {
                String vid = item.select("strong a").attr("href");
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