package com.example.vardon.recorder;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.Locale;

import static android.os.SystemClock.elapsedRealtime;

public class RecordFragment extends Fragment{
    private boolean isRecording;      //正在录音标志
    private TextView tips;             //提示文本框
    private Chronometer chronometer;      //计时器
    private MediaRecorder mRecorder;         //录音API
    private String date;                //文件名
    private long startTime;               //录音开始时间
    private long endTime;                //录音结束时间
    private String time;
    SQLiteDatabase db;
    private long basetime;
    private String fileName;
    private Button record;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View view = inflater.inflate(R.layout.record_fragment, container, false);

        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        record = (Button)getActivity().findViewById(R.id.record_btn);
        chronometer = (Chronometer) getActivity().findViewById(R.id.chronometer);
        tips = (TextView) getActivity().findViewById(R.id.tips);

        if(isRecording==false) record.setBackgroundResource(R.drawable.record);
        else {record.setBackgroundResource(R.drawable.over);
            chronometer.setBase(basetime);
            chronometer.start();}

        record.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if (isRecording == false) {
                   //录音功能
                    recordStart();
               }
               else {
                   //停止录音
                    recordStop();
                   showSetNameDialog();
               }
           }
       });
    }
    public void recordStart(){
        record.setBackgroundResource(R.drawable.over);
        tips.setText("Recording...");
        basetime=elapsedRealtime();
        chronometer.setBase(basetime);
        chronometer.start();
        startTime = chronometer.getBase();
        try {
            //默认文件名选择当前时间
            date = (String) DateFormat.format("yyyy-MM-dd_HH:mm:ss", Calendar.getInstance(Locale.CHINA));
            fileName = date+".mp3";
            try{
            File dir = new File(Environment.getExternalStorageDirectory() + "/recorder");
            if (!dir.exists()) {//判断指定的路径或者指定的目录文件是否已经存在。
                dir.mkdir();//建立文件夹
            }

            File file = new File(Environment.getExternalStorageDirectory() + "/recorder", fileName);

            file.createNewFile();
            mRecorder = new MediaRecorder();
            // 设置录音的声音来源
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置录制的声音的输出格式
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            // 设置声音编码格式
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setOutputFile(file.getAbsolutePath());
            mRecorder.prepare();
            // 开始录音
            mRecorder.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
        isRecording = true;
    }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void recordStop(){
        isRecording = false;
        record.setBackgroundResource(R.drawable.record);
        tips.setText("Tap the botton to start recording");
        chronometer.stop();
        chronometer.setBase(elapsedRealtime());
        endTime = chronometer.getBase();
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        } catch (RuntimeException e) {
            mRecorder.reset();
            mRecorder.release();
            mRecorder = null;
        }


    }


    public void showSetNameDialog(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.rename_file_dialog,null,false);
        final AlertDialog dialog = new AlertDialog.Builder(getContext()).setView(view).create();

        final EditText reNameText = (EditText)view.findViewById(R.id.rename_text);
        Button btn = (Button)view.findViewById(R.id.rename_btn);

        reNameText.setHint(fileName.replace(".mp3",""));
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                reName(reNameText);
                dialog.dismiss();

            }
        });
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                reName(reNameText);
            }
        });

        dialog.show();
    }
    public void reName(EditText reNameText){
        String newFileName = reNameText.getText().toString();
        if(newFileName.length() !=0){

            newFileName = newFileName+".mp3";
            File oleFile = new File(Environment.getExternalStorageDirectory() + "/recorder",fileName);
            File newFile = new File(Environment.getExternalStorageDirectory() + "/recorder",newFileName);
            //执行重命名
            oleFile.renameTo(newFile);
            fileName = newFileName;
        }
        else {
            fileName = reNameText.getHint().toString()+".mp3";
        }

        //保存到数据库
        time = (String) DateFormat.format("mm:ss", (endTime-startTime));
        MainActivity activity = (MainActivity) getActivity();
        activity.saveToDB(1, fileName, time, date);
    }
}
