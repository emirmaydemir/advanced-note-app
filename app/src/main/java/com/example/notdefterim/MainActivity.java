package com.example.notdefterim;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.example.notdefterim.adapter.NoteAdapter;
import com.example.notdefterim.database.NotesDatabase;
import com.example.notdefterim.entities.Notes;
import com.example.notdefterim.listener.NotesListener;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NotesListener {
    public static final int request_addnote=1;
    public static final int request_updatenote=2;
    public static final int request_showallnotes=3;
    private EditText enterSearch;
    private RecyclerView Notes_rec;
    private List<Notes>notesList;
    private NoteAdapter noteAdapter;

    private int notePosition=-1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView addnote=findViewById(R.id.btn_addnote);
        addnote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(),CreateNote.class),request_addnote);
            }
        });
        Notes_rec=findViewById(R.id.rec_notes);
        Notes_rec.setLayoutManager(
                new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        );

        notesList=new ArrayList<>();
        noteAdapter=new NoteAdapter(notesList,this);
        Notes_rec.setAdapter(noteAdapter);

        gNotes(request_showallnotes, false);

        enterSearch=findViewById(R.id.entersearch);
        enterSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(notesList.size() != 0){
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });

    }

    @Override
    public void onNoteClicked(Notes notes, int position) {
        enterSearch.setText(null);
        notePosition=position;
        Intent intent=new Intent(getApplicationContext(), CreateNote.class);
        intent.putExtra("vieworup", true);
        intent.putExtra("note",notes);
        startActivityForResult(intent, request_updatenote);
    }

    private void gNotes(final int request_code, final boolean noteDeleted){
        @SuppressLint("StaticFieldLeak")
        class gNotes extends AsyncTask<Void, Void, List<Notes>> {
            @Override
            protected List<Notes> doInBackground(Void... voids) {
                return NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().AllNotes();
            }
            @Override
            protected void onPostExecute(List<Notes> notes) {
                super.onPostExecute(notes);
                if(request_code == request_showallnotes){
                    notesList.addAll(notes);
                    noteAdapter.notifyDataSetChanged();
                }
                else if(request_code == request_addnote){
                    notesList.add(0,notes.get(0));
                    noteAdapter.notifyItemInserted(0);
                    Notes_rec.smoothScrollToPosition(0);
                }
                else if(request_code == request_updatenote){
                    notesList.remove(notePosition);
                    if(noteDeleted){
                        noteAdapter.notifyItemRemoved(notePosition);
                    }
                    else{
                        notesList.add(notePosition,notes.get(notePosition));
                        noteAdapter.notifyItemChanged(notePosition);
                    }

                }
            }
        }
        new gNotes().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==request_addnote && resultCode==RESULT_OK){
            gNotes(request_addnote,false);
        }
        else if(requestCode==request_updatenote && resultCode==RESULT_OK){
            if(data != null){
                gNotes(request_updatenote, data.getBooleanExtra("note_deleted", false));
            }
        }
    }
}