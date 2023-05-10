package com.github.catvod.spider;

import android.content.Context;
import android.net.UrlQuerySanitizer;
import android.text.TextUtils;

import com.github.catvod.crawler.Spider;
import com.github.catvod.crawler.SpiderDebug;
import com.github.catvod.utils.Misc;
import com.github.catvod.utils.okhttp.OKCallBack;
import com.github.catvod.utils.okhttp.OkHttpUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;


/**
 * Author: @SDL
 */
public class PushAgent extends Spider {
    private static long timeToken = 0;

    private static String accessToken = "";
    private static String refreshToken = "";

    private static Map<String, String> shareToken = new HashMap<>();
    private static Map<String, Long> shareExpires = new HashMap<>();
    private static final Map<String, Map<String, String>> videosMap = new HashMap<>();
    private static final ReentrantLock rLock = new ReentrantLock();
    public static Pattern regexAli = Pattern.compile("(https://www.aliyundrive.com/s/[^\"]+)");
    //TANGSAN
    public static Pattern Folder = Pattern.compile("www.aliyundrive.com/s/([^/]+)(/folder/([^/]+))?");

    public static Pattern regexAliFolder = Pattern.compile("www.aliyundrive.com/s/([^/]+)(/folder/([^/]+))?");

    @Override
    public void init(Context context, String extend) {
        super.init(context, extend);
        if (extend.startsWith("http")) {
            refreshToken = OkHttpUtil.string(extend, null);
        } else {
            refreshToken = extend;
        }
    }

//    @Override
//    public void init(Context context) {
//        super.init(context);
////        refreshToken = OkHttpUtil.string("https://gitea.com/qiaoji/jar/raw/branch/main/token.txt", null);
////        if(refreshToken.length()!=32){
////            refreshToken = "ad3c78559a494bde814f1a6c8c40db51";
////        }
//        refreshToken = "ad3c78559a494bde814f1a6c8c40db51";
//    }

