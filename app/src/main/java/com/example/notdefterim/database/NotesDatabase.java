package com.example.notdefterim.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.notdefterim.dao.NoteDao;
import com.example.notdefterim.entities.Notes;

@Database(entities = Notes.class,version = 1,exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {
    private static NotesDatabase notesDatabase;
    public static synchronized NotesDatabase getNotesDatabase(Context context){
        if(notesDatabase==null){
            notesDatabase= Room.databaseBuilder(
                    context,
                    NotesDatabase.class,
                    "notes_db"
            ).build();
        }
        return notesDatabase;
    }

    public abstract NoteDao noteDao();
}
