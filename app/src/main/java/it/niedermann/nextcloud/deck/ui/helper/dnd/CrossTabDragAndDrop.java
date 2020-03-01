package it.niedermann.nextcloud.deck.ui.helper.dnd;

import android.app.Activity;
import android.graphics.Point;
import android.view.DragEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import it.niedermann.nextcloud.deck.DeckLog;
import it.niedermann.nextcloud.deck.R;
import it.niedermann.nextcloud.deck.model.full.FullCard;
import it.niedermann.nextcloud.deck.ui.card.CardAdapter;
import it.niedermann.nextcloud.deck.ui.stack.StackAdapter;

public class CrossTabDragAndDrop {

    private static final ScrollHelper SCROLL_HELPER = new ScrollHelper();

    public interface CardMovedByDragListener {
        void onCardMoved(FullCard movedCard, long stackId, int position);
    }

    private final Activity activity;
    private final float pxToReact;
    private long lastSwap = 0;
    private long lastMove = 0;

    private final float pxToReactTopBottom;

    private final Set<CardMovedByDragListener> moveListenerList = new HashSet<>(1);

    public CrossTabDragAndDrop(Activity activity) {
        this.activity = activity;
        final float density = activity.getResources().getDisplayMetrics().density;
        this.pxToReact = activity.getResources().getInteger(R.integer.drag_n_drop_dp_to_react) * density;
        this.pxToReactTopBottom = activity.getResources().getInteger(R.integer.drag_n_drop_dp_to_react_top_bottom) * density;
    }

    public void register(final ViewPager viewPager, TabLayout stackLayout) {
        viewPager.setOnDragListener((View v, DragEvent dragEvent) -> {

            DraggedCardLocalState draggedCardLocalState = (DraggedCardLocalState) dragEvent.getLocalState();
            CardView cardView = draggedCardLocalState.getDraggedView();
            switch (dragEvent.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED: {
                    cardView.setVisibility(View.INVISIBLE);
                    draggedCardLocalState.onDragStart(viewPager);
                    break;
                }
                case DragEvent.ACTION_DRAG_LOCATION: {
                    RecyclerView currentRecyclerView = draggedCardLocalState.getRecyclerView();
                    CardAdapter cardAdapter = draggedCardLocalState.getCardAdapter();

                    Point size = new Point();
                    activity.getWindowManager().getDefaultDisplay().getSize(size);

                    long now = System.currentTimeMillis();
                    if (lastSwap + activity.getResources().getInteger(R.integer.drag_n_drop_ms_to_react) < now) { // don't change Tabs so fast!
                        int oldTabPosition = viewPager.getCurrentItem();

                        boolean shouldSwitchTab = true;

                        int newTabPosition = -1;
                        // change tab? if yes, which direction?
                        if (dragEvent.getX() <= pxToReact) {
                            newTabPosition = oldTabPosition - 1;
                        } else if (dragEvent.getX() >= size.x - pxToReact) {
                            newTabPosition = oldTabPosition + 1;
                        } else {
                            shouldSwitchTab = false;
                        }

                        if (shouldSwitchTab && isMovePossible(viewPager, newTabPosition)) {
                            removeItem(currentRecyclerView, cardView, cardAdapter);
                            detectAndKillDuplicatesInNeighbourTab(viewPager, draggedCardLocalState.getDraggedCard(), oldTabPosition, newTabPosition);
                            switchTab(viewPager, stackLayout, draggedCardLocalState, now, newTabPosition);
                            return true;
                        }
                    }

                    //scroll if needed
                    if (dragEvent.getY() <= pxToReactTopBottom) {
                        SCROLL_HELPER.startScroll(currentRecyclerView, ScrollHelper.ScrollDirection.UP);
                    } else if (dragEvent.getY() >= currentRecyclerView.getBottom() - pxToReactTopBottom) {
                        SCROLL_HELPER.startScroll(currentRecyclerView, ScrollHelper.ScrollDirection.DOWN);
                    } else {
                        SCROLL_HELPER.stopScroll();
                    }


                    if (lastMove + activity.getResources().getInteger(R.integer.drag_n_drop_dp_to_react_top_bottom) < now) {
                        //push around the other cards
                        View viewUnder = currentRecyclerView.findChildViewUnder(dragEvent.getX(), dragEvent.getY());

                        if (viewUnder != null) {
                            int toPositon = currentRecyclerView.getChildAdapterPosition(viewUnder);
                            if (toPositon != -1) {
//                                DeckLog.log("dnd childUnder: "+((TextView)((LinearLayout)((LinearLayout)((CardView) viewUnder).getChildAt(0)).getChildAt(0)).getChildAt(0)).getText());
                                int fromPosition = currentRecyclerView.getChildAdapterPosition(cardView);
                                if (fromPosition != -1 && fromPosition != toPositon) {
                                    cardAdapter.moveItem(fromPosition, toPositon);
                                    draggedCardLocalState.setPositionInCardAdapter(toPositon);
                                    lastMove = now;
                                }
                            }
                        }
                    }
                    break;
                }
                case DragEvent.ACTION_DROP: {
                    SCROLL_HELPER.stopScroll();
                    cardView.setVisibility(View.VISIBLE);
                    notifyListeners(draggedCardLocalState);
                    break;
                }
            }
            return true;
        });
    }

