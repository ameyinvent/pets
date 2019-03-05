package com.example.android.pets;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;

/**
 * Created by HP on 26-06-2017.
 */

public class PetCursorAdapter extends CursorAdapter{
    public PetCursorAdapter(Context context, Cursor c) {
        super(context, c,0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item,parent,false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        TextView nameTextView = (TextView)view.findViewById(R.id.name);
        TextView summaryTextView = (TextView)view.findViewById(R.id.summary);

        //Extract the name of the pet from the cursor
        int idColumnName = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_NAME);
        String currentName = cursor.getString(idColumnName);

        //Extract the Breed of the pet from the cursor
        int idColumnBreed = cursor.getColumnIndex(PetContract.PetEntry.COLUMN_PET_BREED);
        String currentBreed = cursor.getString(idColumnBreed);

        //Populating the list view item with name and breed
        nameTextView.setText(currentName);
        if(TextUtils.isEmpty(currentBreed)){
            currentBreed=context.getString(R.string.unknown_breed);
        }
        summaryTextView.setText(currentBreed);

    }
}
