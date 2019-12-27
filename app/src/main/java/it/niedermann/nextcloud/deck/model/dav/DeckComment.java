package it.niedermann.nextcloud.deck.model.dav;

import java.util.Date;

import it.niedermann.nextcloud.deck.util.DateUtil;

public class DeckComment {

    private final String actorType = "users";
    private final String objectType = "deckCard";
    private final String verb = "comment";
    private final Date creationDateTime = DateUtil.nowInGMT();
    private String actorId;
    private String actorDisplayName;
    private String message;

    public  DeckComment() {
    }

    public DeckComment(String actorId, String actorDisplayName, String message) {
        this.actorId = actorId;
        this.actorDisplayName = actorDisplayName;
        this.message = message;
    }

    public String getActorType() {
        return actorType;
    }

    public String getObjectType() {
        return objectType;
    }

    public String getVerb() {
        return verb;
    }

    public Date getCreationDateTime() {
        return creationDateTime;
    }

    public String getActorId() {
        return actorId;
    }

    public void setActorId(String actorId) {
        this.actorId = actorId;
    }

    public String getActorDisplayName() {
        return actorDisplayName;
    }

    public void setActorDisplayName(String actorDisplayName) {
        this.actorDisplayName = actorDisplayName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeckComment that = (DeckComment) o;

        if (actorId != null ? !actorId.equals(that.actorId) : that.actorId != null) return false;
        if (actorDisplayName != null ? !actorDisplayName.equals(that.actorDisplayName) : that.actorDisplayName != null)
            return false;
        return message != null ? message.equals(that.message) : that.message == null;
    }

    @Override
    public int hashCode() {
        int result = actorId != null ? actorId.hashCode() : 0;
        result = 31 * result + (actorDisplayName != null ? actorDisplayName.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        return result;
    }
}
