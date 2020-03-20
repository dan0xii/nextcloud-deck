package it.niedermann.nextcloud.deck.ui.appwidget;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import it.niedermann.nextcloud.deck.R;
import it.niedermann.nextcloud.deck.model.full.FullStack;
import it.niedermann.nextcloud.deck.persistence.sync.adapters.db.DataBaseAdapter;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class StackWidgetFactory implements RemoteViewsService.RemoteViewsFactory {
    private final Context context;
    private final Intent intent;
    private FullStack stack;
    DataBaseAdapter dba;

    StackWidgetFactory(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    @Override
    public void onCreate() {
        dba = new DataBaseAdapter(context);

    }

    @Override
    public void onDataSetChanged() {
//        todo
        stack = dba.getFullStackByLocalIdDirectly(5);
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        Log.d(TAG, "getCount: " + stack.getCards().size());
        return stack == null ? 0 : stack.getCards().size();
    }

    @Override
    public RemoteViews getViewAt(int i) {
        RemoteViews cardView;

        if (stack == null) {
            return null;
        }

        Log.d(TAG, "getViewAt: " + stack.cards.get(i).getTitle());
        String title = stack.cards.get(i).getTitle();
        cardView = new RemoteViews(context.getPackageName(), R.layout.widget_stack_entry);
        cardView.setTextViewText(R.id.widget_entry_content_tv, title);
        return cardView;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}
