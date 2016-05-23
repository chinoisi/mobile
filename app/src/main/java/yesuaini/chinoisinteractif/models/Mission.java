package yesuaini.chinoisinteractif.models;

import java.io.Serializable;
import java.util.List;

import yesuaini.chinoisinteractif.tabs.vocabulary.Character;

/**
 * Created by yesuaini on 23/04/16.
 */
public class Mission extends MissionSummary implements Serializable {

    private String video;
    private String url;
    private String exercice;
    private List<Character> characters;
    private String objectif;
    private String type;

    public String getVideo() { return video; }
    public String getUrl() { return url;}
    public String getExercice() { return exercice; }
    public List<Character> getCharacters() { return characters; }
    public String getObjectif() { return objectif;}
      public String getType() { return type;}
}
