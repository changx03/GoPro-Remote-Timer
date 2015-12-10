package nz.ac.auckland.ivs.goproremotetimer;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class GoProTimerIntentService extends IntentService {

    public GoProTimerIntentService() {
        super("GoProTimerIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        System.out.println("GoProRemoteTimer_GoProTimerIntentService_Started");

        // get URI amd timer value
        String uriSwitchOn = intent.getStringExtra("SWITCH_ON");
        String uriSwitchOff = intent.getStringExtra("SWITCH_OFF");
        String uriCapture = intent.getStringExtra("CAPTURE");
        int timer_value = intent.getIntExtra("timer_value", 7);
        timer_value *= 1000;
        System.out.println("GoProRemoteTimer_GoProTimerIntentService_timer_value = " + timer_value);

        try {
            while(true) {
                if (timer_value == 0) {
                    // to nothing
                    System.out.println("GoProRemoteTimer_GoProTimerIntentService_timer_value = " + timer_value + ". Switch off");
                } else if (timer_value > 0 && timer_value <= 20000) {
                    // TODO: always on
                    sendHttpRequest(uriCapture);
                    SystemClock.sleep(timer_value);
                } else {    // timer_value > 6s
                    sendHttpRequest(uriSwitchOn);
                    Thread.sleep(1000);
                    sendHttpRequest(uriSwitchOn);
                    Thread.sleep(9000);
                    sendHttpRequest(uriCapture);
                    Thread.sleep(10000);
                    sendHttpRequest(uriSwitchOff);
                    Thread.sleep(timer_value - 20000);
                }
            }
        } catch (Exception e) {
            System.out.println("GoProRemoteTimer_GoProTimerIntentService_" + e.toString());
        }
    }

    private void sendHttpRequest(String uri) throws IOException {
        URL url = new URL(uri);
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        int status = httpURLConnection.getResponseCode();
        httpURLConnection.disconnect();
        System.out.println("GoProRemoteTimer_GoProTimerIntentService_status = " + status);
    }
}
