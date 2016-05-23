package yesuaini.chinoisinteractif.episodes;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import yesuaini.chinoisinteractif.R;

public class PractiseActivity extends Activity {
	// ================= STATIC ATTRIBUTES ======================
	
	private static final String KEY_CARD_ID = "cardId";
	private static final String KEY_OTHER_SIDE_SHOWN = "otherSideShown";
	private static final String KEY_FIRST_WORD = "firstWord";
	private static final String KEY_SECOND_WORD = "secondWord";

	// ================= INSTANCE ATTRIBUTES ====================

	private TextView wordCardNat = null;
	private TextView wordCardFor = null;
	private TextView factorText = null;
	private Button otherSideBtn = null;
	private Button knowBtn = null;
	private Button dontKnowBtn = null;

	private String firstWord = null;
	private String secondWord = null;
	private long cardId = -1;
	private int factor = -1;
	private long dictId = -1;

	private int practiseDirection = -1;
	
	private Bundle restoredInstanceState = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			this.restoredInstanceState = savedInstanceState;
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putLong(KEY_CARD_ID, this.cardId);
		outState.putString(KEY_FIRST_WORD, this.firstWord);
		outState.putString(KEY_SECOND_WORD, this.secondWord);
		outState.putBoolean(KEY_OTHER_SIDE_SHOWN, this.wordCardFor.getVisibility() == View.VISIBLE);
	}



}
