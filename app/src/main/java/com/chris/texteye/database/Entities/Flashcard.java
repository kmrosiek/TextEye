package com.chris.texteye.database.Entities;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "flashcard_table", foreignKeys = @ForeignKey(entity = Deck.class,
parentColumns = "id", childColumns = "list_id", onDelete = ForeignKey.CASCADE), indices = @Index("list_id"))
public class Flashcard {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "list_id")
    private int listId;

    /* The front of a flashcard*/
    private String engText;
    private String engSentence;
    /* The back of a flashcard*/
    private String polText;
    private String polSentence;

    /** Metadata*/
    private boolean dictionaryChecked;
    private boolean misspelled;

    private int round;
    private int correctAnswers;

    @Ignore
    public static boolean ALREADY_CHECKED_IN_DICTIONARY = true;
    public static boolean TEXT_IS_MISSPELLED = true;

    public Flashcard(final int listId, final String engText, final String polText,
                     final String engSentence, final String polSentence) {
        this.listId = listId;
        this.engText = engText;
        this.polText = polText;
        this.engSentence = engSentence;
        this.polSentence = polSentence;
        this.dictionaryChecked = true;
    }

    @Ignore
    public Flashcard(final int listId, final String engText) {
        this.listId = listId;
        this.engText = engText;
        this.dictionaryChecked = false;
    }

    @Ignore
    public Flashcard(final int deckId, final String engText, final boolean dicCheck, final boolean misspelled) {
        this.listId = deckId;
        this.engText = engText;
        this.dictionaryChecked = dicCheck;
        this.misspelled = misspelled;
    }

    public int getRound() {
        return round;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getListId() {
        return listId;
    }

    public long getId() {
        return id;
    }

    public String getEngText() {
        return engText;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPolText() {
        return polText;
    }

    public String getEngSentence() {
        return engSentence;
    }

    public String getPolSentence() {
        return polSentence;
    }

    public void setListId(int listId) {
        this.listId = listId;
    }

    public void setEngText(String engText) {
        this.engText = engText;
    }

    public void setEngSentence(String engSentence) {
        this.engSentence = engSentence;
    }

    public void setPolText(String polText) {
        this.polText = polText;
    }

    public void setPolSentence(String polSentence) {
        this.polSentence = polSentence;
    }

    public boolean isDictionaryChecked() {
        return dictionaryChecked;
    }

    public boolean isMisspelled() {
        return misspelled;
    }

    public void setDictionaryChecked(boolean dictionaryChecked) {
        this.dictionaryChecked = dictionaryChecked;
    }

    public void setRound(int round) {
        this.round = round;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void setMisspelled(boolean misspelled) {
        this.misspelled = misspelled;
    }
}
