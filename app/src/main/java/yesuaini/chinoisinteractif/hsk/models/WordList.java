package yesuaini.chinoisinteractif.hsk.models;

public class WordList {
	private int id;
	private String name;
	private int userDefined = 1;

	public WordList(int id, String name, int userDefined) {
		this.id = id;
		this.name = name;
		this.userDefined = userDefined;
	}

	public WordList(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getUserDefined() {
		return userDefined;
	}

	public void setUserDefined(int userDefined) {
		this.userDefined = userDefined;
	}
}
