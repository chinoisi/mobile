package yesuaini.chinoisinteractif.hsk.adapters;

import android.database.Cursor;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class WordListViewBinder implements SimpleCursorAdapter.ViewBinder {

	@Override
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		if (columnIndex == 1) {
			TextView name = (TextView) view;
			name.setText(cursor.getString(cursor.getColumnIndex("name")));
			return true;
		} else if (columnIndex == 2) {
			TextView id = (TextView) view;
			id.setText(cursor.getInt(cursor.getColumnIndex("fk")));
			return true;
		}
		return false;
	}
	
}
