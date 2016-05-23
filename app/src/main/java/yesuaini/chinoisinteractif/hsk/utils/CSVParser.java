package yesuaini.chinoisinteractif.hsk.utils;

import static yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata.T_WORDS;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;
import yesuaini.chinoisinteractif.hsk.models.Hanzi;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;

public class CSVParser { //TODO refactor all or replace with csv4j
	private static final String LOG_TAG = CSVParser.class.getName();
	private static final int ISNOTLEARNED = 0;
	
	public static final char SEMICOLON_DELIMITER = ';';
	public static final char COMMA_DELIMITER = ',';

	private final DatabaseHelper dbh;

	public CSVParser(DatabaseHelper dbh) {
		this.dbh = dbh;
	}

	public void parseWordListCSV(BufferedReader br, int wordListId, char delimiter) {
		try {
			SQLiteDatabase db = null;
			int lineNumber = 0;
			try {
				dbh.clearWordList(wordListId);
				db = dbh.getWritableDatabase();
				SQLiteStatement compiledStatement = db.compileStatement("INSERT INTO " + T_WORDS + "(wordlistid, word, pinyin, definition, searchkey, islearned) VALUES(?,?,?,?,?,?);");
				db.beginTransaction();
				while (true) {
					String line = br.readLine();
					if (line == null || line.isEmpty()) {
						break;
					}
					
					StringTokenizer st = new StringTokenizer(line, String.valueOf(delimiter));
					int tokenNumber = 0;
					String word = null;
					String pinyin = null;
					String definition = null;
					String soundfile = null;
					String searchKey = null;
					while (st.hasMoreTokens()) {
						String token = st.nextToken();
						Log.d(LOG_TAG, "token :  " + token);
						switch (tokenNumber) {
							case 0: //word
								word = token;
								break;
							case 1: //pinyin and searchkey (generated)
								pinyin = PinyinReplacer.convertNumberedToneMarksToVisual(token);
								searchKey = PinyinReplacer.removeAllToneMarks(token.toLowerCase(Locale.US));
								break;
							case 2: //definition
								definition = token.replace(SEMICOLON_DELIMITER, COMMA_DELIMITER);
								break;
							case 3: //soundfile
								soundfile = token;
								break;
							default:
								Log.d(LOG_TAG, "Token not in recognized group: " + token);
								break;
						}
						tokenNumber++;
					}

					Hanzi h = new Hanzi(word, pinyin, definition, searchKey, false, soundfile);
					lineNumber++;
					compiledStatement.bindLong(1, wordListId);
					compiledStatement.bindString(2, h.getWord());
					compiledStatement.bindString(3, h.getPinyin());
					compiledStatement.bindString(4, h.getDefinition());
					compiledStatement.bindString(5, h.getSearchKey());
					compiledStatement.bindLong(6, ISNOTLEARNED);
					compiledStatement.executeInsert();
					compiledStatement.clearBindings();
					Log.d(LOG_TAG, lineNumber + ": " + h.toString());
				}
				db.setTransactionSuccessful();
				Log.d(LOG_TAG, "Successfully imported word list");
			} finally {
				if (db != null) {
					db.endTransaction();
					DatabaseHelper.closeDatabase(db);
				}
			}
			Log.d(LOG_TAG, "Characters read from file: " + lineNumber);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
