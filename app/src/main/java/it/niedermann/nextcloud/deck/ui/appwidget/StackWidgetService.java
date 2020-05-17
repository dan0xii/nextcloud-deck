package it.niedermann.nextcloud.deck.ui.appwidget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackWidgetFactory(this.getApplicationContext(), intent);
    }
}
