package ai.florabot.florabotai.data.model.vision.localizedobjectannotations;

public class LocalizedObjectAnnotation {

    String name;
    float score;

    public LocalizedObjectAnnotation() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
