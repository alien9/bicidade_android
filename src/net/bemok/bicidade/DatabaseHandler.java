package net.bemok.bicidade;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 3;

	// Database Name
	private static final String DATABASE_NAME = "bicidade";

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	// Creating Tables
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE position (id INTEGER PRIMARY KEY, zoom NUMERIC, x NUMERIC, y NUMERIC);");
		db.execSQL("CREATE TABLE settings (name TEXT, value TEXT);");
		db.execSQL("CREATE TABLE legend (image BLOB);");
	}

	// Upgrading database
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS position");
		db.execSQL("DROP TABLE IF EXISTS settings");
		db.execSQL("DROP TABLE IF EXISTS legend");

		// Create tables again
		onCreate(db);
	}
}
