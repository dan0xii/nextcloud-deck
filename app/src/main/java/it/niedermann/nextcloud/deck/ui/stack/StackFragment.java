package it.niedermann.nextcloud.deck.ui.stack;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.niedermann.nextcloud.deck.R;
import it.niedermann.nextcloud.deck.api.IResponseCallback;
import it.niedermann.nextcloud.deck.model.Card;
import it.niedermann.nextcloud.deck.persistence.sync.SyncManager;
import it.niedermann.nextcloud.deck.ui.card.CardAdapter;

public class StackFragment extends Fragment {

    private long id = 0;
    private CardAdapter adapter = null;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    /**
     * @see <a href="https://gunhansancar.com/best-practice-to-instantiate-fragments-with-arguments-in-android/">Best Practice to Instantiate Fragments with Arguments in Android</a>
     * @param id of the current stack
     * @return new fragment instance
     */
    public static StackFragment newInstance(long id) {
        Bundle bundle = new Bundle();
        bundle.putLong("id", id);

        StackFragment fragment = new StackFragment();
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stack, container, false);
        ButterKnife.bind(this, view);
        initRecyclerView();
        if(savedInstanceState != null) {
            this.id = savedInstanceState.getLong("id");
        }
        SyncManager syncManager = new SyncManager(getActivity().getApplicationContext(), getActivity());
        syncManager.getCards(0, id, new IResponseCallback<List<Card>>() {
            @Override
            public void onResponse(List<Card> response) {
                adapter.setCardList(response);
            }

            @Override
            public void onError(Throwable throwable) {
                Log.e("Deck", throwable.getMessage());
                throwable.printStackTrace();
            }
        });
        return view ;
    }

    private void initRecyclerView() {
        adapter = new CardAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        ItemTouchHelper touchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                adapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                /* silence is gold. */
            }
        });

        touchHelper.attachToRecyclerView(recyclerView);
    }
}