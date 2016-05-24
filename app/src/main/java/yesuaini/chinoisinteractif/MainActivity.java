package yesuaini.chinoisinteractif;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import yesuaini.chinoisinteractif.map.EpisodesMapActivity;

public class MainActivity extends AppCompatActivity {
    private static final int SPLASH_DISPLAY_TIME = 800;

    Context mContext;
    Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_home);
        mContext = MainActivity.this;
        mActivity = MainActivity.this;

        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(MainActivity.this, EpisodesMapActivity.class);
                MainActivity.this.startActivity(intent);
                MainActivity.this.finish();
            }
        }, SPLASH_DISPLAY_TIME);

    }

}
