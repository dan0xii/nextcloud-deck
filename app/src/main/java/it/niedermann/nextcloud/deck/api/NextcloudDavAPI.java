package it.niedermann.nextcloud.deck.api;


import io.reactivex.Observable;
import it.niedermann.nextcloud.deck.model.dav.DeckComment;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface NextcloudDavAPI {

    @GET("comments/deckCard/{cardId}")
    Observable<Void> postComment(@Path("cardId") long cardId, @Body DeckComment comment);

}
