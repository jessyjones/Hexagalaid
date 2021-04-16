package com.makerinthemaking.hexagalet.activities

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Color.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import com.makerinthemaking.hexagalet.R
import com.makerinthemaking.hexagalet.activities.ScannerActivity

class MainActivity : AppCompatActivity() {

    private lateinit var loggingTag: String ;

    class AppSetting(private var name: String, private var image: Drawable) {

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
            view.setOnClickListener { Toast.makeText(
                c,
                currentAppSetting.getName(),
                Toast.LENGTH_SHORT
            ).show() }

            return view
        }
    }

    private lateinit var adapter: CustomAdapter
    private lateinit var myListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loggingTag = getString(R.string.app_name)
        Log.i(loggingTag, "starting up")

  /*      myListView = findViewById(R.id.MainListView) as ListView
        var installedApps = getInstalledApps();
        adapter = CustomAdapter(this, installedApps)
        myListView.adapter = adapter

   */     Handler().postDelayed({
            val intent = Intent(this, ScannerActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
            startActivity(intent)
            finish()
        }, 1000)


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
            val color = Palette.from(appImage.toBitmap(50,50)).generate().getVibrantColor(Color.WHITE);
            Log.d("PaletteApp List $i", appName)
            var message = """Color = ${red(color)} - ${green(color)} - ${blue(color)} """
            Log.d("Palette", message)
        }
        return apps ;
    }
}