    private void switchTab(ViewPager viewPager, TabLayout stackLayout, final DraggedCardLocalState draggedCardLocalState, long now, int newPosition) {
        draggedCardLocalState.onTabChanged(viewPager, newPosition);
        viewPager.setCurrentItem(newPosition);
        Objects.requireNonNull(stackLayout.getTabAt(newPosition)).select();

        final RecyclerView recyclerView = draggedCardLocalState.getRecyclerView();
        CardAdapter cardAdapter = draggedCardLocalState.getCardAdapter();

        //insert card in new tab
        View firstVisibleView = recyclerView.getChildAt(0);
        int positionToInsert = firstVisibleView == null ? 0 : recyclerView.getChildAdapterPosition(firstVisibleView) + 1;

        cardAdapter.addItem(draggedCardLocalState.getDraggedCard(), positionToInsert);

        RecyclerView.OnChildAttachStateChangeListener onChildAttachStateChangeListener = new RecyclerView.OnChildAttachStateChangeListener() {
            @Override
            public void onChildViewAttachedToWindow(@NonNull View view) {
                recyclerView.removeOnChildAttachStateChangeListener(this);
                draggedCardLocalState.setInsertedListener(null);
                CardView cardView = (CardView) view;
                cardView.setVisibility(View.INVISIBLE);
                draggedCardLocalState.setDraggedView(cardView);
            }

            @Override
            public void onChildViewDetachedFromWindow(@NonNull View view) {/* do nothing */}
        };
        draggedCardLocalState.setInsertedListener(onChildAttachStateChangeListener);
        recyclerView.addOnChildAttachStateChangeListener(onChildAttachStateChangeListener);

        cardAdapter.notifyItemInserted(positionToInsert);
        cardAdapter.notifyItemChanged(positionToInsert);

        lastSwap = now;
    }

    private static boolean isMovePossible(ViewPager viewPager, int newPosition) {
        return newPosition < Objects.requireNonNull(viewPager.getAdapter()).getCount() && newPosition >= 0;
    }

    private void detectAndKillDuplicatesInNeighbourTab(ViewPager viewPager, FullCard cardToFind, int oldTabPosition, int newTabPosition) {
        int tabPositionToCheck = newTabPosition > oldTabPosition ? newTabPosition + 1 : newTabPosition - 1;

        if (isMovePossible(viewPager, tabPositionToCheck)) {
            viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    viewPager.removeOnPageChangeListener(this);
                    StackAdapter stackAdapter = (StackAdapter) viewPager.getAdapter();

                    DeckLog.log("### tabPositionToCheck: " + tabPositionToCheck);
                    DeckLog.log("### item: " + stackAdapter.getItem(tabPositionToCheck).getAdapter());
                    CardAdapter cardAdapter = stackAdapter.getItem(tabPositionToCheck).getAdapter();
                    cardAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                        @Override
                        public void onChanged() {
                            super.onChanged();
                            cardAdapter.unregisterAdapterDataObserver(this);
                            List<FullCard> cardList = cardAdapter.getCardList();
                            for (int i = 0; i < cardList.size(); i++) {
                                FullCard c = cardList.get(i);
                                if (cardToFind.getCard().getLocalId().equals(c.getLocalId())) {
                                    cardAdapter.removeItem(i);
                                    cardAdapter.notifyItemRemoved(i);
//                                    DeckLog.log("dnd removed dupe at tab "+tabPositionToCheck+": "+c.getCard().getTitle());
                                    break;
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    private static void removeItem(RecyclerView currentRecyclerView, CardView cardView, CardAdapter cardAdapter) {
        int oldCardPosition = currentRecyclerView.getChildAdapterPosition(cardView);

        if (oldCardPosition != -1) {
            cardAdapter.removeItem(oldCardPosition);
        }
    }

    private void notifyListeners(DraggedCardLocalState draggedCardLocalState) {
        for (CardMovedByDragListener listener : moveListenerList) {
            listener.onCardMoved(draggedCardLocalState.getDraggedCard(), draggedCardLocalState.getCurrentStackId(), draggedCardLocalState.getPositionInCardAdapter());
        }
    }

    public void addCardMovedByDragListener(CardMovedByDragListener listener) {
        moveListenerList.add(listener);
    }

    public void removeCardMovedByDragListener(CardMovedByDragListener listener) {
        moveListenerList.remove(listener);
    }
}
