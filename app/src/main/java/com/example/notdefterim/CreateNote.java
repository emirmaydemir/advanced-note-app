package com.example.notdefterim;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PatternMatcher;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notdefterim.database.NotesDatabase;
import com.example.notdefterim.entities.Notes;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

public class CreateNote extends AppCompatActivity {

    private EditText enter_title,enter_text;
    private TextView txt_date,txt_web_link,txt_name_voice;
    private CoordinatorLayout layout_create_note;
    private ImageView image_note,delete_web_link,delete_image,delete_note,recorder_remove;
    private LinearLayout layout_web_link,layout_voice_recorder;
    private AlertDialog dialog_add_link, dialog_add_voice;
    private AlertDialog dialog_remove_notes;
    boolean isBold=true; boolean isIta=true; boolean isLine=true;
    Typeface face;
    Typeface face1;
    Typeface face2;
    Typeface face3;


    private String selectedColor, selectedFont;
    private String selectedImagePath;

    private static String fileName;
    public static String fileName2;
    private MediaRecorder mediaRecorder;
    boolean isRecord;
    boolean inComing=true;

    File path=new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/VRecorder/");


    private static final int REQUEST_CODE_STORAGE_PERMISSION=1;
    private static final int REQUEST_CODE_IMAGE_SELECTED=2;
    private static final int REQUEST_CODE_RECORDER=3;

    private Notes available_note, available_note2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        ImageView imageback=findViewById(R.id.imageback);
        imageback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        enter_title=findViewById(R.id.enternotetitle);
        enter_text=findViewById(R.id.enternote);
        txt_date=findViewById(R.id.datetime);
        layout_create_note=findViewById(R.id.layout_create_note);
        image_note=findViewById(R.id.image_note);
        txt_web_link=findViewById(R.id.txt_web_link);
        face = ResourcesCompat.getFont(this,R.font.medreg);
        face1 = ResourcesCompat.getFont(this,R.font.bold);
        face2 = ResourcesCompat.getFont(this,R.font.ita);
        face3 = ResourcesCompat.getFont(this,R.font.light);
        layout_web_link=findViewById(R.id.layout_web_link);
        delete_web_link=findViewById(R.id.delete_web_link);
        delete_image=findViewById(R.id.delete_image);
        delete_note=findViewById(R.id.btn_delete);
        layout_voice_recorder=findViewById(R.id.voice_recorder_side);
        txt_name_voice=findViewById(R.id.txt_name_voice);
        recorder_remove=findViewById(R.id.image_recorder_removee);

        isRecord=false;

        txt_date.setText(
                new SimpleDateFormat("EEEE, dd MMMM yyyy HH:mm a", Locale.getDefault())
                        .format(new Date())
        );

