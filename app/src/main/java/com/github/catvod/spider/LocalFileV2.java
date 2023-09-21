package com.github.catvod.spider;

import android.content.Context;
import android.os.Environment;
import com.github.catvod.crawler.Spider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 基于原来的 LocalFile.java
 * 和参考 FongMi 的 CatVodSpider 项目的 Local.java 修改而来
 * 支持配置文件传入 ext 参数来决定是否显示隐藏文件
 * 文件播放时获取当前目录下的媒体文件按照名称顺序播放
 */
public class LocalFileV2 extends Spider {

    private final String defaultMediaPic = "https://cdn.jsdelivr.net/gh/zhixc/CatVodTVSpider/pic/video.png@User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36";
    private final String defaultFolderPic = "https://cdn.jsdelivr.net/gh/zhixc/CatVodTVSpider/pic/folder.png@User-Agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36";

    private boolean showAllFile = true;
    private final List<String> media = Arrays.asList("mp4", "mkv", "wmv", "flv", "avi", "mp3", "aac", "flac", "m4a", "ape", "ogg", "rmvb", "ts");

    @Override
    public void init(Context context, String extend) throws Exception {
        super.init(context, extend);
        if (extend != null && extend.equals("showAllFile=false")) showAllFile = false;
    }

    @Override
    public String homeContent(boolean filter) throws Exception {
        JSONObject filterConfig = new JSONObject();
        JSONArray classes = new JSONArray();
        JSONObject newCls = new JSONObject();
        String root = Environment.getExternalStorageDirectory().getAbsolutePath();
        newCls.put("type_id", root);
        newCls.put("type_name", "本地文件");
        // type_flag 是扩展字段，目前可取值0、1、2，该字段不存在时表示正常模式
        newCls.put("type_flag", "1"); // 1、列表形式的文件夹 2、缩略图 0或者不存在表示正常模式
        classes.put(newCls);

        // 补充 支持外部存储路径 start，参考了 FongMi 的 CatVodSpider 项目
        File[] files = new File("/storage").listFiles();
        if (files != null) {
            List<String> exclude = Arrays.asList("emulated", "sdcard", "self");
            for (File file : files) {
                if (exclude.contains(file.getName())) continue;
                JSONObject obj = new JSONObject()
                        .put("type_id", file.getAbsolutePath())
                        .put("type_name", file.getName())
                        .put("type_flag", "1");
                classes.put(obj);
            }
        }
        // 补充 支持外部存储路径 end

        JSONArray jSONArray3 = new JSONArray();

        JSONObject jSONObject4 = new JSONObject();
        jSONObject4.put("class", classes);
        if (filter) {
            jSONObject4.put("filters", new JSONObject("{}"));
        }
        return jSONObject4.toString();
    }

    @Override
    public String categoryContent(String tid, String pg, boolean filter, HashMap<String, String> extend) throws Exception {
        File file = new File(tid);
        File[] list = file.listFiles();
        Arrays.sort(list);
        JSONArray jSONArray2 = new JSONArray();
        for (File f : list) {
            String filename = f.getName();
            if (!showAllFile && filename.indexOf('.') == 0) continue; // 过滤掉隐藏文件、隐藏文件夹
            //String pic = "https://img.tukuppt.com/png_preview/00/18/23/GBmBU6fHo7.jpg!/fw/260";
            String pic = defaultFolderPic;
            if (!f.isDirectory()) {
                //pic = "https://img.tukuppt.com/png_preview/00/42/50/3ySGW7mvyY.jpg!/fw/260";
                pic = defaultMediaPic;
            }
            JSONObject jSONObject2 = new JSONObject();
            jSONObject2.put("vod_id", f.getAbsolutePath());
            jSONObject2.put("vod_name", f.getName());
            jSONObject2.put("vod_pic", pic);
            // 当 vod_tag 为 folder 时会点击该 item 会把当前 vod_id 当成新的类型 ID 重新进
            jSONObject2.put("vod_tag", f.isDirectory() ? "folder" : "file");
            jSONObject2.put("vod_remarks", fileTime(f.lastModified(), "yyyy/MM/dd aHH:mm:ss"));
            jSONArray2.put(jSONObject2);
        }

        JSONObject jSONObject3 = new JSONObject();
        jSONObject3.put("page", 1);
        jSONObject3.put("pagecount", 1);
        jSONObject3.put("limit", jSONArray2.length());
        jSONObject3.put("total", jSONArray2.length());
        jSONObject3.put("list", jSONArray2);
        return jSONObject3.toString();
    }

    private String fileTime(long time, String fmt) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(date);
    }

    @Override
    public String detailContent(List<String> ids) throws Exception {
        String filename = ids.get(0);
        File f = new File(filename);
        StringBuilder vod_play_url = new StringBuilder();
        File parentFile = f.getParentFile();
        String name = f.getName();
        if (parentFile != null) {
            File[] files = parentFile.listFiles();
            name = parentFile.getName();
            if (files != null) {
                Arrays.sort(files);
                for (File file : files) {
                    if (file.isDirectory()) continue;
                    String fileName2 = file.getName();
                    String suffix = getFileExt(fileName2);
                    if (suffix.equals("")) continue;
                    if (!media.contains(suffix)) continue;
                    vod_play_url.append(fileName2).append("$").append(file.getAbsolutePath()).append("#");
                }
            }
        } else {
            vod_play_url.append(name).append("$").append(filename);
        }
        JSONObject vod = new JSONObject();
        vod.put("vod_id", ids.get(0));
        vod.put("vod_name", name);
        vod.put("vod_pic", defaultMediaPic);
        vod.put("type_name", "本地文件");
        vod.put("vod_content", "当前文件所在目录：" + f.getParent());
        if (vod_play_url.length() > 0) {
            vod.put("vod_play_from", "播放");
            vod.put("vod_play_url", vod_play_url);
        }
        JSONObject result = new JSONObject();
        JSONArray list = new JSONArray();
        list.put(vod);
        result.put("list", list);
        return result.toString();
    }

    private static String getFileExt(String name) {
        try {
            return name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return "";
    }

    @Override
    public String playerContent(String flag, String id, List<String> vipFlags) throws Exception {
        JSONObject result = new JSONObject();
        result.put("parse", 0);
        result.put("playUrl", "");
        result.put("url", id);
        return result.toString();
    }
}
