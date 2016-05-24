package yesuaini.chinoisinteractif.hsk;

import static yesuaini.chinoisinteractif.hsk.CharacterListActivity.SELECTED_WORD_LIST_EXTRA;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import android.widget.Toast;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata;
import yesuaini.chinoisinteractif.hsk.repository.SimpleCursorLoader;
import yesuaini.chinoisinteractif.hsk.utils.CSVExporter;
import yesuaini.chinoisinteractif.hsk.utils.CSVParser;
import yesuaini.chinoisinteractif.R;

public class WordListSelectionActivity extends FragmentActivity implements LoaderCallbacks<Cursor> {
	@SuppressWarnings("unused") private static final String LOG_TAG = WordListSelectionActivity.class.getSimpleName();
	protected static final String WORD_LIST_SELECTION_MODE_EXTRA = "yesuaini.chinoisinteractif.wordlistselection.mode";
	protected static final int LIST_MODE = 0;
	protected static final int QUIZ_MODE = 1;
	protected static final int EXAM_MODE = 2;
	private static final String[] queryColumns = { "_id", "name" };
	private static final String[] from = { "name" };
	private static final int[] to = { R.id.wordListNameView };

	private DatabaseHelper dbh;
	private SimpleCursorAdapter adapter;
	private ViewBinder viewBinder;
	private ListView wordListView;
	private int mode;
	private AlertDialog wordListNameInputDialog;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.hsk_wordlists);

		Intent starterIntent = getIntent();
		mode = starterIntent.getIntExtra(WORD_LIST_SELECTION_MODE_EXTRA, 0);
		setTitle(getWordListSelectionModeName(mode) + (mode != LIST_MODE ? " - " : "") + getString(R.string.hsk1_list_str));

		dbh = new DatabaseHelper(this);

		wordListView = (ListView) findViewById(R.id.wordListView);
		registerForContextMenu(wordListView);
		wordListView.setFastScrollEnabled(true);
		wordListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (mode == LIST_MODE || mode == QUIZ_MODE) {
					startActivityForWordList(id);
				} else {
					startExamActivity((int)id);
				}
			}
		});
		refreshList();
		
		getSupportLoaderManager().initLoader(1, null, this);
		
		wordListNameInputDialog = makeNewWordListNameDialog();
		
		Button newWordListButton = (Button) findViewById(R.id.newWordListButton);
		newWordListButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				wordListNameInputDialog.show();
			}
		});
	}

	private String getWordListSelectionModeName(int mode) {
		if (mode == EXAM_MODE) {
			return getString(R.string.exam_str);
		} else if (mode == QUIZ_MODE) {
			return getString(R.string.quiz_str);
		}
		return "";
	}

	private void refreshList() {
		TextView tv = new TextView(this);
		tv.setText(R.string.no_chars_found);
		wordListView.setEmptyView(tv);
		adapter = new SimpleCursorAdapter(this, R.layout.word_list_item, null, from, to);
		adapter.setViewBinder(viewBinder);
		wordListView.setAdapter(adapter);
		
//		adapter.changeCursor(cursor);
	}

	private void startActivityForWordList(long id) {
		Intent intent = null;
		if (mode == LIST_MODE) {
			intent = new Intent(WordListSelectionActivity.this,
					CharacterListActivity.class);
			intent.putExtra(SELECTED_WORD_LIST_EXTRA, (int) id);
		} else if (mode == QUIZ_MODE) {
			if (getDBHelper().getWordListSize(id) >= 4) {
				Editor editor = getSharedPreferences(QuizActivity.PREFS_NAME, MODE_PRIVATE).edit();
				editor.putBoolean(QuizActivity.IS_CACHED, false);
				editor.commit();
				intent = new Intent(WordListSelectionActivity.this, QuizActivity.class);
				intent.putExtra(QuizActivity.SELECTED_QUIZ_TABLE_EXTRA, (int) id);
			} else {
				Toast.makeText(this, R.string.word_list_too_short, Toast.LENGTH_LONG).show();
				return;
			}
		}
		startActivity(intent);
	}
	
	private void startExamActivity(int id) {
		if (getDBHelper().getWordListSize(id) >= 4) {
			Intent intent = new Intent(WordListSelectionActivity.this,
					ExamActivity.class);
			intent.putExtra(ExamActivity.SELECTED_EXAM_WORD_LIST_EXTRA, id);
			SharedPreferences examPrefs = getSharedPreferences(ExamActivity.PREFS_NAME, MODE_PRIVATE);
			boolean ongoing = examPrefs.getInt(ExamActivity.PREF_WORD_LIST_ID, 0) == id 
					&& examPrefs.getInt(ExamActivity.PREF_WORD_LIST_OFFSET, 0) > 0;
			if (ongoing) {
				makeContinueOrResetExamDialog(intent).show();
			} else {
//				examPrefs.edit().putBoolean(ExamActivity.PREF_NEW_EXAM, true).commit();
				intent.putExtra(ExamActivity.NEW_EXAM_EXTRA, true);
				startActivity(intent);
			}
		} else {
			Toast.makeText(this, R.string.exam_word_list_too_short, Toast.LENGTH_LONG).show();
			return;
		}
	}

	private void createNewWordList(String wordListName) {
		int wordListId = getDBHelper().getHighestWordListId() + 1;
		getDBHelper().upsertWordListByName(wordListName, wordListId);
		getSupportLoaderManager().restartLoader(1, null, this);
		Intent intent = new Intent(WordListSelectionActivity.this,
				CreateWordListActivity.class);
		intent.putExtra(CreateWordListActivity.NEW_WORD_LIST_ID_EXTRA, wordListId);
		intent.putExtra(CreateWordListActivity.NEW_WORD_LIST_NAME_EXTRA, wordListName);
		startActivity(intent);
	}

	private void editWordList(int wordListId) {
		if (getDBHelper().isUserDefinedWordList(wordListId)) {
			Intent intent = new Intent(WordListSelectionActivity.this,
					CreateWordListActivity.class);
			intent.putExtra(CreateWordListActivity.NEW_WORD_LIST_ID_EXTRA, wordListId);
			intent.putExtra(CreateWordListActivity.NEW_WORD_LIST_NAME_EXTRA, getDBHelper().getWordListName(wordListId));
			startActivity(intent);
		} else {
			Toast.makeText(this, R.string.system_defined_word_lists_cannot_be_edited, 
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private void deleteWordList(int wordListId) {
		if (getDBHelper().isUserDefinedWordList(wordListId)) {
			getDBHelper().removeWordList(wordListId);
			getSupportLoaderManager().restartLoader(1, null, this);
		} else {
			Toast.makeText(this,
					R.string.system_defined_word_lists_cannot_be_deleted,
					Toast.LENGTH_SHORT).show();
		}
	}
	
	private void renameWordList(int wordListId) {
		if (getDBHelper().isUserDefinedWordList(wordListId)) {
			AlertDialog wordListRenameInputDialog = makeRenameWordListNameDialog(wordListId, 
					getDBHelper().getWordListName(wordListId));
			wordListRenameInputDialog.show();
		} else {
			Toast.makeText(this,
					R.string.system_defined_word_lists_cannot_be_renamed,
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	public void onResume() {
		if (wordListView == null) {
			wordListView = (ListView) findViewById(R.id.wordListView);
			refreshList();
		}
		super.onResume();
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		SharedPreferences examPrefs = getSharedPreferences(ExamActivity.PREFS_NAME, MODE_PRIVATE);
		boolean isExamInProgress = examPrefs.getInt(ExamActivity.PREF_WORD_LIST_ID, 0) > 0 
				&& examPrefs.getInt(ExamActivity.PREF_WORD_LIST_OFFSET, 0) > 0;
		menu.getItem(0).setEnabled(isExamInProgress);
		return super.onPrepareOptionsMenu(menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.select_word_list_menu, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.ongoing_exam_menu_button:
	            showContinueExamDialog();
	            return true;
	        case R.id.import_word_list_menu_button:
	        	showImportWordListDialog();
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}

	private void showImportWordListDialog() {
		makeImportWordListDialog().show();
	}
	
	private AlertDialog makeImportWordListDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		List<String> filesList = null;
		try {
			filesList = new AsyncImportFileListLoader().execute().get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (filesList == null) {
			Toast.makeText(WordListSelectionActivity.this, 
					"No files to import found", Toast.LENGTH_SHORT).show();
			return null;
		}
		
		final CharSequence[] items = new CharSequence[filesList.size()];
		int i = 0;
		for (String file : filesList) {
			items[i++] = file;
		}
		AlertDialog.OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				new AsyncImportFileImporter(items, which).execute();
			}
		};
		
		//TODO replace setItems with setCursor
		return builder.setTitle("Choose a word list").setItems(items, listener).create();
	}

	private void showContinueExamDialog() {
		Intent intent = new Intent(WordListSelectionActivity.this,
				ExamActivity.class);
		SharedPreferences examPrefs = getSharedPreferences(ExamActivity.PREFS_NAME, MODE_PRIVATE);
		int id = examPrefs.getInt(ExamActivity.PREF_WORD_LIST_ID, 0);
		intent.putExtra(ExamActivity.SELECTED_EXAM_WORD_LIST_EXTRA, id);
		makeContinueExamDialog(intent).show();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.word_list_item_context_menu, menu);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
			case R.id.editWordList:
				editWordList((int) info.id);
				return true;
			case R.id.deleteWordList:
				deleteWordList((int) info.id);
				return true;
			case R.id.editWordListName:
				renameWordList((int) info.id);
				return true;
			case R.id.exportWordList:
				exportWordList((int) info.id);
				return true;
			default:
				return super.onContextItemSelected(item);
		}
	}
	
	private void exportWordList(int wordListId) {
		try {
			new CSVExporter(dbh).exportWordListToCSVOnSDCard(wordListId);
			Toast.makeText(this, "Export to SD-card succesful", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(this, "Error during export", Toast.LENGTH_SHORT).show();
			e.printStackTrace();
		}
	}
	
	private AlertDialog makeContinueOrResetExamDialog(final Intent intent) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.ongoing_exam_found);
		builder.setMessage(R.string.exam_reset_continue_cancel);
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
//				SharedPreferences examPrefs = getSharedPreferences(ExamActivity.PREFS_NAME, MODE_PRIVATE);
				if (which == Dialog.BUTTON_POSITIVE) { //continue
//					examPrefs.edit().putBoolean(ExamActivity.PREF_NEW_EXAM, false).commit();
					intent.putExtra(ExamActivity.NEW_EXAM_EXTRA, false);
					startActivity(intent);
				} else if (which == Dialog.BUTTON_NEGATIVE) { //reset
//					examPrefs.edit().putBoolean(ExamActivity.PREF_NEW_EXAM, true).commit();
					intent.putExtra(ExamActivity.NEW_EXAM_EXTRA, true);
					startActivity(intent);
				}
				dialog.dismiss();
			}
		};
		builder.setPositiveButton(R.string.continue_str, listener);
		builder.setNegativeButton(R.string.reset, listener);
		return builder.create();
	}
	
	private AlertDialog makeContinueExamDialog(final Intent intent) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.ongoing_exam_found);
		builder.setMessage(R.string.exam_continue_cancel);
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == Dialog.BUTTON_POSITIVE) { //continue
//					SharedPreferences examPrefs = getSharedPreferences(ExamActivity.PREFS_NAME, MODE_PRIVATE);
//					examPrefs.edit().putBoolean(ExamActivity.PREF_NEW_EXAM, false).commit();
					intent.putExtra(ExamActivity.NEW_EXAM_EXTRA, false);
					startActivity(intent);
					dialog.dismiss();
				} else if (which == Dialog.BUTTON_NEGATIVE) { //cancel
					dialog.dismiss();
				}
			}
		};
		builder.setPositiveButton(R.string.continue_str, listener);
		builder.setNegativeButton(R.string.cancel_option, listener);
		return builder.create();
	}

	private AlertDialog makeNewWordListNameDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.word_list_name);

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);

		builder.setPositiveButton(R.string.ok_option, new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        try {
		        	String wordListName = input.getText().toString();
			        if (wordListName != null && wordListName.length() > 0  
							&& getDBHelper().checkForDuplicateWordListName(wordListName)) {
			        	createNewWordList(wordListName);
					} else {
						Toast.makeText(WordListSelectionActivity.this, 
								R.string.invalid_wordlist_name, Toast.LENGTH_SHORT).show();
					}
		        } finally {
		        	input.setText("");
		        }
		    }
		});
		builder.setNegativeButton(R.string.cancel_option, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	input.setText("");
		    	dialog.cancel();
		    }
		});

		return builder.create();
	}
	
	private AlertDialog makeRenameWordListNameDialog(final int wordListIdToRename, final String previousWordListName) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.word_list_name);

		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		builder.setView(input);
		input.setText(previousWordListName);

		builder.setPositiveButton(R.string.ok_option, new DialogInterface.OnClickListener() { 
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	String wordListName = input.getText().toString();
		        if (wordListName != null && wordListName.length() > 0  
						&& getDBHelper().checkForDuplicateWordListName(wordListName)) {
		        	getDBHelper().upsertWordListByName(wordListName, wordListIdToRename);
		        	getSupportLoaderManager().restartLoader(1, null, WordListSelectionActivity.this);
				} else {
					Toast.makeText(WordListSelectionActivity.this, 
							R.string.invalid_wordlist_name, Toast.LENGTH_SHORT).show();
				}
		    }
		});
		builder.setNegativeButton(R.string.cancel_option, new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        dialog.cancel();
		    }
		});

		return builder.create();
	}

	private DatabaseHelper getDBHelper() {
		if (dbh == null) {
			dbh = new DatabaseHelper(this);
		}
		return dbh;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new WordListCursorLoader(this, getDBHelper());
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
	
	public static final class WordListCursorLoader extends SimpleCursorLoader {
		private final DatabaseHelper dbh;

		public WordListCursorLoader(Context context, DatabaseHelper dbh) {
			super(context);
			this.dbh = dbh;
		}

		@Override
		public Cursor loadInBackground() {
			return dbh.getReadableDatabase().query(DatabaseMetadata.T_WORDLISTS, queryColumns,
					"", null, "", "", "_id");
		}
	}
	
	private class AsyncImportFileListLoader extends AsyncTask<Void, Void, List<String>> {
		private ProgressDialog dialog;

		public AsyncImportFileListLoader() {
			dialog = new ProgressDialog(WordListSelectionActivity.this);
		}
		
		@Override
		protected void onPreExecute() {
			dialog.setMessage(WordListSelectionActivity.this.getText(R.string.loading_import_files_msg));
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}

		@Override
		protected List<String> doInBackground(Void... params) {
			File sdCard = Environment.getExternalStorageDirectory();
			final String exportsFilePath = sdCard.getAbsolutePath() + "//chinoisinteractif//exports//";
			File backupFolder = new File(exportsFilePath);
			if (backupFolder.exists()) {
				Set<String> allWordListNames = new HashSet<String>(dbh.getAllWordListNames());
				List<String> filteredFileNames = new ArrayList<String>();
				for (String importFileName : backupFolder.list()) {
					if (!allWordListNames.contains(importFileName.substring(0, importFileName.length() - 4))) {
						filteredFileNames.add(importFileName);
					}
				}
				return filteredFileNames;
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(List<String> result) {
			if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
		}
	}
	
	private class AsyncImportFileImporter extends AsyncTask<Void, Void, Void> {
		private final CharSequence[] items;
		private final int which;
		private ProgressDialog dialog;

		public AsyncImportFileImporter(CharSequence[] items, int which) {
			this.items = items;
			this.which = which;
			dialog = new ProgressDialog(WordListSelectionActivity.this);
		}
		
		@Override
		protected void onPreExecute() {
			dialog.setMessage(WordListSelectionActivity.this.getText(R.string.importing_wordlist_msg));
			dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			dialog.show();
		}

		@Override
		protected Void doInBackground(Void... params) {
			BufferedReader br;
			final int wordListId = dbh.getHighestWordListId() + 1;
			try {
				String fileName = String.valueOf(items[which]);
				File sdCard = Environment.getExternalStorageDirectory();
				final String exportsFilePath = sdCard.getAbsolutePath() + "//chinoisinteractif//exports//";
				br = new BufferedReader(new FileReader(exportsFilePath + fileName));
				new CSVParser(dbh).parseWordListCSV(br, wordListId, CSVParser.SEMICOLON_DELIMITER);
				String wordListName = fileName.substring(0, fileName.length() - 4);
				dbh.upsertWordListByName(wordListName, wordListId);
			} catch (FileNotFoundException e) {
				try {
					dbh.clearWordList(wordListId);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
			
			getSupportLoaderManager().restartLoader(1, null, WordListSelectionActivity.this);
		}
	}
}
