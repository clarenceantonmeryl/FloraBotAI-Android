package ai.florabot.florabotai.data.model;

import com.google.firebase.Timestamp;

import ai.florabot.florabotai.data.model.vision.VisionData;

public class Photograph {

    String name;
    String url;
    int rotation;
    VisionData visionData;
    // ArrayList<DominantColor> dominantColors;
    // ArrayList<LabelAnnotation> labelAnnotations;
    Timestamp createdAt;

    public Photograph() { }

    public Photograph(String url, int rotation) {
        this.url = url;
        this.rotation = rotation;
        this.visionData = new VisionData();
        this.createdAt = Timestamp.now();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public VisionData getVisionData() {
        return visionData;
    }

    public void setVisionData(VisionData visionData) {
        this.visionData = visionData;
    }

    /*
    public ArrayList<DominantColor> getDominantColors() {
        return dominantColors;
    }

    public void setDominantColors(ArrayList<DominantColor> dominantColors) {
        this.dominantColors = dominantColors;
    }

    public ArrayList<LabelAnnotation> getLabelAnnotations() {
        return labelAnnotations;
    }

    public void setLabelAnnotations(ArrayList<LabelAnnotation> labelAnnotations) {
        this.labelAnnotations = labelAnnotations;
    }
    */

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
