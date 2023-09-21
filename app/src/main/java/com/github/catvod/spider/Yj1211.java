package com.github.catvod.spider;

import android.text.TextUtils;
import com.github.catvod.crawler.Spider;
import com.github.catvod.net.OkHttp;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.*;

public class Yj1211 extends Spider {

    private HashMap<String, String> getHeader() {
        HashMap<String, String> header = new HashMap<>();
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.134 YaBrowser/22.7.1.755 (beta) Yowser/2.5 Safari/537.36");
        return header;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        String srcURL = "http://live.yj1211.work/api/live/getRecommend?page=1&size=20";
        String srcOriginStr = "";
        for (int i = 0; i < 3; i++) {
            srcOriginStr = OkHttp.string(srcURL, getHeader());
            if (srcOriginStr.length() > 0) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }
        JSONArray data = new JSONObject(srcOriginStr).getJSONArray("data");
        JSONArray classes = new JSONArray();
        String cateStr = "{\"推荐\":\"?\",\"斗鱼\":\"ByPlatform?platform=douyu&\",\"哔哩哔哩\":\"ByPlatform?platform=bilibili&\",\"虎牙\":\"ByPlatform?platform=huya&\",\"网易CC\":\"ByPlatform?platform=cc&\"}";
        JSONObject catedef = new JSONObject(cateStr);
        Iterator it = catedef.keys();
        while (it.hasNext()) {
            String key = (String) it.next();
            JSONObject jsonObject = new JSONObject()
                    .put("type_name", key)
                    .put("type_id", catedef.getString(key));
            classes.put(jsonObject);
        }

        JSONObject filterConfig = new JSONObject();
        String geta = "http://live.yj1211.work/api/live/getAllAreas";
        String aaid = "";
        for (int i = 0; i < 3; i++) {
            aaid = OkHttp.string(geta, getHeader());
            if (aaid.length() > 0) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }

        JSONObject aido = new JSONObject(aaid);
        JSONArray aidoa = aido.getJSONArray("data");
        JSONArray extendsAll = new JSONArray();
        for (int j = 0; j < 13; j++) {
            JSONObject newTypeExtend = new JSONObject();
            String typeName = aidoa.getJSONArray(j).getJSONObject(0).getString("typeName");
            newTypeExtend.put("key", "typeName" + j);
            newTypeExtend.put("name", typeName);
            JSONArray newTypeExtendKV = new JSONArray();
            int fg = Math.min(aidoa.getJSONArray(j).length(), 20);
            JSONObject kv = new JSONObject();
            kv.put("n", "全部");
            kv.put("v", typeName + "=" + "all");
            newTypeExtendKV.put(kv);
            for (int k = 0; k < fg; k++) {
                kv = new JSONObject();
                String areaName = aidoa.getJSONArray(j).getJSONObject(k).getString("areaName");
                kv.put("n", areaName);
                kv.put("v", typeName + "=" + areaName);
                newTypeExtendKV.put(kv);
            }
            newTypeExtend.put("value", newTypeExtendKV);
            extendsAll.put(newTypeExtend);
        }
        for (int i = 0; i < 5; i++) {
            String typeId = classes.getJSONObject(i).getString("type_id");
            filterConfig.put(typeId, extendsAll);
        }

        JSONArray videos = new JSONArray();
        int ch = Math.min(data.length(), 10);
        for (int i = 0; i < ch; i++) {
            JSONObject srchome = new JSONObject();
            String platForm = data.getJSONObject(i).getString("platForm");
            String rd = data.getJSONObject(i).getString("roomId");
            String id = platForm + "&" + rd;
            String name = data.getJSONObject(i).getString("ownerName");
            String pic = data.getJSONObject(i).getString("ownerHeadPic");
            String mark = data.getJSONObject(i).getString("categoryName");
            srchome.put("vod_id", id);
            srchome.put("vod_name", name);
            srchome.put("vod_pic", pic);
            srchome.put("vod_remarks", mark);
            videos.put(srchome);
        }

        JSONObject result = new JSONObject()
                .put("class", classes)
                .put("filters", filterConfig)
                .put("list", videos);
        return result.toString();
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        String catt = "";
        switch (tid) {
            case "?":
                catt = "all";
                break;
            case "ByPlatform?platform=douyu&":
                catt = "douyu";
                break;
            case "ByPlatform?platform=bilibili&":
                catt = "bilibili";
                break;
            case "ByPlatform?platform=huya&":
                catt = "huya";
                break;
            case "ByPlatform?platform=cc&":
                catt = "cc";
                break;
        }

        extend = extend == null ? new HashMap<>() : extend;
        String srcurl = "";
        String[] cate = new String[13];
        int pp = 0;
        for (int i = 0; i < 13; i++) {
            cate[i] = extend.containsKey("typeName" + i) ? extend.get("typeName" + i) : ("typeName" + i + "=" + "all");
            String[] info = cate[i].split("=");
            String area = info[1];
            if (!area.contains("all")) {
                pp = pp + 1;
            }
        }
        if (pp == 1) {
            for (int i = 0; i < 13; i++) {
                String[] info = cate[i].split("=");
                String areaType = info[0];
                String area = info[1];
                if (!area.contains("all")) {
                    String urlft = "http://live.yj1211.work/api/live/getRecommendByAreaAll?areaType={areaType}&area={area}&page={pg}";
                    srcurl = urlft.replace("{areaType}", URLEncoder.encode(areaType)).replace("{area}", URLEncoder.encode(area)).replace("{pg}", pg);
                    break;
                }
            }
        } else if (pp == 0 || pp > 1) {
            String urlft = "http://live.yj1211.work/api/live/getRecommend{tid}page={pg}&size=20";
            srcurl = urlft.replace("{tid}", tid).replace("{pg}", pg);
        }
        String srcOrignstr = "";
        for (int i = 0; i < 3; i++) {
            srcOrignstr = OkHttp.string(srcurl, getHeader());
            if (srcOrignstr.length() > 0) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }
        JSONObject srcori = new JSONObject(srcOrignstr);
        JSONArray srcoria = srcori.getJSONArray("data");
        JSONArray videos = new JSONArray();
        for (int i = 0; i < srcoria.length(); i++) {
            JSONObject srchome = new JSONObject();
            String platForm = srcoria.getJSONObject(i).getString("platForm");
            if (pp == 1 && !catt.equals("all")) {
                if (!platForm.equals(catt)) {
                    continue;
                }
            }
            String rd = srcoria.getJSONObject(i).getString("roomId");
            String id = platForm + "&" + rd;
            String name = srcoria.getJSONObject(i).getString("ownerName");
            String pic = srcoria.getJSONObject(i).getString("ownerHeadPic");
            String mark = srcoria.getJSONObject(i).getString("categoryName");
            srchome.put("vod_id", id);
            srchome.put("vod_name", name);
            srchome.put("vod_pic", pic);
            srchome.put("vod_remarks", mark);
            videos.put(srchome);
        }
        JSONObject result = new JSONObject()
                .put("pagecount", pp == 1 ? 50 : Integer.MAX_VALUE)
                .put("list", videos);
        return result.toString();
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String id = ids.get(0);
        String[] info = id.split("&");
        String platform = info[0];
        String roomId = info[1];
        String srcURL = "http://live.yj1211.work/api/live/getRoomInfo?platform=" + platform + "&roomId=" + roomId;
        String srcPlayURL = "http://live.yj1211.work/api/live/getRealUrl?platform=" + platform + "&roomId=" + roomId;
        JSONObject data = new JSONObject(OkHttp.string(srcURL, getHeader()))
                .getJSONObject("data");
        String name = data.getString("roomName");
        String pic = data.getString("roomPic");
        String director = data.getString("ownerName") + " RoomID:" + data.getString("roomId");
        String content = data.getString("categoryName");
        String actor = "观看人数:" + data.getString("online");
        String area = data.getString("platForm");
        String isLive = data.optString("isLive");
        String type = isLive.equals("") ? "录播" : "正在直播中";
        JSONObject vodInfo = new JSONObject()
                .put("vod_id", ids.get(0))
                .put("vod_pic", pic)
                .put("vod_name", name)
                .put("vod_area", area)
                .put("type_name", type)
                .put("vod_actor", actor)
                .put("vod_director", director)
                .put("vod_content", content);
        String playList = "";
        String pl = "";
        List<String> vodItems = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            String[] qq = new String[]{"OD", "HD", "SD", "LD", "FD"};
            String qa = new JSONObject(OkHttp.string(srcPlayURL, getHeader()))
                    .getJSONObject("data")
                    .optString(qq[i]);
            if (qa.isEmpty()) {
                continue;
            }
            switch (qq[i]) {
                case "OD":
                    pl = "原画" + "$" + qa;
                    break;
                case "HD":
                    pl = "超清" + "$" + qa;
                    break;
                case "SD":
                    pl = "高清" + "$" + qa;
                    break;
                case "LD":
                    pl = "清晣" + "$" + qa;
                    break;
                case "FD":
                    pl = "流畅" + "$" + qa;
                    break;
            }
            vodItems.add(pl);
        }
        if (vodItems.size() > 0)
            playList = TextUtils.join("#", vodItems);

