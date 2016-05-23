package yesuaini.chinoisinteractif.models;

/**
 * Created by yesuaini on 23/04/16.
 */
public class Episode {

    private Integer id;
    private String title;
    private String image;
    private String objectif;

    public Integer getId() { return id; }
    public String getImage() {
        return image;
    }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return objectif;
    }

}
