package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
//import com.github.catvod.net.OkHttp;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zhixc
 * 星易影
 */
public class XingYiYing extends Spider {

    private final String siteUrl = "https://www.xingyiying.com";

    private final String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.14; rv:102.0) Gecko/20100101 Firefox/102.0";

    private Map<String, String> getHeader() {
        Map<String, String> header = new HashMap<>();
        header.put("User-Agent", userAgent);
        header.put("Referer", siteUrl + "/");
        return header;
    }

    private Map<String, String> getHeaderForPlay() {
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "*/*");
        header.put("User-Agent", userAgent);
        return header;
    }

    private String req(String url) {
//        return OkHttp.string(url, getHeader());
        return OkHttpUtil.string(url, getHeader());
    }

    private JSONArray parseVodList(String url) throws Exception {
        String html = req(url);
        Elements elements = Jsoup.parse(html).select("[class=v_list] li");
        JSONArray videos = new JSONArray();
        for (Element e : elements) {
            Element item = e.select("a").get(0);
            String vodId = item.attr("href");
            String name = item.attr("title").replaceAll("在线观看", "");
            String pic = item.attr("data-bg");
            String remark = e.select("[class=desc]").text();

            JSONObject vod = new JSONObject();
            vod.put("vod_id", vodId);
            vod.put("vod_name", name);
            vod.put("vod_pic", pic);
            vod.put("vod_remarks", remark);
            videos.put(vod);
        }
        return videos;
    }

    private String find(Pattern pattern, String html) {
        Matcher m = pattern.matcher(html);
        return m.find() ? m.group(1).trim() : "";
    }

    private String parseVodInfo(Element element) {
        StringBuilder sb = new StringBuilder();
        for (Element a : element.select("a")) sb.append(a.text()).append(" / ");
        return sb.toString();
    }

    /*@Override
    public String homeContent(boolean filter) throws Exception {
        JSONArray classes = new JSONArray();
        List<String> typeIds = Arrays.asList("1", "2", "3", "4");
        List<String> typeNames = Arrays.asList("国产动漫", "日本动漫", "欧美动漫", "电影");
        for (int i = 0; i < typeIds.size(); i++) {
            JSONObject c = new JSONObject();
            c.put("type_id", typeIds.get(i));
            c.put("type_name", typeNames.get(i));
            classes.put(c);
        }
        String f = "{\"1\": [{\"name\": \"年份\", \"key\": \"year\", \"value\": [{\"n\": \"全部年份\", \"v\": \"\"}, {\"n\": \"2024\", \"v\": \"2024\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}]}, {\"name\": \"类型\", \"key\": \"class\", \"value\": [{\"n\": \"全部类型\", \"v\": \"\"}, {\"n\": \"奇幻\", \"v\": \"奇幻\"}, {\"n\": \"战斗\", \"v\": \"战斗\"}, {\"n\": \"玄幻\", \"v\": \"玄幻\"}, {\"n\": \"穿越\", \"v\": \"穿越\"}, {\"n\": \"科幻\", \"v\": \"科幻\"}, {\"n\": \"武侠\", \"v\": \"武侠\"}, {\"n\": \"热血\", \"v\": \"热血\"}, {\"n\": \"眈美\", \"v\": \"眈美\"}, {\"n\": \"搞笑\", \"v\": \"搞笑\"}, {\"n\": \"动态漫画\", \"v\": \"动态漫画\"}]}, {\"name\": \"排序\", \"key\": \"by\", \"value\": [{\"n\": \"最新\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}], \"2\": [{\"name\": \"年份\", \"key\": \"year\", \"value\": [{\"n\": \"全部年份\", \"v\": \"\"}, {\"n\": \"2024\", \"v\": \"2024\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}]}, {\"name\": \"类型\", \"key\": \"class\", \"value\": [{\"n\": \"全部类型\", \"v\": \"\"}, {\"n\": \"冒险\", \"v\": \"冒险\"}, {\"n\": \"奇幻\", \"v\": \"奇幻\"}, {\"n\": \"战斗\", \"v\": \"战斗\"}, {\"n\": \"后宫\", \"v\": \"后宫\"}, {\"n\": \"热血\", \"v\": \"热血\"}, {\"n\": \"励志\", \"v\": \"励志\"}, {\"n\": \"搞笑\", \"v\": \"搞笑\"}, {\"n\": \"校园\", \"v\": \"校园\"}, {\"n\": \"机战\", \"v\": \"机战\"}, {\"n\": \"悬疑\", \"v\": \"悬疑\"}, {\"n\": \"治愈\", \"v\": \"治愈\"}, {\"n\": \"百合\", \"v\": \"百合\"}, {\"n\": \"恐怖\", \"v\": \"恐怖\"}, {\"n\": \"泡面番\", \"v\": \"泡面番\"}, {\"n\": \"恋爱\", \"v\": \"恋爱\"}, {\"n\": \"推理\", \"v\": \"推理\"}]}, {\"name\": \"排序\", \"key\": \"by\", \"value\": [{\"n\": \"最新\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}], \"3\": [{\"name\": \"年份\", \"key\": \"year\", \"value\": [{\"n\": \"全部年份\", \"v\": \"\"}, {\"n\": \"2024\", \"v\": \"2024\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}]}, {\"name\": \"类型\", \"key\": \"class\", \"value\": [{\"n\": \"全部类型\", \"v\": \"\"}, {\"n\": \"科幻\", \"v\": \"科幻\"}, {\"n\": \"冒险\", \"v\": \"冒险\"}, {\"n\": \"战斗\", \"v\": \"战斗\"}, {\"n\": \"百合\", \"v\": \"百合\"}, {\"n\": \"奇幻\", \"v\": \"奇幻\"}, {\"n\": \"热血\", \"v\": \"热血\"}, {\"n\": \"搞笑\", \"v\": \"搞笑\"}]}, {\"name\": \"排序\", \"key\": \"by\", \"value\": [{\"n\": \"最新\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}], \"4\": [{\"name\": \"年份\", \"key\": \"year\", \"value\": [{\"n\": \"全部年份\", \"v\": \"\"}, {\"n\": \"2024\", \"v\": \"2024\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}]}, {\"name\": \"类型\", \"key\": \"class\", \"value\": [{\"n\": \"全部类型\", \"v\": \"\"}, {\"n\": \"搞笑\", \"v\": \"搞笑\"}, {\"n\": \"奇幻\", \"v\": \"奇幻\"}, {\"n\": \"治愈\", \"v\": \"治愈\"}, {\"n\": \"科幻\", \"v\": \"科幻\"}, {\"n\": \"喜剧\", \"v\": \"喜剧\"}, {\"n\": \"冒险\", \"v\": \"冒险\"}, {\"n\": \"动作\", \"v\": \"动作\"}, {\"n\": \"爱情\", \"v\": \"爱情\"}]}, {\"name\": \"排序\", \"key\": \"by\", \"value\": [{\"n\": \"最新\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}]}";
        JSONObject filterConfig = new JSONObject(f);
        JSONObject result = new JSONObject();
        result.put("class", classes);
        result.put("filters", filterConfig);
        return result.toString();
    }*/

    /*@Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        // 筛选处理 start
        String year = extend.get("year") == null ? "" : extend.get("year");
        String by = extend.get("by") == null ? "" : extend.get("by");
        String classType = extend.get("class") == null ? "" : extend.get("class");
        // 筛选处理 end

        // https://dm84.tv/show-1--time-战斗--2022-.html
        String cateUrl;
        if (pg.equals("1")) {
            cateUrl = siteUrl + String.format("/show-%s--%s-%s--%s-.html", tid, by, classType, year);
        } else {
            cateUrl = siteUrl + String.format("/show-%s--%s-%s--%s-%s.html", tid, by, classType, year, pg);
        }
        JSONArray videos = parseVodList(cateUrl);
        int page = Integer.parseInt(pg), count = Integer.MAX_VALUE, limit = 36, total = Integer.MAX_VALUE;
        JSONObject result = new JSONObject();
        result.put("page", page);
        result.put("pagecount", count);
        result.put("limit", limit);
        result.put("total", total);
        result.put("list", videos);
        return result.toString();
    }*/

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String vodId = ids.get(0);
        // https://www.xingyiying.com/index.php/vod/detail/id/217717.html
        // https://www.xingyiying.com/index.php/vod/detail/id/183491.html
        // https://www.xingyiying.com/index.php/vod/detail/id/31606.html
        String detailUrl = siteUrl + "/index.php/vod/detail/id/" + vodId + ".html";
        String html = req(detailUrl);
        Document doc = Jsoup.parse(html);
        String name = doc.select("h1").text();
        String pic = doc.select(".module-info-poster img").attr("data-original");
        String typeName = doc.select(".module-info-tag-link").text();
        String year = "";
        String area = "";

        String actor = "";
        String director = "";
        String remark = "";
        Elements elements = doc.select(".module-info-items > .module-info-item");
        for (Element element : elements) {
            String text = element.text();
            if (text.startsWith("导演")) director = parseVodInfo(element);
            if (text.startsWith("主演")) actor = parseVodInfo(element);
            if (text.startsWith("集数")) remark = text;
            if (text.startsWith("备注")) remark = text.replaceAll("备注：", "");
        }
        String description = doc.select(".module-info-introduction").text();

        Elements circuits = doc.select("#y-playList > .tab-item");
        Elements sourceList = doc.select("#panel1");
        Map<String, String> playMap = new LinkedHashMap<>();
        for (int i = 0; i < sourceList.size(); i++) {
            String spanText = circuits.get(i).select("span").text();
            String smallText = circuits.get(i).select("small").text();
            String circuitName = spanText + "【共" + smallText + "集】";
            List<String> vodItems = new ArrayList<>();
            Elements aList = sourceList.get(i).select("a");
            for (Element a : aList) {
                // https://www.xingyiying.com/index.php/vod/play/id/217405/sid/1/nid/1.html
                String episodeUrl = siteUrl + a.attr("href");
                String episodeName = a.text();
                vodItems.add(episodeName + "$" + episodeUrl);
            }
            if (vodItems.size() > 0) {
                playMap.put(circuitName, String.join("#", vodItems));
            }
        }

        JSONObject vod = new JSONObject();
        vod.put("vod_id", ids.get(0));
        vod.put("vod_name", name); // 影片名称
        vod.put("vod_pic", pic); // 图片
        vod.put("type_name", typeName); // 影片类型 选填
        vod.put("vod_year", year); // 年份 选填
        vod.put("vod_area", area); // 地区 选填
        vod.put("vod_remarks", remark); // 备注 选填
        vod.put("vod_actor", actor); // 主演 选填
        vod.put("vod_director", director); // 导演 选填
        vod.put("vod_content", description); // 简介 选填
        if (playMap.size() > 0) {
            vod.put("vod_play_from", String.join("$$$", playMap.keySet()));
            vod.put("vod_play_url", String.join("$$$", playMap.values()));
        }
        JSONArray jsonArray = new JSONArray().put(vod);
        JSONObject result = new JSONObject().put("list", jsonArray);
        return result.toString();
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        return searchContent(key, quick, "1");
    }

    @Override
    public String searchContent(String key, boolean quick, String pg) throws Exception {
        String keyword = URLEncoder.encode(key);
        // https://www.xingyiying.com/index.php/ajax/suggest?mid=1&wd=斗破
        String searchUrl = siteUrl + "/index.php/ajax/suggest?mid=1&wd=" + keyword;
//        if (!pg.equals("1")) searchUrl = siteUrl + "/s-" + keyword + "---------" + pg + ".html";
        if (!pg.equals("1")) return "";
        JSONArray videos = new JSONArray();
        JSONObject searchResult = new JSONObject(req(searchUrl));
        JSONArray items = searchResult.optJSONArray("list");
        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String vodId = item.optString("id");
            String name = item.optString("name");
            String pic = item.optString("pic");
            String remark = "";

            JSONObject vod = new JSONObject();
            vod.put("vod_id", vodId);
            vod.put("vod_name", name);
            vod.put("vod_pic", pic);
            vod.put("vod_remarks", remark);
            videos.put(vod);
        }
        JSONObject result = new JSONObject();
        result.put("list", videos);
        return result.toString();
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        String lastUrl = id;
        int parse = 1;
        String headerStr = getHeader().toString();
        String html = req(lastUrl);
        String player_aaaa = find(Pattern.compile("player_aaaa=(.*?)</script>"), html);
        JSONObject jsonObject = new JSONObject(player_aaaa);
        String url = jsonObject.optString("url");
        if (url.contains(".m3u8") || url.contains(".mp4")) {
            lastUrl = url;
            parse = 0;
            headerStr = getHeaderForPlay().toString();
        }

        JSONObject result = new JSONObject();
        result.put("parse", parse);
        result.put("header", headerStr);
        result.put("playUrl", "");
        result.put("url", lastUrl);
        return result.toString();
    }
}
