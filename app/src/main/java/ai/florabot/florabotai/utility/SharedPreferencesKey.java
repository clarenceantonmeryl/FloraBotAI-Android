package ai.florabot.florabotai.utility;

public enum SharedPreferencesKey {

    NAME("FloraBOT"),
    ALERTS("ALERTS");

    public String KEY;

    SharedPreferencesKey(String key) {
        this.KEY = key;
    }

    @Override
    public String toString() {
        return this.KEY;
    }
}