package yesuaini.chinoisinteractif.episodes;

import java.util.Arrays;
import java.util.Collection;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import yesuaini.chinoisinteractif.R;


public class WordListActivity extends Activity {

	// ================= STATIC ATTRIBUTES ======================

	public static final int MENU_SHOW_HIDE_FILTER = 0;
	public static final int MENU_NEW_WORD = 1;
	public static final int MENU_ORDER = 2;
	public static final int MENU_SELECTION_CHANGE = 3;
	public static final int MENU_MOVE_SELECTED = 4;

	public static final int CTX_DELETE_WORD = 0;
	public static final int CTX_EDIT_WORD = 1;
	public static final int CTX_MOVE_WORD = 2;

	private static final int REQ_PARENT_DICT = 0;

	// ================= INSTANCE ATTRIBUTES ====================

	private EditText filterEdit = null;
	private EditText newWordEdit = null;

	private TextView emptyText = null;

	private Button addWordBtn = null;

	private MenuItem menuFilter = null;

	private long selectedDictId;

	private String orderBy = null;
	private AlertDialog alertDialog = null;

	/**
	 * This is used for storing the moved word Ids when user selects the parent
	 * dictionary
	 */
	private Collection<Long> movedWordIds = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onBackPressed() {
		this.emptyText.setText("");
		super.onBackPressed();
		overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
	}

	@Override
	protected void onResume() {
		super.onResume();

		this.refreshListAdapter();
	}

	@Override
	protected void onPause() {
		super.onPause();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;

		this.menuFilter.setIcon(R.drawable.icon_filter);


		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_SHOW_HIDE_FILTER:
				this.showHideFilter();
				break;
			case MENU_NEW_WORD:
				this.createWord(null);
				break;
			case MENU_ORDER:
				this.showSelectOrderDialog();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem moveSel = menu.findItem(MENU_MOVE_SELECTED);
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		int none = Menu.NONE;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		switch (item.getItemId()) {
			case CTX_DELETE_WORD:
				this.deleteCard(info.id);
				break;
			case CTX_EDIT_WORD:
				this.editCard(info.id);
				break;
			case CTX_MOVE_WORD:
				this.moveCards(Arrays.asList(info.id));
				break;
		}

		return super.onContextItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == REQ_PARENT_DICT) {
			if (resultCode == RESULT_OK) {
				long dictId = data.getExtras().getLong(DictListActivity.KEY_RESULT_DICT_ID);
				this.moveCards(this.movedWordIds, dictId);
			}
		}
	}



	private void refreshListAdapter() {
	}

	private void deleteCard(long id) {

	}

	private void editCard(long id) {

	}

	private void moveCards(Collection<Long> wordIds) {
		this.movedWordIds = wordIds;


		Intent i = new Intent(this, DictListActivity.class);
		i.putExtra(DictListActivity.EXTRAS_ONLY_DICT_SELECTION, true);
		startActivityForResult(i, REQ_PARENT_DICT);
	}

	private void moveCards(Collection<Long> wordIds, long parentDictId) {
		this.refreshListAdapter();
	}

	/**
	 * Hides or Show a filter
	 */
	private void showHideFilter() {
		Resources res = this.getResources();

		if (this.filterEdit.getVisibility() == EditText.GONE) {
			this.filterEdit.setVisibility(EditText.VISIBLE);
			this.filterEdit.requestFocus();
		} else {
			this.filterEdit.setVisibility(EditText.GONE);
			this.filterEdit.setText("");
		}
	}

	private void showSelectOrderDialog() {

		alertDialog.show();
	}

	private void createWord(String natWord) {

	}


}
