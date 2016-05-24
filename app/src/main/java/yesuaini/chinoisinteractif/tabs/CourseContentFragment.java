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

import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.models.Mission;

/**
 * Simple Fragment used to display some meaningful content for each page in the sample's
 * {@link android.support.v4.view.ViewPager}.
 */
public class CourseContentFragment extends WebContentFragment {

    public static CourseContentFragment newInstance(Mission mission) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(ContentFragment.KEY_MISSION, mission);
        CourseContentFragment fragment = new CourseContentFragment();
        fragment.setArguments(bundle);

        return fragment;
    }



    public String getUrl(Mission mission) {
        return mission.getUrl();
    }

    protected int getTabsPagerId() {
        return R.layout.tabs_pager_course_lesson;
    }


    protected int getWebViewId() {
        return R.id.courseWebView;
    }
}
