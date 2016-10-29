package com.lin.mymusic;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
//import android.support.v4.widget.SearchViewCompatIcs;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//Tools--> android-->Device monitor  音乐文件放入sd卡
/*
播放音乐方法为：start();

停止音乐播放的方法为：stop();

控制音乐播放位置为：seekTo(int value);

获得音乐长度为：getDuration();

获得现在播放的位置：getCurrentPosition();

 // 设置音乐播放完的监听
    mediaPlayer.setOnCompletionListener(this);
 */
public class MainActivity extends AppCompatActivity {

    private static int p=0;
    private EditText mEditText;
    private SeekBar mSeekBar;
    private String path;
    private MediaPlayer mMediaPlayer;//多媒体播放类
    private boolean pause; //标记是否暂停
    private int position;//用于记录播放进度
    private List<String> list;
    private String[] strings;
 /*   private  Handler handler;
    private Runnable updateThread;*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        list=new ArrayList<>();
        mMediaPlayer=new MediaPlayer();
        mEditText= (EditText) findViewById(R.id.filename);
        mSeekBar= (SeekBar) findViewById(R.id.seekbar);
//        mSeekBar.setMax(mMediaPlayer.getDuration());//获取歌曲长度做为进度条最大值

        TelephonyManager telephonyManager= (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);  //监听电话
        telephonyManager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);

        search();
        init();


/*
        handler = new Handler();
        updateThread = new Runnable(){
            public void run() {
                //获得歌曲现在播放位置并设置成播放进度条的值
                mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
                //每次延迟100毫秒再启动线程
                // handler.postDelayed(updateThread, 100);
            }
        };*/


    }


    Handler handler = new Handler();
    Runnable updateThread = new Runnable(){
        public void run() {
            //获得歌曲现在播放位置并设置成播放进度条的值
            mSeekBar.setProgress(mMediaPlayer.getCurrentPosition());
            //每次延迟100毫秒再启动线程
            handler.postDelayed(updateThread, 100);
        }
    };



    private void init(){
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {  //seekBar进度监听
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                // fromUser判断是用户改变的滑块的值
                if (fromUser == true) {
                    position=progress;
                    int po=mSeekBar.getProgress();
                   // int p=(po/mSeekBar.getMax())*mMediaPlayer.getDuration();
                    mMediaPlayer.seekTo(po);

                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { //拖动结束调用
                seekBar.setProgress(seekBar.getProgress());

                int test=seekBar.getProgress();

                int max=mMediaPlayer.getDuration();// 歌曲进度的最大值

                mMediaPlayer.seekTo((int)(max*(seekBar.getProgress()/(double)seekBar.getMax())));
            }
        });  //seekBar监听结束
    }//init


    public void search(){

        File[] file = Environment.getExternalStorageDirectory().listFiles();  //获取sd0卡目录所有文件
        if(file!=null) {
            for (int i=0;i<file.length;i++) {
                String s = file[i].getAbsolutePath();
                if(s.toLowerCase().endsWith(".mp3")){  //检索得到MP3文件
                    list.add(s);
                }
            }

            for(int j=0;j<list.size();j++){
                Log.i("------>",list.get(j));
            }
        }
    }

    private final class MyPhoneListener extends PhoneStateListener{
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {  //在来点时暂停播放，通话结束继续播放
            switch (state){
                case TelephonyManager.CALL_STATE_RINGING: //来电话了
                    if(mMediaPlayer.isPlaying()){
                        position=mMediaPlayer.getCurrentPosition();//获取当前播放进度
                        mMediaPlayer.stop();//当播放器activity不在前台时暂停播放
                    }
                    break;
                case TelephonyManager.CALL_STATE_IDLE: //结束通话
                    if(position>0&&path!=null){
                        play();
                        mMediaPlayer.seekTo(position); //
                        position=0;
                       // play();
                      //  position=0;
                    }
                break;
            }//switch
        }
    } //class


    /*
    @Override
    protected void onPause() {  //当前activity不在前台时被调调用
        if(mMediaPlayer.isPlaying()){
            position=mMediaPlayer.getCurrentPosition();//获取当前播放进度
            mMediaPlayer.stop();//当播放器activity不在前台时暂停播放
        }
        super.onPause();
    }

    @Override
    protected void onResume() { //activity继续回到前台时被调用
        if(position>0&&path!=null){
            play();
            mMediaPlayer.seekTo(position); //
            position=0;
        }
        super.onResume();
    }
*/


    @Override
    protected void onDestroy() {
        mMediaPlayer.release();  //摧毁播放类对象
        mMediaPlayer=null;
        super.onDestroy();
    }

    public void mediaplay(View view) {
        switch (view.getId()){
            case R.id.playbutton:
               // String filename=mEditText.getText().toString();//得到文件名
             //   File audio=new File(Environment.getExternalStorageDirectory(),filename);
              //  if(audio.exists()){ //发现文件
                //    path=audio.getAbsolutePath();//获得绝对路径
                path=list.get(p);
                    play();//播放
                //}else{
                    //path=null;
                //    Toast.makeText(MainActivity.this,"sorry the file inexistence !",Toast.LENGTH_SHORT).show();
                //}
            break;
            case R.id.pausebutton: //暂停
                    if(mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();//暂停
                        pause=true;//暂停标记
                        ((Button)view).setText("继续"); //将View强转为button类型
                    }else {
                        if(pause){ //如果被暂停过则继续播放
                            mMediaPlayer.start();
                            pause=false;
                            ((Button)view).setText("暂停");
                        }
                    }
                break;
            case R.id.resetbutton:  //重播
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.seekTo(0);//从头播放音乐
                }else{
                    if(path!=null){
                        play();
                    }
                }
                break;
            case R.id.stopbutton:
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();//停止播放
                    break;
                }
                break;
        }
    }

    private void play() {  //自定义播放函数
        mMediaPlayer.reset();//把各项参数恢复到初始状态
        try {
            mMediaPlayer.setDataSource(path);//设置路径
            mMediaPlayer.prepare();//缓冲
            mMediaPlayer.setOnPreparedListener(new PrepareListener()); //传入position
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private final class PrepareListener implements MediaPlayer.OnPreparedListener{
     /*   private int position;
        public PrepareListener(int position) {
            this.position=position;
        }
*/
        @Override
        public void onPrepared(MediaPlayer mediaPlayer) { //缓冲完时调用
        mMediaPlayer.start();//真正实现开始播放
         //   if(position>0) mMediaPlayer.seekTo(position); //只有大于10时有意义，否则不需要
        }
    }



    public void next(View v){
        if(p==(list.size()-1)) p=0;
        path=list.get(++p);
        play();
        mEditText.setText(list.get(p));
    }

    public void up(View v) {
        if(p==0) p=list.size();
        path=list.get(--p);
        play();
        mEditText.setText(list.get(p));
    }


}
