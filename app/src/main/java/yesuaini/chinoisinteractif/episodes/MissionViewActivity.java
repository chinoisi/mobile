package yesuaini.chinoisinteractif.episodes;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import yesuaini.chinoisinteractif.AbstractNavigationActivity;
import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.map.EpisodesMapActivity;
import yesuaini.chinoisinteractif.map.MissionsByEpisodeMapActivity;
import yesuaini.chinoisinteractif.tabs.MissionActivity;

import static yesuaini.chinoisinteractif.hsk.CharacterListActivity.SELECTED_WORD_LIST_EXTRA;

public class MissionViewActivity extends AbstractNavigationActivity {

	private static final String LOG_TAG = MissionViewActivity.class.getSimpleName();
    SharedPreferences pref;
    int missionId = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Intent intent = getIntent();
		missionId = intent.getIntExtra(SELECTED_WORD_LIST_EXTRA, 1);

		setTitle(getString(R.string.mission_title) +" " + missionId);

			String MissionMsg = getString(R.string.mission_title) +" " + missionId;
			Toast.makeText(this, MissionMsg, Toast.LENGTH_LONG).show();

        Button trainingButton = (Button) findViewById(R.id.mission_training);
        trainingButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MissionViewActivity.this,MissionActivity.class);
                int missionId = getIntent().getIntExtra(SELECTED_WORD_LIST_EXTRA, 1);
                intent.putExtra(SELECTED_WORD_LIST_EXTRA, missionId);
                startActivity(intent);
            }
        });

        Button playButton = (Button) findViewById(R.id.mission_evaluation);
        playButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pref = getBaseContext().getSharedPreferences(MissionsByEpisodeMapActivity.MAP_MISSIONS, 0);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt(MissionsByEpisodeMapActivity.CURRENT_MISSION_LEVEL_KEY, missionId +1);
                editor.commit();
                Intent intent = new Intent(MissionViewActivity.this,MissionsByEpisodeMapActivity.class);
                startActivity(intent);
            }
        });
	}

	@Override
	protected int getToolbarViewId() {
		return R.id.toolbar;
	}

	@Override
	protected int getContentViewActivity() {
		return R.layout.episode_main;
	}

}
