package it.niedermann.nextcloud.deck.api;

import android.content.Context;

import com.nextcloud.android.sso.AccountImporter;
import com.nextcloud.android.sso.api.NextcloudAPI;
import com.nextcloud.android.sso.exceptions.NextcloudFilesAppAccountNotFoundException;
import com.nextcloud.android.sso.exceptions.NoCurrentAccountSelectedException;
import com.nextcloud.android.sso.helper.SingleAccountHelper;
import com.nextcloud.android.sso.model.SingleSignOnAccount;

import it.niedermann.nextcloud.deck.DeckLog;
import retrofit2.NextcloudRetrofitApiBuilder;

/**
 * Created by david on 26.05.17.
 */

public class ApiProvider {

    private static final String DECK_API_ENDPOINT = "/index.php/apps/deck/api/v1.0/";
    private static final String NC_API_ENDPOINT = "/ocs/v2.php/";
    private static final String NC_DAV_ENDPOINT = "/remote.php/dav/";

    private DeckAPI deckAPI;
    private NextcloudServerAPI nextcloudAPI;
    private NextcloudDavAPI nextcloudDavAPI;
    private Context context;
    private SingleSignOnAccount ssoAccount;
    private String ssoAccountName;

    public ApiProvider(Context context) {
        this(context, null);
    }

    public ApiProvider(Context context, String ssoAccountName) {
        this.context = context;
        this.ssoAccountName = ssoAccountName;
        setAccount();
    }

    public void initSsoApi(final NextcloudAPI.ApiConnectedListener callback) {
        NextcloudAPI nextcloudAPI = new NextcloudAPI(context, ssoAccount, GsonConfig.getGson(), callback);
        deckAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, DECK_API_ENDPOINT).create(DeckAPI.class);
        this.nextcloudAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, NC_API_ENDPOINT).create(NextcloudServerAPI.class);
        nextcloudDavAPI = new NextcloudRetrofitApiBuilder(nextcloudAPI, NC_DAV_ENDPOINT).create(NextcloudDavAPI.class);

    }

    private void setAccount() {
        try {
            if (ssoAccountName == null) {
                this.ssoAccount = SingleAccountHelper.getCurrentSingleSignOnAccount(context);
            } else {
                this.ssoAccount = AccountImporter.getSingleSignOnAccount(context, ssoAccountName);
            }
        } catch (NextcloudFilesAppAccountNotFoundException | NoCurrentAccountSelectedException e) {
            DeckLog.logError(e);
        }
    }

    public DeckAPI getDeckAPI() {
        return deckAPI;
    }

    public NextcloudServerAPI getNextcloudAPI() {
        return nextcloudAPI;
    }

    public NextcloudDavAPI getNextcloudDavAPI() {
        return nextcloudDavAPI;
    }

    public String getServerUrl() {
        if (ssoAccount == null){
            setAccount();
        }
        return ssoAccount.url;
    }

    public String getApiPath() {
        return DECK_API_ENDPOINT;
    }

    public String getApiUrl() {
        return getServerUrl() + getApiPath();
    }
}