    private static HashMap<String, String> getHeaders() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.54 Safari/537.36");
        headers.put("Referer", "https://www.aliyundrive.com/");
        return headers;
    }

    private static HashMap<String, String> getHeaders2() {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/94.0.4606.54 Safari/537.36");
        return headers;
    }

    private static String postJson(String url, String jsonStr, Map<String, String> headerMap) {
        OKCallBack.OKCallBackString callback = new OKCallBack.OKCallBackString() {
            @Override
            public void onFailure(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {

            }
        };
        OkHttpUtil.postJson(OkHttpUtil.defaultClient(), url, jsonStr, headerMap, callback);
        return callback.getResult();
    }

    protected static long getTimeSys() {
        return (System.currentTimeMillis() / 1000);
    }

    private static void getRefreshTk() {
        long timeSys = getTimeSys();
        if (accessToken.isEmpty() || timeToken - timeSys <= 600) {
            try {
                JSONObject json = new JSONObject();
                json.put("refresh_token", refreshToken);
                JSONObject response = new JSONObject(postJson("https://api.aliyundrive.com/token/refresh", json.toString(), getHeaders()));
                accessToken = response.getString("token_type") + " " + response.getString("access_token");
                timeToken = response.getLong("expires_in") + timeSys;
            } catch (JSONException e) {
                SpiderDebug.log(e);
            }
        }
    }

    private static synchronized String getShareTk(String shareId, String sharePwd) {
        synchronized (PushAgent.class) {
            try {
                long timeSys = getTimeSys();
                String token = shareToken.get(shareId);
                Long expires = shareExpires.get(shareId);
                if (!TextUtils.isEmpty(token) && expires - timeSys > 600) {
                    return token;
                }
                JSONObject json = new JSONObject();
                json.put("share_id", shareId);
                json.put("share_pwd", sharePwd);
                JSONObject response = new JSONObject(postJson("https://api.aliyundrive.com/v2/share_link/get_share_token", json.toString(), getHeaders()));
                String string = response.getString("share_token");
                shareExpires.put(shareId, timeSys + response.getLong("expires_in"));
                shareToken.put(shareId, string);
                return string;
            } catch (JSONException e) {
                SpiderDebug.log(e);
                return "";
            }
        }
    }

    public static Object[] loadsub(String url) {
        try {
            return new Object[]{200, "application/octet-stream", new ByteArrayInputStream(OkHttpUtil.string(url, getHeaders()).getBytes())};
        } catch (Exception e2) {
            e2.printStackTrace();
            SpiderDebug.log(e2);
            return null;
        }
    }

    public static Object[] File(Map<String, String> params) {
        try {
            String shareId = params.get("share_id");
            return new Object[]{200, "application/octet-stream", new ByteArrayInputStream(getVideoUrl(shareId, getShareTk(shareId, ""), params.get("file_id")).getBytes())};
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return null;
        }
    }

    public static Object[] ProxyMedia(Map<String, String> params) {
        try {
            String shareId = params.get("share_id");
            String fileId = params.get("file_id");
            String mediaId = params.get("media_id");
            String shareToken = getShareTk(shareId, "");
            ReentrantLock reentrantLock = rLock;
            reentrantLock.lock();
            String url = videosMap.get(fileId).get(mediaId);
            if (new Long(new UrlQuerySanitizer(url).getValue("x-oss-expires")) - getTimeSys() <= 60) {
                getVideoUrl(shareId, shareToken, fileId);
                url = videosMap.get(fileId).get(mediaId);
            }
            reentrantLock.unlock();

            OKCallBack.OKCallBackDefault callback = new OKCallBack.OKCallBackDefault() {
                @Override
                public void onFailure(Call call, Exception e) {

                }

                @Override
                public void onResponse(Response response) {

                }
            };
            OkHttpUtil.get(OkHttpUtil.defaultClient(), url, null, getHeaders(),callback);
            return new Object[]{200, "video/MP2T", callback.getResult().body().byteStream()};
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return null;
        }
    }


    public static Object[] vod(Map<String, String> map) {
        String type = map.get("type");
        if (type.equals("m3u8")) {
            return File(map);
        }
        if (type.equals("media")) {
            return ProxyMedia(map);
        }
        return null;
    }

    private static String getVideoUrl(String shareId, String shareToken, String fileId) {
        try {
            getRefreshTk();
            JSONObject json = new JSONObject();
            json.put("share_id", shareId);
            json.put("category", "live_transcoding");
            json.put("file_id", fileId);
            json.put("template_id", "");
            HashMap<String, String> Headers = getHeaders();
            Headers.put("x-share-token", shareToken);
            Headers.put("authorization", accessToken);
            JSONObject jSONObject3 = new JSONObject(postJson("https://api.aliyundrive.com/v2/file/get_share_link_video_preview_play_info", json.toString(), Headers));
            JSONArray playList = jSONObject3.getJSONObject("video_preview_play_info").getJSONArray("live_transcoding_task_list");
            String videoUrl = "";
            String[] orders = new String[]{"FHD", "HD", "SD"};
            for (String or : orders) {
                for (int i = 0; i < playList.length(); i++) {
                    JSONObject obj = playList.getJSONObject(i);
                    if (obj.optString("template_id").equals(or)) {
                        videoUrl = obj.getString("url");
                        break;
                    }
                }
                if (!videoUrl.isEmpty())
                    break;
            }
            if (videoUrl.isEmpty() && playList.length() > 0) {
                videoUrl = playList.getJSONObject(0).getString("url");
            }
            Map<String, List<String>> respHeaderMap = new HashMap<>();
            OkHttpUtil.stringNoRedirect(videoUrl, getHeaders(), respHeaderMap);
            String url = OkHttpUtil.getRedirectLocation(respHeaderMap);
            String medias = OkHttpUtil.string(url, getHeaders());
            String site = url.substring(0, url.lastIndexOf("/")) + "/";
            ArrayList<String> lists = new ArrayList<>();
            Map<String, String> video = new HashMap<>();
            String[] split = medias.split("\n");
            int j = 0;
            for (int i = 0; i < split.length; i++) {
                String vod = split[i];
                if (vod.contains("x-oss-expires")) {
                    j++;
                    video.put("" + j, site + vod);
                    vod = Proxy.localProxyUrl() + "?do=ali&type=media&share_id=" + shareId + "&file_id=" + fileId + "&media_id=" + j;
                }
                lists.add(vod);
            }
            videosMap.put(fileId, video);
//            Map<String,String> o = new HashMap<>();
//            o.put("share_id",shareId);
//            o.put("file_id",fileId);
//            o.put("media_id",""+(--j));
//            ProxyMedia(o);
            return TextUtils.join("\n", lists);
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return "";
        }
    }

    private static String getOriginalVideoUrl(String shareId, String shareToken, String fileId, String category) {
        try {
            getRefreshTk();
            HashMap<String, String> json = getHeaders();
            json.put("x-share-token", shareToken);
            json.put("authorization", accessToken);
            if (category.equals("video")) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("share_id", shareId);
                jSONObject.put("category", "live_transcoding");
                jSONObject.put("file_id", fileId);
                jSONObject.put("template_id", "");
                JSONObject jSONObject2 = new JSONObject(postJson("https://api.aliyundrive.com/v2/file/get_share_link_video_preview_play_info", jSONObject.toString(), json));
                shareId = jSONObject2.getString("share_id");
                fileId = jSONObject2.getString("file_id");
            }
            JSONObject jSONObject3 = new JSONObject();
            if (category.equals("video")) {
                jSONObject3.put("expire_sec", 600);
                jSONObject3.put("file_id", fileId);
                jSONObject3.put("share_id", shareId);
            }
            if (category.equals("audio")) {
                jSONObject3.put("share_id", shareId);
                jSONObject3.put("get_audio_play_info", true);
                jSONObject3.put("file_id", fileId);
            }
            return new JSONObject(postJson("https://api.aliyundrive.com/v2/file/get_share_link_download_url", jSONObject3.toString(), json)).getString("download_url");
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
    }


    public void listFiles(Map<String, String> map, String shareId, String shareToken, String fileId) {
        try {
            String url = "https://api.aliyundrive.com/adrive/v3/file/list";
            HashMap<String, String> headers = getHeaders();
            headers.put("x-share-token", shareToken);
            JSONObject json = new JSONObject();
            json.put("image_thumbnail_process", "image/resize,w_160/format,jpeg");
            json.put("image_url_process", "image/resize,w_1920/format,jpeg");
            json.put("limit", 200);
            json.put("order_by", "updated_at");
            json.put("order_direction", "DESC");
            json.put("parent_file_id", fileId);
            json.put("share_id", shareId);
            json.put("video_thumbnail_process", "video/snapshot,t_1000,f_jpg,ar_auto,w_300");
            String marker = "";
            ArrayList<String> arrayList = new ArrayList<>();
            for(int i=1;i<=50;i++) {
                if (i >1 && marker.isEmpty())
                    break;
                json.put("marker", marker);
                JSONObject data = new JSONObject(postJson(url, json.toString(), headers));
                JSONArray items = data.getJSONArray("items");

                for (int j = 0; j < items.length(); j++) {
                    JSONObject item = items.getJSONObject(j);
                    if (item.getString("type").equals("folder")) {
                        arrayList.add(item.getString("file_id"));
                    } else {
                        //vnd.rn-realmedia-vbr 为rmvb格式
                        if (item.getString("mime_type").contains("video")||item.getString("mime_type").contains("vnd.rn-realmedia-vbr")) {
                            String replace = item.getString("name").replace("#", "_").replace("$", "_");
                            map.put(replace, shareId + "+" + shareToken + "+" + item.getString("file_id")+"+"+item.getString("category"));
                        }
                    }
                }
                marker = data.getString("next_marker");
            }

            for (String item : arrayList) {
                try {
                    listFiles(map, shareId, shareToken, item);
                } catch (Exception e) {
                    SpiderDebug.log(e);
                    return;
                }
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
        }
    }


    @Override
    public String detailContent(List<String> ids) {
        try {
            String url = ids.get(0);
            if (Misc.isVip(url) && !url.contains("qq.com") && !url.contains("mgtv.com")) {
                JSONObject result = new JSONObject();
                JSONArray list = new JSONArray();
                JSONObject vodAtom = new JSONObject();
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", url);
                vodAtom.put("vod_pic", "https://pic.rmb.bdstatic.com/bjh/1d0b02d0f57f0a42201f92caba5107ed.jpeg");
                vodAtom.put("type_name", "官源");
                vodAtom.put("vod_year", "");
                vodAtom.put("vod_area", "");
                vodAtom.put("vod_remarks", "");
                vodAtom.put("vod_actor", "");
                vodAtom.put("vod_director", "");
                vodAtom.put("vod_content", "");
                vodAtom.put("vod_play_from", "jx");
                vodAtom.put("vod_play_url", "立即播放$" + url);
                list.put(vodAtom);
                result.put("list", list);
                return result.toString();
            } else if (Misc.isVip(url) && url.contains("qq.com")) {
                List<String> vodItems = new ArrayList<>();
                JSONObject result = new JSONObject();
                JSONArray lists = new JSONArray();
                JSONObject vodAtom = new JSONObject();
                Document doc = Jsoup.parse(OkHttpUtil.string(url, getHeaders2()));
                String VodName = doc.select("head > title").text();
                Elements playListA = doc.select("div.episode-list-rect__item");
                if (!playListA.isEmpty()) {
                    for (int j = 0; j < playListA.size(); j++) {
                        Element vod = playListA.get(j);
                        String a = vod.select("div").attr("data-vid");
                        String b = vod.select("div").attr("data-cid");
                        String id = "https://v.qq.com/x/cover/" + b + "/" + a;
                        String name = vod.select("div span").text();
                        vodItems.add(name + "$" + id);
                    }
                    String playList = TextUtils.join("#", vodItems);
                    vodAtom.put("vod_play_url", playList);
                } else {
                    vodAtom.put("vod_play_url", "立即播放$" + url);
                }
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", VodName);
                vodAtom.put("vod_pic", "https://img2.baidu.com/it/u=2655029475,2190949369&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=593");
                vodAtom.put("type_name", "腾讯视频");
                vodAtom.put("vod_year", "");
                vodAtom.put("vod_area", "");
                vodAtom.put("vod_remarks", "");
                vodAtom.put("vod_actor", "");
                vodAtom.put("vod_director", "");
                vodAtom.put("vod_content", url);
                vodAtom.put("vod_play_from", "jx");
                lists.put(vodAtom);
                result.put("list", lists);
                return result.toString();
            } else if (Misc.isVip(url) && url.contains("mgtv.com")) {
                List<String> vodItems = new ArrayList<>();
                JSONObject result = new JSONObject();
                JSONArray lists = new JSONArray();
                JSONObject vodAtom = new JSONObject();
                Pattern mgtv = Pattern.compile("https://\\S+mgtv.com/b/(\\d+)/(\\d+).html.*");
                Matcher mgtv1 = mgtv.matcher(url);
                String VodNames = "";
                if (mgtv1.find()) {
                    String Ep = "https://pcweb.api.mgtv.com/episode/list?video_id=" + mgtv1.group(2);
                    JSONObject Data = new JSONObject(OkHttpUtil.string(Ep, getHeaders2()));
                    VodNames = Data.getJSONObject("data").getJSONObject("info").getString("title");
                    JSONArray a = new JSONArray(Data.getJSONObject("data").getString("list"));
                    if (a.length() > 0) {
                        for (int i = 0; i < a.length(); i++) {
                            JSONObject jObj = a.getJSONObject(i);
                            if (jObj.getString("isIntact").equals("1")) {
                                String VodName = jObj.getString("t4");
                                String id = jObj.getString("video_id");
                                String VodId = "https://www.mgtv.com/b/" + mgtv1.group(1) + "/" + id + ".html";
                                vodItems.add(VodName + "$" + VodId);
                            }
                        }
                        String playList = TextUtils.join("#", vodItems);
                        vodAtom.put("vod_play_url", playList);
                    } else {
                        vodAtom.put("vod_play_url", "立即播放$" + url);
                    }
                }
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", VodNames);
                vodAtom.put("vod_pic", "https://img2.baidu.com/it/u=2562822927,704100654&fm=253&fmt=auto&app=138&f=JPEG?w=600&h=380");
                vodAtom.put("type_name", "芒果视频");
                vodAtom.put("vod_year", "");
                vodAtom.put("vod_area", "");
                vodAtom.put("vod_remarks", "");
                vodAtom.put("vod_actor", "");
                vodAtom.put("vod_director", "");
                vodAtom.put("vod_content", url);
                vodAtom.put("vod_play_from", "jx");
                lists.put(vodAtom);
                result.put("list", lists);
                return result.toString();
            } else if (Misc.isVideoFormat(url)) {
                JSONObject result = new JSONObject();
                JSONArray list = new JSONArray();
                JSONObject vodAtom = new JSONObject();
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", url);
                vodAtom.put("vod_pic", "https://pic.rmb.bdstatic.com/bjh/1d0b02d0f57f0a42201f92caba5107ed.jpeg");
                vodAtom.put("type_name", "直连");
                vodAtom.put("vod_play_from", "player");
                vodAtom.put("vod_play_url", "立即播放$" + url);
                list.put(vodAtom);
                result.put("list", list);
                return result.toString();
            } else if (url.startsWith("magnet:")) {
                String name = "";
                Matcher matcher = Pattern.compile("(^|&)dn=([^&]*)(&|$)").matcher(URLDecoder.decode(url));
                if (matcher.find()) {
                    name = matcher.group(2);
                }
                JSONObject result = new JSONObject();
                JSONArray list = new JSONArray();
                JSONObject vodAtom = new JSONObject();
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", !name.equals("") ? name : url);
                vodAtom.put("vod_pic", "https://pic.rmb.bdstatic.com/bjh/1d0b02d0f57f0a42201f92caba5107ed.jpeg");
                vodAtom.put("type_name", "磁力链接");
                vodAtom.put("vod_content", url);
                vodAtom.put("vod_play_from", "magnet");
                vodAtom.put("vod_play_url", "立即播放$" + url);
                list.put(vodAtom);
                result.put("list", list);
                return result.toString();
            } else if (regexAli.matcher(url).find()) {
                Matcher matcher = regexAliFolder.matcher(url);
                if (!matcher.find()) {
                    return "";
                }
                String shareId = matcher.group(1);
                String fileId = matcher.groupCount() == 3 ? matcher.group(3) : "";
                JSONObject json = new JSONObject();
                json.put("share_id", shareId);
                JSONObject shareLinkJson = new JSONObject(postJson("https://api.aliyundrive.com/adrive/v3/share_link/get_share_by_anonymous", json.toString(), getHeaders()));
                JSONArray fileInfoLists = shareLinkJson.getJSONArray("file_infos");
                if (fileInfoLists.length() == 0) {
                    return "";
                }
                JSONObject fileInfo = null;
                if (!TextUtils.isEmpty(fileId)) {
                    for (int i = 0; i < fileInfoLists.length(); i++) {
                        JSONObject item = fileInfoLists.getJSONObject(i);
                        if (item.getString("file_id").equals(item.getString("file_id"))) {
                            fileInfo = item;
                            break;
                        }
                    }
                } else {
                    fileInfo = fileInfoLists.getJSONObject(0);
                    fileId = fileInfo.getString("file_id");
                }
                JSONObject vodAtom = new JSONObject();
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", shareLinkJson.getString("share_name"));
                vodAtom.put("vod_pic", shareLinkJson.getString("avatar"));
                vodAtom.put("vod_content", url);
                vodAtom.put("type_name", "阿里云盘");
                ArrayList<String> vodItems = new ArrayList<>();
                if (!fileInfo.getString("type").equals("folder")) {
                    if (!fileInfo.getString("type").equals("file") || !fileInfo.getString("category").equals("video")) {
                        return "";
                    }
                    fileId = "root";
                }
                String shareTk = getShareTk(shareId, "");
                Map<String, String> hashMap = new HashMap<>();
                listFiles(hashMap, shareId, shareTk, fileId);
                ArrayList<String> arrayList2 = new ArrayList<>(hashMap.keySet());
                Collections.sort(arrayList2);
                for (String item : arrayList2) {
                    vodItems.add(item + "$" + hashMap.get(item));
                }
                if(vodItems.size()>0){
                    ArrayList<String> playLists = new ArrayList<>();
                    playLists.add(TextUtils.join("#", vodItems));
                    playLists.add(TextUtils.join("#", vodItems));
                    vodAtom.put("vod_play_url", TextUtils.join("$$$", playLists));
                    vodAtom.put("vod_play_from", "AliYun$$$AliYun原画");
                }
                JSONObject result = new JSONObject();
                JSONArray list = new JSONArray();
                list.put(vodAtom);
                result.put("list", list);
//                Map<String,String> o = new HashMap<>();
//                o.put("share_id",shareId);
//                o.put("file_id",hashMap.get(arrayList2.get(0)).split("\\+")[2]);
//                getRefreshTk();
//                File(o);
                return result.toString();

            } else if (url.startsWith("http://") || url.startsWith("https://")) {
                JSONObject result = new JSONObject();
                JSONArray list = new JSONArray();
                JSONObject vodAtom = new JSONObject();
                vodAtom.put("vod_id", url);
                vodAtom.put("vod_name", url);
                vodAtom.put("vod_pic", "https://pic.rmb.bdstatic.com/bjh/1d0b02d0f57f0a42201f92caba5107ed.jpeg");
                vodAtom.put("type_name", "网页");
                vodAtom.put("vod_play_from", "parse");
                vodAtom.put("vod_play_url", "立即播放$" + url);
                list.put(vodAtom);
                result.put("list", list);
                return result.toString();
            }
        } catch (Exception e) {
            SpiderDebug.log(e);
            return "";
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) {
        try {
            if (flag.equals("jx")) {
                JSONObject result = new JSONObject();
                result.put("parse", 1);
                result.put("jx", "1");
                result.put("url", id);
                return result.toString();
            } else if (flag.equals("parse")) {
                JSONObject result = new JSONObject();
                result.put("parse", 1);
                result.put("playUrl", "");
                result.put("url", id);
                return result.toString();
            } else if (flag.equals("player") || flag.equals("magnet")) {
                JSONObject result = new JSONObject();
                result.put("parse", 0);
                result.put("playUrl", "");
                result.put("url", id);
                return result.toString();
            }else if (flag.equals("AliYun")) {
                String[] split = id.split("\\+");
                String videoUrl = Proxy.localProxyUrl() + "?do=ali&type=m3u8&share_id=" + split[0] + "&file_id=" + split[2];
                JSONObject result = new JSONObject();
                result.put("parse", "0");
                result.put("playUrl", "");
                result.put("url", videoUrl);
                result.put("header", "");
                return result.toString();
            }else if (flag.equals("AliYun原画")) {
                String[] split = id.split("\\+");
                String url = getOriginalVideoUrl(split[0], split[1], split[2], split[3]);
                Map<String, List<String>> headerMap = new HashMap<>();
                OkHttpUtil.stringNoRedirect(url, getHeaders(), headerMap);
                String videoUrl = OkHttpUtil.getRedirectLocation(headerMap);
                JSONObject result = new JSONObject();
                result.put("parse", "0");
                result.put("playUrl", "");
                result.put("url", videoUrl);
                result.put("header", new JSONObject(getHeaders()).toString());
                return result.toString();

            }
        } catch (Throwable throwable) {

        }
        return "";
    }

    /**
     * 搜索
     *
     * @param key
     * @param quick 是否播放页的快捷搜索
     * @return
     */
    @Override
    public String searchContent(String key, boolean quick) {
        try {
            String trim = key.trim();
            if (!regexAli.matcher(trim).find()) {
                return "";
            }
            JSONArray videos = new JSONArray();
            JSONObject v = new JSONObject();
            v.put("vod_id", trim);
            v.put("vod_name", trim);
            videos.put(v);
            JSONObject result = new JSONObject();
            result.put("list", videos);
            return result.toString();
        } catch (Exception e2) {
            SpiderDebug.log(e2);
            return "";
        }
    }
}
