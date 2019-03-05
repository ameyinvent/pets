/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.pets;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.pets.data.PetContract;
import com.example.android.pets.data.PetDbHelper;

import static android.R.id.content;

/**
 * Displays list of pets that were entered and stored in the app.
 */
public class CatalogActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private static final int PET_LOADER = 0;

    PetCursorAdapter mCursorAdapter;

    // To access our database, we instantiate our subclass of SQLiteOpenHelper
    // and pass the context, which is the current activity.
    PetDbHelper mDbHelper = new PetDbHelper(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        //Find the List view which will be populated with the data
        ListView petListView = (ListView) findViewById(R.id.list_view_pet);

        //Find the emptyView
        View emptyView = (View) findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        //Setup an Adapter to create a list item for each row of pet data in the cursor
        //There is no pet data yet(until the loader finishes) so pass in null for the cursor
        mCursorAdapter = new PetCursorAdapter(this, null);
        petListView.setAdapter(mCursorAdapter);

        //Setup item clickListener
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {

                //Create a new intent to go to Editor Activity
                Intent intent = new Intent(CatalogActivity.this, EditorActivity.class);

                //Form the content uri when a specific pet was clicked on
                //by appending the (id passed on to this method) onto the
                //{@link PetEntry#Content uri}
                //For example the uri clicked on would be "content://com.example.android.pets/pets/2
                //if the pet with id 2 was clicked on
                Uri currentPetUri = ContentUris.withAppendedId(PetContract.PetEntry.CONTENT_URI, id);

                //Set the uri on the data field of the intent
                intent.setData(currentPetUri);

                //Launch the Editor Activity to display the data for the current per.
                startActivity(intent);

            }
        });

        //Kick off the Loader
        getLoaderManager().initLoader(PET_LOADER,null,this);

    }


    private void insertPet(){

        //Create a new map of Values where column names are the keys
        ContentValues values = new ContentValues();
        values.put(PetContract.PetEntry.COLUMN_PET_NAME,"Toto");
        values.put(PetContract.PetEntry.COLUMN_PET_BREED,"Teriar");
        values.put(PetContract.PetEntry.COLUMN_PET_GENDER,1);
        values.put(PetContract.PetEntry.COLUMN_PET_WEIGHT,7);

        //Insert a new row in Pets using ContentResolver
        Uri newUri = getContentResolver().insert(PetContract.PetEntry.CONTENT_URI,values);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_catalog.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_catalog, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertPet();
                return true;
            // Respond to a click on the "Delete all entries" menu option
            case R.id.action_delete_all_entries:
                deleteAllPets();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //Define a projection that specifies the columns from the table we care about
        String[] projection = {
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED};

     //This Loader will execute the ContentProvider query() method on background thread
        return new CursorLoader(this,                //Parent activity context
                PetContract.PetEntry.CONTENT_URI,    //Provider content uri to query
                projection,                          //Columns to include in the resulting cursor
                null,                                //No selection clause
                null,                                //No selection Arguments
                null);                               //Default sort order

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        //Update adapter with the cursor containing the new data
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //Callback called when the data needs to be Deleted
        mCursorAdapter.swapCursor(null);
    }

    private void deleteAllPets(){
        int rowsDeleted = getContentResolver().delete(PetContract.PetEntry.CONTENT_URI, null, null);
        Log.v("CatalogActivity",rowsDeleted+ " rows deleted from pet database");
    }
}