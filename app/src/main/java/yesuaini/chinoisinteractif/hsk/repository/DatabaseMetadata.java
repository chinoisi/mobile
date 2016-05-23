package yesuaini.chinoisinteractif.hsk.repository;

public class DatabaseMetadata {
	/** Table containing all word lists */
	public static final String T_WORDS = "t_words";

	/** Table for cached quizzes. */
	public static final String T_CACHE = "t_cache";
	
	/** Table for the exam questions and metadata. */
	public static final String T_EXAM_RESULTS = "t_exam_results";
	
	/** Table for word list metadata */
	public static final String T_WORDLISTS = "t_wordlists";
	
	/** View for exam word list **/
	public static final String V_EXAM = "v_exam";
	
	// Word list ids used in the category_id column
	public static final int WORD_LIST_ID_HSK1 = 1;
	public static final int WORD_LIST_ID_HSK2 = 2;

	public static final int[] ALL_WORD_LIST_IDS = { WORD_LIST_ID_HSK1, WORD_LIST_ID_HSK2 };
}
