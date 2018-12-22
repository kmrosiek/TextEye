package com.chris.texteye.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.chris.texteye.database.Entities.Deck;
import com.chris.texteye.R;

import java.util.ArrayList;
import java.util.List;

public class MainMenuRecycViewAdapter extends RecyclerView.Adapter<MainMenuRecycViewAdapter.ListHolder> {

    private List<Deck> decks = new ArrayList<>();
    private OnDeckClickListener listener;

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.main_menu_deck_item, parent, false);
        return new ListHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ListHolder holder, int position) {
        Deck deck = decks.get(position);
        holder.nameTextView.setText(deck.getName());
    }

    @Override
    public int getItemCount() {
        return decks.size();
    }

    public void setDecks(List<Deck> notes) {
        this.decks = notes;
        notifyDataSetChanged();
    }

    class ListHolder extends RecyclerView.ViewHolder{

        private TextView nameTextView;

        private ListHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.main_menu_list_item_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if(listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onDeckClick(decks.get(position));
                    }
                }
            });
        }
    }

    public interface OnDeckClickListener {

        void onDeckClick(Deck deck);
    }

    public void setOnDeckClickListener(OnDeckClickListener listener) {
        this.listener = listener;
    }
}
