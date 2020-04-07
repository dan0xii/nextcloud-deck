package it.niedermann.nextcloud.deck.ui.card;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import it.niedermann.nextcloud.deck.DeckLog;
import it.niedermann.nextcloud.deck.R;
import it.niedermann.nextcloud.deck.crosstabdnd.DragAndDropAdapter;
import it.niedermann.nextcloud.deck.crosstabdnd.DraggedItemLocalState;
import it.niedermann.nextcloud.deck.databinding.ItemCardBinding;
import it.niedermann.nextcloud.deck.model.Account;
import it.niedermann.nextcloud.deck.model.Card;
import it.niedermann.nextcloud.deck.model.Label;
import it.niedermann.nextcloud.deck.model.Stack;
import it.niedermann.nextcloud.deck.model.User;
import it.niedermann.nextcloud.deck.model.enums.DBStatus;
import it.niedermann.nextcloud.deck.model.full.FullCard;
import it.niedermann.nextcloud.deck.model.full.FullStack;
import it.niedermann.nextcloud.deck.persistence.sync.SyncManager;
import it.niedermann.nextcloud.deck.persistence.sync.adapters.db.util.LiveDataHelper;
import it.niedermann.nextcloud.deck.ui.EditActivity;
import it.niedermann.nextcloud.deck.util.ColorUtil;
import it.niedermann.nextcloud.deck.util.DateUtil;
import it.niedermann.nextcloud.deck.util.DimensionUtil;
import it.niedermann.nextcloud.deck.util.ViewUtil;

public class CardAdapter extends RecyclerView.Adapter<ItemCardViewHolder> implements DragAndDropAdapter<FullCard> {

    public static final String BUNDLE_KEY_ACCOUNT = "account";
    public static final String BUNDLE_KEY_ACCOUNT_ID = "accountId";
    public static final String BUNDLE_KEY_LOCAL_ID = "localId";
    public static final String BUNDLE_KEY_BOARD_ID = "boardId";
    public static final String BUNDLE_KEY_STACK_ID = "stackId";
    public static final String BUNDLE_KEY_CAN_EDIT = "canEdit";
    public static final Long NO_LOCAL_ID = -1L;
    private final SyncManager syncManager;
    private final Account account;
    private final long boardId;
    private final long stackId;
    private final boolean canEdit;
    @Nullable
    private final SelectCardListener selectCardListener;
    private List<FullCard> cardList = new LinkedList<>();
    private LifecycleOwner lifecycleOwner;
    private List<FullStack> availableStacks = new ArrayList<>();
    private int maxAvatarCount;
    private int maxLabelsShown;
    private int maxLabelsChars;
    private String counterMaxValue;

    public CardAdapter(@NonNull Account account, long boardId, long stackId, boolean canEdit, @NonNull SyncManager syncManager, @NonNull Fragment fragment, @Nullable SelectCardListener selectCardListener) {
        this.lifecycleOwner = fragment;
        this.account = account;
        this.boardId = boardId;
        this.stackId = stackId;
        this.canEdit = canEdit;
        this.syncManager = syncManager;
        this.selectCardListener = selectCardListener;
        setHasStableIds(true);
    }

    @Override
    public long getItemId(int position) {
        return cardList.get(position).getLocalId();
    }

    @NonNull
    @Override
    public ItemCardViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        final Context context = viewGroup.getContext();

        maxAvatarCount = context.getResources().getInteger(R.integer.max_avatar_count);
        maxLabelsShown = context.getResources().getInteger(R.integer.max_labels_shown);
        maxLabelsChars = context.getResources().getInteger(R.integer.max_labels_chars);
        counterMaxValue = context.getString(R.string.counter_max_value);

