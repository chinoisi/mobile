package yesuaini.chinoisinteractif.hsk;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import yesuaini.chinoisinteractif.hsk.adapters.WordViewBinder;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata;
import yesuaini.chinoisinteractif.hsk.repository.SimpleCursorLoader;
import yesuaini.chinoisinteractif.hsk.service.QuizService;
import yesuaini.chinoisinteractif.hsk.utils.AsyncSoundPlayer;
import yesuaini.chinoisinteractif.hsk.utils.SoundManager;
import yesuaini.chinoisinteractif.R;

public class CharacterListActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
	private static final String LOG_TAG = CharacterListActivity.class.getSimpleName();
	private static final String LENGTH_FROM_TOP = "lengthFromTop";
	private static final String LIST_POS = "listPos";
	protected static final String PREFS_NAME = "yesuaini.chinoisinteractif.list.prefs";
	public static final String SELECTED_WORD_LIST_EXTRA = "yesuaini.chinoisinteractif.wordlist";
	
	private DatabaseHelper dbh;
	private ListView listView;
	private WordViewBinder viewBinder;
	private SimpleCursorAdapter adapter;
	private static int wordListId;
	private SoundManager soundManager;
	private AudioManager audioManager;
	private QuizService quizService;
	public static String orderBy = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hsk_charlist);
		
		wordListId = getIntent().getIntExtra(SELECTED_WORD_LIST_EXTRA, DatabaseMetadata.WORD_LIST_ID_HSK1);
		if (wordListId == 0) {
			throw new RuntimeException("ERROR: No word list found");
		}
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		Editor edit = prefs.edit();
		edit.putInt(SELECTED_WORD_LIST_EXTRA, wordListId);
		edit.commit();
		
		dbh = getDBHelper();
		orderBy = getOrderBy();
		
		setTitle(dbh.getWordListName(wordListId));
		
		viewBinder = new WordViewBinder();
		listView = (ListView) findViewById(R.id.charListView);
		initList();
		quizService = new QuizService();
		audioManager = (AudioManager) this.getSystemService(AUDIO_SERVICE);
		soundManager = new SoundManager(getAssets(), quizService.getFilePathByWordList(wordListId));
		listView.setOnItemClickListener(new PlaySoundClickListener());
		listView.setFastScrollEnabled(true);
		
		getSupportLoaderManager().initLoader(0, null, this);
	}

	public boolean handleIsLearnedClick(int wordId) {
		boolean isLearned = getDBHelper().isWordLearned(wordId);
		Log.d(LOG_TAG, "Changing word " + wordId + " islearned status from " + isLearned + " to " + !isLearned);
		isLearned = !isLearned;
		getDBHelper().setWordLearnedStatus(wordId, isLearned);
		return isLearned;
	}
	
	private String getOrderBy() {
		//TODO implement option for ordering by searchkey or default order (e.g. order in input file)
		return "searchkey";
	}

	private void initList() {
		TextView tv = new TextView(this);
		tv.setText("No characters found...");
		listView.setEmptyView(tv);
		String[] from = new String[] {"word", "pinyin", "definition", "islearned"};
		int[] to = new int[] {R.id.charListView, R.id.pinyinListView, R.id.defListView, R.id.isLearnedCheckBox};
		adapter = new SimpleCursorAdapter(this, R.layout.hsk_list_item, null, from, to);
		adapter.setViewBinder(viewBinder);
		listView.setAdapter(adapter);
	}
	
	@Override
	protected void onResume() { //requery for list and restore list position
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		
		if (wordListId == 0) {
			wordListId = prefs.getInt("edu.openhsk.wordlist", DatabaseMetadata.WORD_LIST_ID_HSK1);
		}
		
		if (listView == null) {
			listView = (ListView) findViewById(R.id.charListView);
			initList();
		}
		
		int listPos = prefs.getInt(LIST_POS, 0);
		int lengthFromTop = prefs.getInt(LENGTH_FROM_TOP, 0);
		listView.setSelectionFromTop(listPos, lengthFromTop);
		if (listPos != 0) {
			Log.d(LOG_TAG, "Restoring list position to index " + listPos);
		}
		
		super.onResume();
	}
	
	@Override
	protected void onPause() { //save position in list on pause
		if (listView != null) {
			int position = listView.getFirstVisiblePosition();
			View v = listView.getChildAt(0);
			int lengthFromTop = (v == null) ? 0 : v.getTop();
			SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			Editor edit = prefs.edit();
			edit.putInt(LIST_POS, position);
			edit.putInt(LENGTH_FROM_TOP, lengthFromTop);
			edit.commit();
		}
		
		super.onPause();
	}
	
	private class PlaySoundClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> parent, View view, 
				int position, long id) {
			if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
				//pick out the hanzi of the selected list item
				TextView tv = (TextView) view.findViewById(R.id.charListView);
				String word = tv.getText().toString();
				
				// use hanzi to search for the sound filepath and play sound
				String fileName = dbh.getSoundfileNameByWord(word, wordListId);
				Log.d(LOG_TAG, "Try to play hanzi sound " + fileName);
				new AsyncSoundPlayer().execute(fileName, soundManager);
			}
		}
	}
	
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new WordCursorLoader(this, getDBHelper());
	}

	private DatabaseHelper getDBHelper() {
		if (dbh == null) {
			dbh = new DatabaseHelper(this);
		}
		return dbh;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter.changeCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.changeCursor(null);
	}
	
	@Override
	protected void onDestroy() {
		DatabaseHelper.closeDatabase(getDBHelper().getReadableDatabase());
		super.onDestroy();
	}
	
	public static final class WordCursorLoader extends SimpleCursorLoader {
		private static final String[] queryColumns = new String[] {"_id", "word", "pinyin", "definition", "islearned"};
		private final DatabaseHelper dbh;

		public WordCursorLoader(Context context, DatabaseHelper dbh) {
			super(context);
			this.dbh = dbh;
		}

		@Override
		public Cursor loadInBackground() {
			SQLiteDatabase db = dbh.getReadableDatabase();
			return db.query(DatabaseMetadata.T_WORDS, queryColumns, "wordlistid = ?",
					new String[] {String.valueOf(wordListId)}, "", "", orderBy);
		}
	}
}
