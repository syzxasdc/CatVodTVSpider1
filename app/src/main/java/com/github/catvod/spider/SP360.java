package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
import com.github.catvod.net.SSLSocketFactoryCompat;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

/**
 * @author zhixc
 * 360
 */
public class SP360 extends Spider {

    private final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:102.0) Gecko/20100101 Firefox/102.0";

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject movie = new JSONObject()
                    .put("type_id", "1")
                    .put("type_name", "电影");

            JSONObject teleplay = new JSONObject()
                    .put("type_id", "2")
                    .put("type_name", "电视剧");

            JSONObject varietyType = new JSONObject()
                    .put("type_id", "3")
                    .put("type_name", "综艺");

            JSONObject anime = new JSONObject()
                    .put("type_id", "4")
                    .put("type_name", "动漫");

            JSONArray classes = new JSONArray()
                    .put(movie)
                    .put(teleplay)
                    .put(anime)
                    .put(varietyType);

            // filter 二级筛选 start
            String f = "{\"1\": [{\"key\": \"class\", \"name\": \"类型\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"喜剧\", \"v\": \"喜剧\"}, {\"n\": \"爱情\", \"v\": \"爱情\"}, {\"n\": \"动作\", \"v\": \"动作\"}, {\"n\": \"恐怖\", \"v\": \"恐怖\"}, {\"n\": \"科幻\", \"v\": \"科幻\"}, {\"n\": \"剧情\", \"v\": \"剧情\"}, {\"n\": \"犯罪\", \"v\": \"犯罪\"}, {\"n\": \"奇幻\", \"v\": \"奇幻\"}, {\"n\": \"战争\", \"v\": \"战争\"}, {\"n\": \"悬疑\", \"v\": \"悬疑\"}, {\"n\": \"动画\", \"v\": \"动画\"}, {\"n\": \"文艺\", \"v\": \"文艺\"}, {\"n\": \"记录\", \"v\": \"记录\"}, {\"n\": \"传记\", \"v\": \"传记\"}, {\"n\": \"歌舞\", \"v\": \"歌舞\"}, {\"n\": \"古装\", \"v\": \"古装\"}, {\"n\": \"历史\", \"v\": \"历史\"}, {\"n\": \"惊悚\", \"v\": \"惊悚\"}, {\"n\": \"伦理\", \"v\": \"伦理\"}, {\"n\": \"其他\", \"v\": \"其他\"}]}, {\"key\": \"year\", \"name\": \"年代\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2007\", \"v\": \"2007\"}, {\"n\": \"更早\", \"v\": \"更早\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"内地\", \"v\": \"大陆\"}, {\"n\": \"中国香港\", \"v\": \"香港\"}, {\"n\": \"中国台湾\", \"v\": \"台湾\"}, {\"n\": \"泰国\", \"v\": \"泰国\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"韩国\", \"v\": \"韩国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"法国\", \"v\": \"法国\"}, {\"n\": \"英国\", \"v\": \"英国\"}, {\"n\": \"德国\", \"v\": \"德国\"}, {\"n\": \"印度\", \"v\": \"印度\"}, {\"n\": \"其他\", \"v\": \"其他\"}]}, {\"key\": \"actor\", \"name\": \"明星\", \"value\": [{\"n\": \"全部明星\", \"v\": \"\"}, {\"n\": \"成龙\", \"v\": \"成龙\"}, {\"n\": \"周星驰\", \"v\": \"周星驰\"}, {\"n\": \"李连杰\", \"v\": \"李连杰\"}, {\"n\": \"林正英\", \"v\": \"林正英\"}, {\"n\": \"吴京\", \"v\": \"吴京\"}, {\"n\": \"徐峥\", \"v\": \"徐峥\"}, {\"n\": \"黄渤\", \"v\": \"黄渤\"}, {\"n\": \"王宝强\", \"v\": \"王宝强\"}, {\"n\": \"李小龙\", \"v\": \"李小龙\"}, {\"n\": \"张国荣\", \"v\": \"张国荣\"}, {\"n\": \"洪金宝\", \"v\": \"洪金宝\"}, {\"n\": \"姜文\", \"v\": \"姜文\"}, {\"n\": \"沈腾\", \"v\": \"沈腾\"}, {\"n\": \"邓超\", \"v\": \"邓超\"}, {\"n\": \"巩俐\", \"v\": \"巩俐\"}, {\"n\": \"马丽\", \"v\": \"马丽\"}, {\"n\": \"闫妮\", \"v\": \"闫妮\"}, {\"n\": \"周冬雨\", \"v\": \"周冬雨\"}, {\"n\": \"刘昊然\", \"v\": \"刘昊然\"}, {\"n\": \"汤唯\", \"v\": \"汤唯\"}, {\"n\": \"舒淇\", \"v\": \"舒淇\"}, {\"n\": \"白百何\", \"v\": \"白百何\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"最近热映\", \"v\": \"rankhot\"}, {\"n\": \"最近上映\", \"v\": \"ranklatest\"}, {\"n\": \"最受好评\", \"v\": \"rankpoint\"}]}], \"2\": [{\"key\": \"class\", \"name\": \"类型\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"言情\", \"v\": \"言情\"}, {\"n\": \"剧情\", \"v\": \"剧情\"}, {\"n\": \"伦理\", \"v\": \"伦理\"}, {\"n\": \"喜剧\", \"v\": \"喜剧\"}, {\"n\": \"悬疑\", \"v\": \"悬疑\"}, {\"n\": \"都市\", \"v\": \"都市\"}, {\"n\": \"偶像\", \"v\": \"偶像\"}, {\"n\": \"古装\", \"v\": \"古装\"}, {\"n\": \"军事\", \"v\": \"军事\"}, {\"n\": \"警匪\", \"v\": \"警匪\"}, {\"n\": \"历史\", \"v\": \"历史\"}, {\"n\": \"励志\", \"v\": \"励志\"}, {\"n\": \"神话\", \"v\": \"神话\"}, {\"n\": \"谍战\", \"v\": \"谍战\"}, {\"n\": \"青春\", \"v\": \"青春\"}, {\"n\": \"家庭\", \"v\": \"家庭\"}, {\"n\": \"动作\", \"v\": \"动作\"}, {\"n\": \"情景\", \"v\": \"情景\"}, {\"n\": \"武侠\", \"v\": \"武侠\"}, {\"n\": \"科幻\", \"v\": \"科幻\"}, {\"n\": \"其他\", \"v\": \"其他\"}]}, {\"key\": \"year\", \"name\": \"年代\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2007\", \"v\": \"2007\"}, {\"n\": \"更早\", \"v\": \"更早\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"内地\", \"v\": \"内地\"}, {\"n\": \"中国香港\", \"v\": \"香港\"}, {\"n\": \"中国台湾\", \"v\": \"台湾\"}, {\"n\": \"泰国\", \"v\": \"泰国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"韩国\", \"v\": \"韩国\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"英国\", \"v\": \"英国\"}, {\"n\": \"新加坡\", \"v\": \"新加坡\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"最近热映\", \"v\": \"rankhot\"}, {\"n\": \"最近上映\", \"v\": \"ranklatest\"}, {\"n\": \"最受好评\", \"v\": \"rankpoint\"}]}], \"3\": [{\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"中国大陆\", \"v\": \"中国大陆\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"中国香港\", \"v\": \"中国香港\"}, {\"n\": \"中国台湾\", \"v\": \"中国台湾\"}, {\"n\": \"韩国\", \"v\": \"韩国\"}, {\"n\": \"欧洲\", \"v\": \"欧洲\"}, {\"n\": \"其他\", \"v\": \"泰国\"}]}], \"4\": [{\"key\": \"year\", \"name\": \"年代\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2007\", \"v\": \"2007\"}, {\"n\": \"2006\", \"v\": \"2006\"}, {\"n\": \"2005\", \"v\": \"2005\"}, {\"n\": \"2004\", \"v\": \"2004\"}, {\"n\": \"更早\", \"v\": \"更早\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"内地\", \"v\": \"大陆\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"日本\", \"v\": \"日本\"}]}]}";
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
            HashMap<String, String> ext = new HashMap<>();
            if (extend != null && extend.size() > 0) {
                ext.putAll(extend);
            }
            String by = ext.get("by") == null ? "rankhot" : ext.get("by");
            String classType = ext.get("class") == null ? "" : ext.get("class");
            String year = ext.get("year") == null ? "" : ext.get("year");
            String area = ext.get("area") == null ? "" : ext.get("area");
            String actor = ext.get("actor") == null ? "" : ext.get("actor");
            StringBuilder sb = new StringBuilder()
                    .append("rank=").append(by)
                    .append("&cat=").append(URLEncoder.encode(classType))
                    .append("&year=").append(year)
                    .append("&area=").append(URLEncoder.encode(area))
                    .append("&act=").append(URLEncoder.encode(actor));

            String detailClassId = "";
            String categoryClassId = "";
            switch (tid) {
                case "1": // 电影
                    categoryClassId = "dianying";
                    detailClassId = "m";
                    break;
                case "2": // 电视剧
                    categoryClassId = "dianshi";
                    detailClassId = "tv";
                    break;
                case "3": // 综艺
                    categoryClassId = "zongyi";
                    detailClassId = "va";
                    break;
                case "4": // 动漫
                    categoryClassId = "dongman";
                    detailClassId = "ct";
            }
            
            String cateUrl = "https://api.web.360kan.com/v1/filter/list?catid=" + tid + "&" + sb + "&size=35&callback=";
            String referer = "https://www.360kan.com/" + categoryClassId + "/list?" + sb;
            if (!pg.equals("1")) {
                // 第二页开始
                cateUrl = "https://api.web.360kan.com/v1/filter/list?catid=" + tid + "&" + sb + "&size=35&pageno=" + pg + "&callback=";
                referer = "https://www.360kan.com/dianying/list?" + sb + "&pageno=2";
            }
            String content = getContent(cateUrl, referer);
            JSONArray videos = new JSONArray();
            JSONArray items = new JSONObject(content).getJSONObject("data").getJSONArray("movies");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.getJSONObject(i);
                String id = item.optString("id");
                String vid = "https://api.web.360kan.com/v1/detail?cat=" + tid + "&id=" + id + "&callback=";
                String detailReferer = "https://www.360kan.com/" + detailClassId + "/" + id + ".html";
                JSONObject detailObj = new JSONObject().put("detailUrl", vid).put("detailReferer", detailReferer);
                String name = item.optString("title");
                String pic = "http:" + item.optString("cdncover");
                String remark = item.optString("pubdate");

                JSONObject vod = new JSONObject()
                        .put("vod_id", detailObj.toString())
                        .put("vod_name", name)
                        .put("vod_pic", pic)
                        .put("vod_remarks", remark);
                videos.put(vod);
            }

            int total = new JSONObject(content).getJSONObject("data").optInt("total");
            int count = total % 35 == 0 ? (total / 35) : (total / 35 + 1);

            JSONObject result = new JSONObject()
                    .put("page", Integer.parseInt(pg)) // 当前第几页
                    .put("pagecount", count) // 共有多少页
                    .put("limit", 35) // 每页共有多少条数据
                    .put("total", total) // 总记录数
                    .put("list", videos);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getContent(String targetUrl, String referer) throws IOException {
        Request request = new Request.Builder()
                .url(targetUrl)
                .get()
                .addHeader("User-Agent", userAgent)
                .addHeader("referer", referer)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient()
                .newBuilder()
                .sslSocketFactory(new SSLSocketFactoryCompat(), SSLSocketFactoryCompat.trustAllCert)
                .build();
        Response response = okHttpClient.newCall(request).execute();
        String content = response.body().string();
        response.close();
        return content;
    }

    @Override
    public String detailContent(List<String> ids) {
        try {
            JSONObject detailObj = new JSONObject(ids.get(0));
            String detailUrl = detailObj.optString("detailUrl");
            String detailReferer = detailObj.optString("detailReferer");
            String detailResult = getContent(detailUrl, detailReferer);
            JSONObject data = new JSONObject(detailResult).getJSONObject("data");

            StringBuilder vodPlayUrl = new StringBuilder(); // 线路/播放源 里面的各集的播放页面链接
            StringBuilder vodPlayFrom = new StringBuilder();  // 线路 / 播放源标题
            // 影片标题
            String name = data.optString("title");

            JSONObject playLinksDetail = data.optJSONObject("playlinksdetail");
            JSONArray playLinkSites = data.optJSONArray("playlink_sites");
            if (data.has("allepidetail")) {
                // 有很多集
                // 需要针对一些还没有查询出来的数据做查询，获取其他站源的数据
                JSONObject allEpisodeDetail = data.optJSONObject("allepidetail");
                for (int i = 0; i < playLinkSites.length(); i++) {
                    String site = playLinkSites.get(i) + "";
                    if (allEpisodeDetail.has(site)) {
                        // 有这个站的数据
                        setEpisodes(vodPlayUrl, vodPlayFrom, allEpisodeDetail, site);
                        continue;
                    }
                    // 没有这个站点，发请求查询这个站点的数据
                    int endIndex = detailUrl.indexOf("&callback=");
                    String substring = detailUrl.substring(0, endIndex);
                    String detailURL2 = substring + "&site=" + site + "&callback=";
                    String detailResult2 = getContent(detailURL2, detailReferer);
                    JSONObject data2 = new JSONObject(detailResult2).getJSONObject("data");
                    JSONObject allEpisodeDetail2 = data2.optJSONObject("allepidetail");
                    setEpisodes(vodPlayUrl, vodPlayFrom, allEpisodeDetail2, site);
                }
            } else {
                // 电影的默认处理
                for (int i = 0; i < playLinkSites.length(); i++) {
                    String site = playLinkSites.get(i) + "";
                    vodPlayFrom.append(site).append("$$$");
                    JSONObject item = playLinksDetail.getJSONObject(site);
                    String defaultUrl = item.optString("default_url");
                    vodPlayUrl.append(name).append("$").append(defaultUrl).append("$$$");
                }
            }

            // 图片
            String pic = data.optString("cdncover");
            String typeName = data.optJSONArray("moviecategory").toString();
            String year = data.optString("pubdate");
            String area = data.optJSONArray("area").toString();
            String actor = data.optJSONArray("actor").toString();
            String director = data.optJSONArray("director").toString();
            String description = data.optString("description");


            JSONObject info = new JSONObject()
                    .put("vod_id", ids.get(0))
                    .put("vod_name", name)
                    .put("vod_pic", pic)
                    .put("type_name", typeName) // 影片类型 选填
                    .put("vod_year", year) // 年份 选填
                    .put("vod_area", area) // 地区 选填
                    .put("vod_remarks", "") // 备注 选填
                    .put("vod_actor", actor) // 主演 选填
                    .put("vod_director", director) // 导演 选填
                    .put("vod_content", description) // 简介 选填
                    .put("vod_play_from", vodPlayFrom.toString())
                    .put("vod_play_url", vodPlayUrl.toString());

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

    private static void setEpisodes(StringBuilder vodPlayUrl, StringBuilder vodPlayFrom, JSONObject allEpisodeDetail, String site) {
        vodPlayFrom.append(site).append("$$$");
        JSONArray episodes = allEpisodeDetail.optJSONArray(site);
        for (int i = 0; i < episodes.length(); i++) {
            JSONObject item = episodes.optJSONObject(i);
            String episodeName = item.optString("playlink_num");
            String episodeURL = item.optString("url");
            vodPlayUrl.append(episodeName).append("$").append(episodeURL);
            boolean notLastEpisode = i < episodes.length() - 1;
            vodPlayUrl.append(notLastEpisode ? "#" : "$$$");
        }
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String keyword = URLEncoder.encode(key);
            String searchURL = "https://api.so.360kan.com/index?force_v=1&kw=" + keyword + "&from=&pageno=1&v_ap=1&tab=all";
            String referer = "https://so.360kan.com/?kw=" + keyword;
            String searchResult = getContent(searchURL, referer);
            JSONArray videos = new JSONArray();
            JSONArray rows = new JSONObject(searchResult)
                    .optJSONObject("data")
                    .optJSONObject("longData")
                    .optJSONArray("rows");
            for (int i = 0; i < rows.length(); i++) {
                JSONObject item = rows.getJSONObject(i);
                String id = item.optString("en_id");
                String tid = item.optString("cat_id");
                String vid = "https://api.web.360kan.com/v1/detail?cat=" + tid + "&id=" + id + "&callback=";
                String detailReferer = item.optString("url");
                JSONObject detailObj = new JSONObject().put("detailUrl", vid).put("detailReferer", detailReferer);
                String name = item.optString("titleTxt");
                String pic = item.optString("cover");
                String remark = item.optString("cat_name");

                JSONObject vod = new JSONObject()
                        .put("vod_id", detailObj.toString())
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

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            HashMap<String, String> header = new HashMap<>();
            header.put("user-agent", userAgent);
            JSONObject result = new JSONObject()
                    .put("parse", 1)
                    .put("jx", 1)
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
