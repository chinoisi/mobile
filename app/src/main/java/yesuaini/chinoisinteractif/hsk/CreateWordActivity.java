package yesuaini.chinoisinteractif.hsk;

import yesuaini.chinoisinteractif.AbstractNavigationActivity;
import yesuaini.chinoisinteractif.hsk.models.Hanzi;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import yesuaini.chinoisinteractif.R;

public class CreateWordActivity extends AbstractNavigationActivity {
	protected static final String EDITED_WORD_ID = "yesuaini.chinoisinteractif.createword.id";
	protected static final String NEW_WORD_HANZI = "yesuaini.chinoisinteractif.createword.hanzi";
	protected static final String NEW_WORD_PINYIN = "yesuaini.chinoisinteractif.createword.pinyin";
	protected static final String NEW_WORD_DEFINITION = "yesuaini.chinoisinteractif.createword.definition";
	protected static final String ID_OF_WORD_TO_EDIT_EXTRA = "yesuaini.chinoisinteractif.createword.editWordId";
	private EditText hanziInput;
	private EditText pinyinInput;
	private EditText defInput;
	private int wordId;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.new_word));
		
		DatabaseHelper dbh = new DatabaseHelper(this);
		
		hanziInput = (EditText) findViewById(R.id.hanziInput);
		pinyinInput = (EditText) findViewById(R.id.pinyinInput);
		defInput = (EditText) findViewById(R.id.definitionInput);
		//The xml flags for InputType are allegedly very manufacturer-dependent. This might be the safest method...
		hanziInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		pinyinInput.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
		defInput.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);
		
		Intent intent = getIntent();
		wordId = intent.getIntExtra(ID_OF_WORD_TO_EDIT_EXTRA, -1);
		if (wordId != -1) {
			Hanzi word = dbh.getWordById(wordId);
			hanziInput.setText(word.getWord());
			pinyinInput.setText(word.getPinyin());
			defInput.setText(word.getDefinition());
		}
		
		Button saveButton = (Button) findViewById(R.id.create_word_save_button);
		saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String hanzi = hanziInput.getText().toString();
				String pinyin = pinyinInput.getText().toString();
				String def = defInput.getText().toString();
				if (allInputsValid(hanzi, pinyin, def)) {
					Intent intent = new Intent();
					intent.putExtra(EDITED_WORD_ID, wordId);
					intent.putExtra(NEW_WORD_HANZI, hanzi);
					intent.putExtra(NEW_WORD_PINYIN, pinyin);
					intent.putExtra(NEW_WORD_DEFINITION, def);
					setResult(RESULT_OK, intent);
					finish();
				} else {
					Toast.makeText(CreateWordActivity.this, R.string.invalid_fields, 
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		Button cancelButton = (Button) findViewById(R.id.create_word_cancel_button);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
	}

	@Override
	protected int getToolbarViewId() {
		return R.id.toolbar;
	}

	@Override
	protected int getContentViewActivity() {
		return R.layout.hsk_create_word;
	}

	private boolean allInputsValid(String... inputs) {
		for (String str : inputs) {
			if (str == null || str.length() == 0) {
				return false;
			}
		}
		return true;
	}
}
