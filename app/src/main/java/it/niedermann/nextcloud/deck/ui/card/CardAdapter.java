package it.niedermann.nextcloud.deck.ui.card;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.card.MaterialCardView;
import android.support.design.chip.Chip;
import android.support.design.chip.ChipGroup;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.nextcloud.deck.R;
import it.niedermann.nextcloud.deck.model.Card;
import it.niedermann.nextcloud.deck.model.Label;

public class CardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Card> cardList = new ArrayList<>();

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int position) {
        this.context = viewGroup.getContext();
        View v = LayoutInflater.from(this.context).inflate(R.layout.fragment_card, viewGroup, false);
        return new CardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        Card card = cardList.get(position);
        ((CardViewHolder) viewHolder).cardTitle.setText(card.getTitle());
        ((CardViewHolder) viewHolder).cardDescription.setText(card.getDescription());
        Chip chip;
        for(Label label: card.getLabels()) {
            chip = new Chip(context);
            chip.setText(label.getTitle());
            ((CardViewHolder) viewHolder).labels.addView(chip);
        }
    }

    @Override
    public int getItemCount() {
        return cardList == null ? 0 : cardList.size();
    }

    public void setCardList(@NonNull List<Card> cardList) {
        this.cardList = cardList;
        notifyDataSetChanged();
    }

    public void moveItem(int fromPosition, int toPosition) {
        cardList.add(toPosition, cardList.remove(fromPosition));
        notifyItemMoved(fromPosition, toPosition);
    }

    static class CardViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card)
        MaterialCardView card;
        @BindView(R.id.card_title)
        TextView cardTitle;
        @BindView(R.id.card_description)
        TextView cardDescription;
        @BindView(R.id.labels)
        ChipGroup labels;

        private CardViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            card.setOnClickListener((View clickedView) -> {
                if (Build.VERSION.SDK_INT >= 16) {
                    cardDescription.setMaxLines(cardDescription.getMaxLines() == 3 ? Integer.MAX_VALUE : 3);
                }
            });
            card.setOnLongClickListener((View draggedView) -> {

                // Create a new ClipData.
                // This is done in two steps to provide clarity. The convenience method
                // ClipData.newPlainText() can create a plain text ClipData in one step.

                // Create a new ClipData.Item from the ImageView object's tag
                ClipData dragData = ClipData.newPlainText("TEST", "TEST2");

                // Starts the drag
                draggedView.startDrag(dragData,  // the data to be dragged
                        new View.DragShadowBuilder(draggedView),  // the drag shadow builder
                        draggedView,      // no need to use local data
                        0          // flags (not currently used, set to 0)
                );
                view.setVisibility(View.INVISIBLE);
                Log.v("Deck", "onLongClickListener");
                return true;
            });
        }
    }
}
