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
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhixc
 * 6V电影网（新版页面）
 */
public class SixV extends Spider {

    private String siteURL;

    private final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/39.0.2171.71 Safari/537.36";

    private Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", userAgent);
        header.put("Referer", siteURL + "/");
        return header;
    }

    private Map<String, String> getDetailHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", userAgent);
        return header;
    }

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        if (extend.endsWith("/")) {
            extend = extend.substring(0, extend.lastIndexOf("/"));
        }
        siteURL = extend;
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject comedyMovie = new JSONObject()
                    .put("type_id", "xijupian")
                    .put("type_name", "喜剧片");

            JSONObject actMovie = new JSONObject()
                    .put("type_id", "dongzuopian")
                    .put("type_name", "动作片");

            JSONObject loveFilm = new JSONObject()
                    .put("type_id", "aiqingpian")
                    .put("type_name", "爱情片");

            JSONObject scientificMovie = new JSONObject()
                    .put("type_id", "kehuanpian")
                    .put("type_name", "科幻片");

            JSONObject horribleMovie = new JSONObject()
                    .put("type_id", "kongbupian")
                    .put("type_name", "恐怖片");

            JSONObject plotMovie = new JSONObject()
                    .put("type_id", "juqingpian")
                    .put("type_name", "剧情片");

            JSONObject warMovie = new JSONObject()
                    .put("type_id", "zhanzhengpian")
                    .put("type_name", "战争片");

            JSONObject documentaryMovie = new JSONObject()
                    .put("type_id", "jilupian")
                    .put("type_name", "纪录片");

            JSONObject animeMovie = new JSONObject()
                    .put("type_id", "donghuapian")
                    .put("type_name", "动画片");

            JSONObject domesticTV = new JSONObject()
                    .put("type_id", "dianshiju/guoju")
                    .put("type_name", "国剧");

            JSONObject japaneseAndKoreanTV = new JSONObject()
                    .put("type_id", "dianshiju/rihanju")
                    .put("type_name", "日韩剧");

            JSONObject europeanAndAmericanTV = new JSONObject()
                    .put("type_id", "dianshiju/oumeiju")
                    .put("type_name", "欧美剧");

            JSONArray classes = new JSONArray()
                    .put(comedyMovie)
                    .put(actMovie)
                    .put(loveFilm)
                    .put(scientificMovie)
                    .put(horribleMovie)
                    .put(plotMovie)
                    .put(warMovie)
                    .put(documentaryMovie)
                    .put(animeMovie)
                    .put(domesticTV)
                    .put(japaneseAndKoreanTV)
                    .put(europeanAndAmericanTV);
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
            String cateURL = siteURL + "/" + tid;
            if (!pg.equals("1")) {
                cateURL += "/index_" + pg + ".html";
            }
            String html = getWebContent(cateURL, getHeader());
            Elements items = Jsoup.parse(html).select("#post_container .post_hover");
            JSONArray videos = new JSONArray();
            for (Element item : items) {
                Element li = item.select("[class=zoom]").get(0);
                String vid = li.attr("href");
                String name = li.attr("title");
                String pic = li.select("img").attr("src");
                String remark = item.select("[rel=category tag]").text();

                JSONObject vod = new JSONObject()
                        .put("vod_id", vid)
                        .put("vod_name", name)
                        .put("vod_pic", pic)
                        .put("vod_remarks", remark);
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

    private String getWebContent(String targetURL, Map<String, String> header) throws Exception {
        Request.Builder builder = new Request.Builder();
        for (String key : header.keySet()) {
            String value = header.get(key);
            builder.addHeader(key, value);
        }
        Request request = builder
                .url(targetURL)
                .get()
                .build();
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(new SSLSocketFactoryCompat(), SSLSocketFactoryCompat.trustAllCert)
                .hostnameVerifier((hostname, session) -> true)
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
            String vid = ids.get(0);
            String detailURL = siteURL + vid;
            String html = getWebContent(detailURL, getDetailHeader());
            Document doc = Jsoup.parse(html);
            Elements sourceList = doc.select("#post_content");

            String vod_play_from = "magnet";
            String vod_play_url = "";
            for (Element source : sourceList) {
                Elements aList = source.select("table").select("a");
                for (Element a : aList) {
                    // 如果已经有一条磁力链接了，那么退出for循环
                    // 因为多条磁力链接，TVBox 似乎不会识别播放
                    if (!vod_play_url.equals("")) break;
                    String episodeURL = a.attr("href");
                    String episodeName = a.text();
                    if (!episodeURL.startsWith("magnet")) continue;
                    vod_play_url = episodeName + "$" + episodeURL;
                }
            }

            String partHTML = doc.select(".context").html();
            String name = doc.select(".article_container > h1").text();
            String pic = doc.select("#post_content img").attr("src");
            String typeName = getStrByRegex(Pattern.compile("◎类　　别　(.*?)<br>"), partHTML);
            String year = getStrByRegex(Pattern.compile("◎年　　代　(.*?)<br>"), partHTML);
            String area = getStrByRegex(Pattern.compile("◎产　　地　(.*?)<br>"), partHTML);
            String remark = getStrByRegex(Pattern.compile("◎上映日期　(.*?)<br>"), partHTML);
            String actor = getActorOrDirector(Pattern.compile("◎演　　员　(.*?)</p>"), partHTML);
            if (actor.equals("")) {
                actor = getActorOrDirector(Pattern.compile("◎主　　演　(.*?)</p>"), partHTML);
            }
            String director = getActorOrDirector(Pattern.compile("◎导　　演　(.*?)<br>"), partHTML);
            String description = getDescription(Pattern.compile("◎简　　介(.*?)<hr>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL), partHTML);

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

    private String getStrByRegex(Pattern pattern, String str) {
        try {
            Matcher matcher = pattern.matcher(str);
            if (matcher.find()) {
                return matcher.group(1).trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getActorOrDirector(Pattern pattern, String str) {
        return getStrByRegex(pattern, str)
                .replaceAll("<br>", "")
                .replaceAll("&nbsp;", "")
                .replaceAll("&amp;", "")
                .replaceAll("middot;", "・")
                .replaceAll("　　　　　 ", ",");
    }

    private String getDescription(Pattern pattern, String str) {
        return getStrByRegex(pattern, str)
                .replaceAll("</?[^>]+>", "")  // 去掉 html 标签
                .replaceAll("\n", "") // 去掉换行符
                .replaceAll("　　　　", "")
                .replaceAll("&amp;", "")
                .replaceAll("middot;", "・")
                .replaceAll("ldquo;", "【")
                .replaceAll("rdquo;", "】");
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String searchURL = siteURL + "/e/search/index.php";
            RequestBody formBody = new FormBody.Builder()
                    .add("show", "title")
                    .add("tempid", "1")
                    .add("tbname", "article")
                    .add("mid", "1")
                    .add("dopost", "search")
                    .add("submit", "")
                    .addEncoded("keyboard", key)
                    .build();
            Request request = new Request.Builder()
                    .url(searchURL)
                    .addHeader("User-Agent", userAgent)
                    .addHeader("Origin", siteURL)
                    .addHeader("Referer", siteURL + "/")
                    .post(formBody)
                    .build();
            OkHttpClient okHttpClient = new OkHttpClient()
                    .newBuilder()
                    .sslSocketFactory(new SSLSocketFactoryCompat(), SSLSocketFactoryCompat.trustAllCert)
                    .hostnameVerifier((hostname, session) -> true)
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            if (response.body() == null) return "";
            String html = response.body().string();
            response.close(); // 关闭响应资源
            Elements items = Jsoup.parse(html).select("#post_container [class=zoom]");
            JSONArray videos = new JSONArray();
            for (Element item : items) {
                String vid = item.attr("href");
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