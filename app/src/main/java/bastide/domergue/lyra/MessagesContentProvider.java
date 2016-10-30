package bastide.domergue.lyra;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Created by Franck on 29/10/2016.
 */


public class MessagesContentProvider extends ContentProvider{

    //:::::::::::::::::::::::::://
    //:: URI d'exposition
    //:::::::::::::::::::::::::://
    public static final Uri CONTENT_URI = Uri.parse("content://fr.esiea.lyra");


    // Constantes pour identifier les requetes
    private static final int ALLROWS = 1;
    private static final int SINGLE_ROW = 2;

    // Uri matcher
    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI("fr.esiea.lyra","elements",ALLROWS);
        uriMatcher.addURI("fr.esiea.lyra","elements/#",SINGLE_ROW);
    }

    //:::::::::::::::::::::::::://
    //:: Champs de la table
    //:::::::::::::::::::::::::://
    public static final String KEY_ID = "key_id";                   //Identifiant de chaque message
    public static final String KEY_POSITION_X = "key_position_x";   //Position x du message
    public static final String KEY_POSITION_Y = "key_position_y";
    public static final String KEY_MESSAGE = "key_message";         //Message à afficher

    private MessagesDBHelper myMessagesHelper;



    @Override
    public boolean onCreate() {
        // construction/ouverture de la base de donnée
        myMessagesHelper = new MessagesDBHelper(
                MapsActivity.context,
                MessagesDBHelper.DATABASE_NAME,
                null,
                MessagesDBHelper.DATABASE_VERSION);
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        myMessagesHelper = new MessagesDBHelper(
                MapsActivity.context,
                MessagesDBHelper.DATABASE_NAME,
                null,
                MessagesDBHelper.DATABASE_VERSION);
        //ouverture de la base de donnée
        SQLiteDatabase db = myMessagesHelper.getWritableDatabase();

        //parametres de la requete SQL
        String groupBy = null;
        String having = null;

        //construction de la requete
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        //ajout de la table
        queryBuilder.setTables(MessagesDBHelper.DATABASE_TABLE);

        // si requete de ligne on limite les retours à la premiere ligne
        switch (uriMatcher.match(uri)){
            case SINGLE_ROW : String rowId = uri.getPathSegments().get(1);
                //ajout de la clause where, si on demande un element précis
                queryBuilder.appendWhere(KEY_ID + "=" + rowId);
            default: break;
        }

        //execution de la requete
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, groupBy, having, sortOrder);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)){
            case ALLROWS : return "vnd.android.cursor.dir/vnd.paad.elemental";
            case SINGLE_ROW : return "vnd.android.cursor.item/vnd.paad.elemental";
            default: throw new IllegalArgumentException("URI non reconnue");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        //ouverture de la base de donnée
        myMessagesHelper = new MessagesDBHelper(
                MapsActivity.context,
                MessagesDBHelper.DATABASE_NAME,
                null,
                MessagesDBHelper.DATABASE_VERSION);

        SQLiteDatabase db = myMessagesHelper.getWritableDatabase();

        //hack column vide
        String nullColumnHack = null;

        //Insere les valeurs
        long id = db.insertOrThrow(MessagesDBHelper.DATABASE_TABLE, nullColumnHack, contentValues);

        if (id > -1){
            // contruit l'uri de la ligne crée
            Uri instertedId = ContentUris.withAppendedId(CONTENT_URI, id);

            //notifie le changement des données
            MapsActivity.context.getContentResolver().notifyChange(instertedId, null);

            return instertedId;
        }

        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        //ouverture de la base de donnée
        SQLiteDatabase db = myMessagesHelper.getWritableDatabase();
        // si requete de ligne on limite les retours à la premiere ligne
        switch (uriMatcher.match(uri)){
            case SINGLE_ROW : String rowId = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
            default: break;
        }
        if (selection == null) {
            selection = "1";
        }

        // on effectue la suppression
        int deleteCount = db.delete(MessagesDBHelper.DATABASE_TABLE, selection, selectionArgs);

        //notifie le changement des données
         MapsActivity.context.getContentResolver().notifyChange(uri, null);

        return deleteCount;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        //ouverture de la base de donnée
        SQLiteDatabase db = myMessagesHelper.getWritableDatabase();
        // si requete de ligne on limite les retours à la premiere ligne
        switch (uriMatcher.match(uri)){
            case SINGLE_ROW : String rowId = uri.getPathSegments().get(1);
                selection = KEY_ID + "=" + rowId
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
            default: break;
        }
        if (selection == null) {
            selection = "1";
        }

        // on effectue l'update
        int updateCount = db.update(MessagesDBHelper.DATABASE_TABLE, contentValues, selection, selectionArgs);

        //notifie le changement des données
         MapsActivity.context.getContentResolver().notifyChange(uri, null);

        return updateCount;
    }


    public class MessagesDBHelper extends SQLiteOpenHelper {

        // nom de la base de données
        public static final String DATABASE_NAME = "lyraDatabase";
        // nom de la table
        public static final String DATABASE_TABLE = "Messages";
        // version de la base de données
        public static final int DATABASE_VERSION = 1;

        // script de création de la base de donnée
        private static final String DATABASE_CREATE = "create table " +
                DATABASE_TABLE + " (" + KEY_ID +
                " integer primary key autoincrement, " +
                KEY_POSITION_X + " text not null, " +
                KEY_POSITION_Y+ " text not null, " +
                KEY_MESSAGE+ " text not null); ";


        /**
         * @param context context
         * @param name name
         * @param factory factory
         * @param version version
         */
        public MessagesDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        /*
        Cette fonction est appelée si aucune base n'existe et que le Helper doit la créer
         */
        @Override
        public void onCreate(SQLiteDatabase sqLiteDatabase) {
            sqLiteDatabase.execSQL(DATABASE_CREATE);
        }

        /*
        Cette fonction est appelée si la base existe déjà et que la verison courante ne
        correspond pas à la version demandée
         */
        @Override
        public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
            Log.w("DATABASE", "Mise à jour de la version " +
                    oldVersion + " vers la verison " + newVersion+
                    ": toutes les données seront perdues.");
            sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            onCreate(sqLiteDatabase);
        }
    }
}
