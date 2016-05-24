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

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.common.view.SlidingTabLayout;
import yesuaini.chinoisinteractif.models.Mission;

/**
 * A basic sample which shows how to use
 * * to display a custom {@link ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class SlidingTabsColorsFragment extends Fragment {




    /**
     * This class represents a tab to be displayed by {@link ViewPager} and it's associated
     * {@link SlidingTabLayout}.
     */
    public static class SamplePagerItem {
        private final CharSequence mTitle;
        private final int mIndicatorColor;
        private final int mDividerColor;
        private Mission mission;

        SamplePagerItem(CharSequence title, int indicatorColor, int dividerColor , Mission mission) {
            mTitle = title;
            mIndicatorColor = indicatorColor;
            mDividerColor = dividerColor;
            this.mission = mission;
        }

        Fragment createFragment() {
            return ContentFragment.newInstance(mTitle, mIndicatorColor, mDividerColor, mission);
        }

        CharSequence getTitle() {
            return mTitle;
        }
        int getIndicatorColor() {
            return mIndicatorColor;
        }
        int getDividerColor() {
            return mDividerColor;
        }
        public Mission getMission() {return mission;}
    }



    static class VideoPagerItem extends SamplePagerItem {
        VideoPagerItem(CharSequence title, int indicatorColor, int dividerColor, Mission mission) {
            super(title, indicatorColor, dividerColor, mission);
        }

        Fragment createFragment() {
            return WebContentFragment.newInstance(super.getMission().getVideo());
        }
    }


    static class CoursePagerItem extends SamplePagerItem {
        CoursePagerItem(CharSequence title, int indicatorColor, int dividerColor , Mission mission) {
            super(title,indicatorColor,dividerColor, mission);
        }
        Fragment createFragment() {
            return WebContentFragment.newInstance(super.getMission().getUrl());
        }
    }


    static class ExercicePagerItem extends SamplePagerItem {
        ExercicePagerItem(CharSequence title, int indicatorColor, int dividerColor , Mission mission) {
            super(title,indicatorColor,dividerColor, mission);
        }
        Fragment createFragment() {
            return WebContentFragment.newInstance(super.getMission().getExercice());
        }
    }


    static class MapPagerItem extends SamplePagerItem {
        MapPagerItem(CharSequence title, int indicatorColor, int dividerColor , Mission mission) {
            super(title,indicatorColor,dividerColor, mission);
        }
        Fragment createFragment() {
            return WebContentFragment.newInstance("file:///android_asset/chine/index.html");
        }
    }


    static class VocabularyPagerItem extends SamplePagerItem {
        VocabularyPagerItem(CharSequence title, int indicatorColor, int dividerColor , Mission mission) {
            super(title,indicatorColor,dividerColor, mission);        }

        Fragment createFragment() {
            return VocabularyContentFragment.newInstance(super.getMission());
        }
    }





    static final String LOG_TAG = "SlidingTabsColorsFragment";

    /**
     * A custom {@link ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
     */
    private ViewPager mViewPager;

    /**
     * List of {@link SamplePagerItem} which represent this sample's tabs.
     */
    private List<SamplePagerItem> mTabs = new ArrayList<SamplePagerItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mission mission = (Mission) getArguments().getSerializable(ContentFragment.KEY_MISSION);

        // BEGIN_INCLUDE (populate_tabs)
        /**
         * Populate our tab list with tabs. Each item contains a title, indicator color and divider
         * color, which are used by {@link SlidingTabLayout}.
         */

        mTabs.add(new VideoPagerItem(
                getString(R.string.tab_videolesson), // Title
                Color.RED, // Indicator color
                Color.GRAY, // Divider color
                mission
        ));

        mTabs.add(new CoursePagerItem(
                getString(R.string.tab_course), // Title
                Color.BLUE, // Indicator color
                Color.GRAY, // Divider color
                mission
        ));

        mTabs.add(new ExercicePagerItem(
                getString(R.string.tab_exercices), // Title
                Color.YELLOW, // Indicator color
                Color.GRAY, // Divider color
                mission
        ));

        mTabs.add(new VocabularyPagerItem(
                getString(R.string.tab_vocabulary), // Title
                Color.GREEN, // Indicator color
                Color.GRAY, // Divider color
                mission
        ));

        mTabs.add(new MapPagerItem(
                getString(R.string.tab_map), // Title
                Color.MAGENTA, // Indicator color
                Color.GRAY, // Divider color
                mission
        ));

        // END_INCLUDE (populate_tabs)
    }

    /**
     * Inflates the {@link View} which will be displayed by this {@link Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tabs_fragment_sample, container, false);
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)} has finished.
     * Here we can pick out the {@link View}s we need to configure from the content view.
     *
     * We set the {@link ViewPager}'s adapter to be an instance of
     * {@link SampleFragmentPagerAdapter}. The {@link SlidingTabLayout} is then given the
     * {@link ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(LayoutInflater, ViewGroup, Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new SampleFragmentPagerAdapter(getChildFragmentManager()));
        // END_INCLUDE (setup_viewpager)

        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setViewPager(mViewPager);

        // BEGIN_INCLUDE (tab_colorizer)
        // Set a TabColorizer to customize the indicator and divider colors. Here we just retrieve
        // the tab at the position, and return it's set color
        mSlidingTabLayout.setCustomTabColorizer(new SlidingTabLayout.TabColorizer() {

            @Override
            public int getIndicatorColor(int position) {
                return mTabs.get(position).getIndicatorColor();
            }

            @Override
            public int getDividerColor(int position) {
                return mTabs.get(position).getDividerColor();
            }

        });
        // END_INCLUDE (tab_colorizer)
        // END_INCLUDE (setup_slidingtablayout)
    }
    // END_INCLUDE (fragment_onviewcreated)

    /**
     * The {@link FragmentPagerAdapter} used to display pages in this sample. The individual pages
     * are instances of {@link ContentFragment} which just display three lines of text. Each page is
     * created by the relevant {@link SamplePagerItem} for the requested position.
     * <p>
     * The important section of this class is the {@link #getPageTitle(int)} method which controls
     * what is displayed in the {@link SlidingTabLayout}.
     */
    class SampleFragmentPagerAdapter extends FragmentPagerAdapter {

        SampleFragmentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        /**
         * Return the {@link android.support.v4.app.Fragment} to be displayed at {@code position}.
         * <p>
         * Here we return the value returned from {@link SamplePagerItem#createFragment()}.
         */
        @Override
        public Fragment getItem(int i) {
            return mTabs.get(i).createFragment();
        }

        @Override
        public int getCount() {
            return mTabs.size();
        }

        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        /**
         * Return the title of the item at {@code position}. This is important as what this method
         * returns is what is displayed in the {@link SlidingTabLayout}.
         * <p>
         * Here we return the value returned from {@link SamplePagerItem#getTitle()}.
         */
        @Override
        public CharSequence getPageTitle(int position) {
            return mTabs.get(position).getTitle();
        }
        // END_INCLUDE (pageradapter_getpagetitle)

    }

}