package yesuaini.chinoisinteractif.hsk.repository;

import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQuery;

public class HelpfulCursor extends SQLiteCursor implements Cursor {

	public HelpfulCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
			String editTable, SQLiteQuery query) {
		super(db, driver, editTable, query);
	}

	public byte[] getBlob(String columnName) {
		return getBlob(getColumnIndex(columnName));
	}

	public String getString(String columnName) {
		return getString(getColumnIndex(columnName));
	}

	public short getShort(String columnName) {
		return getShort(getColumnIndex(columnName));
	}

	public int getInt(String columnName) {
		return getInt(getColumnIndex(columnName));
	}

	public long getLong(String columnName) {
		return getLong(getColumnIndex(columnName));
	}

	public float getFloat(String columnName) {
		return getFloat(getColumnIndex(columnName));
	}

	public double getDouble(String columnName) {
		return getDouble(getColumnIndex(columnName));
	}

	public boolean isNull(String columnName) {
		return isNull(getColumnIndex(columnName));
	}
	
	public boolean isFloat(String columnName) {
		return super.isFloat(getColumnIndex(columnName));
	}

	public boolean isBlob(String columnName) {
		return super.isBlob(getColumnIndex(columnName));
	}

	public boolean isString(String columnName) {
		return super.isString(getColumnIndex(columnName));
	}

	public boolean isLong(String columnName) {
		return super.isLong(getColumnIndex(columnName));
	}
}
