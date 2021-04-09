package com.makerinthemaking.hexagalet

import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class MainActivity : AppCompatActivity() {
//    lateinit var listView: ListView
//    var arrayAdapter: ArrayAdapter<*>? = null



    class AppSetting (private var name:String, private var image: Drawable) {

        fun getName(): String { return name  }
        fun getImage(): Drawable { return image  }
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

            //get current quote
            val currentAppSetting = this.getItem(i) as AppSetting

            //reference textviews and imageviews from our layout
            val img = view!!.findViewById<ImageView>(R.id.itemImageView) as ImageView
            val nameTxt = view.findViewById<TextView>(R.id.itemTextView) as TextView

            //BIND data to TextView and ImageVoew
            nameTxt.text = currentAppSetting.getName()
            img.setImageDrawable(currentAppSetting.getImage())

            //handle itemclicks for the ListView
            view.setOnClickListener { Toast.makeText(c, currentAppSetting.getName(), Toast.LENGTH_SHORT).show() }

            return view
        }
    }

    private lateinit var adapter: CustomAdapter
    private lateinit var myListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Hexagalet"
        Log.e("Hexagalet","starting up")

        myListView = findViewById(R.id.MainlistView) as ListView
        var installedApps = getInstalledApps();
        adapter = CustomAdapter(this, installedApps)
        myListView.adapter = adapter
    }


    private fun getInstalledApps() :ArrayList<AppSetting>{
        val list = packageManager.getInstalledPackages(0)
        val apps = ArrayList<AppSetting>()

        for (i in list.indices) {
            val packageInfo = list[i]
            val appName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
            val appImage = packageInfo.applicationInfo.loadIcon(packageManager)
            val app = AppSetting(appName, appImage)
            apps.add(app)
       //     Log.e("App List$i", appName)
        }
        return apps ;
    }
}
