package ai.florabot.florabotai.data.model.vision;

import java.util.ArrayList;

import ai.florabot.florabotai.data.model.vision.imagepropertiesannotation.ImagePropertiesAnnotation;
import ai.florabot.florabotai.data.model.vision.labelannotations.LabelAnnotation;
import ai.florabot.florabotai.data.model.vision.localizedobjectannotations.LocalizedObjectAnnotation;
import ai.florabot.florabotai.data.model.vision.webdetection.WebDetection;

public class VisionData {

    ArrayList<LabelAnnotation> labelAnnotations;
    ImagePropertiesAnnotation imagePropertiesAnnotation;
    ArrayList<LocalizedObjectAnnotation> localizedObjectAnnotations;
    WebDetection webDetection;

    public VisionData() { }

    public ArrayList<LabelAnnotation> getLabelAnnotations() {
        return labelAnnotations;
    }

    public void setLabelAnnotations(ArrayList<LabelAnnotation> labelAnnotations) {
        this.labelAnnotations = labelAnnotations;
    }

    public ImagePropertiesAnnotation getImagePropertiesAnnotation() {
        return imagePropertiesAnnotation;
    }

    public void setImagePropertiesAnnotation(ImagePropertiesAnnotation imagePropertiesAnnotation) {
        this.imagePropertiesAnnotation = imagePropertiesAnnotation;
    }

    public ArrayList<LocalizedObjectAnnotation> getLocalizedObjectAnnotations() {
        return localizedObjectAnnotations;
    }

    public void setLocalizedObjectAnnotations(ArrayList<LocalizedObjectAnnotation> localizedObjectAnnotations) {
        this.localizedObjectAnnotations = localizedObjectAnnotations;
    }

    public WebDetection getWebDetection() {
        return webDetection;
    }

    public void setWebDetection(WebDetection webDetection) {
        this.webDetection = webDetection;
    }
}
