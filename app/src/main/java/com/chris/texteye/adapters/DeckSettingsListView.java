package com.chris.texteye.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.chris.texteye.R;
import com.chris.texteye.database.Entities.Flashcard;

import java.util.List;

public class DeckSettingsListView extends ArrayAdapter<Flashcard> {


    public DeckSettingsListView(@NonNull Context context, List<Flashcard> incoming_words) {
        super(context, R.layout.deck_settings_item, incoming_words);
        this.context = context;
        this.words = incoming_words;
    }

    public void setDeck(List<Flashcard> flashcardFronts) {
        this.words = flashcardFronts;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if(words == null)
            return 0;

        return words.size();
    }

    public int getIdByPosition(final int position) {
        return (int)words.get(position).getId();
    }
    public String getNameByPosition(final int position) {
        return words.get(position).getEngText();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.deck_settings_item, null);
        TextView text_list_child = convertView.findViewById(R.id.english_word_from_list);
        text_list_child.setText(words.get(position).getEngText());
        ImageView translatedIcon = convertView.findViewById(R.id.deck_set_translated_icon);
        if(!words.get(position).isDictionaryChecked())
            translatedIcon.setImageResource(R.drawable.ic_file_download_blue_36dp);
        else if(words.get(position).isMisspelled())
            translatedIcon.setImageResource(R.drawable.ic_close_red_36dp);
        else
            translatedIcon.setImageResource(R.drawable.ic_check_green_36dp);

        if(resetBackground)
            convertView.setBackgroundColor(Color.TRANSPARENT);

        return convertView;
    }

    public void resetItemsBackgrounds() {
        resetBackground = true;
        notifyDataSetChanged();
        resetBackground = false;
    }

    private List<Flashcard> words;
    private Context context;
    private boolean resetBackground = false;
}
