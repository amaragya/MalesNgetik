package com.example.amaragya.notulen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity implements
        RecognitionListener {

    private TextView returnedText,status,timerValue;
    private Button toggleButton,resumebutton,stopbutton;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "VoiceRecognitionActivity";

    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    long timeSwapBuff = 0L;
    long updatedTime = 0L;
    private long startTime = 0L;
    LinearLayout saatmulai;
    public String rekam = "tidak";
public boolean izinrekam = false;
public boolean izinsimpan = false;

    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNTS = 1;
    private static final int MY_PERMISSIONS_REQUEST_GET_ACCOUNT = 12;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        returnedText = (TextView) findViewById(R.id.textView1);
        status = (TextView) findViewById(R.id.status);
        timerValue = (TextView) findViewById(R.id.timer);
        progressBar = (ProgressBar) findViewById(R.id.progressBar1);
        toggleButton = (Button) findViewById(R.id.toggleButton1);
        resumebutton = (Button) findViewById(R.id.resumebutton);
        stopbutton = (Button) findViewById(R.id.stopbutton);
        saatmulai = (LinearLayout) findViewById(R.id.saatmulai);
        progressBar.setVisibility(View.INVISIBLE);
        Typeface fontAwesomeFont = Typeface.createFromAsset(getAssets(), "fontawesome-webfont.ttf");
        toggleButton.setTypeface(fontAwesomeFont);
        stopbutton.setTypeface(fontAwesomeFont);
        resumebutton.setTypeface(fontAwesomeFont);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "id");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        speech = SpeechRecognizer.createSpeechRecognizer(getBaseContext());
        speech.setRecognitionListener(this);

        resumebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rekam == "tidak") {
                    jalankan();
                    speech.startListening(recognizerIntent);
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    rekam = "ya";
                    resumebutton.setText(" | | ");
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 0);
                    MuteAudio();
                    status.setText("Mendengarkan...");
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                    speech.destroy();
                    rekam = "tidak";
                    UnMuteAudio();
                    resumebutton.setText(R.string.recordicon);
                    status.setText("Tap tombol untuk mulai");
                    timeSwapBuff += timeInMilliseconds;
                    customHandler.removeCallbacks(updateTimerThread);
                }
            }
        });
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(izinrekam){
mulaiproses();
                }else{

                    getPermissions();
                }
            }
        });


        stopbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setIndeterminate(false);
                progressBar.setVisibility(View.INVISIBLE);
                speech.stopListening();
                speech.destroy();
                rekam = "tidak";
                startTime = 0L;
                customHandler.removeCallbacks(updateTimerThread);

                toggleButton.setVisibility(View.VISIBLE);
                toggleButton.setText(R.string.recordicon);
                saatmulai.setVisibility(View.GONE);

                status.setText("Tap tombol untuk mulai");
                timerValue.setText("00 : 00 : 00");

               UnMuteAudio();
               if(izinsimpan){
                   simpandata();
               }else{
                   getPermissions2();
               }

            }
        });



    }


    public void mulaiproses(){
        jalankan();
        speech.startListening(recognizerIntent);

        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);


        rekam = "ya";

        startTime = SystemClock.uptimeMillis();
        customHandler.postDelayed(updateTimerThread, 0);

        MuteAudio();

        resumebutton.setText("| |");
        status.setText("Mendengarkan...");
        saatmulai.setVisibility(View.VISIBLE);
        toggleButton.setVisibility(View.GONE);
    }

    public void simpandata(){

    try {
        File folder = new File(Environment.getExternalStorageDirectory() +
                File.separator + "splendbit/");

        if(!folder.exists()){
            folder.mkdirs();
        }

        File myFile = new File("/sdcard/splendbit/datarekam_"+ new Date().getTime()+".txt");


        myFile.createNewFile();
        FileOutputStream fOut = new FileOutputStream(myFile);
        OutputStreamWriter myOutWriter =
                new OutputStreamWriter(fOut);
        myOutWriter.append(returnedText.getText().toString());
        myOutWriter.close();
        fOut.close();
        Toast.makeText(getBaseContext(),"datarekam_"+ new Date().getTime()+".txt Berhasil Disimpan",
                Toast.LENGTH_SHORT).show();
        returnedText.setText("");
    } catch (Exception e) {
        Toast.makeText(getBaseContext(), e.getMessage(),
                Toast.LENGTH_SHORT).show();
    }
}

    public void jalankan(){
        speech = SpeechRecognizer.createSpeechRecognizer(getApplicationContext());
        speech.setRecognitionListener(this);
    }

    public void MuteAudio(){
        AudioManager mAlramMAnager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_MUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0);
        } else {
            mAlramMAnager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_ALARM, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_MUSIC, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_RING, true);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
        }
    }

    public void UnMuteAudio(){
        AudioManager mAlramMAnager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_ALARM, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE,0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_RING, AudioManager.ADJUST_UNMUTE, 0);
            mAlramMAnager.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_UNMUTE, 0);
        } else {
            mAlramMAnager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_ALARM, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_MUSIC, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_RING, false);
            mAlramMAnager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
        }
    }


    private Runnable updateTimerThread = new Runnable() {
        public void run() {
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            updatedTime = timeSwapBuff + timeInMilliseconds;
            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            int hours = mins / 60;
            secs = secs % 60;
            int milliseconds = (int) (updatedTime % 1000);
            timerValue.setText("" + hours + " : "
                    + String.format("%02d", mins) + " : "
                    + String.format("%02d", secs));

            customHandler.postDelayed(this, 0);
        }

    };


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            Log.i(LOG_TAG, "destroy");


        }

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");
        progressBar.setIndeterminate(false);
        progressBar.setMax(10);

    }

    @Override
    public void onBufferReceived(byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);


        Toast.makeText(getApplicationContext(),buffer.toString(),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");

    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        speech.destroy();
        jalankan();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        Log.i(LOG_TAG, "onReadyForSpeech");



    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text ="";
        text +=  returnedText.getText() + " " + matches.get(0) ;
        returnedText.setText(text);

        speech.destroy();
        jalankan();
        speech.startListening(recognizerIntent);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = String.valueOf(errorCode);
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }



    public void getPermissions2() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_GET_ACCOUNT);
        }else{
            izinsimpan = true;
            simpandata();
        }
    }
    public void getPermissions() {
        /* Check and Request permission */
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    MY_PERMISSIONS_REQUEST_GET_ACCOUNTS);
        }else{
            izinrekam = true;
            mulaiproses();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {

            case MY_PERMISSIONS_REQUEST_GET_ACCOUNTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   izinrekam = true;
                    mulaiproses();
                }else{
                    izinrekam = false;
                }
                break;
            case MY_PERMISSIONS_REQUEST_GET_ACCOUNT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(returnedText.getText() != ""){
                        simpandata();
                        izinsimpan = true;
                    }
                }else{
                    izinsimpan = false;
                }
                break;
        }
    }
}