        if (playList.length() == 0)
            playList = "NoStream$nolink";

        Map<String, String> vod_play = new TreeMap<>();
        vod_play.put("YJ1211", playList);
        String vod_play_from = TextUtils.join("$$$", vod_play.keySet());
        String vod_play_url = TextUtils.join("$$$", vod_play.values());
        vodInfo.put("vod_play_from", vod_play_from);
        vodInfo.put("vod_play_url", vod_play_url);

        JSONArray list = new JSONArray()
                .put(vodInfo);
        JSONObject result = new JSONObject()
                .put("list", list);
        return result.toString();
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        JSONObject result = new JSONObject()
                .put("header", getHeader().toString())
                .put("parse", 1)
                .put("playUrl", "")
                .put("url", id);
        return result.toString();
    }

    @Override
    public String searchContent(String key, boolean quick) throws Exception {
        String url = "http://live.yj1211.work/api/live/search?platform=all&keyWords=" + URLEncoder.encode(key) + "&isLive=0";
        String searchResult = "";
        for (int i = 0; i < 3; i++) {
            searchResult = OkHttp.string(url, getHeader());
            if (searchResult.length() > 0) {
                break;
            } else {
                Thread.sleep(1000);
            }
        }

        JSONArray sra = new JSONObject(searchResult)
                .getJSONArray("data");
        JSONArray videos = new JSONArray();
        if (sra.length() > 0) {
            int ch = Math.min(sra.length(), 20);
            for (int i = 0; i < ch; i++) {
                String platForm = sra.getJSONObject(i).getString("platform");
                String rd = sra.getJSONObject(i).getString("roomId");
                String id = platForm + "&" + rd;
                String name = sra.getJSONObject(i).getString("nickName");
                String pic = sra.getJSONObject(i).getString("headPic");
                String mark = "";
                if (!sra.getJSONObject(i).isNull("cateName")) {
                    mark = sra.getJSONObject(i).getString("cateName");
                }
                JSONObject vod = new JSONObject()
                        .put("vod_remarks", mark)
                        .put("vod_id", id)
                        .put("vod_name", name)
                        .put("vod_pic", pic);
                videos.put(vod);
            }
        }
        JSONObject result = new JSONObject()
                .put("list", videos);
        return result.toString();
    }
}