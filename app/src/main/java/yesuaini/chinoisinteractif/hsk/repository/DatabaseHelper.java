package yesuaini.chinoisinteractif.hsk.repository;

import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_CACHE;
import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_EXAM_RESULTS;
import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_WORDLISTS;
import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_WORDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import yesuaini.chinoisinteractif.hsk.models.ExamResult;
import yesuaini.chinoisinteractif.hsk.models.Hanzi;
import yesuaini.chinoisinteractif.hsk.models.QuizHanzi;
import yesuaini.chinoisinteractif.hsk.models.WordList;
import yesuaini.chinoisinteractif.hsk.utils.PinyinReplacer;

public class DatabaseHelper extends SQLiteOpenHelper {
	private static final String LOG_TAG = DatabaseHelper.class.getSimpleName();
	private static final String DBNAME = "openhskdb";
	private static final int DBVERSION = 13;

	private HanziRepository hanziRepository;
	private QuizRepository quizRepository;
	private WordListRepository wordListRepository;

	private DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, DBNAME, null, DBVERSION);
	}

	public DatabaseHelper(Context context) {
		this(context, "", null, 0);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(LOG_TAG, "Creating new database");
		
		// remember to update these statements to contain all database upgrades
		db.execSQL("CREATE TABLE t_words(_id integer primary key, wordlistid integer not null, word text not null, pinyin text not null, definition text not null, searchkey text not null, islearned integer default 0, soundfile text)");

		db.execSQL("CREATE TABLE t_cache(_id integer primary key, word_id integer);");

		db.execSQL("CREATE TABLE t_wordlists(_id integer primary key, name text not null unique, user_defined int not null);");

		db.execSQL("CREATE TABLE t_exam_results(_id integer primary key, wordlistid integer not null, correct_answers integer default 0, wrong_answers integer default 0, exam_date integer not null);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
		Log.d(LOG_TAG, "Upgrading existing database");
		if (oldVer != newVer) {
			for (int ver = oldVer; ver < newVer; ver++) {
				Log.d(LOG_TAG, "Upgrading database to version: " + ver);
				if (ver == 1) {

				}
			}
		}
	}



	private boolean doesTableExist(SQLiteDatabase db, String tableName) {
		Cursor cursor = db.rawQuery(
				"SELECT DISTINCT tbl_name FROM sqlite_master WHERE tbl_name = '"
						+ tableName + "'", null);
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				closeCursor(cursor);
				return true;
			}
			closeCursor(cursor);
		}
		return false;
	}
	
	public Integer[] doWordListHealthCheck() {
		SQLiteDatabase db = getReadableDatabase();
		Integer[] array = getHanziRepository().getEmptyWordListIds(db);
		closeDatabase(db);
		return array;
	}

		public Boolean doWordListHealthCheck(Integer wordListId) {
		SQLiteDatabase db = getReadableDatabase();
		Boolean isEmpty = getHanziRepository().isWordListEmpty(db,wordListId);
		closeDatabase(db);
		return isEmpty;
	}


	public void addNewWord(Hanzi hanzi, int wordListId) throws IOException {
		SQLiteDatabase db = getWritableDatabase();
		try {
			getHanziRepository().createNewWord(db, hanzi, wordListId);
			Log.d(LOG_TAG, "Added word with pinyin: " + hanzi.getPinyin() + " in wordlist " + wordListId);
		} finally {
			closeDatabase(db);
		}
	}
	
	public void addNewWord(String hanzi, String pinyin, String definition, int wordListId) throws IOException {
		Hanzi h = new Hanzi();
		h.setWord(hanzi);
		h.setPinyin(PinyinReplacer.convertNumberedToneMarksToVisual(pinyin));
		h.setDefinition(definition);
		h.setIsLearned(false);
		h.setSearchKey(PinyinReplacer.removeAllToneMarks(pinyin));
		h.setSoundfile(null);
		addNewWord(h, wordListId);
	}
	
	public void clearWordList(int wordListId) throws IOException {
		SQLiteDatabase db = getWritableDatabase();
		getHanziRepository().clearWordList(db, wordListId);
		closeDatabase(db);
	}
	
	public Hanzi getWordById(int id) {
		SQLiteDatabase db = getReadableDatabase();
		Hanzi h = getHanziRepository().getWordById(db, id);
		closeDatabase(db);
		return h;
	}
	
	public String getSoundfileNameByWord(String word, int wordListId) {
		SQLiteDatabase db = getReadableDatabase();
		String filePath = getHanziRepository().getSoundfileNameByWord(db, word, wordListId);
		closeDatabase(db);
		return filePath;
	}
	
	public void invalidateCache() {
		SQLiteDatabase db = getReadableDatabase();
		invalidateCache(db);
		closeDatabase(db);
	}
	
	public void saveQuizForLater(int[] hanziIds) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			db = getReadableDatabase();
			
			invalidateCache(db);
			
			ContentValues values = new ContentValues(1);
			for (int i = 0; i < hanziIds.length; i++) {
				values.put("word_id", hanziIds[i]);
				db.insert(T_CACHE, null, values);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
			closeDatabase(db);
		}
	}
	
	public void removeWordList(int wordListId) {
		SQLiteDatabase db = getReadableDatabase();
		String wordListIdString = String.valueOf(wordListId);
		db.delete(T_WORDS, "wordlistid = ?", new String[] {wordListIdString});
		db.delete(T_WORDLISTS, "_id = ?", new String[] {wordListIdString});
		db.delete(T_EXAM_RESULTS, "wordlistid = ?", new String[] {wordListIdString});
		closeDatabase(db);
	}
	
	public List<QuizHanzi> makeQuizList(boolean isCached, int chosenQuizTable) {
		SQLiteDatabase db = getReadableDatabase();
		List<QuizHanzi> list = getQuizRepository().makeQuizList(db, isCached, chosenQuizTable);
		closeDatabase(db);
		return list;
	}
	
	public int getHighestWordListId() {
		SQLiteDatabase db = getReadableDatabase();
		int id = getWordListRepository().getHighestWordListId(db);
		closeDatabase(db);
		return id;
	}
	
	public void upsertWordListByName(String name, int wordListId) {
		SQLiteDatabase db = getWritableDatabase();
		WordListRepository wordListRepository = getWordListRepository();
		if (wordListRepository.getWordListName(db, wordListId) != null) {
			wordListRepository.setWordListName(db, name, wordListId);
		} else {
			wordListRepository.createWordList(db, wordListId, name);
		}
		closeDatabase(db);
	}
	
	public String getWordListName(int wordListId) {
		SQLiteDatabase db = getReadableDatabase();
		String name = getWordListRepository().getWordListName(db, wordListId);
		closeDatabase(db);
		return name;
	}
	
	public int getWordIdByHanzi(String text, int wordListId) {
		SQLiteDatabase db = getReadableDatabase();
		int id = getHanziRepository().getWordIdByHanzi(db, text, wordListId);
		closeDatabase(db);
		return id;
	}
	
	public Cursor makeCharacterListQuery(int wordListId) {
		Log.d(LOG_TAG, "Word list query for id: " + wordListId);
		SQLiteDatabase db = getReadableDatabase();
		String[] columns = new String[] {"_id", "word", "pinyin", "definition", "islearned"};
		Cursor cursor = db.query(T_WORDS, columns, "wordlistid = ?", 
				new String[] {String.valueOf(wordListId)}, "", "", "searchkey");
		closeDatabase(db);
		return cursor;
	}
	
	private void invalidateCache(SQLiteDatabase db) {
		Cursor cursor = null;
		try {
			cursor = db.query(T_CACHE, new String[] {"_id"}, 
					null, null, null, null, null);
			if (cursor.getCount() > 0) {
				db.delete(T_CACHE, null, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
	}
	
	public boolean checkForDuplicateWordListName(String name) {
		SQLiteDatabase db = getReadableDatabase();
		boolean result = getWordListRepository().findWordListByName(db , name) == -1;
		closeDatabase(db);
		return result;
	}
	
	public void editWord(int wordId, String hanzi, String pinyin, String definition, int wordListId) throws IOException {
		SQLiteDatabase db = getWritableDatabase();
		try {
			getHanziRepository().editWord(
					db,
					wordId,
					wordListId,
					new Hanzi(hanzi, PinyinReplacer
							.convertNumberedToneMarksToVisual(pinyin),
							definition, PinyinReplacer
									.removeAllToneMarks(pinyin)));
			Log.d(LOG_TAG, "Edited word in list " + wordListId + " with pinyin: " + pinyin);
		} finally {
			closeDatabase(db);
		}
	}
	
	public void deleteWord(long wordId) {
		SQLiteDatabase db = getWritableDatabase();
		try {
			getHanziRepository().deleteWord(db, wordId);
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeDatabase(db);
	}

	public boolean isUserDefinedWordList(int wordListId) {
		SQLiteDatabase db = getReadableDatabase();
		boolean result = getWordListRepository().isUserDefined(db, wordListId);
		closeDatabase(db);
		return result;
	}
	
	public int getWordListSize(long wordListId) {
		SQLiteDatabase db = getReadableDatabase();
		int result = getWordListRepository().getNumberOfWords(db, wordListId);
		closeDatabase(db);
		return result;
	}
	
	public void setWordLearnedStatus(int wordId, boolean isLearned) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			getHanziRepository().setIsLearnedStatusForWord(db, isLearned, wordId);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeDatabase(db);
		}
	}
	
	public boolean isWordLearned(int wordId) {
		SQLiteDatabase db = null;
		boolean isLearned = false;
		try {
			db = getWritableDatabase();
			isLearned = getHanziRepository().getIsLearnedStatusForWord(db, wordId);
		} finally {
			closeDatabase(db);
		}
		return isLearned;
	}

	public QuizHanzi getNextExamWord(int wordId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		try {
			String[] columns = {"_id", "word", "pinyin", "definition", "soundfile"};
			cursor = db.query(T_WORDS, columns, "_id = ?",
					new String[] { String.valueOf(wordId) }, null, null, null);
			if (cursor.moveToFirst()) {
				return QuizRepository.convertDbRowToQuizHanzi(cursor);
			}
		} finally {
			closeCursor(cursor);
			closeDatabase(db);
		}
		return null;
	}
	
	public List<QuizHanzi> getChoicesForExam(boolean isCached, int wordId, int wordListId) {
		SQLiteDatabase db = getReadableDatabase();
		List<QuizHanzi> list = null;
		try {
			list = getQuizRepository().makeExamList(db, isCached, wordId, wordListId);
		} finally {
			closeDatabase(db);
		}
		return list;
	}
	
	public void addExamResultToStatistics(int wordList, int correctAnswers,
			int wrongAnswers, long date) {
		SQLiteDatabase db = null;
		try {
			db = getWritableDatabase();
			ContentValues values = new ContentValues(4);
			values.put("wordlistid", wordList);
			values.put("correct_answers", correctAnswers);
			values.put("wrong_answers", wrongAnswers);
			values.put("exam_date", date);
			db.insert(T_EXAM_RESULTS, null, values);
		} finally {
			closeDatabase(db);
		}
	}
	
	public boolean checkForWordListChanges(int wordListId, int size, int highestId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		boolean result = false;
		try {
			cursor = db.rawQuery("SELECT _id FROM " + T_WORDS
					+ " WHERE wordlistid = ? ORDER BY _id DESC LIMIT 1",
					new String[] { String.valueOf(wordListId) });
			cursor.moveToFirst();
			int currentHighestId = cursor.getInt(0);
			closeCursor(cursor);
			cursor = db.rawQuery("SELECT size(_id) FROM " + T_WORDS
					+ " WHERE wordlistid = ?",
					new String[] { String.valueOf(wordListId) });
			cursor.moveToFirst();
			int currentWordListSize = cursor.getInt(0);
			closeCursor(cursor);
			result = size == currentWordListSize && highestId == currentHighestId; 
		} finally {
			closeDatabase(db);
		}
		return result;
	}
	
	public int getWordListHighestId(int wordListId) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		int result;
		try {
			cursor = db.rawQuery("SELECT _id FROM " + T_WORDS
					+ " WHERE wordlistid = ? ORDER BY _id DESC LIMIT 1",
					new String[] { String.valueOf(wordListId) });
			cursor.moveToFirst();
			result = cursor.getInt(0);
		} finally {
			closeCursor(cursor);
			closeDatabase(db);
		}
		return result;
	}
	
	public String getShuffledIdsForWordList(int wordList) {
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = null;
		try {
			cursor = db.rawQuery("SELECT _id FROM " + T_WORDS
					+ " WHERE wordlistid = ? ORDER BY RANDOM();",
					new String[] { String.valueOf(wordList) });
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			while (cursor.moveToNext()) {
				if (!first) {
					sb.append(",");
				}
				sb.append(cursor.getInt(0));
				first = false;
			}
			return sb.toString();
		} finally {
			closeCursor(cursor);
			closeDatabase(db);
		}
	}
	
	public List<ExamResult> getExamResultsHistoryForWordList(int wordListId) {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		List<ExamResult> list = new ArrayList<ExamResult>(7);
		try {
			db = getReadableDatabase();
			cursor = db.rawQuery("SELECT correct_answers, wrong_answers, exam_date " +
									" FROM " + T_EXAM_RESULTS +
									" WHERE wordListId = ? " +
									" ORDER BY exam_date DESC " +
									" LIMIT 7",
							new String[] { String.valueOf(wordListId) });
			while (cursor.moveToNext()) {
				list.add(new ExamResult(
					cursor.getInt(cursor.getColumnIndex("correct_answers")),
					cursor.getInt(cursor.getColumnIndex("wrong_answers")),
					new Date(cursor.getLong(cursor.getColumnIndex("exam_date")))
				));
			}
		} finally {
			closeCursor(cursor);
			closeDatabase(db);
		}
		return list;
	}
	
	public List<WordList> getAllWordLists() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			List<WordList> list = new ArrayList<WordList>();
			db = getReadableDatabase();
			final String[] queryColumns = { "_id", "name", "user_defined" };
			cursor = db.query(T_WORDLISTS, queryColumns, "", null, "", "", "_id");
			while (cursor.moveToNext()) {
				list.add(new WordList(cursor.getInt(cursor.getColumnIndex("_id")), 
						cursor.getString(cursor.getColumnIndex("name")), 
						cursor.getInt(cursor.getColumnIndex("user_defined"))));
			}
			return list;
		} finally {
			closeCursor(cursor);
			closeDatabase(db);
		}
	}
	
	public List<String> getAllWordListNames() {
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try {
			List<String> list = new ArrayList<String>();
			db = getReadableDatabase();
			final String[] queryColumns = { "name" };
			cursor = db.query(T_WORDLISTS, queryColumns, "", null, "", "", "_id");
			while (cursor.moveToNext()) {
				list.add(cursor.getString(cursor.getColumnIndex("name")));
			}
			return list;
		} finally {
			closeCursor(cursor);
			closeDatabase(db);
		}
	}

	protected static void closeCursor(Cursor cursor) {
		try {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error closing cursor");
		}
	}

	public static void closeDatabase(SQLiteDatabase db) {
		try {
			if (db != null && db.isOpen()) {
				db.close();
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error closing db");
		}
	}

	protected static void closeHelper(DatabaseHelper dbh) {
		try {
			if (dbh != null) {
				dbh.close();
			}
		} catch (Exception e) {
			Log.e(LOG_TAG, "Error closing dbh");
		}
	}

	private QuizRepository getQuizRepository() {
		if (quizRepository == null) {
			quizRepository = new QuizRepository();
		}
		return quizRepository;
	}

	private HanziRepository getHanziRepository() {
		if (hanziRepository == null) {
			hanziRepository = new HanziRepository();
		}
		return hanziRepository;
	}
	
	private WordListRepository getWordListRepository() {
		if (wordListRepository == null) {
			wordListRepository = new WordListRepository();
		}
		return wordListRepository;
	}
}
