package yesuaini.chinoisinteractif.hsk.service;

import java.util.List;
import java.util.Random;

import android.util.Log;
import yesuaini.chinoisinteractif.hsk.models.QuizHanzi;
import yesuaini.chinoisinteractif.hsk.repository.DatabaseMetadata;

public class QuizService {
	private static final String LOG_TAG = QuizService.class.getSimpleName();
	
	public int chooseCorrectAnswer(List<QuizHanzi> quizWordList) {
		//randomizes index and answer
		if (quizWordList != null && quizWordList.size() == 4) {
			int randInt = -1;
			randInt = new Random(System.currentTimeMillis()).nextInt(4);
			int idOfAnswer = quizWordList.get(randInt).getId();
			Log.d(LOG_TAG, "Index of answer: " + randInt + 
					" Id of answer: " + idOfAnswer);
			return idOfAnswer;
		} else {
			throw new RuntimeException("List is null or has invalid number of elements");
		}
	}
	
	public String getFilePathByWordList(int wordListId) {
		String filePath = "";
		if (wordListId == DatabaseMetadata.WORD_LIST_ID_HSK1) {
			filePath = "hsk/hsk1/";
		} else if (wordListId == DatabaseMetadata.WORD_LIST_ID_HSK2) {
			filePath = "hsk/hsk2/";
		}
		return filePath;
	}
	
	public static int getCachedHighestId(int[] shuffledIds) {
		int max = -1;
		for (int i : shuffledIds) {
			if (i > max) max = i;
		}
		return max;
	}
	
	public static int[] unmarshallWordIds(String marshalledWordIds) {
		String[] splitWordIdStrings = marshalledWordIds.split(",");
		int[] wordIds = new int[splitWordIdStrings.length];
		for (int i = 0; i < splitWordIdStrings.length; i++) {
			wordIds[i] = Integer.parseInt(splitWordIdStrings[i]);
		}
		return wordIds;
	}
	
	public static String marshallWordIds(int... wordIds) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < wordIds.length; i++) {
			if (i != 0) {
				sb.append(",");
			}
			sb.append(wordIds[i]);
		}
		return sb.toString();
	}
}
