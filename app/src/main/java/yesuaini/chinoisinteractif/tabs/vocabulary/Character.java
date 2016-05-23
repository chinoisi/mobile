package yesuaini.chinoisinteractif.tabs.vocabulary;


import java.io.Serializable;

public class Character implements Serializable {
	private String word;
	private String pinyin;
	private String translation;
	private String definition;
	private String soundfile;

	public Character() {}


	public String getWord() {
		return word;
	}

	public String getPinyin() {
		return pinyin;
	}

	public String getTranslation() {
		return translation;
	}

	public String getDefinition() {
		return definition;
	}

	public String getSoundfile() {
		return soundfile;
	}
}
