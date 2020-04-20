package com.example.networkcamera

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv_set.setOnClickListener {
            val b = Bundle()
            b.putBoolean("isWatch", false)
            startActivity(Intent(this, MeetActivity::class.java).putExtras(b))
        }

        tv_watch.setOnClickListener {
            val b = Bundle()
            b.putBoolean("isWatch", true)
            startActivity(Intent(this, MeetActivity::class.java).putExtras(b))
        }
    }
}
