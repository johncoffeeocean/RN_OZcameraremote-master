package com.oz.ozcameraremote;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText mUrlEditText;
    private EditText mNameEditText;
    private Button mAddButton;
    private ListView mListView;

//    private MyDataManager mDataManager;
    private MyListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        mUrlEditText = findViewById(R.id.urlEditText);
        mNameEditText = findViewById(R.id.nameEditText);
        mAddButton = findViewById(R.id.addButton);
        mListView = findViewById(R.id.listView);

//        mDataManager = new MyDataManager(this);
        mAdapter = new MyListAdapter(this, null);
        mListView.setAdapter(mAdapter);

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mUrlEditText.getText().toString();
                String name = mNameEditText.getText().toString();
                DbHandler db = new DbHandler(MainActivity.this);
                if(name == "") {
                    Toast.makeText(MainActivity.this, "Fill the url or name.", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    db.insertUrl(name, url);
                    mUrlEditText.setText("");
                    updateListView();
                }

//                mDataManager.insert(url);

            }
        });

        mAddButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = mUrlEditText.getText().toString();
                String name = mNameEditText.getText().toString();
                DbHandler db = new DbHandler(MainActivity.this);
                db.insertUrl(name, url);
                mUrlEditText.setText("");
//                mDataManager.insert(url);
                updateListView();
            }
        });

        mAdapter.setOnEditClickListener(new MyListAdapter.OnEditClickListener() {
            @Override
            public void onEditClick(long itemId) {
                Intent intent = new Intent(MainActivity.this, EditActivity.class);
                intent.putExtra("_id", itemId);
                startActivity(intent);
                finish();
            }
        });
        mAdapter.setOnDeleteClickListener(new MyListAdapter.OnDeleteClickListener() {
            @Override
            public void onDeleteClick(long itemId) {
                Toast.makeText(MainActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                DbHandler db = new DbHandler(MainActivity.this);
                db.DeleteUrl(itemId);
                updateListView();
            }
        });
        mAdapter.setOnLaunchClickListener(new MyListAdapter.OnLaunchClickListener() {
            @Override
            public void onLaunchClick(long itemId, String url) {
                Intent intent = new Intent(MainActivity.this, FullscreenActivity.class);
                intent.putExtra("url", url);
                startActivity(intent);
            }
        });

        updateListView();
    }

    private void updateListView() {

        DbHandler db = new DbHandler(this);
        Cursor cursor = db.GetUrls();
//        ListView lv = (ListView) findViewById(R.id.user_list);
//        ListAdapter adapter = new SimpleAdapter(MainActivity2.this, userList, R.layout.list_row,new String[]{"url"}, new int[]{R.id.url});
//        lv.setAdapter(adapter);
//        Cursor cursor = mDataManager.queryAll();
        mAdapter.changeCursor(cursor);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            updateListView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        mDataManager.close();
    }

}
