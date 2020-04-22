package it.niedermann.nextcloud.deck.ui.archivedcards;

import android.os.Bundle;

import androidx.annotation.Nullable;

import it.niedermann.nextcloud.deck.databinding.ActivityArchivedCardsBinding;
import it.niedermann.nextcloud.deck.model.Account;
import it.niedermann.nextcloud.deck.persistence.sync.SyncManager;
import it.niedermann.nextcloud.deck.ui.branding.BrandedActivity;
import it.niedermann.nextcloud.deck.ui.exception.ExceptionHandler;

import static it.niedermann.nextcloud.deck.ui.card.CardAdapter.BUNDLE_KEY_ACCOUNT;
import static it.niedermann.nextcloud.deck.ui.card.CardAdapter.BUNDLE_KEY_BOARD_ID;
import static it.niedermann.nextcloud.deck.ui.card.CardAdapter.BUNDLE_KEY_CAN_EDIT;

public class ArchivedCardsActvitiy extends BrandedActivity {

    private ActivityArchivedCardsBinding binding;
    private ArchivedCardsAdapter adapter;
    private SyncManager syncManager;

    private Account account;
    private long boardId;
    private boolean canEdit = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));

        final Bundle args = getIntent().getExtras();
        if (args == null || !args.containsKey(BUNDLE_KEY_ACCOUNT) || !args.containsKey(BUNDLE_KEY_BOARD_ID)) {
            throw new IllegalArgumentException("Please provide at least " + BUNDLE_KEY_ACCOUNT + " and " + BUNDLE_KEY_BOARD_ID);
        }

        this.account = (Account) args.getSerializable(BUNDLE_KEY_ACCOUNT);
        this.boardId = args.getLong(BUNDLE_KEY_BOARD_ID);
        canEdit = args.getBoolean(BUNDLE_KEY_CAN_EDIT);

        if (this.account == null) {
            throw new IllegalArgumentException(BUNDLE_KEY_ACCOUNT + " must not be null.");
        }
        if (this.boardId <= 0) {
            throw new IllegalArgumentException(BUNDLE_KEY_BOARD_ID + " must a positive long value.");
        }

        binding = ActivityArchivedCardsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        syncManager = new SyncManager(this);

        adapter = new ArchivedCardsAdapter(this, account, boardId, false, syncManager, this);
        binding.recyclerView.setAdapter(adapter);

        syncManager.getArchivedFullCardsForBoard(account.getId(), boardId).observe(this, (fullCards) -> {

        });

    }

    @Override
    public void applyBrand(int mainColor, int textColor) {
        applyBrandToPrimaryToolbar(mainColor, textColor, binding.toolbar);
    }
}