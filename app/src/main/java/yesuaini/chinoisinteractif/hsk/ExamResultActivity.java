package yesuaini.chinoisinteractif.hsk;

import static yesuaini.chinoisinteractif.hsk.WordListSelectionActivity.EXAM_MODE;
import static yesuaini.chinoisinteractif.hsk.WordListSelectionActivity.WORD_LIST_SELECTION_MODE_EXTRA;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import yesuaini.chinoisinteractif.hsk.models.ExamResult;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseHelper;
import yesuaini.chinoisinteractif.hsk.views.Bar;
import yesuaini.chinoisinteractif.hsk.views.BarGraph;
import yesuaini.chinoisinteractif.hsk.views.ExamResultPieChart;
import yesuaini.chinoisinteractif.R;

public class ExamResultActivity extends FragmentActivity {
	private static final int GREEN = -16711936;
	private static final int RED = -65536;
	private static final int RESULTS_TAB = 0;
	private static final int HISTORY_TAB = 1;

	protected static final String WORD_LIST_ID_EXTRA = "yesuaini.chinoisinteractif.examresults.wordListIdExtra";
	protected static final String CORRECT_ANSWERS_EXTRA = "yesuaini.chinoisinteractif.examresults.correctAnswersExtra";
	protected static final String WRONG_ANSWERS_EXTRA = "yesuaini.chinoisinteractif.examresults.wrongAnswersExtra";

