package ai.florabot.florabotai.data.model.vision.labelannotations;

public class LabelAnnotation {

    String description;
    float score;

    public LabelAnnotation() { }

    public LabelAnnotation(String description, float score) {
        this.description = description;
        this.score = score;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
