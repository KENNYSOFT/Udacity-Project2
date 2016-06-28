package kr.KENNYSOFT.Udacity.Project2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieSQLite extends SQLiteOpenHelper
{
	MovieSQLite(Context context,String name,SQLiteDatabase.CursorFactory factory,int version)
	{
		super(context,name,factory,version);
	}

	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL("CREATE TABLE favorite(_id INTEGER PRIMARY KEY AUTOINCREMENT,id INTEGER,poster_path TEXT);");
	}

	public void onUpgrade(SQLiteDatabase db,int oldVersion,int newVersion)
	{
	}
}