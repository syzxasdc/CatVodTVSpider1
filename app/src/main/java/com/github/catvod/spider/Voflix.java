package com.github.catvod.spider;

import android.content.Context;
import android.text.TextUtils;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhixc
 * Vodflix
 */
public class Voflix extends Spider {

    private String siteUrl;

    private final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36";

    /**
     * 爬虫代码初始化
     *
     * @param context 上下文对象
     * @param extend  配置文件的 ext 参数
     */
    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        // 域名经常性发生变化，通过外部配置文件传入，可以方便修改
        if (extend.endsWith("/")) {
            extend = extend.substring(0, extend.lastIndexOf("/"));
        }
        siteUrl = extend;
    }

    /**
     * 首页内容初始化，主要要完成分类id和值、二级筛选等，
     * 也可以在这个方法里面完成首页推荐视频获取
     *
     * @param filter 不用管这个参数
     * @return 返回字符串
     */
    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject movie = new JSONObject()
                    .put("type_id", "1")
                    .put("type_name", "电影");

            JSONObject teleplay = new JSONObject()
                    .put("type_id", "2")
                    .put("type_name", "剧集");

            JSONObject anime = new JSONObject()
                    .put("type_id", "4")
                    .put("type_name", "动漫");

            JSONObject variety = new JSONObject()
                    .put("type_id", "3")
                    .put("type_name", "综艺");

            JSONArray classes = new JSONArray()
                    .put(movie)
                    .put(teleplay)
                    .put(anime)
                    .put(variety);

            // filter 二级筛选 start
            String f = "{\"1\": [{\"key\": \"cateId\", \"name\": \"类型\", \"value\": [{\"n\": \"全部\", \"v\": \"1\"}, {\"n\": \"动作\", \"v\": \"6\"}, {\"n\": \"喜剧\", \"v\": \"7\"}, {\"n\": \"爱情\", \"v\": \"8\"}, {\"n\": \"科幻\", \"v\": \"9\"}, {\"n\": \"恐怖\", \"v\": \"10\"}, {\"n\": \"剧情\", \"v\": \"11\"}, {\"n\": \"战争\", \"v\": \"12\"}, {\"n\": \"动画\", \"v\": \"23\"}]}, {\"key\": \"class\", \"name\": \"剧情\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"喜剧\", \"v\": \"喜剧\"}, {\"n\": \"爱情\", \"v\": \"爱情\"}, {\"n\": \"恐怖\", \"v\": \"恐怖\"}, {\"n\": \"动作\", \"v\": \"动作\"}, {\"n\": \"科幻\", \"v\": \"科幻\"}, {\"n\": \"剧情\", \"v\": \"剧情\"}, {\"n\": \"战争\", \"v\": \"战争\"}, {\"n\": \"警匪\", \"v\": \"警匪\"}, {\"n\": \"犯罪\", \"v\": \"犯罪\"}, {\"n\": \"动画\", \"v\": \"动画\"}, {\"n\": \"奇幻\", \"v\": \"奇幻\"}, {\"n\": \"武侠\", \"v\": \"武侠\"}, {\"n\": \"冒险\", \"v\": \"冒险\"}, {\"n\": \"枪战\", \"v\": \"枪战\"}, {\"n\": \"恐怖\", \"v\": \"恐怖\"}, {\"n\": \"悬疑\", \"v\": \"悬疑\"}, {\"n\": \"惊悚\", \"v\": \"惊悚\"}, {\"n\": \"经典\", \"v\": \"经典\"}, {\"n\": \"青春\", \"v\": \"青春\"}, {\"n\": \"文艺\", \"v\": \"文艺\"}, {\"n\": \"微电影\", \"v\": \"微电影\"}, {\"n\": \"古装\", \"v\": \"古装\"}, {\"n\": \"历史\", \"v\": \"历史\"}, {\"n\": \"运动\", \"v\": \"运动\"}, {\"n\": \"农村\", \"v\": \"农村\"}, {\"n\": \"儿童\", \"v\": \"儿童\"}, {\"n\": \"网络电影\", \"v\": \"网络电影\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"中国大陆\", \"v\": \"中国大陆\"}, {\"n\": \"中国香港\", \"v\": \"中国香港\"}, {\"n\": \"中国台湾\", \"v\": \"中国台湾\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"法国\", \"v\": \"法国\"}, {\"n\": \"英国\", \"v\": \"英国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"韩国\", \"v\": \"韩国\"}, {\"n\": \"德国\", \"v\": \"德国\"}, {\"n\": \"泰国\", \"v\": \"泰国\"}, {\"n\": \"印度\", \"v\": \"印度\"}, {\"n\": \"意大利\", \"v\": \"意大利\"}, {\"n\": \"西班牙\", \"v\": \"西班牙\"}, {\"n\": \"加拿大\", \"v\": \"加拿大\"}, {\"n\": \"其他\", \"v\": \"其他\"}]}, {\"key\": \"year\", \"name\": \"年份\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"时间\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}], \"2\": [{\"key\": \"cateId\", \"name\": \"类型\", \"value\": [{\"n\": \"全部\", \"v\": \"2\"}, {\"n\": \"国产剧\", \"v\": \"13\"}, {\"n\": \"港台剧\", \"v\": \"14\"}, {\"n\": \"日韩剧\", \"v\": \"15\"}, {\"n\": \"欧美剧\", \"v\": \"16\"}, {\"n\": \"纪录片\", \"v\": \"21\"}, {\"n\": \"泰国剧\", \"v\": \"24\"}]}, {\"key\": \"class\", \"name\": \"剧情\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"古装\", \"v\": \"古装\"}, {\"n\": \"战争\", \"v\": \"战争\"}, {\"n\": \"青春偶像\", \"v\": \"青春偶像\"}, {\"n\": \"喜剧\", \"v\": \"喜剧\"}, {\"n\": \"家庭\", \"v\": \"家庭\"}, {\"n\": \"犯罪\", \"v\": \"犯罪\"}, {\"n\": \"动作\", \"v\": \"动作\"}, {\"n\": \"奇幻\", \"v\": \"奇幻\"}, {\"n\": \"剧情\", \"v\": \"剧情\"}, {\"n\": \"历史\", \"v\": \"历史\"}, {\"n\": \"经典\", \"v\": \"经典\"}, {\"n\": \"乡村\", \"v\": \"乡村\"}, {\"n\": \"情景\", \"v\": \"情景\"}, {\"n\": \"商战\", \"v\": \"商战\"}, {\"n\": \"网剧\", \"v\": \"网剧\"}, {\"n\": \"其他\", \"v\": \"其他\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"中国大陆\", \"v\": \"中国大陆\"}, {\"n\": \"中国台湾\", \"v\": \"中国台湾\"}, {\"n\": \"中国香港\", \"v\": \"中国香港\"}, {\"n\": \"韩国\", \"v\": \"韩国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"泰国\", \"v\": \"泰国\"}, {\"n\": \"英国\", \"v\": \"英国\"}, {\"n\": \"新加坡\", \"v\": \"新加坡\"}, {\"n\": \"其他\", \"v\": \"其他\"}]}, {\"key\": \"year\", \"name\": \"年份\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2006\", \"v\": \"2006\"}, {\"n\": \"2005\", \"v\": \"2005\"}, {\"n\": \"2004\", \"v\": \"2004\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"时间\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}], \"4\": [{\"key\": \"class\", \"name\": \"剧情\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"情感\", \"v\": \"情感\"}, {\"n\": \"科幻\", \"v\": \"科幻\"}, {\"n\": \"热血\", \"v\": \"热血\"}, {\"n\": \"推理\", \"v\": \"推理\"}, {\"n\": \"搞笑\", \"v\": \"搞笑\"}, {\"n\": \"冒险\", \"v\": \"冒险\"}, {\"n\": \"萝莉\", \"v\": \"萝莉\"}, {\"n\": \"校园\", \"v\": \"校园\"}, {\"n\": \"动作\", \"v\": \"动作\"}, {\"n\": \"机战\", \"v\": \"机战\"}, {\"n\": \"运动\", \"v\": \"运动\"}, {\"n\": \"战争\", \"v\": \"战争\"}, {\"n\": \"少年\", \"v\": \"少年\"}, {\"n\": \"少女\", \"v\": \"少女\"}, {\"n\": \"社会\", \"v\": \"社会\"}, {\"n\": \"原创\", \"v\": \"原创\"}, {\"n\": \"亲子\", \"v\": \"亲子\"}, {\"n\": \"益智\", \"v\": \"益智\"}, {\"n\": \"励志\", \"v\": \"励志\"}, {\"n\": \"其他\", \"v\": \"其他\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"中国\", \"v\": \"中国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"欧美\", \"v\": \"欧美\"}, {\"n\": \"其他\", \"v\": \"其他\"}]}, {\"key\": \"year\", \"name\": \"年份\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2007\", \"v\": \"2007\"}, {\"n\": \"2006\", \"v\": \"2006\"}, {\"n\": \"2005\", \"v\": \"2005\"}, {\"n\": \"2004\", \"v\": \"2004\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"时间\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}], \"3\": [{\"key\": \"class\", \"name\": \"剧情\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"选秀\", \"v\": \"选秀\"}, {\"n\": \"情感\", \"v\": \"情感\"}, {\"n\": \"访谈\", \"v\": \"访谈\"}, {\"n\": \"播报\", \"v\": \"播报\"}, {\"n\": \"旅游\", \"v\": \"旅游\"}, {\"n\": \"音乐\", \"v\": \"音乐\"}, {\"n\": \"美食\", \"v\": \"美食\"}, {\"n\": \"纪实\", \"v\": \"纪实\"}, {\"n\": \"曲艺\", \"v\": \"曲艺\"}, {\"n\": \"生活\", \"v\": \"生活\"}, {\"n\": \"游戏互动\", \"v\": \"游戏互动\"}, {\"n\": \"财经\", \"v\": \"财经\"}, {\"n\": \"求职\", \"v\": \"求职\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"内地\", \"v\": \"内地\"}, {\"n\": \"港台\", \"v\": \"港台\"}, {\"n\": \"日韩\", \"v\": \"日韩\"}, {\"n\": \"欧美\", \"v\": \"欧美\"}]}, {\"key\": \"year\", \"name\": \"年份\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2007\", \"v\": \"2007\"}, {\"n\": \"2006\", \"v\": \"2006\"}, {\"n\": \"2005\", \"v\": \"2005\"}, {\"n\": \"2004\", \"v\": \"2004\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"时间\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}]}";
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

    /**
     * 获取首页推荐视频
     *
     * @return 返回字符串
     */
    @Override
    public String homeVideoContent() {
        try {
            String hotURL = siteUrl + "/label/new.html";
            String html = getWebContent(hotURL);
            Elements lis = Jsoup.parse(html)
                    .select("[class=module-items module-poster-items]")
                    .get(0)
                    .select(".module-item");
            JSONArray videos = new JSONArray();
            for (Element li : lis) {
                String vid = siteUrl + li.attr("href");
                String name = li.attr("title");
                String pic = li.select("img").attr("data-original");
                String remark = li.select("[class=module-item-note]").text();
                JSONObject vod = new JSONObject()
                        .put("vod_id", vid)
                        .put("vod_name", name)
                        .put("vod_pic", pic)
                        .put("vod_remarks", remark);
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

    /**
     * 分类内容方法
     *
     * @param tid    影片分类id值，来自 homeContent 里面的 type_id 值
     * @param pg     第几页
     * @param filter 不用管这个参数
     * @param extend 用户已经选择的二级筛选数据
     * @return 返回字符串
     */
    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) {
        try {
            // 筛选处理 start
            HashMap<String, String> ext = new HashMap<>();
            if (extend != null && extend.size() > 0) {
                ext.putAll(extend);
            }
            String cateId = ext.get("cateId") == null ? tid : ext.get("cateId");
            String area = ext.get("area") == null ? "" : ext.get("area");
            String year = ext.get("year") == null ? "" : ext.get("year");
            String by = ext.get("by") == null ? "" : ext.get("by");
            String classType = ext.get("class") == null ? "" : ext.get("class");
            // 筛选处理 end

            // 电影第二页
            // https://www.voflix.com/show/1--------2---.html
//            String cateUrl = siteUrl + String.format("/show/%s--------%s---.html", tid, pg);
            String cateUrl = siteUrl + String.format("/show/%s-%s-%s-%s-----%s---%s.html", cateId, area, by, classType, pg, year);
            String content = getWebContent(cateUrl);
            Elements lis = Jsoup.parse(content)
                    .select(".module-items")
                    .select(".module-item");
            JSONArray videos = new JSONArray();
            for (Element li : lis) {
                String vid = siteUrl + li.attr("href");
                String name = li.attr("title");
                String pic = li.select("img").attr("data-original");
                String remark = li.select("[class=module-item-note]").text();
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

    /**
     * 根据传入的URL获取网页源码
     *
     * @param targetUrl 请求的 URL
     * @return 返回网页源码
     * @throws IOException 抛出 IO 异常
     */
    private String getWebContent(String targetUrl) throws IOException {
        Request request = new Request.Builder()
                .url(targetUrl)
                .get()
                .addHeader("User-Agent", userAgent)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(new SSLSocketFactoryCompat(), SSLSocketFactoryCompat.trustAllCert) // 取消证书认证
                .build();
        Response response = okHttpClient.newCall(request).execute();
        if (response.body() == null) return "";
        String content = response.body().string();
        response.close();
        return content;
    }

    /**
     * 影片详情方法
     *
     * @param ids ids.get(0) 来源于分类方法或搜索方法的 vod_id
     * @return 返回字符串
     */
    @Override
    public String detailContent(List<String> ids) {
        try {
            String detailUrl = ids.get(0);
            String html = getWebContent(detailUrl);
            Document doc = Jsoup.parse(html);
            Elements sources = doc.select(".module-play-list");
            Elements circuits = doc.select(".module-tab-item");
            Map<String, String> playMap = new LinkedHashMap<>();
            for (int i = 0; i < sources.size(); i++) {
                String spanText = circuits.get(i).select("span").text();
                String smallText = circuits.get(i).select("small").text();
                String playFromText = spanText + "(共" + smallText + "集)";
                Elements aElements = sources.get(i).select("a");
                List<String> vodItems = new ArrayList<>();
                for (Element a : aElements) {
                    String href = siteUrl + a.attr("href");
                    String text = a.select("span").text();
                    vodItems.add(text + "$" + href);
                }
                if (vodItems.size() > 0) {
                    playMap.put(playFromText, TextUtils.join("#", vodItems));
                }
            }

            // 影片标题
            String title = doc.select(".module-info-heading > h1").text();
            // 图片
            String pic = doc.select("[class=ls-is-cached lazy lazyload]")
                    .attr("data-original");
            Elements elements = doc.select(".module-info-heading").select(".module-info-tag-link");
            String classifyName = ""; // 影片类型
            String year = ""; // 影片年代
            String area = ""; // 地区
            if (elements.size() >= 3) {
                classifyName = elements.get(2).select("a").text();
                year = elements.get(0).select("a").text();
                area = elements.get(1).select("a").text();
            }
            String remark = getStrByRegex("备注：(.*?)</div>", html);
            String actor = getStrByRegex("主演：(.*?)</div>", html);
            String director = getStrByRegex("导演：(.*?)</div>", html);
            String description = doc.select(".module-info-introduction-content").text();

            // 线路 / 播放源标题拼接的字符串
            String vod_play_from = TextUtils.join("$$$", playMap.keySet());
            // 线路 / 播放源 里面的各集的播放页面链接拼接的字符串
            String vod_play_url = TextUtils.join("$$$", playMap.values());

            JSONObject vodInfo = new JSONObject()
                    .put("vod_id", ids.get(0)) // 必填
                    .put("vod_name", title)
                    .put("vod_pic", pic)
                    .put("type_name", classifyName) // 选填
                    .put("vod_year", year) // 选填
                    .put("vod_area", area) // 选填
                    .put("vod_remarks", remark) // 选填
                    .put("vod_actor", actor) // 选填
                    .put("vod_director", director) // 选填
                    .put("vod_content", description) // 选填
                    .put("vod_play_from", vod_play_from) // 必须有，否则播放可能存在问题
                    .put("vod_play_url", vod_play_url); // 必须有，否则播放可能存在问题

            JSONArray listInfo = new JSONArray()
                    .put(vodInfo);
            JSONObject result = new JSONObject()
                    .put("list", listInfo);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 正则获取字符串
     * @param regexStr  正则字符串
     * @param htmlStr   网页源码
     * @return  返回正则获取的字符串结果
     */
    private String getStrByRegex(String regexStr, String htmlStr) {
        if (regexStr == null) {
            return "";
        }
        try {
            Pattern pattern = Pattern.compile(regexStr, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(htmlStr);
            if (matcher.find()) {
                return matcher.group(1)
                        .trim()
                        .replaceAll("</?[^>]+>", "")
                        .replace("\n", "")
                        .replace("\t", "")
                        .trim();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 搜索
     *
     * @param key   关键字/词
     * @param quick 不要使用这个参数
     * @return 返回值
     */
    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String url = siteUrl + "/index.php/ajax/suggest?mid=1&wd=" + URLEncoder.encode(key) + "&limit=20";
            String content = getWebContent(url);
            JSONArray list = new JSONObject(content).getJSONArray("list");
            JSONArray videos = new JSONArray();
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                String vid = siteUrl + "/detail/" + item.getInt("id") + ".html";
                String name = item.getString("name");
                String pic = item.getString("pic");
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
            HashMap<String, String> header = new HashMap<>();
            header.put("User-Agent", userAgent);
            JSONObject result = new JSONObject()
                    .put("parse", 1) // 1 表示需要嗅探， 0表示可以直连
                    .put("header", header.toString())
                    .put("playUrl", "")
                    .put("url", id);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}