package ai.florabot.florabotai.data.model;

import com.google.firebase.Timestamp;

public class User {

    String displayName;
    Timestamp createdAt;
    Timestamp updatedAt;

    public User() {

    }

    public User(String displayName, Timestamp updatedAt) {
        this.displayName = displayName;
        this.updatedAt = updatedAt;
    }

    public User(String displayName, Timestamp createdAt, Timestamp updatedAt) {
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
}
