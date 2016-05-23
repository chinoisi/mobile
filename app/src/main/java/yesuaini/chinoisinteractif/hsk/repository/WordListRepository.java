package yesuaini.chinoisinteractif.hsk.repository;

import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_WORDLISTS;
import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_WORDS;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class WordListRepository {
	private static final String LOG_TAG = WordListRepository.class.getSimpleName();

	public int getHighestWordListId(SQLiteDatabase db) {
		Cursor cursor = db.rawQuery("SELECT max(_id) FROM " + T_WORDLISTS, null);
		cursor.moveToFirst();
		int id = cursor.getInt(0);
		DatabaseHelper.closeCursor(cursor);
		return id;
	}

	public void setWordListName(SQLiteDatabase db, String name, int wordListId) {
		ContentValues values = new ContentValues(1);
		values.put("name", name);
		int affectedRows = db.update(T_WORDLISTS, values, "_id = ?", 
				new String[]{String.valueOf(wordListId)});
		if (affectedRows == 0) {
			db.insert(T_WORDLISTS, null, values);
		}
	}

	public String getWordListName(SQLiteDatabase db, int wordListId) {
		Cursor cursor = db.rawQuery("SELECT name FROM " + T_WORDLISTS + " WHERE _id = ?", 
				new String[]{String.valueOf(wordListId)});
		String name;
		if (cursor.moveToFirst()) {
			name = cursor.getString(0);
		} else {
			name = null;
		}
		DatabaseHelper.closeCursor(cursor);
		return name;
	}

	public int findWordListByName(SQLiteDatabase db, String name) {
		Cursor cursor = db.rawQuery("SELECT _id FROM " + T_WORDLISTS + " WHERE name = ?", new String[] {name});
		int id;
		if (cursor.moveToFirst()) {
			id = cursor.getInt(0);
		} else {
			id = -1;
		}
		DatabaseHelper.closeCursor(cursor);
		return id;
	}

	public void createWordList(SQLiteDatabase db, int wordListId, String name) {
		ContentValues values = new ContentValues(3);
		values.put("_id", wordListId);
		values.put("name", name);
		values.put("user_defined", 1);
		long result = db.insertOrThrow(T_WORDLISTS, null, values);
		if (result == -1) {
			Log.e(LOG_TAG, "An error occurred when inserting word list with id: " + wordListId);
		}
	}

	public boolean isUserDefined(SQLiteDatabase db, int wordListId) {
		Cursor cursor = db.rawQuery("SELECT user_defined FROM " + T_WORDLISTS + " WHERE _id = ?", 
				new String[] {String.valueOf(wordListId)});
		cursor.moveToFirst();
		boolean result = cursor.getInt(0) == 1;
		DatabaseHelper.closeCursor(cursor);
		return result;
	}

	public int getNumberOfWords(SQLiteDatabase db, long wordListId) {
		int result = 0;
		Cursor c = db.rawQuery("SELECT count(_id) FROM " + T_WORDS + " WHERE wordlistid = ?", 
				new String[]{String.valueOf(wordListId)});
		if (c.moveToFirst()) {
			result = c.getInt(0);
		}
		DatabaseHelper.closeCursor(c);
		return result;
	}

}
