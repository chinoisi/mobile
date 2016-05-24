package yesuaini.chinoisinteractif.hsk.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;
import android.util.Log;

public class CSVExporter {
	private static final String LOG_TAG = CSVExporter.class.getSimpleName();
	private final DatabaseHelper dbh;

	public CSVExporter(DatabaseHelper dbh) {
		this.dbh = dbh;
	}
	
	public void exportWordListToCSVOnSDCard(int wordListId) throws Exception {
		SQLiteDatabase db = null;
		BufferedWriter writer = null;
		try {
			db = dbh.getReadableDatabase();
			
			//get word list name for use in filename
			String fileName = "testlist";
			Cursor cursor = db.rawQuery("SELECT name FROM t_wordlists WHERE _id = ?", 
					new String[] {String.valueOf(wordListId)});
			cursor.moveToFirst();
			fileName = cursor.getString(0);
			cursor.close();
			
			//get all words in a cursor
			cursor = db.rawQuery("SELECT word,pinyin,definition FROM t_words WHERE wordlistid = ?", 
					new String[] {String.valueOf(wordListId)});
			
			//create file
			File sdCard = Environment.getExternalStorageDirectory();
			String backupDBPath = sdCard.getAbsolutePath() + "//chinoisinteractif//exports//";
			File file = new File(backupDBPath);
			file.mkdirs();
			File file2 = new File(file, fileName + ".csv");
			file2.createNewFile();
			
			//write words to file
			writer = new BufferedWriter(new FileWriter(file2));
			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				String word = cursor.getString(0);
				String pinyin = cursor.getString(1);
				String def = cursor.getString(2);
				writer.write(word + ";" + pinyin + ";" + def);
				writer.newLine();
				
				cursor.moveToNext();
			}
			cursor.close();
			
			Log.d(LOG_TAG, "Export successful");
		} finally {
			if (writer != null) writer.close();
			DatabaseHelper.closeDatabase(db);
		}
	}
}
