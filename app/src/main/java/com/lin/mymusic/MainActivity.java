package com.lin.mymusic;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterViewFlipper;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
public class MainActivity extends Activity implements Runnable{

    private static int p=0;
   // private EditText mEditText;
    private Spinner mSpinner;
    private ArrayAdapter mAdapter;
    private TextView mName;
    private SeekBar mSeekBar;
    private String path;
    private MediaPlayer mMediaPlayer;//多媒体播放类
    private boolean pause; //标记是否暂停
    private int position;//用于记录播放进度
    private List<String> list;
    private List<String> name;
    private ImageButton pauseButton;
    private AdapterViewFlipper mViewFlipper;
    int []iconId={R.mipmap.img_1,R.mipmap.img_2,R.mipmap.img_3,R.mipmap.img_4,R.mipmap.img_5,
            R.mipmap.img_6,R.mipmap.img_7,R.mipmap.img_8,R.mipmap.img_9};
    private TextView textView_name;


 /*   private  Handler handler;
    private Runnable updateThread;*/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);


        list=new ArrayList<>();//用于存放音乐文件路径
        name=new ArrayList<String>();//用于存放歌曲名字
        mMediaPlayer=new MediaPlayer();
    //    mEditText= (EditText) findViewById(R.id.filename);
        mName= (TextView) findViewById(R.id.tv_name);
        mSeekBar= (SeekBar) findViewById(R.id.seekbar);
        pauseButton= (ImageButton) findViewById(R.id.pause_button);
        mViewFlipper= (AdapterViewFlipper) findViewById(R.id.adapterviewflipper);
        textView_name= (TextView) findViewById(R.id.text_name);




        //mSpinner.setOnItemSelectedListener( new SpinnerListener());//点击监听

        BaseAdapter adapter=new BaseAdapter() { //AdapterViewFlipper的适配器
            @Override
            public int getCount() {
                return iconId.length;
            }

            @Override
            public Object getItem(int i) {
                return i;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
               //创建一个View
                ImageView mImageView=new ImageView(MainActivity.this);
                mImageView.setImageResource(iconId[i]);
                //设置ImageView的伸缩类型
                mImageView.setScaleType(ImageView.ScaleType.FIT_XY);
                //为imageView设置布局参数
                mImageView.setLayoutParams(new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        android.app.ActionBar.LayoutParams.MATCH_PARENT));
                return mImageView;
            }
        };
        mViewFlipper.setAdapter(adapter);

