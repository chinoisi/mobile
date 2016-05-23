package yesuaini.chinoisinteractif.hsk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import yesuaini.chinoisinteractif.AbstractNavigationActivity;
import yesuaini.chinoisinteractif.hsk.models.QuizHanzi;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;
import yesuaini.chinoisinteractif.hsk.service.QuizService;
import yesuaini.chinoisinteractif.hsk.utils.AsyncSoundPlayer;
import yesuaini.chinoisinteractif.hsk.utils.SoundManager;
import yesuaini.chinoisinteractif.R;

public class ExamActivity extends AbstractNavigationActivity {
	@SuppressWarnings("unused") private static final String LOG_TAG = ExamActivity.class.getSimpleName();
	private static final float SMALL_TEXTSIZE = 14f;
	private static final float DEFAULT_TEXTSIZE = 16f;
	private static final int ID_ARRAY[] = new int[] {R.id.defView0, R.id.defView1, R.id.defView2, R.id.defView3};

	protected static final String PREFS_NAME = "yesuaini.chinoisinteractif.exam.prefs";
	protected static final String PREF_WORD_LIST_OFFSET = "wordListOffset";
	protected static final String PREF_WORD_LIST_ID = "wordListId";
	private static final String PREF_PINYIN_SHOWN = "pinyinShown";
	private static final String PREF_WRONG_ANSWERS = "wrongAnswer";
	private static final String PREF_CORRECT_ANSWERS = "correctAnswer";
	private static final String PREF_CACHED_WORD_IDS = "cachedWordIds";
	private static final String PREF_CACHED_ALTERNATIVES = "cachedAlternativeWordIds";
	
	protected static final String SELECTED_EXAM_WORD_LIST_EXTRA = "yesuaini.chinoisinteractif.exam.selectedWordListId";
	protected static final String NEW_EXAM_EXTRA = "yesuaini.chinoisinteractif.exam.newExam";

	private Button[] answerButtonArray;
	private boolean pinyinShown = true;
	private int wordIdOfAnswer;
	
	//exam-specific fields
	private int wordListId;
	private int wordListOffset;
	private int[] shuffledIds;
	
	private SoundManager soundManager;
	private AudioManager audioManager;
	private DatabaseHelper dbh;
	private QuizService quizService;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		dbh = new DatabaseHelper(this); 
		
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		Intent intent = getIntent();

		//maybe not very pedagogical to show answers before end of exam...
		pinyinShown = prefs.getBoolean(PREF_PINYIN_SHOWN, true);
		
		//get data from the Intent that started this activity
		wordListId = intent.getIntExtra(CharacterListActivity.SELECTED_WORD_LIST_EXTRA, -1);
		setTitle(getString(R.string.exam_str) + ": " + dbh.getWordListName(wordListId));
		prefs.edit().putInt(PREF_WORD_LIST_ID, wordListId).commit();
		if (intent.getBooleanExtra(NEW_EXAM_EXTRA, false)) {
			generateShuffledWordList(prefs);
			intent.removeExtra(NEW_EXAM_EXTRA);
		} else { //get cached word list
			shuffledIds = QuizService.unmarshallWordIds(prefs.getString(PREF_CACHED_WORD_IDS, ""));
			wordListOffset = prefs.getInt(PREF_WORD_LIST_OFFSET, 0);
			//if cached data is incompatible, regenerate word list
			if (shuffledIds.length != dbh.getWordListSize(wordListId) 
					|| QuizService.getCachedHighestId(shuffledIds) != dbh.getWordListHighestId(wordListId)) {
				generateShuffledWordList(prefs);
			}
		}
		
		//sanity check word list offset and word list size
		assert shuffledIds.length > 0;
		assert wordListOffset < shuffledIds.length;
		
		((ProgressBar) findViewById(R.id.quizProgressBar)).setProgress(calculateExamProgress(wordListOffset, shuffledIds.length));
		findViewById(R.id.isLearnedCheckBox).setVisibility(View.GONE);
		
		//generate new or recover cached quiz and answer
		List<QuizHanzi> quizWordList = new ArrayList<QuizHanzi>(4);	
		int indexOfAnswer = generateQuiz(quizWordList, wordListOffset);
		assert indexOfAnswer > -1;
		
		//display quiz
		initQuizView(quizWordList, indexOfAnswer);
		
		audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
		soundManager = new SoundManager(getAssets(), getQuizService()
				.getFilePathByWordList(wordListId));
	}

	@Override
	protected int getToolbarViewId() {
		return R.id.toolbar;
	}

	@Override
	protected int getContentViewActivity() {
		return R.layout.hsk_quiz;
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private int calculateExamProgress(int currentOffset, int length) {
		return (int)((wordListOffset / (float)shuffledIds.length) * 100);
	}

	private void generateShuffledWordList(SharedPreferences prefs) {
		resetPrefs(prefs);
		wordListOffset = 0;
		String marshalledWordIds = dbh.getShuffledIdsForWordList(wordListId);
		shuffledIds = QuizService.unmarshallWordIds(marshalledWordIds);
		Editor editor = prefs.edit();
		editor.putString(PREF_CACHED_WORD_IDS, marshalledWordIds);
//		editor.putBoolean(PREF_NEW_EXAM, false);
		editor.commit();
	}

	private int generateQuiz(List<QuizHanzi> quizWordList, int wordListOffset) {
		int randomIndex = -1;
		try {
			//get next word by offset
			QuizHanzi examWord = dbh.getNextExamWord(shuffledIds[wordListOffset]);
			wordIdOfAnswer = examWord.getId();
			
			//get three random words
			List<QuizHanzi> randomWords = getQuizAlternativesFromCache();
			if (randomWords == null || randomWords.isEmpty()) {
				randomWords = dbh.getChoicesForExam(false, examWord.getId(), wordListId);
				updateCache(randomWords);
			}
			
			//shuffle words
			randomIndex = new Random().nextInt(4);
			int choicesIndex = 0;
			for (int i = 0; i < 4; i++) {
				if (i == randomIndex) {
					quizWordList.add(examWord);
				} else {
					quizWordList.add(randomWords.get(choicesIndex++));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return randomIndex;
	}
	
	private List<QuizHanzi> getQuizAlternativesFromCache() {
		List<QuizHanzi> list = new ArrayList<QuizHanzi>();
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		String marshalledWordIds = prefs.getString(PREF_CACHED_ALTERNATIVES, "");
		if (!marshalledWordIds.equals("")) {
			int[] wordIds = QuizService.unmarshallWordIds(marshalledWordIds);
			for (int i = 0; i < wordIds.length; i++) {
				list.add(dbh.getNextExamWord(wordIds[i]));
			}
		}
		return list;
	}
	
	private void updateCache(List<QuizHanzi> list) {
		String marshalledWordIds = QuizService.marshallWordIds(
			list.get(0).getId(),
			list.get(1).getId(),
			list.get(2).getId()
		);
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		prefs.edit().putString(PREF_CACHED_ALTERNATIVES, marshalledWordIds).commit();
	}
	
	/**
	 * Initialization and display of the quiz in the UI. This method is 
	 * only called at the start of the activity. 
	 * @param quizWordList the generated list of words included in the quiz
	 * @param indexOfAnswer the index of the correct word in the quizWordList
	 */
	private void initQuizView(List<QuizHanzi> quizWordList, int indexOfAnswer) {
		PlaySoundClickListener onHanziClickListener = new PlaySoundClickListener();
		TextView examWordView = (TextView) findViewById(R.id.quizWordView);
		examWordView.setText(quizWordList.get(indexOfAnswer).getWord());
		examWordView.setOnClickListener(onHanziClickListener);
		TextView examPinyinView = (TextView) findViewById(R.id.pinyinLabel);
		examPinyinView.setText(quizWordList.get(indexOfAnswer).getPinyin());
		examPinyinView.setOnClickListener(onHanziClickListener);
		if (pinyinShown) {
			examPinyinView.setVisibility(View.VISIBLE);
		}
		answerButtonArray = new Button[ID_ARRAY.length];
		for (int i = 0; i < ID_ARRAY.length; i++) {
			Button defButton = (Button) findViewById(ID_ARRAY[i]);
			answerButtonArray[i] = defButton;
			String definition = quizWordList.get(i).getDefinition();
			defButton.setText(definition);
			if (definition.length() > 105) {
				answerButtonArray[i].setTextSize(SMALL_TEXTSIZE);
			} else {
				answerButtonArray[i].setTextSize(DEFAULT_TEXTSIZE);
			}
			defButton.setBackgroundResource(R.drawable.btn_recolored);
			int id = quizWordList.get(i).getId();
			defButton.setOnClickListener(new OnExamAnswerListener(id));
		}
	}

	/**
	 * Resets the UI, generates and displays a new quiz.
	 */
	private void resetQuizView() {
		getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit().putString(PREF_CACHED_ALTERNATIVES, "").commit();
		List<QuizHanzi> quizWordList = new ArrayList<QuizHanzi>(4);
		int indexOfAnswer = generateQuiz(quizWordList, wordListOffset);
		
		TextView examWordView = (TextView) findViewById(R.id.quizWordView);
		examWordView.setText(quizWordList.get(indexOfAnswer).getWord());
		TextView examPinyinView = (TextView) findViewById(R.id.pinyinLabel);
		examPinyinView.setText(quizWordList.get(indexOfAnswer).getPinyin());
		for (int i = 0; i < ID_ARRAY.length; i++) {
			String definition = quizWordList.get(i).getDefinition();
			answerButtonArray[i].setText(definition);
			answerButtonArray[i].setEnabled(true);
			if (definition.length() > 105) {
				answerButtonArray[i].setTextSize(SMALL_TEXTSIZE);
			} else {
				answerButtonArray[i].setTextSize(DEFAULT_TEXTSIZE);
			}
			answerButtonArray[i].setBackgroundResource(R.drawable.btn_recolored);
			int id = quizWordList.get(i).getId();
			answerButtonArray[i].setOnClickListener(new OnExamAnswerListener(id));
		}
	}
	
	private void resetPrefs(SharedPreferences prefs) {
		prefs.edit()
//			.putBoolean(PREF_NEW_EXAM, false)
			.putInt(PREF_CORRECT_ANSWERS, 0)
			.putInt(PREF_WRONG_ANSWERS, 0)
			.putInt(PREF_WORD_LIST_OFFSET, 0)
			.putString(PREF_CACHED_WORD_IDS, "")
			.putString(PREF_CACHED_ALTERNATIVES, "")
			.commit();
	}

	private class OnExamAnswerListener implements OnClickListener {
		private final int id;
		
		public OnExamAnswerListener(int id) {
			this.id = id;
		}
		
		public void onClick(View view) {
			SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			Editor editor = prefs.edit();
			TextView examPinyinView = (TextView) findViewById(R.id.pinyinLabel);
			if (id == wordIdOfAnswer) { //correct answer
				editor.putInt(PREF_CORRECT_ANSWERS, prefs.getInt(PREF_CORRECT_ANSWERS, 0) + 1).commit();
				new AsyncColorSwitcher((Button) view, null, examPinyinView).execute((Long[])null);
			} else { //wrong answer
				editor.putInt(PREF_WRONG_ANSWERS, prefs.getInt(PREF_WRONG_ANSWERS, 0) + 1).commit();
				Button incorrectButton = (Button) view;
				new AsyncColorSwitcher(null, 
						incorrectButton, examPinyinView).execute((Long[])null);
			}
		}
	}
	
	private class AsyncColorSwitcher extends AsyncTask<Long,Void,Void> {
		private final Button correctButton;
		private final Button incorrectButton;
		private final View affectedView;
		
		public AsyncColorSwitcher(Button correctButton, Button incorrectButton, View affectedView) {
			this.correctButton = correctButton;
			this.incorrectButton = incorrectButton;
			this.affectedView = affectedView;
		}
		
		@Override
		protected void onPreExecute() {
			if (incorrectButton != null) { //wrong answer chosen
				incorrectButton.setBackgroundColor(Color.RED);
			} else if (correctButton != null) { //correct answer chosen
				correctButton.setBackgroundColor(Color.GREEN);
			}
			//show pinyin only when answered
			affectedView.setVisibility(View.VISIBLE);
			
			//disable buttons to prevent double taps
			for (Button button : answerButtonArray) {
				button.setEnabled(false);
			}
		}
		
		@Override
		protected Void doInBackground(Long... args) {
			try {
				long sleepTime = (args != null && args.length > 0) ? args[0] : 1500;
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void v) {
			affectedView.setVisibility(pinyinShown ? View.VISIBLE : View.GONE);
			SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
			wordListOffset++;
			prefs.edit().putInt(PREF_WORD_LIST_OFFSET, wordListOffset).commit();
			((ProgressBar) findViewById(R.id.quizProgressBar)).setProgress(
					calculateExamProgress(wordListOffset, shuffledIds.length));
			if (wordListOffset < shuffledIds.length) {
				resetQuizView();
			} else {
				Intent intent = new Intent(ExamActivity.this, ExamResultActivity.class);
				intent.putExtra(ExamResultActivity.WORD_LIST_ID_EXTRA, wordListId);
				intent.putExtra(ExamResultActivity.CORRECT_ANSWERS_EXTRA, prefs.getInt(PREF_CORRECT_ANSWERS, 0));
				intent.putExtra(ExamResultActivity.WRONG_ANSWERS_EXTRA, prefs.getInt(PREF_WRONG_ANSWERS, 0));
				resetPrefs(prefs);
				startActivity(intent);
				finish();
			}
		}
	}
	
	private QuizService getQuizService() {
		if (quizService == null) {
			quizService = new QuizService();
		}
		return quizService;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    getMenuInflater().inflate(R.menu.quizmenu, menu);
	    menu.removeItem(R.id.show_correct_answer); //don't show this button exams
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
		switch (item.getItemId()) {
	        case R.id.enable_pinyin: {
	        	boolean isPinyinShown = prefs.getBoolean(PREF_PINYIN_SHOWN, true);
	            pinyinShown = !isPinyinShown;
	            findViewById(R.id.pinyinLabel).setVisibility(pinyinShown ? View.VISIBLE : View.GONE);
	            prefs.edit().putBoolean(PREF_PINYIN_SHOWN, pinyinShown).commit();
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
				String word = ((TextView) findViewById(R.id.quizWordView)).getText().toString();
				new AsyncSoundPlayer().execute(dbh.getSoundfileNameByWord(word, wordListId), soundManager);
			}
		}
	}
}
