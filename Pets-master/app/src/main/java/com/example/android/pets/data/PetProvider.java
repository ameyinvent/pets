package com.example.android.pets.data;

import android.app.LoaderManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import static android.R.attr.id;
import static android.R.attr.name;

/**
 * Created by HP on 25-06-2017.
 */

public class PetProvider extends ContentProvider {

    //DataBase helper object
    private PetDbHelper mDbHelper;

    public static final String LOG_TAG = PetProvider.class.getSimpleName();

    //Uri matcher code for whole TAble
    private static final int PETS = 100;

    //Uri matcher code for a specific row
    private static final int PETS_ID = 101;

    //Setup UriMatcher Object
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    //Setting up the UriMatcher variables
    static{
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS,PETS);
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY,PetContract.PATH_PETS + "/#",PETS_ID);
    }

    /*
    Initialize the provider and DataBase
     */
    @Override
    public boolean onCreate() {
        mDbHelper = new PetDbHelper(getContext());
        return false;
    }

    /*
    Perform the query for the given Uri Use the given projection, selection, se;ection arguments and sort order
     */

    @Override
    public Cursor query( Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        //Get Readable DataBase
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //This cursor will hold the result of the Query
        Cursor cursor = null;

        //Figure out if the given Uri matches with the coded Integer values
        int match = sUriMatcher.match(uri);

        //Performing the specific task depending on the value of match
        switch (match){
            case PETS:
                //for PETS query the whole table Directly
                cursor = database.query(PetContract.PetEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;

            case PETS_ID:
                //for specific columns of the table
                selection = PetContract.PetEntry._ID+"=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};

                cursor = database.query(PetContract.PetEntry.TABLE_NAME,projection,selection,selectionArgs,
                        null,null,sortOrder);
                break;

            default:
                throw new IllegalArgumentException("Cannot query unknown URI"+uri);

        }

        //Set notification uri on the cursor
        //so we know what content uri the cursor was created for
        //If the data at this uri changes then we knoe that we need to reload the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    /*
    Inserts new data into the provider with the given Content values
     */
    @Override
    public Uri insert( Uri uri, ContentValues contentValues) {

        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return insertPet(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for "+uri);
        }
    }

    /*
    insertPet() to insert a particular column
     */

    private Uri insertPet(Uri uri, ContentValues values) {

        //Check if the name is not null
        String name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
        if (name == null)
            throw new IllegalArgumentException("Pet requires a Name");

        //Check if weight is not negetive
        Integer weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight < 0 && weight != null)
            throw new IllegalArgumentException("The weight should be positive");

        //Check if gender is not null
        Integer gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
        if ((gender == null) || !(PetContract.PetEntry.isValidGender(gender)))
            throw new IllegalArgumentException("The gender is reqiured to be entered");

        //Get a writable DataBase
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        //Insert the database
        long id = database.insert(PetContract.PetEntry.TABLE_NAME, null, values);

        if (id == -1){
            Log.e(LOG_TAG, "Failed to Insert the Data " + uri);
        return null;
    }

    //Notify all the listeners that the data has changed
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri,id);
    }


    /*
    Delete the Data at the given selection and selection arguments
     */
    @Override
    public int delete( Uri uri,String selection, String[] selectionArgs) {
        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        //Perform the delete on the database and get the number of rows affected
        int rowsDeleted = database.delete(PetContract.PetEntry.TABLE_NAME,selection,selectionArgs);

        //if one or more rows have been updated then notify all the Listeners that the uri has changed
        if(rowsDeleted != 0)
            getContext().getContentResolver().notifyChange(uri, null);


        switch(match){

            case PETS:
                //Delete all the rows that match the selection and selectionArgs
                return database.delete(PetContract.PetEntry.TABLE_NAME,selection,selectionArgs);

            case PETS_ID:
                //Delete a single row by the ID given in the URI
                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[] {String.valueOf(ContentUris.parseId(uri))};
                return database.delete(PetContract.PetEntry.TABLE_NAME,selection,selectionArgs);

            default:
                throw new IllegalArgumentException("Deletion nt Supported for"+uri);
        }
    }

    /*
    Updates the data at the given selection and selection arguments with the given content values
     */
    @Override
    public int update( Uri uri, ContentValues contentValues, String selection,String[] selectionArgs) {

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PETS:
                return updatePets(uri, contentValues, selection, selectionArgs);

            case PETS_ID:
                //For the PETS_ID case code, extract out the id from the uri
                //So we know which row to update. selection will be "id=?" and
                //selection argmuments will be a String array connecting the actual ID.

                selection = PetContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePets(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update not supported for "+uri );
        }
    }

    private int updatePets(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //Get Readable DataBase
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        //This cursor will hold the result of the Query
        Cursor cursor = null;

        //Figure out if the given Uri matches with the coded Integer values
        int match = sUriMatcher.match(uri);

        //Sanity check for valid name
        if(contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_NAME))
        {
            String name = contentValues.getAsString(PetContract.PetEntry.COLUMN_PET_NAME);
                    if (name == null)
                        throw new IllegalArgumentException("Enter a Pet Name");
        }

        //Sanity Check for valid weight
        if(contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT))
        {
            Integer weight = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight<0)
                throw new IllegalArgumentException("Enter a Pet Weight");
        }

        //Sanity Check for valid gender
        if(contentValues.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER))
        {
            Integer gender = contentValues.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if((gender == null)||!(PetContract.PetEntry.isValidGender(gender)))
                throw new IllegalArgumentException("Enter a Pet Gender");
        }

        //if there is new values to update then dont update
        if(contentValues.size()==0)
            return 0;

        //Get the database to writing mode
        database = mDbHelper.getWritableDatabase();

        //Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(PetContract.PetEntry.TABLE_NAME,contentValues,selection,selectionArgs);

        //if one or more rows have been updated then notify all the Listeners that the uri has changed
        if(rowsUpdated != 0)
            getContext().getContentResolver().notifyChange(uri, null);

        return database.update(PetContract.PetEntry.TABLE_NAME,contentValues,selection,selectionArgs);


    }

    /*
    Return the MIME type of the data for the content URI
     */

    @Override
    public String getType( Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case PETS:
                return PetContract.PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetContract.PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri " + uri + "with match " +match);
        }
    }
}
