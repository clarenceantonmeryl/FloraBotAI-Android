package ai.florabot.florabotai.data.model.vision.webdetection;

import java.util.ArrayList;

public class WebDetection {

    ArrayList<BestGuessLabel> bestGuessLabels;

    public WebDetection() {}

    public ArrayList<BestGuessLabel> getBestGuessLabels() {
        return bestGuessLabels;
    }

    public void setBestGuessLabels(ArrayList<BestGuessLabel> bestGuessLabels) {
        this.bestGuessLabels = bestGuessLabels;
    }
}
