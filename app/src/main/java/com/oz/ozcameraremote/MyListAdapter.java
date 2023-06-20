package com.oz.ozcameraremote;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

public class MyListAdapter extends CursorAdapter {

    private LayoutInflater mInflater;
    private OnEditClickListener mOnEditClickListener;
    private OnDeleteClickListener mOnDeleteClickListener;
    private OnLaunchClickListener mOnLaunchClickListener;

    public interface OnEditClickListener {
        void onEditClick(long itemId);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(long itemId);
    }

    public interface OnLaunchClickListener {
        void onLaunchClick(long itemId, String url);
    }

    public MyListAdapter(Context context, Cursor cursor) {
        super(context, cursor, false);
        mInflater = LayoutInflater.from(context);
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        mOnEditClickListener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener listener) {
        mOnDeleteClickListener = listener;
    }

    public void setOnLaunchClickListener(OnLaunchClickListener listener) {
        mOnLaunchClickListener = listener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return mInflater.inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.nameTextView);

        long itemId = cursor.getLong(cursor.getColumnIndex("_id"));
        final String url = cursor.getString(cursor.getColumnIndex("url"));
        final String name = cursor.getString(cursor.getColumnIndex("name"));

        nameTextView.setText(name);

//        ImageButton editButton = view.findViewById(R.id.editButton);
        Button editButton = view.findViewById(R.id.editButton);
        Button deleteButton = view.findViewById(R.id.deleteButton);
        TextView launchButton = view.findViewById(R.id.nameTextView);

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnEditClickListener != null) {
                    mOnEditClickListener.onEditClick(itemId);
                }
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnDeleteClickListener != null) {
                    mOnDeleteClickListener.onDeleteClick(itemId);
                }
            }
        });

        launchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnLaunchClickListener != null) {
                    mOnLaunchClickListener.onLaunchClick(itemId, url);
                }
            }
        });
    }
}