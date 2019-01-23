package com.example.androidthings;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.HandlerThread;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import com.google.android.things.contrib.driver.apa102.Apa102;
import com.google.android.things.contrib.driver.pwmspeaker.Speaker;
import java.io.IOException;
import java.util.Random;
import static com.example.androidthings.MusicNotes.TUNE;

public class MainActivity extends Activity {

    private static final int LED_BRIGHTNESS = 1; // 0 ... 31
    private static final Apa102.Mode LED_MODE = Apa102.Mode.BGR;
    private SeekBar spinValue;
    private Apa102 mLedstrip;
    private HandlerThread mPioThread;
    private Speaker mSpeaker;
    double spinSpeed = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        setContentView(R.layout.main_activity);
        super.onCreate(savedInstanceState);

        try {
            mSpeaker = new Speaker(BoardDefaults.getPwmPin());
            mSpeaker.stop();
        } catch (IOException ignored) {}

        mPioThread = new HandlerThread("pioThread");
        mPioThread.start();

        try {
            mLedstrip = new Apa102(BoardDefaults.getSPIPort(), LED_MODE);
            mLedstrip.setBrightness(LED_BRIGHTNESS);
        } catch (IOException ignored) {}

        final Button button = findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                spinValue = findViewById(R.id.seekBar);
                int spinForce = 300 - (spinValue.getProgress());
                play(spinForce);
            }
        });
    }

    public void play(int spinForce){
        int[] colors = new int[] {Color.RED, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.RED, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW, Color.YELLOW};

        Random random = new Random(); int rnd = random.nextInt(((10 - 1) + 1) + 1);
        Random random2 = new Random(); int rnd2 = random2.nextInt(((100 - 1) + 1) + 1);

        spinSpeed = rnd + spinForce;
        int[] d = new int[0];
        int[] fill;
        int c = 0;
        double note;

        for (int i = 1; spinSpeed < (500+rnd2); i++){
            System.out.println(spinSpeed);
            fill = new int[]{colors[i], colors[1 + i], colors[2 + i], colors[3 + i], colors[4 + i], colors[5 + i], colors[6 + i]};
            i = i % 7;
            if(spinSpeed<200){
                spinSpeed *= 1.03;
            } else if (spinSpeed<400){
                spinSpeed *= 1.05;
            } else {
                spinSpeed *= 1.07;
            }

            d = fill;

            note = TUNE[c];

            try {
                mSpeaker.play(note);
            } catch (IOException e) {
                e.printStackTrace();
            }

            c++;
            c = c % 3;

            try {
                mLedstrip.write(fill);
            } catch (IOException ignored) {}
            try {
                Thread.sleep((int)spinSpeed);
            } catch (InterruptedException ignored) {}
        }

        try {
            mSpeaker.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }

        int check = -256;
        int i = 0;

        while(check == -256){
            check = d[i];
            i++;
        }

        String result = String.valueOf(i-1);
        System.out.println(result);
        TextView textView = findViewById(R.id.resultText);
        textView.setText(result);

        try {
            mSpeaker.stop();
        } catch (IOException ignored) {}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPioThread.quitSafely();
        try {
            mLedstrip.close();
            mSpeaker.stop();
            mSpeaker.close();
        } catch (IOException ignored) {
        } finally {
            mLedstrip = null;
            mSpeaker = null;
        }
    }
}