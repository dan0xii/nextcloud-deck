package it.niedermann.nextcloud.deck.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.List;

import it.niedermann.nextcloud.deck.model.Attachment;
import it.niedermann.nextcloud.deck.model.Label;
import it.niedermann.nextcloud.deck.model.full.FullBoard;
import it.niedermann.nextcloud.deck.model.full.FullCard;
import it.niedermann.nextcloud.deck.model.full.FullStack;
import it.niedermann.nextcloud.deck.model.ocs.Activity;
import it.niedermann.nextcloud.deck.model.ocs.Capabilities;
import it.niedermann.nextcloud.deck.model.ocs.DeckComment;

/**
 * Created by david on 27.06.17.
 */

public class GsonConfig {

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'hh:mm:ssZ";

    public static Gson getGson() {
        Type boardList = new TypeToken<List<FullBoard>>() {}.getType();
        Type board = new TypeToken<FullBoard>() {}.getType();
        Type cardList = new TypeToken<FullCard>() {}.getType();
        Type card = new TypeToken<FullCard>() {}.getType();
        Type labelList = new TypeToken<Label>() {}.getType();
        Type label = new TypeToken<Label>() {}.getType();
        Type stackList = new TypeToken<List<FullStack>>() {}.getType();
        Type stack = new TypeToken<FullStack>() {}.getType();
        Type capabilities = new TypeToken<Capabilities>() {}.getType();
        Type capabilitiesList = new TypeToken<List<Capabilities>>() {}.getType();
        Type activity = new TypeToken<Activity>() {}.getType();
        Type activityList = new TypeToken<List<Activity>>() {}.getType();
        Type attachment = new TypeToken<Attachment>() {}.getType();
        Type attachmentList = new TypeToken<List<Attachment>>() {}.getType();
        Type comment = new TypeToken<DeckComment>() {}.getType();
        Type commentList = new TypeToken<List<DeckComment>>() {}.getType();

        return new GsonBuilder()
                .setDateFormat(DATE_PATTERN)
                .setLenient()
                .registerTypeAdapter(Date.class,        new GsonUTCDateAdapter())
                .registerTypeAdapter(boardList,         new NextcloudArrayDeserializer<>("boards", FullBoard.class))
                .registerTypeAdapter(board,             new NextcloudDeserializer<>("board", FullBoard.class))
                .registerTypeAdapter(cardList,          new NextcloudArrayDeserializer<>("cards", FullCard.class))
                .registerTypeAdapter(card,              new NextcloudDeserializer<>("card", FullCard.class))
                .registerTypeAdapter(labelList,         new NextcloudArrayDeserializer<>("labels", Label.class))
                .registerTypeAdapter(label,             new NextcloudDeserializer<>("label", Label.class))
                .registerTypeAdapter(stackList,         new NextcloudArrayDeserializer<>("stacks", FullStack.class))
                .registerTypeAdapter(stack,             new NextcloudDeserializer<>("stack", FullStack.class))
                .registerTypeAdapter(capabilitiesList,  new NextcloudArrayDeserializer<>("capabilities", Capabilities.class))
                .registerTypeAdapter(capabilities,      new NextcloudDeserializer<>("capability", Capabilities.class))
                .registerTypeAdapter(activityList,      new NextcloudDeserializer<>("activities", Activity.class))
                .registerTypeAdapter(activity,          new NextcloudDeserializer<>("activity", Activity.class))
                .registerTypeAdapter(attachmentList,    new NextcloudArrayDeserializer<>("attachments", Attachment.class))
                .registerTypeAdapter(attachment,        new NextcloudDeserializer<>("attachment", Attachment.class))
                .registerTypeAdapter(commentList,       new NextcloudArrayDeserializer<>("comments", Attachment.class))
                .registerTypeAdapter(comment,           new NextcloudDeserializer<>("comment", Attachment.class))
                .create();
    }

}
