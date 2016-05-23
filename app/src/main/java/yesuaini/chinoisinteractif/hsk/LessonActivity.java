package yesuaini.chinoisinteractif.hsk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

import yesuaini.chinoisinteractif.AbstractNavigationActivity;
import yesuaini.chinoisinteractif.R;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;
import yesuaini.chinoisinteractif.hsk.utils.CSVParser;

import static yesuaini.chinoisinteractif.hsk.CharacterListActivity.SELECTED_WORD_LIST_EXTRA;

public class LessonActivity extends AbstractNavigationActivity {

    private static final String LOG_TAG = LessonActivity.class.getSimpleName();
	private DatabaseHelper dbh;
	int lessonId = 1;
	String fileName="vocabulary.csv";

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbh = new DatabaseHelper(this);
		Intent intent = getIntent();
		lessonId = intent.getIntExtra(SELECTED_WORD_LIST_EXTRA, 1);

        setTitle(getString(R.string.mission_title) +" " +lessonId);
        Boolean isWordListsToBeUpdated = dbh.doWordListHealthCheck(lessonId);
		if (!isWordListsToBeUpdated) {
			Log.d(LOG_TAG, "No data needed to be loaded");
			LinearLayout foreground = (LinearLayout) findViewById(R.id.main_menu_layout);
			foreground.setVisibility(View.VISIBLE);
		} else {
			new AsyncParser().execute(lessonId);
		}



        Button vocabularyButton = (Button) findViewById(R.id.lessonVocabulary);
        vocabularyButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LessonActivity.this,CharacterListActivity.class);
				int lessonId = getIntent().getIntExtra(SELECTED_WORD_LIST_EXTRA, 1);
				intent.putExtra(SELECTED_WORD_LIST_EXTRA, lessonId);
				startActivity(intent);
			}
		});

        Button charButton = (Button) findViewById(R.id.lessonCharacter);
        charButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LessonActivity.this, CharacterViewActivity.class);
				int lessonId = getIntent().getIntExtra(SELECTED_WORD_LIST_EXTRA, 1);
				intent.putExtra(SELECTED_WORD_LIST_EXTRA, lessonId);
				Random rand = new Random();
				intent.putExtra("yesuaini.chinoisinteractif.randomindex", rand.nextInt(151)+1);
				startActivity(intent);
			}
		});
        
        Button quizButton = (Button) findViewById(R.id.lessonExercice);
        quizButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(LessonActivity.this, QuizActivity.class);
				int lessonId = getIntent().getIntExtra(SELECTED_WORD_LIST_EXTRA, 1);
                intent.putExtra(QuizActivity.SELECTED_QUIZ_TABLE_EXTRA, lessonId);
				startActivity(intent);
			}
		});
        
        Button examButton = (Button) findViewById(R.id.lessonExam);
        examButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LessonActivity.this, ExamActivity.class);
				int lessonId = getIntent().getIntExtra(SELECTED_WORD_LIST_EXTRA, 1);
                intent.putExtra(ExamActivity.SELECTED_EXAM_WORD_LIST_EXTRA, lessonId);
                startActivity(intent);
			}
		});
    }

    @Override
    protected int getToolbarViewId() {
        return R.id.toolbar;
    }

    @Override
    protected int getContentViewActivity() {
        return R.layout.lesson;
    }

    public void parseLessonFile(Integer[] indexesToUpdate) {
        try {	
			CSVParser parser = new CSVParser(dbh);
        	BufferedReader br;
        	long startTime = System.currentTimeMillis();

			for (int i = 0; i < indexesToUpdate.length; i++) {
                Log.d(LOG_TAG, "Loading lessons/"+fileName);
				br = new BufferedReader(new InputStreamReader(this.getAssets().open("missions/" +lessonId+"/"+fileName), "UTF8"));
				parser.parseWordListCSV(br, lessonId, CSVParser.COMMA_DELIMITER );
			}
			long endTime = System.currentTimeMillis() - startTime;
			Log.d(LOG_TAG, "Parsing and storing took " + endTime + " ms");
		} catch (IOException e) {
			Toast.makeText(this, R.string.fatal_parser_error, Toast.LENGTH_LONG).show();
			e.printStackTrace();
		}
	}
	
	private class AsyncParser extends AsyncTask<Integer,Integer,Boolean> {
		private final ProgressDialog dialog;
		
		public AsyncParser() {
			dialog = new ProgressDialog(LessonActivity.this);
		}
		
		@Override
		protected void onPreExecute() {
			this.dialog.setMessage(LessonActivity.this.getText(R.string.installing_msg));
			this.dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			this.dialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Integer... params) {
			if (params.length != 0) {
				parseLessonFile(params);
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
			LinearLayout foreground = (LinearLayout) findViewById(R.id.main_menu_layout);
			foreground.setVisibility(View.VISIBLE);
		}
	}




}