        ImageView saveImage=findViewById(R.id.btn_save);
        saveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteSave();
            }
        });

        selectedColor="#333333";
        selectedFont="";
        selectedImagePath="";
        available_note2=new Notes();


        if(getIntent().getBooleanExtra("vieworup",false)){
            available_note = (Notes) getIntent().getSerializableExtra("note");
            available_note2=available_note;
            updateOrViewNote();
        }

        layout_voice_recorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File[] file=path.listFiles();
                for(File single: file){
                        if (single.getName().toLowerCase().endsWith(".amr") && single.getName().matches(available_note2.getVoice())) {
                            inComing=false;
                            Uri uri = FileProvider.getUriForFile(CreateNote.this, CreateNote.this.getApplicationContext().getPackageName() + ".provider", single);
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(uri, "audio/x-wav");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            CreateNote.this.startActivity(intent);
                        }

                    else if(single.getName().toLowerCase().endsWith(".amr") && single.getName().matches(txt_name_voice.getText().toString()) && available_note2.getVoice().isEmpty() ){
                        inComing=false;
                        Uri uri = FileProvider.getUriForFile(CreateNote.this,CreateNote.this.getApplicationContext().getPackageName() + ".provider", single);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "audio/x-wav");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        CreateNote.this.startActivity(intent);
                    }

                }

                if (file.length == 0 || inComing){
                    inComing = true;
                    Toast.makeText(getApplicationContext(),"Kayıt dosyası bulunamadı",Toast.LENGTH_SHORT).show();
                }

            }
        });

        delete_web_link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_web_link.setText(null);
                layout_web_link.setVisibility(View.GONE);
            }
        });

        delete_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                image_note.setImageBitmap(null);
                image_note.setVisibility(View.GONE);
                delete_image.setVisibility(View.GONE);
                selectedImagePath="";
            }
        });

        recorder_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_name_voice.setText(null);
                layout_voice_recorder.setVisibility(View.GONE);
            }
        });

        variation();

        setCreateLayoutColor();

    }

    private void updateOrViewNote(){
        enter_title.setText(available_note.getTitle());
        enter_text.setText(available_note.getNoteText());
        txt_date.setText(available_note.getDate());

        if(available_note.getImagePath() !=null && !available_note.getImagePath().trim().isEmpty()){
            image_note.setImageBitmap(BitmapFactory.decodeFile(available_note.getImagePath()));
            image_note.setVisibility(View.VISIBLE);
            delete_image.setVisibility(View.VISIBLE);
            selectedImagePath = available_note.getImagePath();
        }

        if(available_note.getWeb() != null && !available_note.getWeb().trim().isEmpty()){
            txt_web_link.setText(available_note.getWeb());
            layout_web_link.setVisibility(View.VISIBLE);
        }

        if (available_note.getVoice() != null && !available_note.getVoice().trim().isEmpty()){
            txt_name_voice.setText(available_note.getVoice());
            layout_voice_recorder.setVisibility(View.VISIBLE);
        }
    }


    private void noteSave(){
        if(enter_title.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Lütfen başlık bilgisini doldurunuz.",Toast.LENGTH_SHORT).show();
            return;
        }
        else if(enter_text.getText().toString().trim().isEmpty()){
            Toast.makeText(this,"Lütfen not içeriğini doldurunuz.",Toast.LENGTH_SHORT).show();
            return;
        }
        final Notes notes=new Notes();
        notes.setTitle(enter_title.getText().toString());
        notes.setNoteText(enter_text.getText().toString());
        notes.setDate(txt_date.getText().toString());
        notes.setColor(selectedColor);
        notes.setImagePath(selectedImagePath);
        notes.setFont(selectedFont);


        if (layout_voice_recorder.getVisibility() == View.VISIBLE) {
            notes.setVoice(txt_name_voice.getText().toString());
        }


        if(layout_web_link.getVisibility() == View.VISIBLE){
            notes.setWeb(txt_web_link.getText().toString());
        }

        if(available_note != null){
            notes.setId(available_note.getId()); // bizim veritabanında aynı id değerine sahip bir değer eklenince eski not kayboluyor ve yenisi yerine geçiyor öyle ayarlandı notdao da o yüzden eger bir nota tıklandıysa ve güncellemek için tike basıldıysa yeni not oluşturmak yerine eski notun yerine yenisini koyuyoruz bu kod parçacığı sayesinde
        }

        @SuppressLint("StaticFieldLeak")
        class SaveNoteTask extends AsyncTask<Void, Void, Void>{
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao().insertNote(notes);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent=new Intent();
                setResult(RESULT_OK,intent);
                finish();
            }
        }
        new SaveNoteTask().execute();
    }

    private void variation(){
        final LinearLayout layout_variation=findViewById(R.id.layout_variation);
        final BottomSheetBehavior<LinearLayout> bottomSheetBehavior=BottomSheetBehavior.from(layout_variation);
        layout_variation.findViewById(R.id.txt_variation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bottomSheetBehavior.getState()!=BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
                else{
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageC1= layout_variation.findViewById(R.id.imageC1);
        final ImageView imageC2= layout_variation.findViewById(R.id.imageC2);
        final ImageView imageC3= layout_variation.findViewById(R.id.imageC3);
        final ImageView imageC4= layout_variation.findViewById(R.id.imageC4);
        final ImageView imageC5= layout_variation.findViewById(R.id.imageC5);
        final ImageView imageC6= layout_variation.findViewById(R.id.imageC6);
        final ImageView imageC7= layout_variation.findViewById(R.id.imageC7);
        final ImageView imageC8= layout_variation.findViewById(R.id.imageC8);
        final ImageView imageC9= layout_variation.findViewById(R.id.imageC9);
        final ImageView imageC10= layout_variation.findViewById(R.id.imageC10);
        final ImageView imageC11= layout_variation.findViewById(R.id.imageC11);
        final ImageView imageC12= layout_variation.findViewById(R.id.imageC12);
        final ImageView imageC13= layout_variation.findViewById(R.id.imageC13);
        final ImageView imageC14= layout_variation.findViewById(R.id.imageC14);
        final ImageView imageC15= layout_variation.findViewById(R.id.imageC15);
        final ImageView imageC16= layout_variation.findViewById(R.id.imageC16);
        final ImageView imageC17= layout_variation.findViewById(R.id.imageC17);

        layout_variation.findViewById(R.id.color1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#333333";
                imageC1.setImageResource(R.drawable.ic_done);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });

        layout_variation.findViewById(R.id.color2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#FDBE3B";
                imageC1.setImageResource(0);
                imageC2.setImageResource(R.drawable.ic_done);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });

        layout_variation.findViewById(R.id.color3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#FF4842";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(R.drawable.ic_done);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });

        layout_variation.findViewById(R.id.color4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#3A52FC";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(R.drawable.ic_done);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });

        layout_variation.findViewById(R.id.color5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#000000";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(R.drawable.ic_done);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color6).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#87CEEB";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(R.drawable.ic_done);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color7).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#00BFFF";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(R.drawable.ic_done);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color8).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#4169E1";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(R.drawable.ic_done);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color9).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#7CFC00";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(R.drawable.ic_done);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color10).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#2E8B57";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(R.drawable.ic_done);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color11).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#008000";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(R.drawable.ic_done);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color12).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#EE82EE";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(R.drawable.ic_done);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color13).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#FF69B4";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(R.drawable.ic_done);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color14).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#C71585";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(R.drawable.ic_done);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color15).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#ffe740";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(R.drawable.ic_done);
                imageC16.setImageResource(0);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color16).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#FFA500";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(R.drawable.ic_done);
                imageC17.setImageResource(0);
                setCreateLayoutColor();
            }
        });
        layout_variation.findViewById(R.id.color17).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedColor="#8B4513";
                imageC1.setImageResource(0);
                imageC2.setImageResource(0);
                imageC3.setImageResource(0);
                imageC4.setImageResource(0);
                imageC5.setImageResource(0);
                imageC6.setImageResource(0);
                imageC7.setImageResource(0);
                imageC8.setImageResource(0);
                imageC9.setImageResource(0);
                imageC10.setImageResource(0);
                imageC11.setImageResource(0);
                imageC12.setImageResource(0);
                imageC13.setImageResource(0);
                imageC14.setImageResource(0);
                imageC15.setImageResource(0);
                imageC16.setImageResource(0);
                imageC17.setImageResource(R.drawable.ic_done);
                setCreateLayoutColor();
            }
        });

        if(available_note != null && available_note.getColor() != null && !available_note.getColor().trim().isEmpty()){
            switch (available_note.getColor()){
                case "#FDBE3B":
                    layout_variation.findViewById(R.id.color2).performClick();
                    break;
                case "#FF4842":
                    layout_variation.findViewById(R.id.color3).performClick();
                    break;
                case "#3A52FC":
                    layout_variation.findViewById(R.id.color4).performClick();
                    break;
                case "#000000":
                    layout_variation.findViewById(R.id.color5).performClick();
                    break;
                case "#87CEEB":
                    layout_variation.findViewById(R.id.color6).performClick();
                    break;
                case "#00BFFF":
                    layout_variation.findViewById(R.id.color7).performClick();
                    break;
                case "#4169E1":
                    layout_variation.findViewById(R.id.color8).performClick();
                    break;
                case "#7CFC00":
                    layout_variation.findViewById(R.id.color9).performClick();
                    break;
                case "#2E8B57":
                    layout_variation.findViewById(R.id.color10).performClick();
                    break;
                case "#008000":
                    layout_variation.findViewById(R.id.color11).performClick();
                    break;
                case "#EE82EE":
                    layout_variation.findViewById(R.id.color12).performClick();
                    break;
                case "#FF69B4":
                    layout_variation.findViewById(R.id.color13).performClick();
                    break;
                case "#C71585":
                    layout_variation.findViewById(R.id.color14).performClick();
                    break;
                case "#ffe740":
                    layout_variation.findViewById(R.id.color15).performClick();
                    break;
                case "#FFA500":
                    layout_variation.findViewById(R.id.color16).performClick();
                    break;
                case "#8B4513":
                    layout_variation.findViewById(R.id.color17).performClick();
                    break;
            }
        }
        ImageView img_bold=layout_variation.findViewById(R.id.variation_add_bold);
        ImageView img_ita=layout_variation.findViewById(R.id.variation_add_italic);
        ImageView img_underline=layout_variation.findViewById(R.id.variation_add_line);

        layout_variation.findViewById(R.id.variation_add_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isBold) {
                    selectedFont="bold";
                    img_bold.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.colornote2));
                    img_ita.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.coloricon));
                    img_underline.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.coloricon));
                    enter_text.setTypeface(face1);
                    isBold=false;
                    isIta=true;
                    isLine=true;
                }
                else {
                    selectedFont="";
                    img_bold.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.coloricon));
                    enter_text.setTypeface(face);
                    isBold=true;
                }
            }
        });

        layout_variation.findViewById(R.id.variation_add_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isIta) {
                    selectedFont="ita";
                    img_ita.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.colornote2));
                    img_bold.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.coloricon));
                    img_underline.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.coloricon));
                    enter_text.setTypeface(face2);
                    isIta=false;
                    isBold=true;
                    isLine=true;
                }
                else {
                    selectedFont="";
                    img_ita.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.coloricon));
                    enter_text.setTypeface(face);
                    isIta=true;
                }
            }
        });

        layout_variation.findViewById(R.id.variation_add_line).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isLine) {
                    selectedFont="line";
                    img_underline.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.colornote2));
                    img_bold.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.coloricon));
                    img_ita.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.coloricon));
                    enter_text.setTypeface(face3);
                    isLine=false;
                    isBold=true;
                    isIta=true;
                }
                else {
                    selectedFont="";
                    img_underline.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.coloricon));
                    enter_text.setTypeface(face);
                    isLine=true;
                }
            }
        });

        if(available_note != null && available_note.getFont() != null && !available_note.getFont().trim().isEmpty()) {
            switch (available_note.getFont()) {
                case "bold":
                    enter_text.setTypeface(face1);
                    img_bold.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.colornote2));
                    selectedFont="bold";
                    isBold=false;
                    break;
                case "ita":
                    enter_text.setTypeface(face2);
                    img_ita.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.colornote2));
                    selectedFont="ita";
                    isIta=false;
                    break;
                case "line":
                    enter_text.setTypeface(face3);
                    img_underline.setColorFilter(ContextCompat.getColor(CreateNote.this, R.color.colornote2));
                    selectedFont="line";
                    isLine=false;
                    break;
                default:
                    enter_text.setTypeface(face);
            }
        }


        layout_variation.findViewById(R.id.variation_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNote.this,
                            new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_STORAGE_PERMISSION
                    );
                }

                else {
                    imageSelected();
                }
            }
        });

        layout_variation.findViewById(R.id.variation_add_link).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showLinkDialog();
            }
        });

        layout_variation.findViewById(R.id.variation_add_voice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(
                            CreateNote.this,
                            new String[] {Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE},
                            REQUEST_CODE_RECORDER
                    );
                }

                else {
                        showVoiceDialog();
                }
            }
        });

        if(available_note != null){
            delete_note.setVisibility(View.VISIBLE);
            layout_variation.findViewById(R.id.layout_remove_note).setVisibility(View.VISIBLE);
            layout_variation.findViewById(R.id.layout_remove_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteDialog();
                }
            });

            delete_note.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDeleteDialog();
                }
            });

        }

    }

    private void showDeleteDialog(){
        if(dialog_remove_notes == null){
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateNote.this);
            View view = LayoutInflater.from(this).inflate(R.layout.delete_note, (ViewGroup) findViewById(R.id.delete_note_container));
            builder.setView(view);
            dialog_remove_notes=builder.create();
            if(dialog_remove_notes.getWindow() != null){
                dialog_remove_notes.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            view.findViewById(R.id.txt_delete_notes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    @SuppressLint("StaticFieldLeak")
                    class DeleteNotesTask extends AsyncTask<Void, Void, Void>{

                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getNotesDatabase(getApplicationContext()).noteDao()
                                    .deleteNote(available_note);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent=new Intent();
                            intent.putExtra("note_deleted",true);
                            setResult(RESULT_OK,intent);
                            finish();
                        }
                    }
                    new DeleteNotesTask().execute();
                }
            });

            view.findViewById(R.id.txt_cancel_delete_notes).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_remove_notes.dismiss();
                }
            });
        }
        dialog_remove_notes.show();
    }


    private void setCreateLayoutColor(){
       layout_create_note.setBackgroundColor(Color.parseColor(selectedColor));
    }

    private void imageSelected(){
        Intent intent =new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent,REQUEST_CODE_IMAGE_SELECTED);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                imageSelected();
            }

            else {
                Toast.makeText(this,"Galeriye erişim izni reddedildi...",Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == REQUEST_CODE_RECORDER && grantResults.length > 0){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showVoiceDialog();
            }

            else {
                Toast.makeText(this,"Mikrofona erişim izni reddedildi...",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_IMAGE_SELECTED && resultCode == RESULT_OK){
            if(data != null){
                Uri imageSelectedUri=data.getData();
                if(imageSelectedUri != null){

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(imageSelectedUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        image_note.setImageBitmap(bitmap);
                        image_note.setVisibility(View.VISIBLE);
                        delete_image.setVisibility(View.VISIBLE);

                        selectedImagePath = pathFromUri(imageSelectedUri);

                    }

                    catch (Exception exception){
                        Toast.makeText(this,exception.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

    }

    private String pathFromUri(Uri contentUri){
        String filePath;
        Cursor cursor=getContentResolver().query(contentUri,null,null,null,null);
        if(cursor == null){
            filePath = contentUri.getPath();
        }
        else{
             cursor.moveToFirst();
             int index=cursor.getColumnIndex("_data");
             filePath=cursor.getString(index);
             cursor.close();
        }
        return filePath;
    }

    private void showLinkDialog(){
        if(dialog_add_link == null){
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateNote.this);
            View view= LayoutInflater.from(this).inflate(R.layout.add_link, (ViewGroup) findViewById(R.id.layout_add_link));
            builder.setView(view);

            dialog_add_link=builder.create();

            if(dialog_add_link.getWindow() != null){
                dialog_add_link.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText enterUrl= view.findViewById(R.id.enterUrl);
            enterUrl.requestFocus();

            view.findViewById(R.id.txt_add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(enterUrl.getText().toString().trim().isEmpty()){
                        Toast.makeText(CreateNote.this,"Url giriniz",Toast.LENGTH_SHORT).show();
                    }

                    else if(!Patterns.WEB_URL.matcher(enterUrl.getText().toString()).matches()){
                        Toast.makeText(CreateNote.this,"Geçerli bir Url giriniz",Toast.LENGTH_SHORT).show();
                    }

                    else{
                        txt_web_link.setText(enterUrl.getText().toString());
                        layout_web_link.setVisibility(View.VISIBLE);
                        dialog_add_link.dismiss();
                    }
                }
            });

            view.findViewById(R.id.txt_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog_add_link.dismiss();
                }
            });
        }

        dialog_add_link.show();
    }

    private void showVoiceDialog(){
        if(dialog_add_voice == null){
            AlertDialog.Builder builder=new AlertDialog.Builder(CreateNote.this);
            View view= LayoutInflater.from(this).inflate(R.layout.add_voice, (ViewGroup) findViewById(R.id.layout_add_voice_recorder));
            builder.setView(view);

            dialog_add_voice=builder.create();

            if(dialog_add_voice.getWindow() != null){
                dialog_add_voice.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }
            SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.getDefault());
            String date=simpleDateFormat.format(new Date());
            fileName = path +"/recording_"+date+".amr";
            fileName2="recording_"+date+".amr";

            if(!path.exists()){
                path.mkdirs();
            }

            Chronometer chronometer=view.findViewById(R.id.Chronometer);
            GifImageView gifImageView=view.findViewById(R.id.image_gif);
            TextView txt_record_state=view.findViewById(R.id.txt_rec_state);
            ImageButton btn_recorder=view.findViewById(R.id.btn_recorder);

            btn_recorder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    layout_voice_recorder.setVisibility(View.VISIBLE);
                    txt_name_voice.setText(fileName2);
                    if(!isRecord){
                        try {
                            startRecord();
                            gifImageView.setVisibility(View.VISIBLE);
                            chronometer.setBase(SystemClock.elapsedRealtime());
                            chronometer.start();
                            txt_record_state.setText("Recording...");
                            available_note2.setVoice(fileName2);
                            btn_recorder.setImageResource(R.drawable.ic_stop);
                            isRecord=true;
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),"Kayıt başarısız",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(isRecord){
                        stopRecord();
                        gifImageView.setVisibility(View.GONE);
                        chronometer.setBase(SystemClock.elapsedRealtime());
                        chronometer.stop();
                        txt_record_state.setText("");
                        btn_recorder.setImageResource(R.drawable.ic_record);
                        isRecord=false;
                    }
                }
            });


        }
        dialog_add_voice.show();
    }

    private void startRecord(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mediaRecorder.setOutputFile(fileName);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mediaRecorder.start();
    }

    private void stopRecord(){
        mediaRecorder.stop();
        mediaRecorder.release();
        mediaRecorder=null;
    }

}