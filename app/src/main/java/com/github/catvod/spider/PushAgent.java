package com.github.catvod.spider;

import com.github.catvod.crawler.Spider;
import com.github.catvod.utils.Misc;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLDecoder;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PushAgent extends Spider {

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String detailURL = ids.get(0).trim();
        if (isThunderSupport(detailURL)) {
            String name = "";
            Matcher matcher = Pattern.compile("(^|&)dn=([^&]*)(&|$)").matcher(URLDecoder.decode(detailURL));
            if (matcher.find()) {
                name = matcher.group(2);
            }
            String finalName = !name.equals("") ? name : detailURL;
            return getResultStr(ids, finalName, "磁力链接|迅雷链接", "magnet");
        } else if (Misc.isVip(detailURL)) {
            return getResultStr(ids, detailURL, "解析类链接", "解析");
        } else if (Misc.isVideoFormat(detailURL)) {
            return getResultStr(ids, detailURL, "可以直接播放的直链", "直连");
        }
        return getResultStr(ids, detailURL, "嗅探类链接", "嗅探");
    }

    private static boolean isThunderSupport(String url) {
        // magnet:?xt=urn:btih:f41aba874ebfe2d2323f6b06e0e4f28dd1b1fab0
        // ed2k://|file|%E3%80%90%E9%AB%98%E6%B8%85MP4%E7%94%B5%E5%B
        return url.toLowerCase().startsWith("magnet:?xt=")
                || url.toLowerCase().startsWith("thunder://")
                || url.toLowerCase().startsWith("ftp://")
                || url.toLowerCase().startsWith("ed2k://");
    }

    private String getResultStr(List<String> ids, String name, String typeName, String vod_play_from) {
        try {
            String url = ids.get(0).trim();
            JSONObject vod = new JSONObject()
                    .put("vod_id", ids.get(0))
                    .put("vod_name", name)
                    .put("vod_pic", "https://pic.rmb.bdstatic.com/bjh/1d0b02d0f57f0a42201f92caba5107ed.jpeg")
                    .put("type_name", typeName)
                    .put("vod_content", url)
                    .put("vod_play_from", vod_play_from)
                    .put("vod_play_url", "立即播放$" + url);
            JSONArray list = new JSONArray().put(vod);
            JSONObject result = new JSONObject().put("list", list);
            return result.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        JSONObject result = new JSONObject();
        switch (flag) {
            case "magnet":
            case "直连":
                result.put("parse", 0);
                break;
            case "解析":
                result.put("parse", 1).put("jx", "1");
                break;
            default: // 默认是嗅探
                result.put("parse", 1);
        }
        result.put("playUrl", "");
        result.put("url", id);
        return result.toString();
    }
}
