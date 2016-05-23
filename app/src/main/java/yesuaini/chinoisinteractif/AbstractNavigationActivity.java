package yesuaini.chinoisinteractif;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import yesuaini.chinoisinteractif.handwriting.EvaluateHandWritingActivity;
import yesuaini.chinoisinteractif.lazyimagedownload.LessonListActivity;
import yesuaini.chinoisinteractif.map.EpisodesMapActivity;
import yesuaini.chinoisinteractif.tabs.MissionActivity;

import static yesuaini.chinoisinteractif.hsk.CharacterListActivity.SELECTED_WORD_LIST_EXTRA;

public abstract class AbstractNavigationActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewActivity());

        Toolbar toolbar = initToolbar(getToolbarViewId());
        initAdView();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        initNavigationView();
    }

    protected abstract int getToolbarViewId();

    protected abstract int getContentViewActivity();

    protected Toolbar initToolbar(int toolbarViewId) {
        Toolbar toolbar = (Toolbar) findViewById(toolbarViewId);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        return toolbar;
    }

    protected void initNavigationView() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void initAdView() {
        AdView adView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .setRequestAgent("android_studio:ad_template").build();
        adView.loadAd(adRequest);
    }

    protected void initWebView(WebView webView, String url) {
        WebSettings webSetting = webView.getSettings();
        webSetting.setJavaScriptEnabled(true);
        webSetting.setJavaScriptCanOpenWindowsAutomatically(true);
        webSetting.setAllowContentAccess(true);
        webSetting.setAllowFileAccess(true);
        webSetting.setLoadsImagesAutomatically(true);
        webSetting.setDisplayZoomControls(true);
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        webView.setWebViewClient(new MyBrowser());
        webView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.navigation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_camera) {
            Intent intent = new Intent(this, MissionActivity.class);
            intent.putExtra(SELECTED_WORD_LIST_EXTRA, 2);
            this.startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_gallery) {
            Intent intent = new Intent(this, yesuaini.chinoisinteractif.lazyimagedownload.LessonListActivity.class);
            this.startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_slideshow) {
            Intent intent = new Intent(this, LessonListActivity.class);
            this.startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_download) {
            Intent intent = new Intent(this, ExerciceActivity.class);
            this.startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_map) {
            Intent intent = new Intent(this,EpisodesMapActivity.class);
            intent.putExtra(SELECTED_WORD_LIST_EXTRA, 1);
            this.startActivity(intent);
            this.finish();
        } else if (id == R.id.nav_setting) {
            Intent intent = new Intent(this, EvaluateHandWritingActivity.class);
            this.startActivity(intent);
            this.finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private class MyBrowser extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }
}
