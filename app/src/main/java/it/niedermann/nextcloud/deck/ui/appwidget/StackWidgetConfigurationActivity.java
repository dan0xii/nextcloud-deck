package it.niedermann.nextcloud.deck.ui.appwidget;

import android.os.Bundle;
import android.util.Log;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.lifecycle.LiveData;

import java.util.List;

import it.niedermann.nextcloud.deck.R;
import it.niedermann.nextcloud.deck.model.Account;
import it.niedermann.nextcloud.deck.model.Board;
import it.niedermann.nextcloud.deck.persistence.sync.SyncManager;
import it.niedermann.nextcloud.deck.persistence.sync.adapters.db.DataBaseAdapter;
import it.niedermann.nextcloud.deck.ui.board.BoardAdapter;

public class StackWidgetConfigurationActivity extends AppCompatActivity {
    // todo
    // stackId 5
    // boardId 0
    // accountId 0
    private DataBaseAdapter dba;
    LiveData<List<Board>> b;
    private SyncManager syncManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);
        setContentView(R.layout.widget_stack_configuration);
        Log.d("TAG", "onCreate: ");
        AppCompatSpinner boardSpinner = (AppCompatSpinner) findViewById(R.id.board_spinner2);
        dba = new DataBaseAdapter(getApplicationContext());
        syncManager = new SyncManager(this);

        syncManager.readAccount(0).observe(this, (Account account) -> {
            Log.d("TAG", "onCreate: got here");
            syncManager.getBoards(0).observe(this, (List<Board> boardsList) -> {
                Board[] boardsArray = new Board[boardsList.size()];
                boardsArray = boardsList.toArray(boardsArray);
                Log.d("config", "onCreate: boardsArray " + boardsArray[0].getTitle());
                SpinnerAdapter adapter = new BoardAdapter(this, boardsArray);

                boardSpinner.setAdapter(adapter);
            });
        });
    }
}