        LayoutInflater layoutInflater = LayoutInflater.from(context);
        ItemCardBinding binding = ItemCardBinding.inflate(layoutInflater, viewGroup, false);
        return new ItemCardViewHolder(binding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ItemCardViewHolder viewHolder, int position) {
        final Context context = viewHolder.itemView.getContext();
        final FullCard card = cardList.get(position);

        viewHolder.binding.card.setOnClickListener((v) -> {
            if (selectCardListener == null) {
                Intent intent = new Intent(v.getContext(), EditActivity.class);
                intent.putExtra(BUNDLE_KEY_ACCOUNT_ID, card.getAccountId());
                intent.putExtra(BUNDLE_KEY_BOARD_ID, boardId);
                intent.putExtra(BUNDLE_KEY_LOCAL_ID, card.getLocalId());
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                selectCardListener.onCardSelected(card);
            }
        });
        if (canEdit && selectCardListener == null) {
            viewHolder.binding.card.setOnLongClickListener((v) -> {
                ClipData dragData = ClipData.newPlainText("cardid", card.getLocalId() + "");

                // Starts the drag
                v.startDrag(dragData,  // the data to be dragged
                        new View.DragShadowBuilder(v),  // the drag shadow builder
                        new DraggedItemLocalState<>(card, viewHolder.binding.card, this, position),      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );
                DeckLog.log("onLongClickListener");
                return true;
            });
            setupMoveMenu(card.getAccountId(), boardId);
        } else {
            viewHolder.binding.cardMenu.setVisibility(View.GONE);
        }
        viewHolder.binding.cardTitle.setText(card.getCard().getTitle());
        String description = card.getCard().getDescription();
        if (description != null && description.length() > 0) {
            viewHolder.binding.cardDescription.setText(card.getCard().getDescription());
            viewHolder.binding.cardDescription.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.cardDescription.setVisibility(View.GONE);
        }

        boolean showDetails = false;

        if (card.getAssignedUsers() != null && card.getAssignedUsers().size() > 0) {
            setupAvatars(viewHolder.binding.peopleList, card);
            viewHolder.binding.peopleList.setVisibility(View.VISIBLE);
            showDetails = true;
        } else {
            viewHolder.binding.peopleList.setVisibility(View.GONE);
        }

        viewHolder.binding.notSyncedYet.setVisibility(DBStatus.LOCAL_EDITED.equals(card.getStatusEnum()) ? View.VISIBLE : View.GONE);

        if (card.getCard().getDueDate() != null) {
            setupDueDate(viewHolder.binding.cardDueDate, card.getCard());
            viewHolder.binding.cardDueDate.setVisibility(View.VISIBLE);
            showDetails = true;
        } else {
            viewHolder.binding.cardDueDate.setVisibility(View.GONE);
        }

        final int attachmentsCount = card.getAttachments().size();

        if (attachmentsCount == 0) {
            viewHolder.binding.cardCountAttachments.setVisibility(View.GONE);
        } else {
            setupCounter(viewHolder.binding.cardCountAttachments, attachmentsCount);

            viewHolder.binding.cardCountAttachments.setVisibility(View.VISIBLE);
            showDetails = true;
        }

        final int commentsCount = card.getCommentCount();

        if (commentsCount == 0) {
            viewHolder.binding.cardCountComments.setVisibility(View.GONE);
        } else {
            setupCounter(viewHolder.binding.cardCountComments, commentsCount);

            viewHolder.binding.cardCountComments.setVisibility(View.VISIBLE);
            showDetails = true;
        }

        viewHolder.binding.labels.removeAllViews();
        if (card.getLabels() != null && card.getLabels().size() > 0) {
            setupLabels(viewHolder.binding.labels, card.getLabels());
            viewHolder.binding.labels.setVisibility(View.VISIBLE);
            showDetails = true;
        } else {
            viewHolder.binding.labels.setVisibility(View.GONE);
        }

        if (showDetails) {
            viewHolder.binding.cardDetailsContainer.setVisibility(View.VISIBLE);
        } else {
            viewHolder.binding.cardDetailsContainer.setVisibility(View.GONE);
        }

        viewHolder.binding.cardMenu.setOnClickListener(v -> onOverflowIconClicked(v, card));
    }

    private void setupMoveMenu(long accountId, long boardId) {
        syncManager.getStacksForBoard(accountId, boardId).observe(lifecycleOwner, (stacks) -> availableStacks = stacks);
    }

    private void setupLabels(@NonNull ChipGroup labels, List<Label> labelList) {
        final Context context = labels.getContext();
        Chip chip;
        for (int i = 0; i < labelList.size(); i++) {
            if (i > maxLabelsShown - 1 && labelList.size() > maxLabelsShown) {
                chip = new Chip(context);
                chip.setChipIcon(ContextCompat.getDrawable(context, R.drawable.ic_more_horiz_black_24dp));
                chip.setEnsureMinTouchTargetSize(false);
                chip.setTextStartPadding(0);
                chip.setTextEndPadding(0);
                labels.addView(chip);
                break;
            }
            Label label = labelList.get(i);
            chip = new Chip(context);
            chip.setEnsureMinTouchTargetSize(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            }
            String labelTitle = label.getTitle();
            if (labelTitle.length() > maxLabelsChars - 1) {
                chip.setText(labelTitle.substring(0, maxLabelsChars));
            } else {
                chip.setText(labelTitle.substring(0, 1));
            }

            try {
                int labelColor = Color.parseColor("#" + label.getColor());
                ColorStateList c = ColorStateList.valueOf(labelColor);
                chip.setChipBackgroundColor(c);
                chip.setTextColor(ColorUtil.getForegroundColorForBackgroundColor(labelColor));
            } catch (IllegalArgumentException e) {
                DeckLog.logError(e);
            }

            labels.addView(chip);
        }
    }

    private void setupCounter(@NonNull TextView textView, int count) {
        if (count > 99) {
            textView.setText(counterMaxValue);
        } else if (count > 1) {
            textView.setText(String.valueOf(count));
        } else if (count == 1) {
            textView.setText("");
        }
    }

    private void setupDueDate(@NonNull TextView cardDueDate, Card card) {
        final Context context = cardDueDate.getContext();
        cardDueDate.setText(DateUtil.getRelativeDateTimeString(context, card.getDueDate().getTime()));
        ViewUtil.themeDueDate(context, cardDueDate, card.getDueDate());
    }

