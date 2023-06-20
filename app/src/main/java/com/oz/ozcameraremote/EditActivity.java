package com.oz.ozcameraremote;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class EditActivity extends AppCompatActivity {

    private EditText mUrlEditText;

    private EditText mNameEditText;
    private Button mSaveButton;
    private Button mBackButton;

    private DbHandler db;
    private long mId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        mUrlEditText = findViewById(R.id.urlEditText);
        mNameEditText = findViewById(R.id.nameEditText);
        mSaveButton = findViewById(R.id.saveButton);
        mBackButton = findViewById(R.id.backButton);

        DbHandler db = new DbHandler(this);

        Intent intent = getIntent();
        mId = getIntent().getLongExtra("_id", 0);
        Cursor cursor = (Cursor) db.GetUrlById(mId);
        if (cursor != null) {
            cursor.moveToFirst();
            String url = cursor.getString(cursor.getColumnIndex("url"));
            String name = cursor.getString(cursor.getColumnIndex("name"));
            mUrlEditText.setText(url);
            mNameEditText.setText(name);
        }

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mUrlEditText.getText().toString();
                String name = mNameEditText.getText().toString();
                if(name.isEmpty() || url.isEmpty()) {
                    Toast.makeText(EditActivity.this, "Fill the url or name.", Toast.LENGTH_SHORT).show();
                    return;
                }
                db.UpdateUrl(name, url, mId);
                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(EditActivity.this, MainActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        db.close();
    }

}