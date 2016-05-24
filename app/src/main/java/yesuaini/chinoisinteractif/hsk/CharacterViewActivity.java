package yesuaini.chinoisinteractif.hsk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import yesuaini.chinoisinteractif.AbstractNavigationActivity;
import yesuaini.chinoisinteractif.hsk.models.Hanzi;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;
import yesuaini.chinoisinteractif.R;

public class CharacterViewActivity extends AbstractNavigationActivity {
	private static final String LOG_TAG = CharacterViewActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.random_word));
		
		Intent intent = getIntent();
		int id = intent.getIntExtra("yesuaini.chinoisinteractif.randomindex", 1);
        
		DatabaseHelper dbh = new DatabaseHelper(this);
		Hanzi charData = dbh.getWordById(id);
		if (charData != null) {
			TextView tv = (TextView) findViewById(R.id.charView);
			tv.setText(charData.getWord());
			
			TextView tv2 = (TextView) findViewById(R.id.pinyinView);
			tv2.setText(charData.getPinyin());
			
			TextView tv1 = (TextView) findViewById(R.id.descrView);
			tv1.setText(charData.getDefinition());
		} else {
			String errorMsg = "No hanzi found for id: " + id;
			Log.e(LOG_TAG, errorMsg);
			Toast.makeText(this, errorMsg, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected int getToolbarViewId() {
		return R.id.toolbar;
	}

	@Override
	protected int getContentViewActivity() {
		return R.layout.hsk_singlechar;
	}

}
