
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by binyaya on 2015/8/9.
 */
public class BaseInfoDBOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "baseInfoDB.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME_UNIQUE_NO = "prd_unique_no";
    /**
     * SYS_UNIQUE_NO建表语句
     */
    public static final String CREATE_TABLE_SYS_UNIQUE_NO = "create table "+ TABLE_NAME_UNIQUE_NO +" (" +
            "id integer primary key autoincrement, " +
            "BAR_CODE text, " +
            "UNIQUE_NO text," +
            "IS_USED INTEGER DEFAULT 0)";


    public static final String DROP_TABLE_SYS_UNIQUE_NO="DROP TABLE IF EXISTS "+ TABLE_NAME_UNIQUE_NO;

    public BaseInfoDBOpenHelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SYS_UNIQUE_NO);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
