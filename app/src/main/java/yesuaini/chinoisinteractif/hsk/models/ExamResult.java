package yesuaini.chinoisinteractif.hsk.models;

import java.util.Date;

public class ExamResult {

	private final int correctAnswers;
	private final int wrongAnswers;
	private final Date examDate;

	/**
	 * @param correctAnswers
	 * @param wrongAnswers
	 * @param examDate
	 */
	public ExamResult(int correctAnswers, int wrongAnswers, Date examDate) {
		this.correctAnswers = correctAnswers;
		this.wrongAnswers = wrongAnswers;
		this.examDate = examDate;
	}
	
	public int getCorrectAnswers() {
		return correctAnswers;
	}

	public int getWrongAnswers() {
		return wrongAnswers;
	}

	public Date getExamDate() {
		return examDate;
	}

}
