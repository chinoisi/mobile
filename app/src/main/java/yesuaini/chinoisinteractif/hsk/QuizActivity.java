package yesuaini.chinoisinteractif.hsk;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import yesuaini.chinoisinteractif.AbstractNavigationActivity;
import yesuaini.chinoisinteractif.hsk.models.QuizHanzi;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;
import yesuaini.chinoisinteractif.hsk.service.QuizService;
import yesuaini.chinoisinteractif.hsk.utils.AsyncSoundPlayer;
import yesuaini.chinoisinteractif.hsk.utils.SoundManager;
import yesuaini.chinoisinteractif.R;

public class QuizActivity extends AbstractNavigationActivity {
	private static final String LOG_TAG = QuizActivity.class.getSimpleName();
	private static final float SMALL_TEXTSIZE = 14f;
	private static final float DEFAULT_TEXTSIZE = 16f;
	private static final String ID_OF_ANSWER = "idOfAnswer";
	private static final String PINYIN_SHOWN = "pinyinShown";
	private static final String CORRECT_ANSWER_SHOWN = "correctAnswerShown";
	private static final int idArray[] = new int[] {R.id.defView0, R.id.defView1, R.id.defView2, R.id.defView3};

	protected static final String PREFS_NAME = "yesuaini.chinoisinteractif.quiz.prefs";
	protected static final String IS_CACHED = "isCached";
	protected static final String SELECTED_QUIZ_TABLE_EXTRA = "yesuaini.chinoisinteractif.quiz.chosenQuizTableName";

	private TextView quizWordView;
	private TextView quizPinyinView;
	private Button[] buttonArray;
	private boolean correctAnswerShown = true;
	private boolean pinyinShown = true;
	private int idOfAnswer; //the Hanzi id of the answer
	private int answerButtonIndex = -1; //the correct answer button array index
	private int chosenQuizTable;
	
	private SoundManager soundManager;
	private AudioManager audioManager;
	private DatabaseHelper dbh;
	private QuizService quizService;
	private CheckBox isLearnedView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbh = new DatabaseHelper(this); 
		
		//get data from global SharedPreferences
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		pinyinShown = prefs.getBoolean(PINYIN_SHOWN, true);
		correctAnswerShown = prefs.getBoolean(CORRECT_ANSWER_SHOWN, true);
		
		//get data from the Intent that started this activity
		Intent intent = getIntent();
		int chosenQuizTableStringExtra = intent.getIntExtra(CharacterListActivity.SELECTED_WORD_LIST_EXTRA, 1);

		chosenQuizTable = chosenQuizTableStringExtra;
		setTitle(getString(R.string.quiz_str) + ": " + dbh.getWordListName(chosenQuizTable));
		
		//generate new or recover cached quiz and answer
		List<QuizHanzi> quizWordList = new ArrayList<QuizHanzi>(4);	
		int indexOfAnswer = generateQuiz(quizWordList, checkCache());
		
		//display quiz
		displayQuiz(quizWordList, indexOfAnswer);
		
		audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		soundManager = new SoundManager(getAssets(), getQuizService().getFilePathByWordList(chosenQuizTable));
	}

	@Override
	protected int getToolbarViewId() {
		return R.id.toolbar;
	}

	@Override
	protected int getContentViewActivity() {
		return R.layout.hsk_quiz;
	}

	private QuizService getQuizService() {
		if (quizService == null) {
			quizService = new QuizService();
		}
		return quizService;
	}

	private int generateQuiz(List<QuizHanzi> quizWordList, boolean isCached) {
		List<QuizHanzi> list;
		if (isCached == true) { //cached quiz
			list = dbh.makeQuizList(isCached, chosenQuizTable);
			if (list == null) { //if list is null, invalidate cache
				dbh.invalidateCache();
				isCached = false;
			}
		} else { //new quiz			
			list = dbh.makeQuizList(isCached, chosenQuizTable);
			idOfAnswer = getQuizService().chooseCorrectAnswer(list);
			updateCache(list);
		}
		
		int indexOfAnswer = -1;
		quizWordList.addAll(list);
		for (int i = 0; i < quizWordList.size(); i++) {
			if (quizWordList.get(i).getId() == idOfAnswer) {
				indexOfAnswer = i;
				break;
			}
		}
		
		return indexOfAnswer;
	}
	
	private boolean checkCache() {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		boolean isCached = prefs.getBoolean(IS_CACHED, false);
		idOfAnswer = prefs.getInt(ID_OF_ANSWER, -1);
		Log.d(LOG_TAG, "isCached: " + isCached + 
				" idOfAnswer: " + idOfAnswer);
		return isCached;
	}

	private void updateCache(List<QuizHanzi> list) {
		int[] hanziIds = { 
			list.get(0).getId(),
			list.get(1).getId(), 
			list.get(2).getId(),
			list.get(3).getId(), 
		};
		dbh.saveQuizForLater(hanziIds);
		
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(ID_OF_ANSWER, idOfAnswer);
		editor.putBoolean(IS_CACHED, true);
		editor.commit();
	}
	
	/**
	 * Initialization and display of the quiz in the UI. This method is 
	 * only called at the start of the activity. 
	 * @param quizWordList the generated list of words included in the quiz
	 * @param indexOfAnswer the index of the correct word in the quizWordList
	 */
	private void displayQuiz(List<QuizHanzi> quizWordList, int indexOfAnswer) {
		PlaySoundClickListener onHanziClickListener = new PlaySoundClickListener();
		isLearnedView = (CheckBox) findViewById(R.id.isLearnedCheckBox);
		isLearnedView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(LOG_TAG, "Marking word " + idOfAnswer + " as learned");
				dbh.setWordLearnedStatus(idOfAnswer, true);
				new AsyncColorSwitcher(null, null).execute(500L);
			}
		});
		quizWordView = (TextView) findViewById(R.id.quizWordView);
		quizWordView.setText(quizWordList.get(indexOfAnswer).getWord());
		quizWordView.setOnClickListener(onHanziClickListener);
		quizPinyinView = (TextView) findViewById(R.id.pinyinLabel);
		quizPinyinView.setText(quizWordList.get(indexOfAnswer).getPinyin());
		quizPinyinView.setOnClickListener(onHanziClickListener);
		if (pinyinShown) {
			quizPinyinView.setVisibility(View.VISIBLE);
		}
		buttonArray = new Button[idArray.length];
		for (int i = 0; i < idArray.length; i++) {
			Button defButton = (Button) findViewById(idArray[i]);
			buttonArray[i] = defButton;
			String definition = quizWordList.get(i).getDefinition();
			defButton.setText(definition);
			if (definition.length() > 105) {
				buttonArray[i].setTextSize(SMALL_TEXTSIZE);
			} else {
				buttonArray[i].setTextSize(DEFAULT_TEXTSIZE);
			}
			defButton.setBackgroundResource(R.drawable.btn_recolored);
			int id = quizWordList.get(i).getId();
			defButton.setOnClickListener(new OnQuizAnswerListener(id));
			if (id == idOfAnswer) {
				answerButtonIndex = i;
			}
		}
	}

	/**
	 * Resets the UI and displays a new quiz.
	 */
	private void resetQuiz() {
		List<QuizHanzi> quizWordList = new ArrayList<QuizHanzi>(4);
		int indexOfAnswer = generateQuiz(quizWordList, false);
		
		quizWordView.setText(quizWordList.get(indexOfAnswer).getWord());
		quizPinyinView.setText(quizWordList.get(indexOfAnswer).getPinyin());
		isLearnedView.setChecked(false);
		for (int i = 0; i < idArray.length; i++) {
			String definition = quizWordList.get(i).getDefinition();
			buttonArray[i].setText(definition);
			buttonArray[i].setEnabled(true);
			if (definition.length() > 105) {
				buttonArray[i].setTextSize(SMALL_TEXTSIZE);
			} else {
				buttonArray[i].setTextSize(DEFAULT_TEXTSIZE);
			}
			buttonArray[i].setBackgroundResource(R.drawable.btn_recolored);
			int id = quizWordList.get(i).getId();
			buttonArray[i].setOnClickListener(new OnQuizAnswerListener(id));
			if (id == idOfAnswer) {
				answerButtonIndex = i;
			}
		}
	}
	
	private class OnQuizAnswerListener implements OnClickListener {
		private final int id;
		
		public OnQuizAnswerListener(int id) {
			this.id = id;
		}
		
		public void onClick(View view) {
			if (id == idOfAnswer) { //correct answer
				new AsyncColorSwitcher((Button) view, null).execute((Long[])null);
			} else { //wrong answer
				Button correctButton = buttonArray[answerButtonIndex];
				Button incorrectButton = (Button) view;
				new AsyncColorSwitcher(correctButton, incorrectButton).execute((Long[])null);
			}
		}
	}
	
	private class AsyncColorSwitcher extends AsyncTask<Long,Void,Void> {
		private final Button correctButton;
		private final Button incorrectButton;
		
		public AsyncColorSwitcher(Button correctButton, Button incorrectButton) {
			this.correctButton = correctButton;
			this.incorrectButton = incorrectButton;
		}
		
		@Override
		protected void onPreExecute() {
			if (incorrectButton != null) { //wrong answer chosen
				incorrectButton.setBackgroundColor(Color.RED);
				if (correctAnswerShown) { //show correct answer only if set in preferences
					correctButton.setBackgroundColor(Color.GREEN);
				}
			} else if (correctButton != null) { //correct answer chosen
				correctButton.setBackgroundColor(Color.GREEN);
			}
			//show pinyin only when answered
			quizPinyinView.setVisibility(View.VISIBLE);
			
			//disable buttons to prevent double taps
			for (Button button : buttonArray) {
				button.setEnabled(false);
			}
		}
		
		@Override
		protected Void doInBackground(Long... args) {
			try {
				long sleepTime = 1500;
				if (args != null && args.length > 0) {
					sleepTime = args[0];
				}
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			quizPinyinView.setVisibility(
					pinyinShown ? View.VISIBLE : View.GONE);
			resetQuiz();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.quizmenu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		Editor editor = prefs.edit();
	    switch (item.getItemId()) {
	        case R.id.show_correct_answer: {
	        	boolean isCorrectAnswerShown = prefs.getBoolean(CORRECT_ANSWER_SHOWN, true);
	            correctAnswerShown = !isCorrectAnswerShown;
	            editor.putBoolean(CORRECT_ANSWER_SHOWN, correctAnswerShown).commit();
	            return true;
	        }
	        case R.id.enable_pinyin: {
	        	boolean isPinyinShown = prefs.getBoolean(PINYIN_SHOWN, true);
	            pinyinShown = !isPinyinShown;
	            quizPinyinView.setVisibility(pinyinShown ? View.VISIBLE : View.GONE);
	            editor.putBoolean(PINYIN_SHOWN, pinyinShown).commit();
	            return true;
	        }
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private class PlaySoundClickListener implements OnClickListener {
		
		@Override
		public void onClick(View v) {
			if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
				TextView tv = (TextView) findViewById(R.id.quizWordView);
				String word = tv.getText().toString();
				
				String fileName = dbh.getSoundfileNameByWord(word, chosenQuizTable);
				new AsyncSoundPlayer().execute(fileName, soundManager);
			}
		}
	}
}
