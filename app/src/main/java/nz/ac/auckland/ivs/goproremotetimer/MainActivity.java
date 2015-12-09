package nz.ac.auckland.ivs.goproremotetimer;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // URI
    public static final String GOPRO_ID = "10.5.5.9";
    public String URI_PHOTO_MODE;
    public String URI_SWITCH_OFF;
    public String URI_SWITCH_ON;
    public String URI_CAPTURE;

    // conditions
    public static final int SWITCH_ON = 1;
    public static final int SWITCH_OFF = 0;
    public static final int PHOTO_MODE = 2;

    // For SharedPreferences
    private int timer_value;
    public String GOPRO_PASSWORD;
    public static final String PREFS_NAME = "SettingsFile";

    // IntentService
    private GoProTimerIntentService gService;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Restore preferences
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        timer_value = preferences.getInt("timerValue", 10);
        GOPRO_PASSWORD = preferences.getString("password", "ivsnzivsnz");
        updateUri();

        final EditText editText = (EditText) findViewById(R.id.edTxt_Password);
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setText(GOPRO_PASSWORD);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    GOPRO_PASSWORD = editText.getText().toString();
                    updateUri();
                    editText.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    toast("Password: " + GOPRO_PASSWORD);
                    return true;
                }
                return false;
            }
        });

        final Button btnSwitch = (Button) findViewById(R.id.btn_SwitchOnOFF);
        btnSwitch.setTag(1);
        btnSwitch.setText(R.string.switch_on);
        btnSwitch.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final int status = (Integer) v.getTag();
                if (status == 1) {
                    // TODO: switch on
                    String passing = String.valueOf(SWITCH_ON);
                    new SendURL2GoPro().execute(passing);
                    btnSwitch.setText(R.string.switch_off);
                    v.setTag(0);
                    toast("Waiting for GoPro switching on");
                } else {
                    // TODO: switch off
                    String passing = String.valueOf(SWITCH_OFF);
                    new SendURL2GoPro().execute(passing);
                    btnSwitch.setText(R.string.switch_on);
                    v.setTag(1);
                    toast("Waiting for GoPro switching off");
                }
            }
        });

        final Button btnPhotoMode = (Button) findViewById(R.id.btn_PhotoMode);
        btnPhotoMode.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                String passing = String.valueOf(PHOTO_MODE);
                new SendURL2GoPro().execute(passing);
                toast("Waiting for GoPro switching to Photo Mode");
            }
        });

        final NumberPicker np_tmr = (NumberPicker) findViewById(R.id.numPic_timer);
        np_tmr.setMaxValue(1800);
        np_tmr.setMinValue(1);
        np_tmr.setValue(timer_value);
        np_tmr.setWrapSelectorWheel(true);
        np_tmr.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                timer_value = newVal;
            }
        });

        final Intent intent  = new Intent(this, GoProTimerIntentService.class);

        final Button btnTimerUpdate = (Button) findViewById(R.id.btn_TmrUpdate);
        btnTimerUpdate.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                System.out.println("GoProRemoteTimer_MainActivity_timer_value = " + timer_value);
                // TODO: update service parameter and turn on the intent service
                stopService();

                intent.putExtra("SWITCH_ON", URI_SWITCH_ON);
                intent.putExtra("SWITCH_OFF", URI_SWITCH_OFF);
                intent.putExtra("CAPTURE", URI_CAPTURE);
                intent.putExtra("timer_value", timer_value);
                startService(intent);
                toast(String.valueOf("Set timer to " + timer_value + "s."));
            }
        });

        final Button btnShut = (Button) findViewById(R.id.btn_ShutDownTmr);
        btnShut.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO: shut down the service
//                intent.putExtra("timer_value", 0);
//                startService(intent);
                stopService();
                toast("Timer service has been stopped");
            }
        });

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();


        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void updateUri() {
        URI_PHOTO_MODE = "http://" + GOPRO_ID + "/camera/CM?t=" + GOPRO_PASSWORD + "&p=%01";
        URI_SWITCH_OFF = "http://" + GOPRO_ID + "/bacpac/PW?t=" + GOPRO_PASSWORD + "&p=%00";
        URI_SWITCH_ON = "http://" + GOPRO_ID + "/bacpac/PW?t=" + GOPRO_PASSWORD + "&p=%01";
        URI_CAPTURE = "http://" + GOPRO_ID + "/bacpac/SH?t=" + GOPRO_PASSWORD + "&p=%01";
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_exit) {
            finish();
            System.exit(0);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://nz.ac.auckland.ivs.goproremotetimer/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://nz.ac.auckland.ivs.goproremotetimer/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);

        // Use shared preferences to update timer value
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("timerValue", timer_value);
        editor.putString("password", GOPRO_PASSWORD);
        editor.commit();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.disconnect();
    }

    class SendURL2GoPro extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... passing) {
            String passed = passing[0];
            return triggerUrl(passed);
        }

        private Boolean triggerUrl(String passed) {
            String mUri = "";
            switch (Integer.valueOf(passed)) {
                case SWITCH_OFF:
                    mUri = URI_SWITCH_OFF;
                    break;
                case SWITCH_ON:
                    mUri = URI_SWITCH_ON;
                    break;
                case PHOTO_MODE:
                    mUri = URI_PHOTO_MODE;
                    break;
                default:
                    mUri = URI_SWITCH_OFF;
                    break;
            }
            try {
                URL url = new URL(mUri);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                int status = httpURLConnection.getResponseCode();
                httpURLConnection.disconnect();
                System.out.println("GoProRemoteTimer_MainActivity_status = " + status);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }

    public void toast(CharSequence text) {
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }

    private void stopService() {
        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();

        Iterator<ActivityManager.RunningAppProcessInfo> iter = runningAppProcesses.iterator();

        while(iter.hasNext()){
            ActivityManager.RunningAppProcessInfo next = iter.next();

            String pricessName = getPackageName() + ":GoProService";

            if(next.processName.equals(pricessName)){
                android.os.Process.killProcess(next.pid);
                break;
            }
        }
    }
}