//        mSeekBar.setMax(mMediaPlayer.getDuration());//获取歌曲长度做为进度条最大值

        TelephonyManager telephonyManager= (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);  //监听电话
        telephonyManager.listen(new MyPhoneListener(), PhoneStateListener.LISTEN_CALL_STATE);

        search();
        init();
        mMediaPlayer.setOnCompletionListener(new mMediaPlayersetOnCompletionListener());

        //由于以下带码用到search()函数得到的数据所有要在调用search()之后执行，否则出错 //可能是由于线程的缘故
        mSpinner=(Spinner)findViewById(R.id.spinner);
        //1.为下拉列表定义一个数组适配器，这个数组适配器就用到里前面定义的list。装的都是list所添加的内容
        mAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_spinner_item,name);//样式为原安卓里面有的android.R.layout.simple_spinner_item，让这个数组适配器装list内容。
        //2.为适配器设置下拉菜单样式。adapter.setDropDownViewResource
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //3.以上声明完毕后，建立适配器,有关于sipnner这个控件的建立。用到myspinner
        mSpinner.setAdapter(mAdapter);
        mSpinner.setSelection(0, false);
        //4.为下拉列表设置各种点击事件，以响应菜单中的文本item被选中了，用setOnItemSelectedListener
        mSpinner.setOnItemSelectedListener(new SpinnerListener());
                /*Spinner.OnItemSelectedListener() {   //选择item的选择点击监听事件
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                Toast.makeText(MainActivity.this,""+arg2,Toast.LENGTH_SHORT).show();
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });*/

    } //onCreate

    @Override
    public void run() {

    }

    /*class SpinnerSelectedListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            textView_name.setText(""+mArrayAdapter.getItem(i));
        }
    }*/

    class mMediaPlayersetOnCompletionListener implements MediaPlayer.OnCompletionListener{ //一首歌播放结束监听

        @Override
        public void onCompletion(MediaPlayer mediaPlayer) {
            //自动循环播放
            if (p == (list.size() - 1)) p = 0;
            path = list.get(++p);
            if (path != null) {
                if (handler.post(updateThread)) {

                } else {
                    handler.post(updateThread);  //如果没开启多线程则开启  以更新进度条
                }
                play();
                mName.setText(name.get(p));
                mViewFlipper.startFlipping();//开始自动播放图片
            }else {
                mViewFlipper.stopFlipping();//停止播放图片
                Toast.makeText(MainActivity.this,"没有歌曲了哦！",Toast.LENGTH_SHORT).show();
            }
        }
    }

    Handler handler = new Handler(); //多线程
    Runnable updateThread = new Runnable(){
        public void run() {
            //获得歌曲现在播放位置并设置成播放进度条的值
            int max=mSeekBar.getMax();
            mSeekBar.setProgress((int)(max*(mMediaPlayer.getCurrentPosition()/(float) mMediaPlayer.getDuration())));
            //每次延迟100毫秒再启动线程
            handler.postDelayed(updateThread, 100); //单位毫秒
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

                //int test=seekBar.getProgress();

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
                    String str=getFileName(s);
                    name.add(str); //保持歌曲名
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
                if(list!=null) {
                    path = list.get(p);
                    play();//播放
                    handler.post(updateThread);//启动线程
                    mViewFlipper.startFlipping();//图片开始变换
                    //}else{
                    //path=null;
                    //    Toast.makeText(MainActivity.this,"sorry the file inexistence !",Toast.LENGTH_SHORT).show();
                    //}
                }else {
                    Toast.makeText(MainActivity.this,"当前文件夹还没有歌曲哦！",Toast.LENGTH_SHORT).show();
                }
            break;
            case R.id.pause_button: //暂停
                    if(mMediaPlayer.isPlaying()){
                        mMediaPlayer.pause();//暂停
                        pause=true;//暂停标记
                     //   ((Button)view).setText("继续"); //将View强转为button类型
                        pauseButton.setImageResource(R.drawable.stop);
                        mViewFlipper.stopFlipping();//图片停止播放
                    }else {
                        if(pause){ //如果被暂停过则继续播放
                            mMediaPlayer.start();
                            pause=false;
                        //    ((Button)view).setText("暂停");
                            pauseButton.setImageResource(R.drawable.star);
                            mViewFlipper.startFlipping();//开始播放图片
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
                    mViewFlipper.stopFlipping();//图片停止播放
                    break;
                }
                break;
        }
    }

    private void play() {  //自定义播放函数
        mMediaPlayer.reset();//把各项参数恢复到初始状态
        pauseButton.setImageResource(R.drawable.star);
        try {
            if(path!=null) {
                mMediaPlayer.setDataSource(path);//设置路径
                mMediaPlayer.prepare();//缓冲
                mMediaPlayer.setOnPreparedListener(new PrepareListener()); //传入position
            }else
            {
                Toast.makeText(MainActivity.this,"播放列表为空",Toast.LENGTH_SHORT).show();
            }
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

    public String getFileName(String pathandname){ //通过绝对路径的到文件名

        int start=pathandname.lastIndexOf("/");
        int end=pathandname.lastIndexOf(".");
        if(start!=-1 && end!=-1){
            return pathandname.substring(start+1,end);
        }else{
            return null;
        }
    }


    public void next(View v) {
        if(list!=null) {
            if (p == (list.size() - 1)) p = 0;
            path = list.get(++p);
            if (path != null) {
                if (handler.post(updateThread)) {

                } else {
                    handler.post(updateThread);  //如果没开启多线程则开启  以更新进度条
                }
                play();
                //mName.setText(list.get(p));
                //String str=getFileName(path);
                mName.setText(name.get(p));
                mViewFlipper.startFlipping();//开始自动播放图片
            } else {
                Toast.makeText(MainActivity.this, "没有歌曲了哦！", Toast.LENGTH_SHORT).show();
            }
        }else {
            Toast.makeText(MainActivity.this,"文件夹为空！",Toast.LENGTH_SHORT).show();
        }
    }

    public void up(View v) {
        if(list!=null){
        if (p == 0) p = list.size();
        path = list.get(--p);
        if (path != null) {
            if (handler.post(updateThread)) {

            } else {
                handler.post(updateThread);  //如果没开启多线程则开启  以更新进度条
            }
            play();
            //mName.setText(list.get(p));
            //String string=getFileName(path);
            mName.setText(name.get(p));
            mViewFlipper.startFlipping();//开始播放图片
        }else{
            Toast.makeText(MainActivity.this,"没有歌曲了哦！",Toast.LENGTH_SHORT).show();
        }
        }else{
            Toast.makeText(MainActivity.this,"当前文件夹为空！",Toast.LENGTH_SHORT).show();
        }
    }

    private class SpinnerListener implements AdapterView.OnItemSelectedListener { //spinner点击监听

        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
           // Log.i("---------->position=",""+i);
           // textView_name.setText(i);
            //Toast.makeText(MainActivity.this,""+i,Toast.LENGTH_SHORT).show();
            p=i;
            path = list.get(p);
                    if (handler.post(updateThread)) {

                    } else {
                        handler.post(updateThread);  //如果没开启多线程则开启  以更新进度条
                    }
                    play();
                    mName.setText(name.get(p));
                    mViewFlipper.startFlipping();//开始自动播放图片

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }
}
