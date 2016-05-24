package yesuaini.chinoisinteractif.hsk.repository;

import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_CACHE;
import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_WORDS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import yesuaini.chinoisinteractif.hsk.models.QuizHanzi;

public class QuizRepository {
	private static final String[] QUIZ_WORD_COLUMNS = new String[] {"_id", "word", "pinyin", "definition", "soundfile"};
	
	public List<QuizHanzi> makeQuizList(SQLiteDatabase db, boolean quizIsCached, int wordListId) {
		Cursor cursor = null;
		try {
			String[] selectionArgs = new String[] {String.valueOf(wordListId)};
			if (quizIsCached) {
				cursor = db.query(T_WORDS, QUIZ_WORD_COLUMNS, "wordlistid = ? AND _id IN (SELECT word_id FROM " + T_CACHE + ")", selectionArgs, null, null, null);
			} else {
				cursor = db.query(T_WORDS, QUIZ_WORD_COLUMNS, "wordlistid = ? AND islearned = 0", selectionArgs, null, null, "RANDOM()", "4");
			}
			
			if (cursor.getCount() < 4) { //if word list has >4 non-learned words, widen search criteria
				DatabaseHelper.closeCursor(cursor);
				cursor = db.query(T_WORDS, QUIZ_WORD_COLUMNS, "wordlistid = ?", selectionArgs, null, null, "RANDOM()", "4");
			}
			
			if (cursor.getCount() == 4) {
				List<QuizHanzi> list = new ArrayList<QuizHanzi>(3);
				while (cursor.moveToNext()) {
					list.add(convertDbRowToQuizHanzi(cursor));
				}
				return list;
			} else {
	    		throw new Exception("Error, wrong amount of words in cursor: " + cursor.getCount());
	    	}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			DatabaseHelper.closeCursor(cursor);
		}
		
		return null;
	}
	
	protected static QuizHanzi convertDbRowToQuizHanzi(Cursor cursor) {
		assert cursor.getColumnCount() == 5;
		int wordId = cursor.getInt(cursor.getColumnIndex("_id"));
		String wordStr = cursor.getString(cursor.getColumnIndex("word"));
		String pinyinStr = cursor.getString(cursor.getColumnIndex("pinyin"));
		String defStr = cursor.getString(cursor.getColumnIndex("definition"));
		String soundStr = cursor.getString(cursor.getColumnIndex("soundfile"));
		return new QuizHanzi(wordId, wordStr, pinyinStr, defStr, soundStr);
	}

	public List<QuizHanzi> makeExamList(SQLiteDatabase db, boolean isCached, int currentWordId, int wordListId) {
		Cursor cursor = null;
		try {
			String[] selectionArgs = {String.valueOf(currentWordId), String.valueOf(wordListId)};
			cursor = db.query(T_WORDS, QUIZ_WORD_COLUMNS, "_id != ? AND wordlistid = ?", selectionArgs , null, null, "RANDOM()", "3");
			if (cursor.getCount() == 3) {
				List<QuizHanzi> list = new ArrayList<QuizHanzi>(3);
				while (cursor.moveToNext()) {
					list.add(convertDbRowToQuizHanzi(cursor));
				}
				return list;
			} else {
				throw new IOException("Error, wrong amount of words in result: " + cursor.getCount());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			DatabaseHelper.closeCursor(cursor);
		}
		return null;
	}
}
