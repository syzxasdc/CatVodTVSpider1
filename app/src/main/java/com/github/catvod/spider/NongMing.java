package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.okhttp.OkHttpUtil;
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
 * 农民影视
 */
public class NongMing extends Spider {

    private static final String siteUrl = "https://v.xiangdao.me";

    // 请求头部设置
    protected HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 14_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/14.1.1 Mobile/15E148 Safari/604.1");
        return headers;
    }

    @Override
    public String homeContent(boolean filter) {
        try {
            JSONObject result = new JSONObject();
            JSONArray classes = new JSONArray();

            JSONObject movieType = new JSONObject();
            JSONObject teleplayType = new JSONObject();
            JSONObject varietyType = new JSONObject();
            JSONObject anime = new JSONObject();

            movieType.put("type_id", "1");
            movieType.put("type_name", "电影");

            teleplayType.put("type_id", "2");
            teleplayType.put("type_name", "电视剧");

            varietyType.put("type_id", "3");
            varietyType.put("type_name", "综艺");

            anime.put("type_id", "4");
            anime.put("type_name", "动漫");

            classes.put(movieType);
            classes.put(teleplayType);
            classes.put(varietyType);
            classes.put(anime);

            result.put("class", classes);


            // filter 二级筛选 start
            if (filter) {
                String s = "{\"1\": [{\"key\": \"cateId\", \"name\": \"类型\", \"value\": [{\"n\": \"全部\", \"v\": \"1\"}, {\"n\": \"动作片\", \"v\": \"5\"}, {\"n\": \"喜剧片\", \"v\": \"6\"}, {\"n\": \"爱情片\", \"v\": \"7\"}, {\"n\": \"科幻片\", \"v\": \"8\"}, {\"n\": \"恐怖片\", \"v\": \"9\"}, {\"n\": \"剧情片\", \"v\": \"10\"}, {\"n\": \"战争片\", \"v\": \"11\"}, {\"n\": \"惊悚片\", \"v\": \"16\"}, {\"n\": \"奇幻片\", \"v\": \"17\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"大陆\", \"v\": \"大陆\"}, {\"n\": \"香港\", \"v\": \"香港\"}, {\"n\": \"台湾\", \"v\": \"台湾\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"韩国\", \"v\": \"韩国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"泰国\", \"v\": \"泰国\"}, {\"n\": \"新加坡\", \"v\": \"新加坡\"}, {\"n\": \"马来西亚\", \"v\": \"马来西亚\"}, {\"n\": \"印度\", \"v\": \"印度\"}, {\"n\": \"英国\", \"v\": \"英国\"}, {\"n\": \"法国\", \"v\": \"法国\"}, {\"n\": \"加拿大\", \"v\": \"加拿大\"}, {\"n\": \"西班牙\", \"v\": \"西班牙\"}, {\"n\": \"俄罗斯\", \"v\": \"俄罗斯\"}, {\"n\": \"其它\", \"v\": \"其它\"}]}, {\"key\": \"year\", \"name\": \"年代\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2007\", \"v\": \"2007\"}, {\"n\": \"2006\", \"v\": \"2006\"}, {\"n\": \"2005\", \"v\": \"2005\"}, {\"n\": \"2004\", \"v\": \"2004\"}, {\"n\": \"2003\", \"v\": \"2003\"}, {\"n\": \"2002\", \"v\": \"2002\"}, {\"n\": \"2001\", \"v\": \"2001\"}, {\"n\": \"2000\", \"v\": \"2000\"}, {\"n\": \"1999\", \"v\": \"1999\"}, {\"n\": \"1998\", \"v\": \"1998\"}, {\"n\": \"1997\", \"v\": \"1997\"}, {\"n\": \"1996\", \"v\": \"1996\"}, {\"n\": \"1995\", \"v\": \"1995\"}, {\"n\": \"1994\", \"v\": \"1994\"}, {\"n\": \"1993\", \"v\": \"1993\"}, {\"n\": \"1992\", \"v\": \"1992\"}, {\"n\": \"1991\", \"v\": \"1991\"}, {\"n\": \"1990\", \"v\": \"1990\"}, {\"n\": \"1989\", \"v\": \"1989\"}, {\"n\": \"1988\", \"v\": \"1988\"}, {\"n\": \"1987\", \"v\": \"1987\"}, {\"n\": \"1986\", \"v\": \"1986\"}, {\"n\": \"1985\", \"v\": \"1985\"}, {\"n\": \"1984\", \"v\": \"1984\"}, {\"n\": \"1983\", \"v\": \"1983\"}, {\"n\": \"1982\", \"v\": \"1982\"}, {\"n\": \"1981\", \"v\": \"1981\"}, {\"n\": \"1980\", \"v\": \"1980\"}, {\"n\": \"1979\", \"v\": \"1979\"}, {\"n\": \"1978\", \"v\": \"1978\"}, {\"n\": \"1977\", \"v\": \"1977\"}, {\"n\": \"1976\", \"v\": \"1976\"}, {\"n\": \"1975\", \"v\": \"1975\"}, {\"n\": \"1974\", \"v\": \"1974\"}, {\"n\": \"1973\", \"v\": \"1973\"}, {\"n\": \"1972\", \"v\": \"1972\"}, {\"n\": \"1971\", \"v\": \"1971\"}, {\"n\": \"1970\", \"v\": \"1970\"}, {\"n\": \"1969\", \"v\": \"1969\"}, {\"n\": \"1968\", \"v\": \"1968\"}, {\"n\": \"1967\", \"v\": \"1967\"}, {\"n\": \"1966\", \"v\": \"1966\"}, {\"n\": \"1965\", \"v\": \"1965\"}, {\"n\": \"1964\", \"v\": \"1964\"}, {\"n\": \"1963\", \"v\": \"1963\"}, {\"n\": \"1962\", \"v\": \"1962\"}, {\"n\": \"1961\", \"v\": \"1961\"}, {\"n\": \"1960\", \"v\": \"1960\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"时间\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}], \"2\": [{\"key\": \"cateId\", \"name\": \"类型\", \"value\": [{\"n\": \"全部\", \"v\": \"2\"}, {\"n\": \"国产剧\", \"v\": \"12\"}, {\"n\": \"港台泰\", \"v\": \"13\"}, {\"n\": \"日韩剧\", \"v\": \"14\"}, {\"n\": \"欧美剧\", \"v\": \"15\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"大陆\", \"v\": \"大陆\"}, {\"n\": \"香港\", \"v\": \"香港\"}, {\"n\": \"台湾\", \"v\": \"台湾\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"韩国\", \"v\": \"韩国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"泰国\", \"v\": \"泰国\"}, {\"n\": \"新加坡\", \"v\": \"新加坡\"}, {\"n\": \"马来西亚\", \"v\": \"马来西亚\"}, {\"n\": \"印度\", \"v\": \"印度\"}, {\"n\": \"英国\", \"v\": \"英国\"}, {\"n\": \"法国\", \"v\": \"法国\"}, {\"n\": \"加拿大\", \"v\": \"加拿大\"}, {\"n\": \"西班牙\", \"v\": \"西班牙\"}, {\"n\": \"俄罗斯\", \"v\": \"俄罗斯\"}, {\"n\": \"其它\", \"v\": \"其它\"}]}, {\"key\": \"year\", \"name\": \"年代\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2007\", \"v\": \"2007\"}, {\"n\": \"2006\", \"v\": \"2006\"}, {\"n\": \"2005\", \"v\": \"2005\"}, {\"n\": \"2004\", \"v\": \"2004\"}, {\"n\": \"2003\", \"v\": \"2003\"}, {\"n\": \"2002\", \"v\": \"2002\"}, {\"n\": \"2001\", \"v\": \"2001\"}, {\"n\": \"2000\", \"v\": \"2000\"}, {\"n\": \"1999\", \"v\": \"1999\"}, {\"n\": \"1998\", \"v\": \"1998\"}, {\"n\": \"1997\", \"v\": \"1997\"}, {\"n\": \"1996\", \"v\": \"1996\"}, {\"n\": \"1995\", \"v\": \"1995\"}, {\"n\": \"1994\", \"v\": \"1994\"}, {\"n\": \"1993\", \"v\": \"1993\"}, {\"n\": \"1992\", \"v\": \"1992\"}, {\"n\": \"1991\", \"v\": \"1991\"}, {\"n\": \"1990\", \"v\": \"1990\"}, {\"n\": \"1989\", \"v\": \"1989\"}, {\"n\": \"1988\", \"v\": \"1988\"}, {\"n\": \"1987\", \"v\": \"1987\"}, {\"n\": \"1986\", \"v\": \"1986\"}, {\"n\": \"1985\", \"v\": \"1985\"}, {\"n\": \"1984\", \"v\": \"1984\"}, {\"n\": \"1983\", \"v\": \"1983\"}, {\"n\": \"1982\", \"v\": \"1982\"}, {\"n\": \"1981\", \"v\": \"1981\"}, {\"n\": \"1980\", \"v\": \"1980\"}, {\"n\": \"1979\", \"v\": \"1979\"}, {\"n\": \"1978\", \"v\": \"1978\"}, {\"n\": \"1977\", \"v\": \"1977\"}, {\"n\": \"1976\", \"v\": \"1976\"}, {\"n\": \"1975\", \"v\": \"1975\"}, {\"n\": \"1974\", \"v\": \"1974\"}, {\"n\": \"1973\", \"v\": \"1973\"}, {\"n\": \"1972\", \"v\": \"1972\"}, {\"n\": \"1971\", \"v\": \"1971\"}, {\"n\": \"1970\", \"v\": \"1970\"}, {\"n\": \"1969\", \"v\": \"1969\"}, {\"n\": \"1968\", \"v\": \"1968\"}, {\"n\": \"1967\", \"v\": \"1967\"}, {\"n\": \"1966\", \"v\": \"1966\"}, {\"n\": \"1965\", \"v\": \"1965\"}, {\"n\": \"1964\", \"v\": \"1964\"}, {\"n\": \"1963\", \"v\": \"1963\"}, {\"n\": \"1962\", \"v\": \"1962\"}, {\"n\": \"1961\", \"v\": \"1961\"}, {\"n\": \"1960\", \"v\": \"1960\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"时间\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}], \"3\": [{\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"大陆\", \"v\": \"大陆\"}, {\"n\": \"香港\", \"v\": \"香港\"}, {\"n\": \"台湾\", \"v\": \"台湾\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"韩国\", \"v\": \"韩国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"泰国\", \"v\": \"泰国\"}, {\"n\": \"新加坡\", \"v\": \"新加坡\"}, {\"n\": \"马来西亚\", \"v\": \"马来西亚\"}, {\"n\": \"印度\", \"v\": \"印度\"}, {\"n\": \"英国\", \"v\": \"英国\"}, {\"n\": \"法国\", \"v\": \"法国\"}, {\"n\": \"加拿大\", \"v\": \"加拿大\"}, {\"n\": \"西班牙\", \"v\": \"西班牙\"}, {\"n\": \"俄罗斯\", \"v\": \"俄罗斯\"}, {\"n\": \"其它\", \"v\": \"其它\"}]}, {\"key\": \"year\", \"name\": \"年代\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2007\", \"v\": \"2007\"}, {\"n\": \"2006\", \"v\": \"2006\"}, {\"n\": \"2005\", \"v\": \"2005\"}, {\"n\": \"2004\", \"v\": \"2004\"}, {\"n\": \"2003\", \"v\": \"2003\"}, {\"n\": \"2002\", \"v\": \"2002\"}, {\"n\": \"2001\", \"v\": \"2001\"}, {\"n\": \"2000\", \"v\": \"2000\"}, {\"n\": \"1999\", \"v\": \"1999\"}, {\"n\": \"1998\", \"v\": \"1998\"}, {\"n\": \"1997\", \"v\": \"1997\"}, {\"n\": \"1996\", \"v\": \"1996\"}, {\"n\": \"1995\", \"v\": \"1995\"}, {\"n\": \"1994\", \"v\": \"1994\"}, {\"n\": \"1993\", \"v\": \"1993\"}, {\"n\": \"1992\", \"v\": \"1992\"}, {\"n\": \"1991\", \"v\": \"1991\"}, {\"n\": \"1990\", \"v\": \"1990\"}, {\"n\": \"1989\", \"v\": \"1989\"}, {\"n\": \"1988\", \"v\": \"1988\"}, {\"n\": \"1987\", \"v\": \"1987\"}, {\"n\": \"1986\", \"v\": \"1986\"}, {\"n\": \"1985\", \"v\": \"1985\"}, {\"n\": \"1984\", \"v\": \"1984\"}, {\"n\": \"1983\", \"v\": \"1983\"}, {\"n\": \"1982\", \"v\": \"1982\"}, {\"n\": \"1981\", \"v\": \"1981\"}, {\"n\": \"1980\", \"v\": \"1980\"}, {\"n\": \"1979\", \"v\": \"1979\"}, {\"n\": \"1978\", \"v\": \"1978\"}, {\"n\": \"1977\", \"v\": \"1977\"}, {\"n\": \"1976\", \"v\": \"1976\"}, {\"n\": \"1975\", \"v\": \"1975\"}, {\"n\": \"1974\", \"v\": \"1974\"}, {\"n\": \"1973\", \"v\": \"1973\"}, {\"n\": \"1972\", \"v\": \"1972\"}, {\"n\": \"1971\", \"v\": \"1971\"}, {\"n\": \"1970\", \"v\": \"1970\"}, {\"n\": \"1969\", \"v\": \"1969\"}, {\"n\": \"1968\", \"v\": \"1968\"}, {\"n\": \"1967\", \"v\": \"1967\"}, {\"n\": \"1966\", \"v\": \"1966\"}, {\"n\": \"1965\", \"v\": \"1965\"}, {\"n\": \"1964\", \"v\": \"1964\"}, {\"n\": \"1963\", \"v\": \"1963\"}, {\"n\": \"1962\", \"v\": \"1962\"}, {\"n\": \"1961\", \"v\": \"1961\"}, {\"n\": \"1960\", \"v\": \"1960\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"时间\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}], \"4\": [{\"key\": \"cateId\", \"name\": \"类型\", \"value\": [{\"n\": \"全部\", \"v\": \"4\"}, {\"n\": \"动漫剧\", \"v\": \"18\"}, {\"n\": \"动漫片\", \"v\": \"19\"}]}, {\"key\": \"area\", \"name\": \"地区\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"大陆\", \"v\": \"大陆\"}, {\"n\": \"香港\", \"v\": \"香港\"}, {\"n\": \"台湾\", \"v\": \"台湾\"}, {\"n\": \"美国\", \"v\": \"美国\"}, {\"n\": \"韩国\", \"v\": \"韩国\"}, {\"n\": \"日本\", \"v\": \"日本\"}, {\"n\": \"泰国\", \"v\": \"泰国\"}, {\"n\": \"新加坡\", \"v\": \"新加坡\"}, {\"n\": \"马来西亚\", \"v\": \"马来西亚\"}, {\"n\": \"印度\", \"v\": \"印度\"}, {\"n\": \"英国\", \"v\": \"英国\"}, {\"n\": \"法国\", \"v\": \"法国\"}, {\"n\": \"加拿大\", \"v\": \"加拿大\"}, {\"n\": \"西班牙\", \"v\": \"西班牙\"}, {\"n\": \"俄罗斯\", \"v\": \"俄罗斯\"}, {\"n\": \"其它\", \"v\": \"其它\"}]}, {\"key\": \"year\", \"name\": \"年代\", \"value\": [{\"n\": \"全部\", \"v\": \"\"}, {\"n\": \"2023\", \"v\": \"2023\"}, {\"n\": \"2022\", \"v\": \"2022\"}, {\"n\": \"2021\", \"v\": \"2021\"}, {\"n\": \"2020\", \"v\": \"2020\"}, {\"n\": \"2019\", \"v\": \"2019\"}, {\"n\": \"2018\", \"v\": \"2018\"}, {\"n\": \"2017\", \"v\": \"2017\"}, {\"n\": \"2016\", \"v\": \"2016\"}, {\"n\": \"2015\", \"v\": \"2015\"}, {\"n\": \"2014\", \"v\": \"2014\"}, {\"n\": \"2013\", \"v\": \"2013\"}, {\"n\": \"2012\", \"v\": \"2012\"}, {\"n\": \"2011\", \"v\": \"2011\"}, {\"n\": \"2010\", \"v\": \"2010\"}, {\"n\": \"2009\", \"v\": \"2009\"}, {\"n\": \"2008\", \"v\": \"2008\"}, {\"n\": \"2007\", \"v\": \"2007\"}, {\"n\": \"2006\", \"v\": \"2006\"}, {\"n\": \"2005\", \"v\": \"2005\"}, {\"n\": \"2004\", \"v\": \"2004\"}, {\"n\": \"2003\", \"v\": \"2003\"}, {\"n\": \"2002\", \"v\": \"2002\"}, {\"n\": \"2001\", \"v\": \"2001\"}, {\"n\": \"2000\", \"v\": \"2000\"}, {\"n\": \"1999\", \"v\": \"1999\"}, {\"n\": \"1998\", \"v\": \"1998\"}, {\"n\": \"1997\", \"v\": \"1997\"}, {\"n\": \"1996\", \"v\": \"1996\"}, {\"n\": \"1995\", \"v\": \"1995\"}, {\"n\": \"1994\", \"v\": \"1994\"}, {\"n\": \"1993\", \"v\": \"1993\"}, {\"n\": \"1992\", \"v\": \"1992\"}, {\"n\": \"1991\", \"v\": \"1991\"}, {\"n\": \"1990\", \"v\": \"1990\"}, {\"n\": \"1989\", \"v\": \"1989\"}, {\"n\": \"1988\", \"v\": \"1988\"}, {\"n\": \"1987\", \"v\": \"1987\"}, {\"n\": \"1986\", \"v\": \"1986\"}, {\"n\": \"1985\", \"v\": \"1985\"}, {\"n\": \"1984\", \"v\": \"1984\"}, {\"n\": \"1983\", \"v\": \"1983\"}, {\"n\": \"1982\", \"v\": \"1982\"}, {\"n\": \"1981\", \"v\": \"1981\"}, {\"n\": \"1980\", \"v\": \"1980\"}, {\"n\": \"1979\", \"v\": \"1979\"}, {\"n\": \"1978\", \"v\": \"1978\"}, {\"n\": \"1977\", \"v\": \"1977\"}, {\"n\": \"1976\", \"v\": \"1976\"}, {\"n\": \"1975\", \"v\": \"1975\"}, {\"n\": \"1974\", \"v\": \"1974\"}, {\"n\": \"1973\", \"v\": \"1973\"}, {\"n\": \"1972\", \"v\": \"1972\"}, {\"n\": \"1971\", \"v\": \"1971\"}, {\"n\": \"1970\", \"v\": \"1970\"}, {\"n\": \"1969\", \"v\": \"1969\"}, {\"n\": \"1968\", \"v\": \"1968\"}, {\"n\": \"1967\", \"v\": \"1967\"}, {\"n\": \"1966\", \"v\": \"1966\"}, {\"n\": \"1965\", \"v\": \"1965\"}, {\"n\": \"1964\", \"v\": \"1964\"}, {\"n\": \"1963\", \"v\": \"1963\"}, {\"n\": \"1962\", \"v\": \"1962\"}, {\"n\": \"1961\", \"v\": \"1961\"}, {\"n\": \"1960\", \"v\": \"1960\"}]}, {\"key\": \"by\", \"name\": \"排序\", \"value\": [{\"n\": \"时间\", \"v\": \"time\"}, {\"n\": \"人气\", \"v\": \"hits\"}, {\"n\": \"评分\", \"v\": \"score\"}]}]}";
                JSONObject filterConfig = new JSONObject(s);
                result.put("filters", filterConfig);
            }
            // filter 二级筛选 end

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

            HashMap<String, String> ext = new HashMap<>();
            if (extend != null && extend.size() > 0) {
                ext.putAll(extend);
            }

            String area = ext.get("area") == null ? "" : ext.get("area");
            String year = ext.get("year") == null ? "" : ext.get("year");
            String by = ext.get("by") == null ? "" : ext.get("by");
            String classType = ext.get("class") == null ? tid : ext.get("class");
