package com.example.networkcamera

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.example.networkcamera.dataType.CameraListRes
import com.example.networkcamera.dataType.CameraSetReq
import com.example.networkcamera.dataType.CameraSetRes
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), Observer {

    private val list = ArrayList<CameraListRes.Camera>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        APIManager.instance.addObserver(this)

        img_camera_set.setOnClickListener {
            showCameraSetDialog()
        }

        img_camera_watch.setOnClickListener {
            DialogManager.instance.showLoading(this)
            APIManager.instance.doCameraList()
        }
    }

    override fun onResume() {
        super.onResume()
        APIManager.instance.addObserver(this)
    }

    override fun onStop() {
        super.onStop()
        APIManager.instance.deleteObserver(this)
    }

    override fun update(o: Observable?, arg: Any?) {
        when(arg) {
            is CameraSetRes -> {
                runOnUiThread {
                    DialogManager.instance.cancelLoading()
                    if (arg.status == 0) {
                        val b = Bundle()
                        b.putString("id", arg.result.id)
                        b.putBoolean("isWatch", false)
                        startActivity(Intent(this, MeetActivity::class.java).putExtras(b))
                    }
                }
            }

            is CameraListRes -> {
                runOnUiThread {
                    DialogManager.instance.cancelLoading()
                    if (arg.status == 0) {
                        if (arg.result.list.isNotEmpty()) {
                            list.clear()
                            list.addAll(arg.result.list)
                            showCameraListDialog()
                        } else
                            Toast.makeText(this, "尚未架設任何攝影機", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun showCameraSetDialog() {
        DialogManager.instance.showCustom(this, R.layout.dialog_enter, true)?.let {
            val ed_name = it.findViewById<EditText>(R.id.ed_name)
            val img_delete = it.findViewById<ImageView>(R.id.img_delete)
            val tv_cancel = it.findViewById<TextView>(R.id.tv_cancel)
            val tv_ok = it.findViewById<TextView>(R.id.tv_ok)

            img_delete.setOnClickListener { ed_name.setText("") }

            tv_ok.setOnClickListener {
                val name = ed_name.text.toString()

                if (name.isNotEmpty() && !name.isBlank()) {
                    hideKeyBoard(it)
                    DialogManager.instance.dismissDialog()
                    DialogManager.instance.showLoading(this)
                    APIManager.instance.doCameraSet(CameraSetReq(name))
                }
            }

            tv_cancel.setOnClickListener {
                hideKeyBoard(it)
                DialogManager.instance.dismissDialog()
            }
        }
    }

    private fun showCameraListDialog() {
        DialogManager.instance.showCustom(this, R.layout.dialog_list, cancelable = false)?.let {
            val tv_title = it.findViewById<TextView>(R.id.tv_title)
            val listView = it.findViewById<ListView>(R.id.listView)
            val tv_ok = it.findViewById<TextView>(R.id.tv_ok)
            val tv_cancel = it.findViewById<TextView>(R.id.tv_cancel)
            val adapter = ListCameraAdapter(this, list.toTypedArray())

            tv_title.text = "選擇攝影機"
            listView.adapter = adapter

            tv_ok.setOnClickListener {
                val position = listView.checkedItemPosition
                if (position != -1) {
                    DialogManager.instance.dismissDialog()

                    val b = Bundle()
                    b.putString("id", list[position].id)
                    b.putBoolean("isWatch", true)
                    startActivity(Intent(this, MeetActivity::class.java).putExtras(b))
                }
            }

            tv_cancel.setOnClickListener {
                DialogManager.instance.dismissDialog()
            }
        }
    }

    private fun hideKeyBoard(view: View) {
        val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken,0)
    }
}
