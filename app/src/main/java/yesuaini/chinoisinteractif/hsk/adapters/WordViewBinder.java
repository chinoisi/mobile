package yesuaini.chinoisinteractif.hsk.adapters;

import android.database.Cursor;
import android.graphics.Color;
import android.view.View;
import android.widget.CheckBox;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class WordViewBinder implements SimpleCursorAdapter.ViewBinder {
	
	public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
		final Cursor constCursor = cursor;
		//columnIndex == 0 is the mandatory _id for the SQLite table.
		if (columnIndex == 1) { //word
			TextView word = (TextView) view;
			word.setText(constCursor.getString(constCursor.getColumnIndex("word")));
			return true;
		} else if (columnIndex == 2) { //pinyin
			TextView pinyin = (TextView) view;
			pinyin.setText(constCursor.getString(constCursor.getColumnIndex("pinyin")));
			return true;
		} else if (columnIndex == 3) { //def
			TextView def = (TextView) view;
			def.setText(constCursor.getString(constCursor.getColumnIndex("definition")));
			return true;
		} else if (columnIndex == 4) { //islearned
			final CheckBox isLearnedView = (CheckBox) view;
			isLearnedView.setChecked(constCursor.getInt(constCursor.getColumnIndex("islearned")) == 1);
			isLearnedView.setClickable(false);
			return true;
		}
		return false;
	}

}
