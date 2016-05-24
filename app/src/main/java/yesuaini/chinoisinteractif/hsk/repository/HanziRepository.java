package yesuaini.chinoisinteractif.hsk.repository;

import static yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper.closeCursor;
import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.ALL_WORD_LIST_IDS;
import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_WORDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import yesuaini.chinoisinteractif.hsk.models.Hanzi;

public class HanziRepository {
	private static final String LOG_TAG = HanziRepository.class.getSimpleName();

    public long createNewWord(SQLiteDatabase db, Hanzi h, int wordListId) throws IOException {
		long wordId = db.insertOrThrow(T_WORDS, null, h.toContentValues(wordListId));
		if (wordId == -1) {
			throw new IOException("Failed to insert data into word list table");
		}
		return wordId;
	}

	public Integer[] getEmptyWordListIds(SQLiteDatabase db) {
		List<Integer> list = new ArrayList<Integer>();
		Cursor cursor = null;
		try {
			for (int i = 0; i < ALL_WORD_LIST_IDS.length; i++) {
				cursor = db.query(T_WORDS, new String[] {"_id"}, "wordListId = ?", new String[] {String.valueOf(ALL_WORD_LIST_IDS[i])}, null, null, null);
				if (cursor.getCount() <= 0) {
					list.add(i);
				}
			}
		} finally {
			closeCursor(cursor);
		}
		Integer[] wordListsToBeUpdated = new Integer[list.size()];
		if (!list.isEmpty()) {
			list.toArray(wordListsToBeUpdated);
		}
		return wordListsToBeUpdated;
	}

	public Boolean isWordListEmpty(SQLiteDatabase db, Integer wordListId) {
		List<Integer> list = new ArrayList<Integer>();
		Cursor cursor = null;
		try {
		cursor = db.query(T_WORDS, new String[] {"_id"}, "wordListId = ?", new String[] {String.valueOf(wordListId)}, null, null, null);
				if (cursor.getCount() <= 0) {
					return true;
				}
		} finally {
			closeCursor(cursor);
		}
		return false;
	}



	public Hanzi getWordById(SQLiteDatabase db, int wordId) {
		Hanzi hanzi = null;
		Cursor cursor = null;
		try {
			String[] columns = new String[] {"_id", "word", "pinyin", "definition"};
			cursor = db.query(T_WORDS, columns, "_id = ?", new String[] {"" + wordId}, null, null, null);
			if (cursor.moveToFirst()) {
				hanzi = new Hanzi();
				hanzi.setWord(cursor.getString(cursor.getColumnIndex(columns[1])));
				hanzi.setPinyin(cursor.getString(cursor.getColumnIndex(columns[2])));
				hanzi.setDefinition(cursor.getString(cursor.getColumnIndex(columns[3])));
			}
		} finally {
			closeCursor(cursor);
		}
		return hanzi;
	}

	public String getSoundfileNameByWord(SQLiteDatabase db, String word, int wordListId) {
		String fileName = null;
		if (word == null || word.equals("")) {
			return fileName;
		}
		
		Cursor cursor = null;
		try {
			cursor = db.query(T_WORDS, new String[] {"_id","soundfile"}, "wordlistid = ? AND word = ?", new String[] {String.valueOf(wordListId), word}, null, null, null);
			if (cursor != null && cursor.moveToFirst()) {
					fileName = cursor.getString(cursor.getColumnIndex("soundfile"));
			} else {
				Log.e(LOG_TAG, "Cursor is null or empty for query word: " + word);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			closeCursor(cursor);
		}
		
		return fileName;
	}

	public void clearWordList(SQLiteDatabase db, int wordListId) throws IOException {
		int deletedRows = db.delete(T_WORDS, "wordlistid = ?", new String[] {String.valueOf(wordListId)});
		Log.d(LOG_TAG, "Deleted " + deletedRows + " in word list table");
	}

	public boolean getIsLearnedStatusForWord(SQLiteDatabase db, int wordId) {
		Cursor cursor = db.rawQuery("SELECT islearned FROM " + T_WORDS + " WHERE _id = ?", new String[] {String.valueOf(wordId)});
		cursor.moveToFirst();
		int result = cursor.getInt(0);
		closeCursor(cursor);
		return result == 1;
	}
	
	public void setIsLearnedStatusForWord(SQLiteDatabase db, boolean isLearned, int wordId) throws IOException {
		ContentValues values = new ContentValues(1);
		values.put("islearned", isLearned);
		int updatedCount = db.update(T_WORDS, values , "_id = ?", new String[]{String.valueOf(wordId)});
		if (updatedCount == 0) {
			throw new IOException("Failed to update word " + wordId);
		}
	}

	public int getWordIdByHanzi(SQLiteDatabase db, String text, int wordListId) {
		Cursor cursor = db.query(T_WORDS, new String[]{"_id"}, "word = ? and wordlistid = ?", new String[]{text, String.valueOf(wordListId)}, null, null, null);
		cursor.moveToFirst();
		int id = cursor.getInt(0);
		closeCursor(cursor);
		return id;
	}

	public void deleteWord(SQLiteDatabase db, long wordId) throws IOException {
		int deletedCount = db.delete(T_WORDS, "_id = ?", new String[] {String.valueOf(wordId)});
		if (deletedCount == 0) {
			throw new IOException("Failed to delete word with id: " + wordId);
		}
		Log.d(LOG_TAG, "Deleted word with id: " + wordId);
	}
	
	public void editWord(SQLiteDatabase db, int wordId, int wordListId, Hanzi h) throws IOException {
		int updatedCount = db.update(T_WORDS, h.toContentValues(wordListId), "_id = ?", new String[] {String.valueOf(wordId)});
		if (updatedCount == 0) {
			throw new IOException("Failed to update word with id: " + wordId);
		}
		Log.d(LOG_TAG, "Updated word with id: " + wordId);
	}
}
