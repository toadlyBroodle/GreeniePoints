package ca.toadlybroodle.greeniepoints;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;


public class MainActivity extends AppCompatActivity {

    public static final int NUM_COUNTERS = 9;
    public static final String PREFS_FILE_NAME = "GreeniePointsPrefsFile";
    public static final String PREFS_PRESS_TIME = "TimeOfLastButtonPress";
    //private static final String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAji39kFHWmyCo+cajqS5f+qq7gwQ1NU7Mx6MvScrG4TzT2fXjWehM5KHiQ+i3C2sGp4lNuvqhzyriY0qKXEp8R6ZPbr/004V9h4udiEz9GNp1WRZISK+bqKQM6pBlSnynysfb5wK2fSCUtpwVEVEevdgMNCGbOUyySH/kKXy2oOhM4K2rW0MiP8rBk5Fv6TcCeRE4ebZFhwLjvTjMu5k8GS9DmanymbVHwth6wmEdkjoN0E/peaIj3aF+4fjc1hDurQ5oh8HgEAw7eq2U5HQAJUg5Hml8lAusDLRsd2dMKOssFUto//gsdXp0Ui08rS+BVQsOZPCB/VXlXQk4XOmsiQIDAQAB";

    InterstitialAd mInterstitialAd;

    SharedPreferences mPrefs;
    Points[] mPtsArr = new Points[NUM_COUNTERS];
    TextView[] mTxtArr = new TextView[NUM_COUNTERS];

    long lastPressTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mInterstitialAd = new InterstitialAd(this);
        //InterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712"); // for testing
        mInterstitialAd.setAdUnitId("ca-app-pub-8742224953501062/1607469035"); // for deployment

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
               // TODO add cheating notification
                Toast.makeText(getApplicationContext(), "Cheating is discouraged via advertising...", Toast.LENGTH_SHORT).show();
            }
        });
        requestNewInterstitial();


        // load saved info and preferences
        mPrefs = getSharedPreferences(PREFS_FILE_NAME, 0);
        lastPressTime = mPrefs.getLong(PREFS_PRESS_TIME, 0);

        // get references to counter views
        for (int i = 0; i < NUM_COUNTERS; i++)
        {
            String name = "text" + i;
            int resID = getResources().getIdentifier(name, "id", "ca.toadlybroodle.greeniepoints");
            mTxtArr[i] = (TextView) findViewById(resID);
        }

        // set up objects
        for (int i = 0; i < NUM_COUNTERS; i++)
        {
            mPtsArr[i] = new Points(mPrefs.getInt("counter" + i, 0));
        }

        UpdateCounters();
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
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onStop(){
        super.onStop();

        // Save info and preferences for next time
        SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong(PREFS_PRESS_TIME, lastPressTime);
        // save individual button points data
        for (int i = 1; i < NUM_COUNTERS; i++)
        {
            editor.putInt("counter" + i, mPtsArr[i].points);
        }
        editor.commit();
    }

    public void UpdateCounters() {

        int total = 0;
        for (int i = 1; i < NUM_COUNTERS; i++)
        {
            int iSubTot = mPtsArr[i].points;
            mTxtArr[i].setText(String.valueOf(iSubTot));

            total += iSubTot;
        }

        // and finally set overall total counter
        //mTxtArr[0].setText("420");
        mTxtArr[0].setText(String.valueOf(total));
    }

    public void onClickButton(View v) {
        long currTime = System.currentTimeMillis();

        // show ad if loaded and cheating detected
        if (mInterstitialAd.isLoaded() && currTime < lastPressTime + 30000)
            mInterstitialAd.show();
        lastPressTime = currTime;

        // determine which button pushed
        String id = v.getResources().getResourceName(v.getId());
        int idNum = Integer.parseInt(id.substring(id.length() - 1));

        // add point to respective counter
        mPtsArr[idNum].AddPoint();
        UpdateCounters();
       }

    private void requestNewInterstitial() {
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("SEE_YOUR_LOGCAT_TO_GET_YOUR_DEVICE_ID")
                .build();

        mInterstitialAd.loadAd(adRequest);
    }

}
