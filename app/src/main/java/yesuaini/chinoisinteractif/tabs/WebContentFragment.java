package yesuaini.chinoisinteractif.tabs;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import yesuaini.chinoisinteractif.R;

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * Created by yesuaini on 28/04/16.
 */
public class WebContentFragment extends Fragment {

    private static final String KEY_URL = "";
    private ProgressDialog progress;

    public static WebContentFragment newInstance(String uri) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_URL, uri);
        WebContentFragment fragment = new WebContentFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(getTabsPagerId(), container, false);
    }

    protected int getTabsPagerId() { return R.layout.tabs_pager_web_view;}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            String url = (String)args.getSerializable(KEY_URL);
            WebView webView = (WebView) view.findViewById(R.id.webView);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            webView.getSettings().setLoadsImagesAutomatically(true);
            webView.getSettings().setPluginState(WebSettings.PluginState.ON);
            webView.getSettings().setAllowFileAccess(true);
            webView.getSettings().setAllowFileAccessFromFileURLs(true);
            webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
            webView.getSettings().setLightTouchEnabled(true);
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setBuiltInZoomControls(true);
            webView.getSettings().setUseWideViewPort(true);
            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            webView.loadUrl(url);
            webView.setWebChromeClient(new WebChromeClient());
//            webView.setWebChromeClient(new WebChromeClient() {
//                @Override
//                public void onProgressChanged(WebView view1, int newProgress) {
//                    if (newProgress > 0) {
//                        showProgressDialog(getString(R.string.loading_web_view));
//                    }
//                    if (newProgress >= 100) {
//                        hideProgressDialog();
//                    }
//                }
//
//                private void showProgressDialog(final String message) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (progress == null || !progress.isShowing()) {
//                                progress = ProgressDialog.show(getActivity(), "", message);
//                            }
//                        }
//                    });
//                }
//
//                private void hideProgressDialog() {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                           try {
//                               if (progress.isShowing())
//                                   progress.dismiss();
//                           } catch (Throwable e) {}
//                        }
//                    });
//                }
//
//            });


          }
    }

}
