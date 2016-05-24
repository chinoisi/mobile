package yesuaini.chinoisinteractif.lazyimagedownload;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import yesuaini.chinoisinteractif.AbstractNavigationActivity;
import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.models.MissionSummary;

public class LessonListActivity extends AbstractNavigationActivity {
    
    ListView list;
    LazyImageLoadAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        list=(ListView)findViewById(R.id.list);
        int lessonId = 1;
        InputStream inputStream = null;
        try {
            inputStream = this.getAssets().open("missions/missions.json");
        Reader reader = new InputStreamReader(inputStream);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("M/d/yy hh:mm a");
        Gson gson = gsonBuilder.create();
        List<MissionSummary> lessonSummaries = Arrays.asList(gson.fromJson(reader, MissionSummary[].class));
        inputStream.close();
            adapter=new LazyImageLoadAdapter(this, lessonSummaries);
            list.setAdapter(adapter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onDestroy() {
        list.setAdapter(null);
        super.onDestroy();
    }
    
    public OnClickListener listener=new OnClickListener(){
        @Override
        public void onClick(View arg0) {
            adapter.imageLoader.clearCache();
            adapter.notifyDataSetChanged();
        }
    };
    

    private class LessonFetcher extends AsyncTask<Void, Void, String> {
        private static final String TAG = "LessonFetcher";

        @Override
        protected String doInBackground(Void... params) {
            try {
                int lessonId = 1;
                InputStream inputStream = getAssets().open("missions/missions.json");
                Reader reader = new InputStreamReader(inputStream);
                GsonBuilder gsonBuilder = new GsonBuilder();
                gsonBuilder.setDateFormat("M/d/yy hh:mm a");
                Gson gson = gsonBuilder.create();
                List<MissionSummary> lessonSummaries = Arrays.asList(gson.fromJson(reader, MissionSummary[].class));
                inputStream.close();
            } catch (Exception ex) {
                Log.e(TAG, "Failed to parse JSON due to: " + ex);
            }

            return null;
        }
    }


    @Override
    protected int getToolbarViewId() {
        return R.id.toolbar;
    }

    @Override
    protected int getContentViewActivity() {
        return R.layout.lessonlist;
    }
}