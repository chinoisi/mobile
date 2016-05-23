package yesuaini.chinoisinteractif.episodes;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import yesuaini.chinoisinteractif.R;

/**
 * Activity class
 * 
 * @author Lucky
 * 
 */
public class DictListActivity extends Activity {
	// ================= STATIC ATTRIBUTES ======================

	public static final int MENU_SHOW_HIDE_FILTER = 0;
	public static final int MENU_NEW_DICT = 1;
	public static final int MENU_EXPORT = 2;

	public static final int CTX_MENU_DELETE = 50;
	public static final int CTX_MENU_EDIT = 51;
	public static final int CTX_MENU_SHOW_CHILDREN = 52;
	public static final int CTX_MENU_MOVE_UNDER = 53;
	public static final int CTX_MENU_MOVE_ROOT = 54;
	

	public static final String EXTRAS_MESSAGE = "message";
	public static final String EXTRAS_ONLY_DICT_SELECTION = "onlyDictSelection";

	public static final String KEY_RESULT_DICT_ID = "resDictId";
	
	private static final int REQUEST_PARENT_DICT = 100;

	// ================= INSTANCE ATTRIBUTES ====================

	private EditText filterEdit = null;
	private MenuItem menuFilter = null;
	private View goUpView = null;
	private TextView parentFolderNameText = null;
	private TextView emptyText = null;

	private AlertDialog alertDialog = null;

	private Long parentDictId = null;

	/**
	 * If this is true, this activity has no menu and only returns the id of the
	 * dictionary
	 */
	private boolean onlyDictSelection = false;
	
	/**
	 * This is used for storing the moved dictionary id until user selects parent dictionary.
	 */
	private Long movedDictionaryId = null;

	// ================= CONSTRUCTORS ===========================

	// ================= OVERRIDEN METHODS ======================

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		}

	@Override
	public void onBackPressed() {
		if (this.parentDictId != null) {

		} else {
			this.emptyText.setText("");
			super.onBackPressed();
			overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();


	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		int none = Menu.NONE;


		return super.onCreateOptionsMenu(menu);
	}


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

	 if (requestCode == REQUEST_PARENT_DICT && resultCode == RESULT_OK) {
			long id = data.getExtras().getLong(KEY_RESULT_DICT_ID);
			this.movedDictionaryId = null;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		int none = Menu.NONE;

		super.onCreateContextMenu(menu, v, menuInfo);
	}




	private void handleBundle(Bundle b) {
		if (b.containsKey(EXTRAS_MESSAGE)) {
			this.setTitle(b.getString(EXTRAS_MESSAGE));
		}

		if (b.containsKey(EXTRAS_ONLY_DICT_SELECTION)) {
			this.onlyDictSelection = b.getBoolean(EXTRAS_ONLY_DICT_SELECTION);
		}
	}


}
