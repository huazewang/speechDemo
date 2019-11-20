package com.iflytek.voicedemo;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.util.JsonParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.HashMap;
import java.util.LinkedHashMap;

import static com.iflytek.voicedemo.MainActivity.mResultText;

public class VoiceUtil {
    private Context context = null;
    private boolean biaodian = false;
    private Toast mToast;
    private RecognizerDialog mIatDialog;
    private SpeechSynthesizer mTts;
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    private VoiceListener voiceListener;
    private static String TAG = IatDemo.class.getSimpleName();
    private String resultType = "json";
    private StringBuffer buffer = new StringBuffer();
    private boolean mTranslateEnable = false;
//    Handler han = new Handler(){
//
//        @Override
//        public void handleMessage(Message msg) {
//            super.handleMessage(msg);
//            if (msg.what == 0x001) {
//                executeStream();
//            }
//        }
//    };
    public VoiceUtil(Context context, boolean biaodian, VoiceListener voiceListener) {
        this.context = context;
        this.biaodian = biaodian;
        this.voiceListener = voiceListener;
        String Tag="VoiceUtil";

        initSetting();
        initTTSSetting();
    }

    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
        }
    };

    public void voiceShow() {
        if (!mIatDialog.isShowing()) {
            mIatDialog.show();
            //获取字体所在的控件，设置为"",隐藏字体，
            TextView txt = (TextView)mIatDialog.getWindow().getDecorView().findViewWithTag("textlink");
            txt.setText("");
        }
    }

    public void vSpeaking(String voicer, String SPcontent) {
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        mTts.startSpeaking(SPcontent.toString().trim(), mSynListener);
    }

    public void vStopSpeaking() {
        mTts.stopSpeaking();
    }

    //初始化监听
    private InitListener mTtsInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
        }
    };

    //合成监听器
    private SynthesizerListener mSynListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            Log.i("Tag","开始播放");
        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {
            Log.i("Tag","暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            Log.i("Tag","继续播放");
        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {

        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };
    private void printTransResult (RecognizerResult results) {
        String trans  = JsonParser.parseTransResult(results.getResultString(),"dst");
        String oris = JsonParser.parseTransResult(results.getResultString(),"src");

        if( TextUtils.isEmpty(trans)||TextUtils.isEmpty(oris) ){
            Log.d(TAG,"解析结果失败，请确认是否已开通翻译功能。");
        }else{
            Log.d(TAG,"原始语言:\n"+oris+"\n目标语言:\n"+trans);
        }

    }
    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        try {
            JSONObject resultJson = new
                    JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        if (voiceListener != null) {
            voiceListener.getVoiceResult(resultBuffer.toString());
        }
    }
    /**
     * 听写UI监听器
     */
    private  RecognizerDialogListener recognizerDialogListener = new RecognizerDialogListener() {


        @Override
        public void onResult(RecognizerResult results, boolean b) {
            if( mTranslateEnable ){
                printTransResult( results );
            }else{
                printResult(results);
            }
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    };
private void initSetting() {
    mIatDialog = new RecognizerDialog(context, mInitListener);
    //mIatDialog.setParameter(SpeechConstant.PARAMS, null);
    // 设置听写引擎
    mIatDialog.setParameter(SpeechConstant.ENGINE_TYPE,SpeechConstant.TYPE_CLOUD);
    // 设置返回结果格式
    mIatDialog.setParameter(SpeechConstant.RESULT_TYPE, "json");
    mIatDialog.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
    mIatDialog.setParameter(SpeechConstant.ACCENT, "mandarin");
    // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
    mIatDialog.setParameter(SpeechConstant.VAD_BOS, "3000");
    // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音    即用户多长时间不说话则当做超时处理1000~10000
    mIatDialog.setParameter(SpeechConstant.VAD_EOS, "1000");
    // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
    mIatDialog.setParameter(SpeechConstant.ASR_PTT, biaodian ? "1" : "0");
    // 设置音频保存路径，保存音频格式仅为pcm，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
    mIatDialog.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory()+ "/iflytek/wavaudio.pcm");
    // 设置听写结果是否结果动态修正，为“1”则在听写过程中动态递增地返回结果，否则只在听写结束之后返回最终结果
    mIatDialog.setParameter(SpeechConstant.ASR_DWA, "0");
    mIatDialog.setListener(recognizerDialogListener);
    //--------------------------------------------------------------------------
    mIatDialog.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
    mIatDialog.setParameter(SpeechConstant.ASR_AUDIO_PATH, getWavFilePath());
}
    private void initTTSSetting() {
        mTts= SpeechSynthesizer.createSynthesizer(context, mTtsInitListener);

        //mTts.setParameter(SpeechConstant.VOICE_NAME, voicer); // 设置发音人
        mTts.setParameter(SpeechConstant.SPEED, "45");//设置语速
        mTts.setParameter(SpeechConstant.PITCH, "50");//设置音调
        mTts.setParameter(SpeechConstant.VOLUME, "90");//设置音量，范围 0~100
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
        //----------------------------------------------------------
        //mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");  // 设置播放器音频流类型
    }
    public static String parseIatResult(String json) {
        StringBuffer ret = new StringBuffer();
        try {
            JSONTokener tokener = new JSONTokener(json);
            JSONObject joResult = new JSONObject(tokener);
            JSONArray words = joResult.getJSONArray("ws");
            for (int i = 0; i < words.length(); i++) {
                JSONArray items = words.getJSONObject(i).getJSONArray("cw");
                JSONObject obj = items.getJSONObject(0);
                ret.append(obj.getString("w"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret.toString();
    }
    /**
     * 判断是否有外部存储设备sdcard
     */
    public static boolean isSdcardExit(){
        if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            return true;
        else
            return false;
    }
    public static String getWavFilePath(){
        String mAudioWavPath = "query.wav";
        if(isSdcardExit()){
            String fileBasePath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mAudioWavPath = fileBasePath+"/"+"query.wav";
        }
        return mAudioWavPath;
    }
    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }
}