//            String lang = ext.get("lang") == null ? "" : ext.get("lang");

            String cateUrl = siteUrl + String.format("/vod-list-id-%s-pg-%s-order--by-%s-class-0-year-%s-letter--area-%s-lang-.html", classType, pg, by, year, area);
            String content = OkHttpUtil.string(cateUrl, getHeaders());
            Document doc = Jsoup.parse(content);
            Elements listElements = doc.select("[class=globalPicList]")
                    .select("li");

            for (Element element : listElements) {
                JSONObject vod = new JSONObject();
                Element item = element.select("a").get(0);
                String vod_id = siteUrl + item.attr("href");
                String vod_name = item.attr("title");
                String vod_pic = item.select("img").attr("src");

                List<TextNode> textNodes = item.select(".sBottom").select("span").textNodes();
                String vod_remarks = textNodes.size() > 0 ? textNodes.get(0).text() : "";
                vod.put("vod_id", vod_id);
                vod.put("vod_name", vod_name);
                vod.put("vod_pic", vod_pic);
                vod.put("vod_remarks", vod_remarks);
                jSONArray.put(vod);

            }
            result.put("page", Integer.parseInt(pg));
            result.put("pagecount", Integer.MAX_VALUE);
            result.put("limit", listElements.size());
            result.put("total", Integer.MAX_VALUE);
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

            String detailUrl = ids.get(0);
            String content = OkHttpUtil.string(detailUrl, getHeaders());
            Document detailPage = Jsoup.parse(content);
            Elements sources = detailPage.select("[class=numList]");

            StringBuilder vod_play_url = new StringBuilder(); // 线路/播放源 里面的各集的播放页面链接
            StringBuilder vod_play_from = new StringBuilder();  // 线路 / 播放源标题
            for (int i = 0; i < sources.size(); i++) {
                int b = i + 1;
                vod_play_from.append("源").append(b).append("$$$");

                Elements aElementArray = sources.get(i).select("a");
                for (int j = aElementArray.size() - 1; j >= 0; j--) { // 逆序遍历，因为网站的集数是从大到小的
                    String href = aElementArray.get(j).attr("href");
                    String playUrl = siteUrl + href;
                    String playTitle = aElementArray.get(j).text();
                    vod_play_url.append(playTitle).append("$").append(playUrl);
                    vod_play_url.append(j == 0 ? "$$$" : "#");// 逆序遍历，最后一集的索引为 0
                }
            }

            // 影片标题
            String title = detailPage.select(".page-hd")
                    .select("a")
                    .get(0)
                    .attr("title");

            // 图片
            String pic = detailPage.select(".page-hd")
                    .select("img")
                    .attr("src");

            // 影片名称、图片等赋值
            info.put("vod_id", ids.get(0));
            info.put("vod_name", title);
            info.put("vod_pic", pic);

            // -------------------- 选填部分 start --------------------
            String classifyName = detailPage.select(".type-title").text();
            String year = detailPage.select(".detail-con")
                    .select("span")
                    .get(0).select("em").text();
            String area = "";
            info.put("type_name", classifyName);
            info.put("vod_year", year);
            info.put("vod_area", area);

            String remarks = "";
            String actor = "";
            String director = "";
            String brief = detailPage.select(".detail-con").select("p").text().replaceAll("简 介：", "");
            Elements descItem = detailPage.select(".desc_item");
            if (descItem.size() >= 3) {
                actor = descItem.get(1).text().replaceAll("主演: ", "");
                director = descItem.get(2).text().replaceAll("导演: ", "");
            }

            info.put("vod_remarks", remarks);
            info.put("vod_actor", actor);
            info.put("vod_director", director);
            info.put("vod_content", brief);
            // -------------------- 选填部分 end ---------------------

            info.put("vod_play_from", vod_play_from.toString());
            info.put("vod_play_url", vod_play_url.toString());

            list_info.put(info);
            result.put("list", list_info);

            return result.toString();
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String searchContent(String key, boolean quick) {
        try {

            String url = siteUrl + "/index.php?m=vod-search&wd=" + URLEncoder.encode(key);
            String searchResult = OkHttpUtil.string(url, getHeaders());
            Document searchResultPage = Jsoup.parse(searchResult);
            JSONObject result = new JSONObject();
            JSONArray videoList = new JSONArray();

            Elements list = searchResultPage.select("[id=data_list]")
                    .select("li");
            for (Element item : list) {
                Element aElement = item.select(".pic").select("a").get(0);
                String id = siteUrl + aElement.attr("href");
                String title = item.select(".sTit").text();

                String cover = aElement.select("img").attr("data-src");
                String remark = "";
                JSONObject v = new JSONObject();
                v.put("vod_id", id);
                v.put("vod_name", title);
                v.put("vod_pic", cover);
                v.put("vod_remarks", remark);
                videoList.put(v);
            }

            result.put("list", videoList);
            return result.toString();

        } catch (Exception e) {
            e.printStackTrace();
            SpiderDebug.log(e);
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            JSONObject result = new JSONObject();

            HashMap<String, String> headers = getHeaders();
//            headers.put("","");
            result.put("parse", 1); // 1表示需要嗅探，如果是0则表示解析
            result.put("header", headers);
            result.put("playUrl", "");
            result.put("url", id);
            return result.toString();

        } catch (Exception e) {
            SpiderDebug.log(e);
        }
        return "";
    }
}
