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
import yesuaini.chinoisinteractif.episodes.MissionViewActivity;

import static yesuaini.chinoisinteractif.hsk.CharacterListActivity.SELECTED_WORD_LIST_EXTRA;

// TODO : episodes (lessons) --> missions (activité pédagogique)  /// Passage d un épisode à l autre --> évaluation
// page mission : numéro mission / Objectif / Difficulté de la mission  / Bouton de jeu "Mener la mission"
// / 3 boutons d aides - video - vocabulaires - cours en video conference (redirection vers site)
// mission bonus  : culturel
// Ajout
public class MissionsByEpisodeMapActivity extends AbstractNavigationActivity {
    public static final String MAP_MISSIONS = "MapMissions";
    MissionsByEpisodeMap mImageMap;

    @Override
    protected int getToolbarViewId() {
        return R.id.toolbar;
    }

    @Override
    protected int getContentViewActivity() {
        return R.layout.map;
    }


    Activity mActivity;
    public static  String CURRENT_MISSION_LEVEL_KEY ="currentMissionLevel";
    SharedPreferences pref;
    SharedPreferences prefEpisodes;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		mActivity = this;
        pref = getBaseContext().getSharedPreferences(MAP_MISSIONS, 0);
        mImageMap = (MissionsByEpisodeMap)findViewById(R.id.missionByEpisodeMap);
        int level = pref.getInt(CURRENT_MISSION_LEVEL_KEY, 1);
        mImageMap.setCurrentLevel(level);

        if (level >= mImageMap.getMaxIndex()) {
            Intent intent = new Intent(mActivity, EpisodesMapActivity.class);
            intent.putExtra(SELECTED_WORD_LIST_EXTRA, level);
            int levelEpisode = prefEpisodes.getInt(CURRENT_MISSION_LEVEL_KEY, 1);
            SharedPreferences.Editor editor = prefEpisodes.edit();
            editor.putInt(EpisodesMapActivity.CURRENT_EPISODE_LEVEL_KEY, levelEpisode+1);
            editor.commit();
            mActivity.startActivity(intent);
            mActivity.finish();
        }

        mImageMap.addOnImageMapClickedHandler(new ImageMap.OnImageMapClickedHandler()
        {
			@Override
			public void onImageMapClicked(int id, ImageMap imageMap)
			{
			//	episodesMap.centerAndShowArea(id);
			//	episodesMap.showBubble(id);
                int level = pref.getInt(CURRENT_MISSION_LEVEL_KEY, 1);

                int index = mImageMap.getAreaIndex(id);

                if (index <= level) {
                    Intent intent = new Intent(mActivity, MissionViewActivity.class);
                    intent.putExtra(SELECTED_WORD_LIST_EXTRA, index);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putInt(CURRENT_MISSION_LEVEL_KEY, index);
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

        mImageMap.centerToLevelArea();

    }


    protected Toolbar initToolbar(int toolbarViewId) {
        Toolbar toolbar = (Toolbar) findViewById(toolbarViewId);
        return toolbar;
    }

}