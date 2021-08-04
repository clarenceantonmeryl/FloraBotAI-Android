package ai.florabot.florabotai.data.model.vision;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;

import ai.florabot.florabotai.data.model.vision.imagepropertiesannotation.DominantColor;
import ai.florabot.florabotai.data.model.vision.labelannotations.LabelAnnotation;
import ai.florabot.florabotai.data.model.vision.localizedobjectannotations.LocalizedObjectAnnotation;
import ai.florabot.florabotai.data.model.vision.webdetection.BestGuessLabel;
import ai.florabot.florabotai.ui.details.DetailsActivity;

public class VisionInfo {

    private ArrayList<MetaTag> metaTags = new ArrayList<>();
    private String name = "";
    private String bestGuess = "";
    private boolean spectrumAvailable;
    private boolean alert = false;
    private Bitmap bitmap;

    private VisionData visionData;
    private int spectrumWidth = 0;
    private int spectrumHeight = 0;

    private ArrayList<String> subscribedAlerts = new ArrayList<>();
    private ArrayList<String> alerts = new ArrayList<>();

    public VisionInfo(VisionData visionData, ArrayList<String> subscribedAlerts) {
        this.visionData = visionData;
        this.subscribedAlerts = subscribedAlerts;
        // initAlert();
        processData();
    }

    public VisionInfo(VisionData visionData, ArrayList<String> subscribedAlerts, int spectrumWidth, int spectrumHeight) {
        this.visionData = visionData;
        this.spectrumWidth = spectrumWidth;
        this.spectrumHeight = spectrumHeight;
        this.subscribedAlerts = subscribedAlerts;
        // initAlert();
        processData();
        pricessSpectrum();
    }

    /*
    private void initAlert() {
        subscribedAlerts.add("Pest");
        subscribedAlerts.add("Fox");
        subscribedAlerts.add("Mold");
        subscribedAlerts.add("Renard");
        subscribedAlerts.add("Disease");
        subscribedAlerts.add("Pathology");
    }
    */

    public ArrayList<MetaTag> getMetaTags() {
        return metaTags;
    }

    public void setMetaTags(ArrayList<MetaTag> metaTags) {
        this.metaTags = metaTags;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBestGuess() {
        return bestGuess;
    }

    public void setBestGuess(String bestGuess) {
        this.bestGuess = bestGuess;
    }

    public boolean isSpectrumAvailable() {
        return spectrumAvailable;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isAlert() {
        return alert;
    }

    public ArrayList<String> getAlerts() {
        return alerts;
    }

    private void processData() {

        if (visionData.getLocalizedObjectAnnotations() != null) {

            ArrayList<LocalizedObjectAnnotation> localizedObjectAnnotations = visionData.getLocalizedObjectAnnotations();

            localizedObjectAnnotations.sort((o1, o2) -> (int)(o2.getScore() * 100) - (int)(o1.getScore() * 100));

            for (LocalizedObjectAnnotation localizedObjectAnnotation : localizedObjectAnnotations) {

                String name = localizedObjectAnnotation.getName().trim();

                if (findMetaTagByName(metaTags, name) == null) {
                    metaTags.add(new MetaTag(name, "Object", localizedObjectAnnotation.getScore()));
                }
            }

            String metaNames = metaTags.toString();
            metaNames = metaNames.replace("[", "");
            metaNames = metaNames.replace("]", "");

            if (!metaNames.contains("&") && !metaNames.contains(" and ")) {
                String search = ", ";
                String replacement = " and ";

                int lastIndexOfSearch = metaNames.lastIndexOf(search);
                if (lastIndexOfSearch != -1) {
                    this.name = metaNames.substring(0, lastIndexOfSearch) +
                            replacement +
                            metaNames.substring(lastIndexOfSearch + search.length());
                } else {
                    this.name = metaNames;
                }
            } else {
                this.name = metaNames;
            }
        }

        if (visionData.getWebDetection() != null) {

            for (BestGuessLabel bestGuessLabel: visionData.getWebDetection().getBestGuessLabels()) {

                String name = bestGuessLabel.getLabel().trim().toUpperCase();

                if (name.length() == 0) {
                    continue;
                }

                if (findMetaTagByName(metaTags, name) == null) {
                    metaTags.add(0, new MetaTag(name, "Best Guess", 1f));
                }

                this.bestGuess = name;
            }
        }


        if (visionData.getLabelAnnotations() != null) {

            ArrayList<LabelAnnotation> labelAnnotations = visionData.getLabelAnnotations();

            labelAnnotations.sort(new DetailsActivity.LabelComparator());

            for (LabelAnnotation labelAnnotation : labelAnnotations) {

                String name = labelAnnotation.getDescription();

                if (findMetaTagByName(metaTags, name) == null) {
                    metaTags.add(new MetaTag(name, "Label", labelAnnotation.getScore()));
                }
            }

            if (this.name.equals("")) {
                this.name = labelAnnotations.get(0).getDescription();
            }
        }

        if (this.name.equals("")) {
            this.name = this.bestGuess;
            this.bestGuess = "";
        }

        // subscribedAlerts = new ArrayList<>();

        for(MetaTag metaTag : metaTags) {

            String name = metaTag.getName().trim().toLowerCase();

            String alertResult = findAlertByName(subscribedAlerts, name);

            if (alertResult != null) {
                alert = true;
                metaTag.setAlert(true);

                if (findAlertByName(alerts, alertResult) == null) {
                    alerts.add(alertResult);
                }
            }
        }
    }

    private static MetaTag findMetaTagByName(ArrayList<MetaTag> metaTags, String name) {
        return metaTags.stream().filter(metaTag -> name.equals(metaTag.getName().trim())).findFirst().orElse(null);
    }

    private static String findAlertByName(ArrayList<String> alerts, String name) {
        return alerts.stream().filter(alert -> name.trim().toLowerCase().contains(alert.trim().toLowerCase())).findFirst().orElse(null);
    }

    private void pricessSpectrum() {

        if (visionData.getImagePropertiesAnnotation() != null && spectrumWidth > 0 && spectrumHeight > 0) {

            this.spectrumAvailable = true;

            this.bitmap = Bitmap.createBitmap(spectrumWidth, spectrumHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            ArrayList<DominantColor> dominantColors = visionData.getImagePropertiesAnnotation().getDominantColors().getColors();

            dominantColors.sort(new DetailsActivity.ColorComparator());

            float totalScore = 0;
            for (DominantColor dominantColor : dominantColors) {
                totalScore += dominantColor.getScore() * 100;
            }

            float currentX = 0;
            for (DominantColor dominantColor : visionData.getImagePropertiesAnnotation().getDominantColors().getColors()) {

                float width = dominantColor.getScore() * 100 * spectrumWidth / totalScore;

                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                paint.setColor(Color.rgb(dominantColor.getColor().getRed(), dominantColor.getColor().getGreen(), dominantColor.getColor().getBlue()));
                canvas.drawRect(currentX, 0f, (currentX + width), spectrumHeight, paint);
                currentX += width;
            }

        } else {

            this.spectrumAvailable = false;
        }

    }

}
