/*
* Copyright 2013 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/


package yesuaini.chinoisinteractif.tabs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import yesuaini.chinoisinteractif.AbstractNavigationActivity;
import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.models.Mission;


import static yesuaini.chinoisinteractif.hsk.CharacterListActivity.SELECTED_WORD_LIST_EXTRA;


/**
 * A simple launcher activity containing a summary sample description, sample log and a custom
 * {@link android.support.v4.app.Fragment} which can display a view.
 * <p>
 * For devices with displays with a width of 720dp or greater, the sample log is always visible,
 * on other devices it's visibility is controlled by an item on the Action Bar.
 */
public class MissionActivity extends AbstractNavigationActivity {

    public static final String TAG = "MissionActivity";
    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int lessonId = getIntent().getIntExtra(SELECTED_WORD_LIST_EXTRA, 1);
        Mission mission = getMission(lessonId);
        setTitle(mission.getTitle());

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabsColorsFragment fragment = new SlidingTabsColorsFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable(ContentFragment.KEY_MISSION, mission);
            fragment.setArguments(bundle);
            transaction.replace(R.id.tabs_content_fragment, fragment);
            transaction.commit();
        }
    }

    private Mission getMission(int missionId) {
        InputStream inputStream = null;
        Mission mission = null;
        try {
            inputStream = this.getAssets().open("missions/" + missionId + "/missions.json");
            Reader reader = new InputStreamReader(inputStream);
            GsonBuilder gsonBuilder = new GsonBuilder();
            gsonBuilder.setDateFormat("M/d/yy hh:mm a");
            Gson gson = gsonBuilder.create();
            mission = gson.fromJson(reader, Mission.class);
            Log.e(TAG, "mission: " + mission);
            inputStream.close();
        } catch (IOException e) {
            Log.e(TAG, "exception: " + e);
            e.printStackTrace();
        }
        return mission;
    }

    @Override
    protected int getToolbarViewId() {
            return R.id.toolbar;
    }

    @Override
    protected int getContentViewActivity() {
        return R.layout.activity_tabs;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}
