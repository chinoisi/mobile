/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package yesuaini.chinoisinteractif.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.models.Mission;
import yesuaini.chinoisinteractif.tabs.vocabulary.Character;
import yesuaini.chinoisinteractif.tabs.vocabulary.CharacterListAdapter;

/**
 * Simple Fragment used to display some meaningful content for each page in the sample's
 * {@link android.support.v4.view.ViewPager}.
 */
public class VocabularyContentFragment extends Fragment {

    private static final String KEY_TITLE = "title";
    private static final String KEY_INDICATOR_COLOR = "indicator_color";
    private static final String KEY_DIVIDER_COLOR = "divider_color";


    /**
     * @return a new instance of {@link VocabularyContentFragment}, adding the parameters into a bundle and
     * setting them as arguments.
     */
    public static VocabularyContentFragment newInstance(Mission mission) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ContentFragment.KEY_MISSION, mission);
        VocabularyContentFragment fragment = new VocabularyContentFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tabs_pager_vocabulary, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();

        if (args != null) {
            Mission mission = (Mission) args.getSerializable(ContentFragment.KEY_MISSION);
            ListView listView = (ListView) view.findViewById(R.id.charListView);
            List<Character> characters = mission.getCharacters();
            if (characters!=null && !characters.isEmpty()) {
                listView.setAdapter(new CharacterListAdapter(getActivity(), characters));
            } else {
                TextView tv = new TextView(getActivity());
                tv.setText("No characters found...");
                listView.setEmptyView(tv);
            }


        }
    }

    private void initList(ListView listView, List<Character> characters) {




	}


}
