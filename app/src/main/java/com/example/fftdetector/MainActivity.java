package com.example.fftdetector;

import androidx.appcompat.app.AppCompatActivity;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


//https://www.developpez.net/forums/d938180/java/general-java/java-mobiles/android/generer/
public class MainActivity extends AppCompatActivity {

    private Button play;
    private Button stop;
    private TextView txt1;
    private TextView tv;

    AudioTrack at;
    public int minbufsizbytes;
    public int bufsizbytes;
    public int bufsizsamps;
    public final int samprate = 44100;
    public short[] buffer=null;
    public final float f=20000.0f; //beep freq
    public final float MAXVOL=(float)1.0;
    public final float MINVOL=(float)0.01;
    public final int LOOPON= -1;
    public final int LOOPOFF=0;
    public int tmp;
    int ok=0;



    MediaPlayer mediaPlayer;
    AudioTrack audioOut = null;
    int sampleRate = 44100;

    int minSize = AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

    byte [] music;
    short[] music2Short;

    InputStream is;

    //final int REQUEST_PERMISSION_CODE=1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        play = findViewById(R.id.play);
        stop = findViewById(R.id.stop);
        txt1=findViewById(R.id.txt1);
        tv=findViewById(R.id.tv);


        minbufsizbytes = AudioTrack.getMinBufferSize(samprate,AudioFormat.CHANNEL_CONFIGURATION_MONO,AudioFormat.ENCODING_PCM_16BIT)*15; //870 at 8000, 4800 at 44100
        bufsizbytes=minbufsizbytes;

        tv.setText("minbufsizebytes "+minbufsizbytes+"\n");
        bufsizsamps=bufsizbytes/2;
        buffer = new short[bufsizsamps];
        fillbuf();

        try{
            at = new AudioTrack(AudioManager.STREAM_MUSIC,samprate,
                    AudioFormat.CHANNEL_CONFIGURATION_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,bufsizbytes,AudioTrack.MODE_STREAM);
        } catch (IllegalArgumentException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
            tv.append("create audiotracksnafu "+e+"\n");
        }

        at.setStereoVolume(1.0f,1.0f);


        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

               /* mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.son2);
                mediaPlayer.start();*/
                tv.append("in playit!\n");
                at.play();
                ok=1;

                while (ok==1){
                    tmp=at.write(buffer, 0, bufsizsamps);
                    //SystemClock.sleep(1000L);
                    tv.append("tmpp="+tmp+"\n");
                }

            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (at != null){
                    tv.append("in stop!\n"); //
                    at.stop();
                    ok=0;
                }
            }
        });



        playAudio();

        int i;
        //buffer with the signal
        try{
            while (((i = is.read(music)) != -1)) {
                ByteBuffer.wrap(music).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(music2Short);
            }
        } catch (IOException e){
            e.printStackTrace();
        }

        //timeSize should be a power of two.
        int timeSize= 2^(nearest_power_2(minSize));

        FFT a = new FFT(1024,sampleRate);

        //conversion shortfloat

        //conversion Short To  Float

/*
        LineGraphSeries series = new GraphViewSeries();

        for (int i=0; i<music2Short.length;i++){
            series.(Float.toString(a.real[i]));
        }

        graph.addSeries(series);
*/
        a.forward(Tofloat(music2Short));
        txt1.setText(Float.toString(a.real[500]));



    }

    public void fillbuf(){
        double omega,t;
        double dt=1.0/samprate; //sec per samp

        t=0.0;
        omega=(float)(2.0*Math.PI*f);
        for(int i=0; i < bufsizsamps; i++){
            buffer[i]=(short)(32000.0*Math.sin(omega*t));
            t+=dt;
        }
    }

    public void initialize(){


        is = getResources().openRawResource(R.raw.son2);


        audioOut = new AudioTrack(
                AudioManager.STREAM_MUSIC,          // Stream Type
                sampleRate,                         // Initial Sample Rate in Hz
                AudioFormat.CHANNEL_OUT_MONO,       // Channel Configuration
                AudioFormat.ENCODING_PCM_16BIT,     // Audio Format
                minSize,                            // Buffer Size in Bytes
                AudioTrack.MODE_STREAM);            // Streaming static Buffer

    }

    public int nearest_power_2(int x){
        int i=0;
        int p = 1;
        while (p<x){
            p=2*p;
            i++;
        }
        if ((x-p)<(x-p/2))
            return i;
        else
            return i-1;
    }

    public void playAudio() {

        this.initialize();

        if ( (minSize/2) % 2 != 0 ) {
            /*If minSize divided by 2 is odd, then subtract 1 and make it even*/
            music2Short     = new short [((minSize/2) - 1)/2];
            music           = new byte  [(minSize/2) - 1];
        }
        else {
            /* Else it is even already */
            music2Short     = new short [minSize/4];
            music           = new byte  [minSize/2];
        }
    }

    public float[] Tofloat(short[] s){
        int len = s.length;
        float[] f= new float[len];
        for (int i=0;i<len;i++){
            f[i]=s[i];
        }
        return f;
    }
}
