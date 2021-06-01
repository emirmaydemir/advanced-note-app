package com.example.notdefterim.adapter;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.notdefterim.R;
import com.example.notdefterim.entities.Notes;
import com.example.notdefterim.listener.NotesListener;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder>{

    private List<Notes>notes;
    private NotesListener notesListener;
    private Timer timer;
    private List<Notes> notesSource;

    public NoteAdapter(List<Notes> notes, NotesListener notesListener) {
        this.notes = notes;
        this.notesListener=notesListener;
        notesSource=notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout,parent,false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNotes(notes.get(position));
        holder.layout_notes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notesListener.onNoteClicked(notes.get(position),notesSource.indexOf(notes.get(position)));
                //searchNotes("");
            }
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder{
        TextView txt_title,txt_content,txt_date;
        LinearLayout layout_notes;
        RoundedImageView imageNote;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_title=itemView.findViewById(R.id.txt_title);
            txt_content=itemView.findViewById(R.id.txt_content);
            txt_date=itemView.findViewById(R.id.txt_date);
            layout_notes=itemView.findViewById(R.id.layoutnotes);
            imageNote=itemView.findViewById(R.id.imageNote);
        }

        void setNotes(Notes notes){
            txt_title.setText(notes.getTitle());
            if(notes.getNoteText().trim().isEmpty()){
                txt_content.setVisibility(View.GONE);
            }
            else{
                txt_content.setText(notes.getNoteText());
            }
            txt_date.setText(notes.getDate());

            GradientDrawable gradientDrawable=(GradientDrawable) layout_notes.getBackground();
            if(notes.getColor() != null){
                gradientDrawable.setColor(Color.parseColor(notes.getColor()));
            }
            else{
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if(notes.getImagePath() != null){
                imageNote.setImageBitmap(BitmapFactory.decodeFile(notes.getImagePath()));
                imageNote.setVisibility(View.VISIBLE);
            }
            else {
                imageNote.setVisibility(View.GONE);
            }

        }

    }

    public void searchNotes(final String searchKey){
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if(searchKey.trim().isEmpty()){
                    notes=notesSource;
                }
                else{
                    ArrayList<Notes> sought_note= new ArrayList<>();
                    for(Notes notes:notesSource){
                        if(notes.getTitle().toLowerCase().contains(searchKey.toLowerCase())
                        || notes.getNoteText().toLowerCase().contains(searchKey.toLowerCase())){
                            sought_note.add(notes);
                        }
                    }
                        notes=sought_note;
                }
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                       notifyDataSetChanged();
                    }
                });

            }
        },500);
    }

    public void cancelTimer(){
        if( timer != null){
            timer.cancel();
        }
    }

}
