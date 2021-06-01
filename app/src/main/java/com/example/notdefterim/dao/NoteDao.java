package com.example.notdefterim.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.notdefterim.entities.Notes;

import java.util.List;

@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Notes> AllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(Notes notes);

    @Delete
    void deleteNote(Notes notes);

    //@Query("SELECT * FROM notes WHERE title LIKE:key OR note_text LIKE:key")
    //List<Notes> searchDatabase(String key);


}
