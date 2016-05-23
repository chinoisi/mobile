package yesuaini.chinoisinteractif.hsk;
import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata;
import yesuaini.chinoisinteractif.hsk.repository.SimpleCursorLoader;
import yesuaini.chinoisinteractif.R;

public class CreateWordListActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
	private static final String LOG_TAG = CreateWordListActivity.class.getSimpleName();
	private static final int ADD_WORD_REQUEST_CODE = 1;

	protected static final String NEW_WORD_LIST_ID_EXTRA = "yesuaini.chinoisinteractif.createwordlist.wordListId";
	protected static final String NEW_WORD_LIST_NAME_EXTRA = "yesuaini.chinoisinteractif.createwordlist.wordListName";
	
	private ListView listView;
	private DatabaseHelper dbh;
	private SimpleCursorAdapter adapter;
	private ViewBinder viewBinder;
	private static int wordListId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hsk_create_word_list);
		
		Intent intent = getIntent();
		wordListId = intent.getIntExtra(NEW_WORD_LIST_ID_EXTRA, -1);
		setTitle(intent.getStringExtra(NEW_WORD_LIST_NAME_EXTRA));
		
		dbh = getDBHelper();
		
		listView = (ListView) findViewById(R.id.hsk_wordListListView);
		registerForContextMenu(listView);
		refreshList();
		
		getSupportLoaderManager().initLoader(1, null, this);
		
		View footerView = findViewById(R.id.wordListFooterButtonAddNewWord);
		footerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(CreateWordListActivity.this, CreateWordActivity.class);
				startActivityForResult(intent, ADD_WORD_REQUEST_CODE);
			}
		});
		View footerView2 = findViewById(R.id.wordListFooterButtonSaveWordList);
		footerView2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(CreateWordListActivity.this, WordListSelectionActivity.class));
			}
		});
		
		listView.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				editWord((int)id);
			}
		});
	}
	
	@Override
	protected void onResume() {
		if (listView == null) {
			listView = (ListView) findViewById(R.id.hsk_wordListListView);
			refreshList();
		}
		super.onResume();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		//unused
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ADD_WORD_REQUEST_CODE && resultCode == RESULT_OK) {
			int wordId = data.getIntExtra(CreateWordActivity.EDITED_WORD_ID, -1);
			String hanzi = data.getStringExtra(CreateWordActivity.NEW_WORD_HANZI);
			String pinyin = data.getStringExtra(CreateWordActivity.NEW_WORD_PINYIN);
			String definition = data.getStringExtra(CreateWordActivity.NEW_WORD_DEFINITION);
			try {
				if (wordId != -1) {
					getDBHelper().editWord(wordId, hanzi, pinyin, definition, wordListId);
				} else {
					getDBHelper().addNewWord(hanzi, pinyin, definition, wordListId);
				}
				getSupportLoaderManager().restartLoader(1, null, this);
				Log.d(LOG_TAG, "Added " + hanzi + " to list " + wordListId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void refreshList() {
		TextView tv = new TextView(this);
		tv.setText(R.string.no_chars_found);
		listView.setEmptyView(tv);
		String[] from = new String[] {"word", "pinyin", "definition", "islearned"};
		int[] to = new int[] {R.id.charListView, R.id.pinyinListView, R.id.defListView};
		adapter = new SimpleCursorAdapter(this, R.layout.hsk_list_item, null, from, to);
		adapter.setViewBinder(viewBinder);
		listView.setAdapter(adapter);
	}

	private DatabaseHelper getDBHelper() {
		if (dbh == null) {
			dbh = new DatabaseHelper(this);
		}
		return dbh;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.word_context_menu, menu);
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
	    switch (item.getItemId()) {
	        case R.id.deleteWordOptionsItem:
	        	getDBHelper().deleteWord(info.id);
	        	getSupportLoaderManager().restartLoader(1, null, this);
	        	return true;
	        case R.id.editWordOptionsItem:
	        	editWord((int)info.id);
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}

	private void editWord(int id) {
		Intent intent = new Intent(CreateWordListActivity.this, CreateWordActivity.class);
		intent.putExtra(CreateWordActivity.ID_OF_WORD_TO_EDIT_EXTRA, id);
		startActivityForResult(intent, ADD_WORD_REQUEST_CODE);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new WordCursorLoader(this, getDBHelper());
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
			return dbh.getReadableDatabase().query(DatabaseMetadata.T_WORDS, queryColumns, "wordlistid = ?",
					new String[] {String.valueOf(wordListId)}, "", "", "");
		}
	}
}
