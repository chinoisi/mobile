/*
 * Copyright (C) 2011 Scott Lund
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yesuaini.chinoisinteractif.map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import yesuaini.chinoisinteractif.AbstractNavigationActivity;
import yesuaini.chinoisinteractif.R;

import static yesuaini.chinoisinteractif.hsk.CharacterListActivity.SELECTED_WORD_LIST_EXTRA;


public class EpisodesMapActivity extends AbstractNavigationActivity {
    public static final String MAP_EPISODES = "MapEpisodes";
    private static final String LOG_TAG = EpisodesMapActivity.class.getSimpleName();
    ImageMap imageMap;

    @Override
    protected int getToolbarViewId() {
        return R.id.toolbar;
    }

    @Override
    protected int getContentViewActivity() {
        return R.layout.map;
    }


    Activity mActivity;
    public static  String CURRENT_EPISODE_LEVEL_KEY ="currentEpisodeLevel";
    SharedPreferences pref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);

		mActivity = this;
        pref = getBaseContext().getSharedPreferences(MAP_EPISODES, 0);
        imageMap = (ImageMap)findViewById(R.id.imageMap);
        int level = pref.getInt(CURRENT_EPISODE_LEVEL_KEY, 1);
        imageMap.setCurrentLevel(level);

        imageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler()
        {
			@Override
			public void onImageMapClicked(int id, ImageMap imageMap)
			{
			//	imageMap.centerAndShowArea(id);
			//	imageMap.showBubble(id);
                int level = pref.getInt(CURRENT_EPISODE_LEVEL_KEY, 1);

                int index = imageMap.getAreaIndex(id);

                if (index <= level) {
                    Intent intent = new Intent(mActivity, MissionsByEpisodeMapActivity.class);
                    intent.putExtra(SELECTED_WORD_LIST_EXTRA, index);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt(CURRENT_EPISODE_LEVEL_KEY, index);
                    editor.commit();
                    mActivity.startActivity(intent);
                    mActivity.finish();
                }
			}

			@Override
			public void onBubbleClicked(int id)
			{

			}
		});

        imageMap.centerToLevelArea();

    }

    protected Toolbar initToolbar(int toolbarViewId) {
        Toolbar toolbar = (Toolbar) findViewById(toolbarViewId);
        return toolbar;
    }


}