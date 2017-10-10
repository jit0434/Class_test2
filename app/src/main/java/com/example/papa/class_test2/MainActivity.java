package com.example.papa.class_test2;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v4.os.EnvironmentCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.R.attr.button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    Button startR, stopR, startP, stopP;
    Thread runner;
    private static double mEMA = 0.0;
    static final private double EMA_FLITER = 0.6;
    public static final int RequestPermissionCode = 1;

    String AudioPathInDevice = null;

    MediaRecorder mideaRecorder;
    MediaPlayer mediaPlayer;

    public double soundDb(double ampl){
        return  20+Math.log10(getAmplitudeEMA()/ampl);
    }
    public double getAmplitude(){
        if(mideaRecorder != null){
            return (mideaRecorder.getMaxAmplitude());
        }
        else
            return 0;
    }
    public double getAmplitudeEMA(){
        double amp = getAmplitude();
        mEMA = EMA_FLITER+amp + (1.0 - EMA_FLITER)+mEMA;
        return (int) mEMA;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startR = (Button)findViewById(R.id.buttonRecord);
        stopR = (Button)findViewById(R.id.buttonStop);
        startP = (Button)findViewById(R.id.buttonPlay);
        stopP = (Button)findViewById(R.id.buttonStopPlay);

        stopR.setEnabled(false);
        stopP.setEnabled(false);
        startP.setEnabled(false);

        startR.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                if(CheckPermission()){

                    AudioPathInDevice = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/"+"AudioRecord.3gpp";
                    MediaRecorderReady();

                    try{
                        mideaRecorder.prepare();
                        mideaRecorder.start();
                    }catch (IllegalStateException e){
                        e.printStackTrace();
                    }catch (IOException e){
                        e.printStackTrace();
                    }

                    startR.setEnabled(false);
                    stopR.setEnabled(true);
                    startP.setEnabled(false);

                    Toast.makeText(MainActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();

                }
                else{
                    RequestPermission();
                }
            }
        });

        stopR.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                mideaRecorder.stop();
                stopR.setEnabled(false);
                startP.setEnabled(true);
                startR.setEnabled(true);
                stopP.setEnabled(false);


                Toast.makeText(MainActivity.this, "Recording Completed", Toast.LENGTH_SHORT).show();

            }
        });


        startP.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) throws IllegalStateException,SecurityException,IllegalArgumentException {
                stopR.setEnabled(false);
                startR.setEnabled(false);
                stopP.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioPathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(MainActivity.this, " Last Recording Playing", Toast.LENGTH_SHORT).show();
            }
        });

        stopP.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                stopR.setEnabled(false);
                startR.setEnabled(false);
                stopP.setEnabled(false);
                startP.setEnabled(true);


            }

        });

    }

    public void MediaRecorderReady(){
        mideaRecorder = new MediaRecorder();
        mideaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mideaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mideaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mideaRecorder.setOutputFile(AudioPathInDevice);
    }

    private  void RequestPermission(){
        ActivityCompat.requestPermissions(MainActivity.this,new String[]{WRITE_EXTERNAL_STORAGE,RECORD_AUDIO},RequestPermissionCode);
    }


    public void onReuestPermissionResult(int requestCode, @NonNull String[] permissions,@NonNull int[] grantresults){
        switch (requestCode){
            case RequestPermissionCode:
                if(grantresults.length>0){
                    boolean StoragePermission = grantresults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecorderPermission = grantresults[1] == PackageManager.PERMISSION_GRANTED;

                    if(StoragePermission && RecorderPermission){
                        Toast.makeText(this, "Permission is Granted", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(this, "Permission is denied", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    public boolean CheckPermission(){
        int result = ContextCompat.checkSelfPermission(getApplicationContext(), WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(), RECORD_AUDIO);

        return  result == PackageManager.PERMISSION_GRANTED &&
                result1 == PackageManager.PERMISSION_GRANTED;
    }


}
