package com.iflytek.voicedemo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private VoiceUtil voice;
    private Context context = null;
    public static EditText mResultText;
    private TextView txtView;
    private StringBuffer buffer = new StringBuffer();
    public String voicer="xiaoqi";
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = this;
        mResultText = ((EditText) findViewById(R.id.iat_text));
        txtView=(TextView) findViewById(R.id.textView);
        findViewById(R.id.button).setOnClickListener(this);
        findViewById(R.id.button2).setOnClickListener(this);
        showVoiceListener();
    }
    private void showVoiceListener()
    {
        mResultText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }
        });
        voice = new VoiceUtil(this.context,false,new VoiceListener() {
            public void getVoiceResult(String result) {
                mResultText.setText(null);
                mResultText.setText(result);
                txtView.setText("");
                txtView.setText(result);
                if(!result.equals("")|result!=null)
                {
                    new Handler().postDelayed(new Runnable(){
                        @Override
                        public void run()    {
//                            TtsPlay();
                              voice.vSpeaking(voicer,mResultText.getText().toString());
                        }}, 1000);
                }
            }
        });
    }
    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId())
        {
            case R.id.button:
//                intent = new Intent(MainActivity.this, IatDemo.class);
                buffer.setLength(0);
                mResultText.setText(null);// 清空显示内容
                mIatResults.clear();
                voice.voiceShow();
                break;
            case R.id.button2:
                // 语音合成
                intent = new Intent(MainActivity.this, TtsDemo.class);
                break;
            default:
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