	private ViewPager viewPager;
	private static int wordListId;
	private static int correctAnswers;
	private static int wrongAnswers;
	private static AsyncExamHistoryVisualizer asyncExamHistoryVisualizer;
	private DatabaseHelper dbh;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.hsk_exam_results);
		setTitle(getString(R.string.exam_result));

		Intent intent = getIntent();
		wordListId = intent.getIntExtra(WORD_LIST_ID_EXTRA, 0);
		correctAnswers = intent.getIntExtra(CORRECT_ANSWERS_EXTRA, 0);
		wrongAnswers = intent.getIntExtra(WRONG_ANSWERS_EXTRA, 0);

		dbh = getDatabaseHelper();
		Executors.newSingleThreadExecutor().execute(
				new StatisticUpdater(dbh, wordListId,
						correctAnswers, wrongAnswers));
		
		asyncExamHistoryVisualizer = new AsyncExamHistoryVisualizer();

		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(new ExamResultsPagerAdapter(
				getSupportFragmentManager()));
	}

	public class ExamResultsPagerAdapter extends FragmentPagerAdapter {
		public ExamResultsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
			Fragment fragment = new ExamResultsTabFragment();
			Bundle args = new Bundle();
			args.putInt(ExamResultsTabFragment.ARG_OBJECT, i);
			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return "OBJECT " + (position + 1);
		}
	}

	public static class ExamResultsTabFragment extends Fragment {
		public static final String ARG_OBJECT = "object";

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			int viewId = getArguments().getInt(ARG_OBJECT);

			if (viewId == RESULTS_TAB) {
				return getResultsTab(inflater, container);
			} else if (viewId == HISTORY_TAB) {
				return getHistoryTab(inflater, container);
			} else {
				throw new RuntimeException("No such tab id: " + viewId);
			}
		}

		private View getResultsTab(LayoutInflater inflater, ViewGroup container) {
			View rootView = inflater.inflate(R.layout.hsk_exam_piechart, container,
					false);

			TextView correctAnswersTextView = (TextView) rootView
					.findViewById(R.id.correctAnswersTextView);
			correctAnswersTextView.setText("" + correctAnswers);
			TextView wrongAnswersTextView = (TextView) rootView
					.findViewById(R.id.wrongAnswersTextView);
			wrongAnswersTextView.setText("" + wrongAnswers);

			String grade = "F";
			int total = wrongAnswers + correctAnswers;
			float correctPart = correctAnswers / (float) total;
			if (correctPart >= 0.95) {
				grade = "A";
			} else if (correctPart >= 0.9) {
				grade = "B";
			} else if (correctPart >= 0.8) {
				grade = "C";
			} else if (correctPart >= 0.7) {
				grade = "D";
			} else if (correctPart >= 0.6) {
				grade = "E";
			} else {
				grade = "F";
			}

			TextView gradeTextView = (TextView) rootView
					.findViewById(R.id.gradeTextView);
			gradeTextView.setText(grade);
			gradeTextView.setTextColor(grade.equals("F") ? RED : GREEN);

			try {
				ExamResultPieChart pie = (ExamResultPieChart) rootView
						.findViewById(R.id.pieChart);
				ArrayList<Float> percentages = new ArrayList<Float>();
				percentages.add(correctPart * 100f);
				percentages.add(100f - correctPart * 100f);
				pie.setAdapter(percentages);
			} catch (ExamResultPieChart.DisproportionatePieChartSumException e) {
				e.printStackTrace();
			}

			return rootView;
		}

		private View getHistoryTab(LayoutInflater inflater, ViewGroup container) {
			View rootView = inflater.inflate(R.layout.hsk_exam_history, container,
					false);
			
			asyncExamHistoryVisualizer.setBarGraph((BarGraph) rootView.findViewById(R.id.graph));
			asyncExamHistoryVisualizer.execute();

			return rootView;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.hsk_exam_result_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.exam_result_menu_back_btn:
			returnToWordList();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void returnToWordList() {
		Intent intent = new Intent(ExamResultActivity.this,
				WordListSelectionActivity.class);
		intent.putExtra(WORD_LIST_SELECTION_MODE_EXTRA, EXAM_MODE);
		startActivity(intent);
		finish();
	}
	
	private static char getExamGradeFromScore(int correctAnswers, int wrongAnswers) {
		int total = wrongAnswers + correctAnswers;
		float correctPart = correctAnswers / (float) total;
		char grade;
		if (correctPart >= 0.95) {
			grade = 'A';
		} else if (correctPart >= 0.9) {
			grade = 'B';
		} else if (correctPart >= 0.8) {
			grade = 'C';
		} else if (correctPart >= 0.7) {
			grade = 'D';
		} else if (correctPart >= 0.6) {
			grade = 'E';
		} else {
			grade = 'F';
		}
		return grade;
	}
	
	private DatabaseHelper getDatabaseHelper() {
		if (dbh == null) {
			dbh = new DatabaseHelper(this);
		}
		return dbh;
	}

	private class StatisticUpdater implements Runnable {
		private final DatabaseHelper dbh;
		private final int wordList;
		private final int correctAnswers;
		private final int wrongAnswers;

		public StatisticUpdater(DatabaseHelper dbh, int wordList,
				int correctAnswers, int wrongAnswers) {
			this.dbh = dbh;
			this.wordList = wordList;
			this.correctAnswers = correctAnswers;
			this.wrongAnswers = wrongAnswers;
		}

		@Override
		public void run() {
			dbh.addExamResultToStatistics(wordList, correctAnswers,
					wrongAnswers, new Date().getTime());
		}
	}
	
	private class AsyncExamHistoryVisualizer extends AsyncTask<Void, Void, Void> {
		private BarGraph barGraph;
		private ArrayList<Bar> points = new ArrayList<Bar>();

		@Override
		protected Void doInBackground(Void... params) {
			List<ExamResult> list = getDatabaseHelper().getExamResultsHistoryForWordList(wordListId);
			Collections.reverse(list);
			DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
			for (ExamResult examResult : list) {
				char grade = getExamGradeFromScore(examResult.getCorrectAnswers(), 
						examResult.getWrongAnswers());
				points.add(new Bar(grade != 'F' ? GREEN : RED, 
						dateFormat.format(examResult.getExamDate()), 
						(float) examResult.getCorrectAnswers(), 
						String.valueOf(grade)));
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			barGraph.setBars(points);
		}

		public void setBarGraph(BarGraph barGraph) {
			this.barGraph = barGraph;
		}
	}
}
