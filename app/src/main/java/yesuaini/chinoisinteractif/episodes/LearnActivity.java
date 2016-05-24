package yesuaini.chinoisinteractif.episodes;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import yesuaini.chinoisinteractif.R;

public class LearnActivity extends Activity {
	// ================= STATIC ATTRIBUTES ======================

	private static final String KEY_POSITION = "pos";

	// ================= INSTANCE ATTRIBUTES ====================

	private TextView wordCardNat = null;
	private TextView wordCardFor = null;
	private TextView cardFactor = null;
	private TextView cardPos = null;
	private TextView posClickLabel = null;
	private Button nextBtn = null;
	private Button prevBtn = null;

	private Cursor wordCursor = null;
	private long dictId = 0;
	private int position = 0;


	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {
			this.position = savedInstanceState.getInt(KEY_POSITION);
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



		int cnt = this.wordCursor.getCount();
		if (this.position >= cnt) { // User could delete some card
			this.position = cnt - 1;
		}

		this.wordCursor.moveToPosition(this.position);
	}

	@Override
	protected void onPause() {
		this.wordCursor.close();

		super.onPause();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(KEY_POSITION, this.position);
	}



}
