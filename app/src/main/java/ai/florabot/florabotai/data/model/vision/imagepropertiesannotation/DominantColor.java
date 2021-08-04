package ai.florabot.florabotai.data.model.vision.imagepropertiesannotation;

public class DominantColor {

    Color color;
    float score;

    public DominantColor() {}

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
