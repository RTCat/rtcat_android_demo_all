package com.shishimao.demo_all;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import android.app.ActionBar;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shishimao.sdk.LooperExecutor;
import com.shishimao.sdk.RTCat;
import com.shishimao.sdk.Receiver;
import com.shishimao.sdk.ReceiverObserver;
import com.shishimao.sdk.Sender;
import com.shishimao.sdk.Session;
import com.shishimao.sdk.SessionObserver;
import com.shishimao.sdk.SessionSendConfig;
import com.shishimao.sdk.Stream;
import com.shishimao.sdk.http.RTCatRequests;
import com.shishimao.sdk.tools.L;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;


public class MainActivity extends Activity {

    HashMap<String,Sender> senders = new HashMap<>();
    HashMap<String,Receiver> receivers = new HashMap<>();
    HashMap<String,GLSurfaceView> glList = new HashMap<>();
    LooperExecutor executor = new LooperExecutor();
    EditText editText;
    TextView textView;
    Stream localStream;

    GLSurfaceView videoView;
    GLSurfaceView videoViewRemote;

    Session session;
    int x = 0;
    int y = 0;
    RTCat cat;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = (TextView)findViewById(R.id.tv_content);
        textView.setMovementMethod(ScrollingMovementMethod.getInstance());

        editText = (EditText)findViewById(R.id.et_input);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEve ) {
                if (i == KeyEvent.ACTION_DOWN || i == EditorInfo.IME_ACTION_SEND) {
                    sendMessage();
                }
                return true;
            }
        });
        videoView = (GLSurfaceView)findViewById(R.id.glview);

        executor.requestStart();

        cat = new RTCat(MainActivity.this,true,true,true, L.VERBOSE);

        localStream = cat.createStream();
        localStream.play(videoView);

    }


    public void createSession(View view)
    {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    RTCatRequests requests = new RTCatRequests(Config.APIKEY,Config.SECRET);
                    String token  = requests.getToken(Config.SESSIONID,"pub");
                    session = cat.createSession(token);

                    class SessionHandler implements SessionObserver
                    {
                        @Override
                        public void in(String token) {
                            l(token + " is in");

                            JSONObject attr = new JSONObject();
                            try{
                                attr.put("type","main");
                                attr.put("name","old wang");
                            }catch (Exception e)
                            {

                            }

                            SessionSendConfig ssc = new SessionSendConfig(localStream,attr,true);
                            session.sendTo(ssc, token);
                        }

                        @Override
                        public void out(final String token) {

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    t(token + " is out");

                                    LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);

                                    layout.removeView(glList.get(token));
                                }

                            });


                        }

                        @Override
                        public void connected(JSONArray wits) {
                            l("connected main");

                            String wit = "";
                            for(int i=0;i<wits.length();i++)
                            {
                                try {
                                    wit = wit+ wits.getString(i);

                                }catch (Exception e)
                                {
                                    l(e.toString());
                                }
                            }


                            JSONObject attr = new JSONObject();
                            try{
                                attr.put("type","main");
                                attr.put("name","old wang");
                            }catch (Exception e)
                            {

                            }

                            SessionSendConfig ssc = new SessionSendConfig(localStream,attr,true);
                            session.send(ssc);
                        }

                        @Override
                        public void remote(final Receiver receiver) {
                            try {
                                final String name = receiver.getAttr().getString("name");
                                final String token = receiver.getFrom();
                                receivers.put(receiver.getId(),receiver);

                                receiver.addObserver(new ReceiverObserver() {
                                    @Override
                                    public void stream(final Stream stream) {
                                        t(receiver.getFrom() + " st//ream");
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                videoViewRemote = new GLSurfaceView(MainActivity.this);

                                                glList.put(token,videoViewRemote);
                                                LinearLayout layout = (LinearLayout) findViewById(R.id.main_layout);
                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ActionBar.LayoutParams.MATCH_PARENT, 0, 3);
                                                layout.addView(videoViewRemote, params);
                                                stream.play(videoViewRemote);
                                            }
                                        });

                                    }

                                    @Override
                                    public void message(final String message) {

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        JSONObject data = new JSONObject(message);
                                                        String mes = data.getString("content");
                                                        textView.append(name + ":" + mes + "\n");
                                                    }catch (Exception e)
                                                    {
                                                        e.printStackTrace();
                                                    }

                                                }
                                            });


                                    }

                                    @Override
                                    public void close() {

                                    }
                                });

                                receiver.response();
                            }catch (Exception e)
                            {
                                l(e.toString());
                            }


                        }

                        @Override
                        public void local(Sender sender) {
                            senders.put(sender.getId(),sender);
                        }

                        @Override
                        public void message(String token, String message) {

                        }

                        @Override
                        public void error(String error) {

                        }
                    }

                    SessionHandler sh = new SessionHandler();

                    session.addObserver(sh);

                    session.connect();

                }catch (Exception e)
                {
                    l(e.toString());
                }
            }
        });

    }

    public void upLocalStream(View view)
    {
        t("up");
        localStream.update(50,50,50,50);
    }

    public void sendMessage()
    {
        String message = editText.getText().toString().trim();
        JSONObject packet = new JSONObject();

        try {
            packet.put("type","message");
            packet.put("content",message);
        }catch (Exception e)
        {

        }

        editText.setText("");

        for (Sender sender:senders.values())
        {
            sender.sendMessage(packet);
        }

        textView.append("self : " + message + "\n");
    }

    public void switchCamera(View view)
    {
        localStream.switchCamera();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        //按下键盘上返回按钮
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }else{
            return super.onKeyDown(keyCode, event);
        }
    }

    public void l(String o)
    {

        Log.d("RTCatLog", o);
    }


    public void t(String o)
    {
        Toast.makeText(MainActivity.this, o,
                Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (session != null)
        {
            session.disconnect();
        }

        if(localStream != null)
        {
            localStream.dispose();
        }
    }
}