    private void setupAvatars(@NonNull RelativeLayout peopleList, FullCard card) {
        final Context context = peopleList.getContext();
        int avatarSize = DimensionUtil.getAvatarDimension(context, R.dimen.avatar_size_small);
        peopleList.removeAllViews();
        RelativeLayout.LayoutParams avatarLayoutParams;
        int avatarCount;
        for (avatarCount = 0; avatarCount < card.getAssignedUsers().size() && avatarCount < maxAvatarCount; avatarCount++) {
            avatarLayoutParams = new RelativeLayout.LayoutParams(avatarSize, avatarSize);
            avatarLayoutParams.setMargins(0, 0, avatarCount * context.getResources().getDimensionPixelSize(R.dimen.avatar_overlapping_small), 0);
            avatarLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            ImageView avatar = new ImageView(context);
            avatar.setLayoutParams(avatarLayoutParams);
            peopleList.addView(avatar);
            avatar.requestLayout();
            ViewUtil.addAvatar(context, avatar, account.getUrl(), card.getAssignedUsers().get(avatarCount).getUid(), avatarSize, R.drawable.ic_person_grey600_24dp);
        }

        // Recalculate container size based on avatar count
        int size = context.getResources().getDimensionPixelSize(R.dimen.avatar_overlapping_small) * (avatarCount - 1) + avatarSize;
        ViewGroup.LayoutParams rememberParam = peopleList.getLayoutParams();
        rememberParam.width = size;
        peopleList.setLayoutParams(rememberParam);
    }

    @Override
    public int getItemCount() {
        return cardList == null ? 0 : cardList.size();
    }

    public void insertItem(FullCard fullCard, int position) {
        cardList.add(position, fullCard);
        notifyItemInserted(position);
    }

    @Override
    public List<FullCard> getItemList() {
        return this.cardList;
    }

    public void moveItem(int fromPosition, int toPosition) {
        cardList.add(toPosition, cardList.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }

    public void removeItem(int position) {
        cardList.remove(position);
        notifyItemRemoved(position);
    }

    private void onOverflowIconClicked(View view, FullCard card) {
        final Context context = view.getContext();
        PopupMenu popup = new PopupMenu(context, view);
        popup.inflate(R.menu.card_menu);
        prepareOptionsMenu(popup.getMenu(), card);

        popup.setOnMenuItemClickListener(item -> optionsItemSelected(context, item, card));
        popup.show();
    }

    private void prepareOptionsMenu(Menu menu, FullCard card) {
        if (containsUser(card.getAssignedUsers(), account.getUserName())) {
            menu.removeItem(menu.findItem(R.id.action_card_assign).getItemId());
        } else {
            menu.removeItem(menu.findItem(R.id.action_card_unassign).getItemId());
        }
    }

    public void setCardList(@NonNull List<FullCard> cardList) {
        this.cardList.clear();
        this.cardList.addAll(cardList);
        notifyDataSetChanged();
    }

    private boolean containsUser(List<User> userList, String username) {
        if (userList != null) {
            for (User user : userList) {
                if (user.getPrimaryKey().equals(username)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean optionsItemSelected(Context context, MenuItem item, FullCard card) {
        switch (item.getItemId()) {
            case R.id.action_card_assign: {
                new Thread(() -> syncManager.assignUserToCard(syncManager.getUserByUidDirectly(card.getCard().getAccountId(), account.getUserName()), card.getCard())).start();
                return true;
            }
            case R.id.action_card_unassign: {
                new Thread(() -> syncManager.unassignUserFromCard(syncManager.getUserByUidDirectly(card.getCard().getAccountId(), account.getUserName()), card.getCard())).start();
                return true;
            }
            case R.id.action_card_move: {
                int currentStackItem = 0;
                CharSequence[] items = new CharSequence[availableStacks.size()];
                for (int i = 0; i < availableStacks.size(); i++) {
                    final Stack stack = availableStacks.get(i).getStack();
                    items[i] = stack.getTitle();
                    if (stack.getLocalId().equals(stackId)) {
                        currentStackItem = i;
                    }
                }
                final FullCard newCard = card;
                new AlertDialog.Builder(context)
                        .setSingleChoiceItems(items, currentStackItem, (dialog, which) -> {
                            dialog.cancel();
                            newCard.getCard().setStackId(availableStacks.get(which).getStack().getLocalId());
                            LiveDataHelper.observeOnce(syncManager.updateCard(newCard), lifecycleOwner, (c) -> {
                                // Nothing to do here...
                            });
                            DeckLog.log("Moved card \"" + card.getCard().getTitle() + "\" to \"" + availableStacks.get(which).getStack().getTitle() + "\"");
                        })
                        .setNegativeButton(android.R.string.cancel, null)
                        .setTitle(context.getString(R.string.action_card_move_title, card.getCard().getTitle()))
                        .show();
                return true;
            }
            case R.id.action_card_archive: {
                syncManager.archiveCard(card);
                return true;
            }
            case R.id.action_card_delete: {
                syncManager.deleteCard(card.getCard());
                return true;
            }
        }
        return true;
    }
}
