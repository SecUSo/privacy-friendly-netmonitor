package org.secuso.privacyfriendlynetmonitor.DatabaseUtil;

import android.app.Application;
import android.os.AsyncTask;

import org.greenrobot.greendao.database.Database;
import org.secuso.privacyfriendlynetmonitor.ConnectionAnalysis.Report;

import java.util.HashMap;
import java.util.List;


/**
 * Created by m4rc0 on 12.11.2017.
 */

public class DBApp extends Application {

    public static final boolean ENCRYPTED = false;

    private static DaoSession daoSession;
    private static DBApp mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        new DBAppAsyncTask().execute("");
    }

    public DaoSession getDaoSession(){
        return daoSession;
    }

    static class DBAppAsyncTask extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {
            System.out.println("Starting Database Async Task");
            DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(mContext, ENCRYPTED ? "reports-db-encrypted" : "reports-db");
            Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
            daoSession = new DaoMaster(db).newSession();
            return "";
        }
    }

}
