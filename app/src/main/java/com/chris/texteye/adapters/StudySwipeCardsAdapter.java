package com.chris.texteye.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.chris.texteye.R;
import com.chris.texteye.database.Entities.Flashcard;

import java.util.ArrayList;
import java.util.List;


public class StudySwipeCardsAdapter extends BaseAdapter {

    private List<Flashcard> flashcards = new ArrayList<>();
    private Context context;

    public StudySwipeCardsAdapter(Context context) {
        this.context = context;
    }

    public void pop_front(){
        flashcards.remove(0);
    }

    public void setFlashcards(List<Flashcard> flashcards) {
        this.flashcards = flashcards;
        notifyDataSetChanged();
    }

    public Flashcard getCurrentFlashcard(){
        if(flashcards.size() > 0)
            return flashcards.get(0);

        return null;
    }

    @Override
    public int getCount() {
        return flashcards.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        public FrameLayout background;
        private TextView wordTextView;
        private TextView sentenceTextView;

    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        ViewHolder viewHolder = new ViewHolder();
        if (rowView == null) {

            LayoutInflater inflater = (LayoutInflater) context.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
            rowView = inflater.inflate(R.layout.flashcard, parent, false);
            // configure view holder
            viewHolder.wordTextView = rowView.findViewById(R.id.flashcard_text);
            viewHolder.background = rowView.findViewById(R.id.background);
            viewHolder.sentenceTextView = rowView.findViewById(R.id.flashcard_sentence);
            rowView.setTag(viewHolder);

        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if(viewHolder != null && viewHolder.wordTextView != null && viewHolder.sentenceTextView != null) {
            viewHolder.wordTextView.setText(flashcards.get(position).getEngText());
            viewHolder.sentenceTextView.setText(flashcards.get(position).getEngSentence());
        }

        return rowView;
    }
}
