package com.zeusee.main.hyperlandmark;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ResourceDecoder
 * @Description TODO
 * @Author : kevin
 * @Date 2021/02/03 16:19
 * @Version 1.0
 */
public class ResourceDecoder {
    protected static final String TAG = "ResourceDecoder";
    protected ByteBuffer mDataBuffer;
    private String mDataPath;

    public ResourceDecoder(String dataPath){
        mDataPath = dataPath;
    }

    public void init() throws IOException {
        File file = new File(mDataPath);
        mDataBuffer = ByteBuffer.allocateDirect((int) file.length());
        FileInputStream inputStream = new FileInputStream(file);
        byte[] buffer = new byte[2048];
        boolean result = false;
        try{
            int length;
            while ((length=inputStream.read(buffer))!=-1){
                mDataBuffer.put(buffer, 0, length);
            }
            result = true;
        } catch (IOException e){
            Log.e(TAG, "init", e);
        } finally {
            if (inputStream != null){
                inputStream.close();
            }
        }
        if(!result){
            throw new IOException("Failed to parse data file!");
        }
    }

    public static Pair<String, String> getResourceFile(String folder) {
        String index = null;
        String data = null;
        File file = new File(folder);
        String[] list = file.list();
        if(list == null){
            return null;
        }
        for(int i=0;i<list.length;i++){
            if(list[i].equals("index.idx")){
                index = list[i];
            }
            else if(list[i].equals("resource.res")) {
                data = list[i];
            }
        }
        if(TextUtils.isEmpty(index) && !TextUtils.isEmpty(data)) {
            return new Pair<>(index, data);
        }
        else {
            return null;
        }
    }

    public static List<FaceStickerJson> decodeStickerData(Context context, String folderPath)
            throws IOException, JSONException {

        InputStream is = context.getAssets().open(folderPath);
        String stickerJson = convertToString(is);

        JSONObject jsonObject = new JSONObject(stickerJson);
        List<FaceStickerJson> dynamicSticker = new ArrayList<>();

        JSONArray stickerList = jsonObject.getJSONArray("stickerList");
        for (int i = 0; i < stickerList.length(); i++) {
            JSONObject jsonData = stickerList.getJSONObject(i);
            FaceStickerJson data;
            data = new FaceStickerJson();
            JSONArray centerIndexList = jsonData.getJSONArray("centerIndexList");
            data.centerIndexList = new int[centerIndexList.length()];
            for (int j = 0; j < centerIndexList.length(); j++) {
                data.centerIndexList[j] = centerIndexList.getInt(j);
            }
            data.offsetX = (float) jsonData.getDouble("offsetX");
            data.offsetY = (float) jsonData.getDouble("offsetY");
            data.baseScale = (float) jsonData.getDouble("baseScale");
            data.startIndex = jsonData.getInt("startIndex");
            data.endIndex = jsonData.getInt("endIndex");
            data.width = jsonData.getDouble("width");
            data.height = jsonData.getDouble("height");
            data.deep = jsonData.getDouble("deep");
            data.frames = jsonData.getInt("frames");
            data.action = jsonData.getInt("action");
            data.stickerName = jsonData.getString("stickerName");
            data.readType = jsonData.getString("readType");
            data.duration = jsonData.getInt("duration");
            data.stickerLooping = (jsonData.getInt("stickerLooping") == 1);
            data.maxCount = jsonData.optInt("maxCount", 5);

            dynamicSticker.add(data);
        }
        return dynamicSticker;
    }

    public static String convertToString(InputStream inputStream)
            throws IOException {
        BufferedReader localBufferedReader = new BufferedReader(new InputStreamReader(inputStream, "utf-8"));

        StringBuilder localStringBuilder = new StringBuilder();
        String str;
        while ((str = localBufferedReader.readLine()) != null) {
            localStringBuilder.append(str).append("\n");
        }
        return localStringBuilder.toString();
    }
}
