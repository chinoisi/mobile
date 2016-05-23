package yesuaini.chinoisinteractif;

import android.os.Bundle;
import android.webkit.WebView;

public class ExerciceActivity extends AbstractNavigationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WebView webView = (WebView)findViewById(R.id.webView);
        initWebView(webView, "file:///android_asset/handwriting/index.html");
    }

    @Override
    protected int getToolbarViewId() {
        return R.id.toolbar;
    }

    @Override
    protected int getContentViewActivity() {
        return R.layout.activity_navigation;
    }


}
