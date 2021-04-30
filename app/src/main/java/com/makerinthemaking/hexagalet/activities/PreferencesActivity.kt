package com.makerinthemaking.hexagalet.activities

import android.content.ContentValues
import android.content.Context
import android.content.pm.ApplicationInfo
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.facebook.shimmer.ShimmerFrameLayout
import com.makerinthemaking.hexagalet.R
import java.util.*


class PreferencesActivity : AppCompatActivity() {

    private lateinit var loggingTag: String ;
    private lateinit var adapter: CustomAdapter
    private lateinit var myListView: ListView
  //  private var appDao: AppSettingDao? = null ;
    var mydb: DBHelper? = null
    private var mShimmerViewContainer: ShimmerFrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)
        loggingTag = getString(R.string.app_name)
        Log.i(loggingTag, "starting up")

     //   initStore() ;
        mydb = DBHelper(applicationContext)

        mShimmerViewContainer = findViewById(R.id.shimmer_view_container)
        retrieveAppList();
        // var array_list: ArrayList<String> = mydb!!.allCotacts
        // Log.e("arrayList", array_list.toString()) ;

       /* var installedApps = getInstalledApps();
        adapter = CustomAdapter(this, installedApps)
        myListView.adapter = adapter
        array_list = mydb!!.allCotacts
        Log.e("arrayList 2", array_list.toString()) ;

*/
    }

    private fun retrieveAppList()
    {
        myListView = findViewById(R.id.MainRecyclerView)
        var installedApps = getInstalledApps();
        adapter = CustomAdapter(this, installedApps)
        myListView.adapter = adapter
        this.mShimmerViewContainer?.stopShimmerAnimation();
        this.mShimmerViewContainer?.setVisibility(View.GONE)

    }

    override fun onResume() {
        this.mShimmerViewContainer?.startShimmerAnimation();
        super.onResume()
    }

    override fun onStop() {
        this.mShimmerViewContainer?.stopShimmerAnimation();
        super.onStop()
    }

    private fun getInstalledApps() :ArrayList<PreferencesActivity.AppSetting>{
        val list = packageManager.getInstalledPackages(0)
        val apps = ArrayList<PreferencesActivity.AppSetting>()

        for (i in list.indices) {
            val packageInfo = list[i]
            if(packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0)
            {
                val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                val appPackage = packageInfo.packageName
                val appImage = packageInfo.applicationInfo.loadIcon(packageManager)
                val app = PreferencesActivity.AppSetting(appName, appImage, "")
                apps.add(app)
                val palette = Palette.from(appImage.toBitmap(50, 50)).generate()
                val color_1 = palette.getVibrantColor(Color.WHITE);
                val color_2 = palette.getLightVibrantColor(Color.YELLOW);
                var colorCode_1 = String.format("#%06x", 0xFFFFFF and color_1)
                var colorCode_2 = String.format("#%06x", 0xFFFFFF and color_2)
                val commande = "g:$colorCode_1:$colorCode_2/"
                Log.d(loggingTag, "$appName -  $commande")
                app.setCommand(commande)

/*
                val random = Random()
                val nextInt = random.nextInt(0xffffff + 1)
                val colorCode = String.format("#%06x", nextInt)
                val commande = "g:" + colorCode + colorCode + "/"
                app.setCommand(commande)
                mydb!!.insertApp(appPackage, colorCode)
                */

            }

        }
        return apps ;
    }


    @Entity
    class AppSetting(
        @PrimaryKey private var name: String,
        private var image: Drawable,
        private var command: String
    ) {

        fun getName(): String { return name  }
        fun getImage(): Drawable { return image  }
        fun getCommand(): String { return command}
        fun setCommand(receivedCommand: String) { command =  receivedCommand }
    }


    class CustomAdapter(private var c: Context, private var apps: ArrayList<AppSetting>) : BaseAdapter() {

        override fun getCount(): Int   {  return apps.size  }
        override fun getItem(i: Int): Any {  return apps[i] }
        override fun getItemId(i: Int): Long { return i.toLong()}

        override fun getView(i: Int, view: View?, viewGroup: ViewGroup): View {
            var view = view
            if (view == null) {
                //inflate layout resource if its null
                view = LayoutInflater.from(c).inflate(R.layout.appsettingslayout, viewGroup, false)
            }

            val currentAppSetting = this.getItem(i) as AppSetting

            val img = view!!.findViewById<ImageView>(R.id.itemImageView) as ImageView
            val nameTxt = view.findViewById<TextView>(R.id.itemTextView) as TextView

            nameTxt.text = currentAppSetting.getName()
            img.setImageDrawable(currentAppSetting.getImage())

            view.setOnClickListener { Toast.makeText(
                c,
                currentAppSetting.getName(),
                Toast.LENGTH_SHORT
            ).show() }

            return view
        }
    }
}
public class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, 2) {
    val TAG :String = "DBHelper"
    //        private val hp: HashMap? = null
    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "onCreate")
        // TODO Auto-generated method stub
        db.execSQL(
            "create table apps " +
                    "( packageName text primary key, command text)"
        )
      //  closeDatabase(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // TODO Auto-generated method stub
        db.execSQL("DROP TABLE IF EXISTS apps")
        onCreate(db)
    }

    fun insertApp(
        packageName: String?,
        command: String?,
    ): Boolean {
        Log.d(TAG, "insertApp")
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put("packageName", packageName)
        contentValues.put("command", command)
        db.insertWithOnConflict("apps", null, contentValues, CONFLICT_REPLACE)
  //      closeDatabase(db)
        return true
    }

    fun getData(packageId: String): Cursor {
        Log.d(TAG, "getData")

        val db = this.readableDatabase
        val num = DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME).toInt()
        Log.d(TAG, num.toString())
//        closeDatabase(db)
//        return db.rawQuery("select * from apps where 'packageName' LIKE $packageId", null)
        val c = db.rawQuery("SELECT * FROM apps WHERE packageName = ?", arrayOf(packageId))

        return c;
    }

    fun getCommand(packageId: String): String {
        Log.d(TAG, "getCommand")
        var returnData = getData(packageId)
        returnData.moveToFirst()
        var id: String = "f:00ffff:00ff00/"
        if( returnData != null && returnData.moveToFirst() )
        {
             id = returnData.getString(returnData.getColumnIndex("command")) // id is column name in db
        }
        else
        {
            Log.d("DB", "unknown package")
        }
        return id
    }

    fun numberOfRows(): Int {
        val db = this.readableDatabase
        return DatabaseUtils.queryNumEntries(db, CONTACTS_TABLE_NAME).toInt()
//        closeDatabase(db)
    }

    val allCotacts: ArrayList<String>
        get() {
            val array_list = ArrayList<String>()
            val db = this.readableDatabase
            val res: Cursor = db.rawQuery("select * from apps", null)
            res.moveToFirst()
            while (res.isAfterLast() === false) {
                array_list.add(res.getString(res.getColumnIndex(CONTACTS_COLUMN_NAME)))
                res.moveToNext()
            }
      //      closeDatabase(db)
            return array_list
        }

    private fun closeDatabase(db: SQLiteDatabase){
        if (db != null && db.isOpen()) {
            db.close();
        }
    }
    companion object {
        const val DATABASE_NAME = "MyDBName.db"
        const val CONTACTS_TABLE_NAME = "apps"
        const val CONTACTS_COLUMN_NAME = "packageName"

    }
}