package com.example.networkcamera

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import com.facebook.react.modules.core.PermissionListener
import org.jitsi.meet.sdk.*
import org.jitsi.meet.sdk.R
import java.net.URL
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.facebook.react.ReactRootView
import com.facebook.react.views.view.ReactViewGroup
import kotlinx.android.synthetic.main.activity_meet.*


class MeetActivity : FragmentActivity(), JitsiMeetActivityInterface {
    private lateinit var roomID: String

    private var view: JitsiMeetView? = null

    override fun requestPermissions(p0: Array<out String>?, p1: Int, p2: PermissionListener?) {

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        JitsiMeetActivityDelegate.onActivityResult(
            this, requestCode, resultCode, data
        )
    }

    override fun onBackPressed() {
        JitsiMeetActivityDelegate.onBackPressed()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.example.networkcamera.R.layout.activity_meet)

        intent?.extras?.let {
            view = JitsiMeetView(this)

            val isWatch = it.getBoolean("isWatch")

            val options = JitsiMeetConferenceOptions.Builder()
                .setServerURL(URL("https://meet.jit.si"))
                .setRoom("mmslab40640686319")
                .setSubject("居家監控")
                .setVideoMuted(isWatch)
                .setAudioMuted(isWatch)
                .setFeatureFlag("chat.enabled", false)
                .setFeatureFlag("invite.enabled", false)
                .build()

            view?.join(options)

            view?.listener = object : JitsiMeetViewListener {
                override fun onConferenceTerminated(p0: MutableMap<String, Any>?) {
                    Log.e("onConferenceTerminated", p0.toString())
                    finish()
                }

                override fun onConferenceJoined(p0: MutableMap<String, Any>?) {
                    Log.e("onConferenceJoined", p0.toString())

                    tv_hint.visibility = View.GONE
                    react_view.addView(view)
                }

                override fun onConferenceWillJoin(p0: MutableMap<String, Any>?) {
                    Log.e("onConferenceWillJoin", p0.toString())

                    view_black.visibility = if (isWatch) View.GONE else View.VISIBLE
                }
            }

//            val a = view?.get(0)
//            if (a is ReactRootView) {
//                Log.e("debug", "進入")
//                Log.e("debug", "${a.childCount}")
//            }

            tv_hint.text = if (isWatch) "正在連線到攝影機..." else "正在設定攝影機..."

            btn_close.setOnClickListener {
                view?.leave()
            }
        }
    }

    override fun onDestroy() {
        view?.dispose()
        view = null

        JitsiMeetActivityDelegate.onHostDestroy(this)

        Log.e("RTC2Activity", "onDestroy")
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        JitsiMeetActivityDelegate.onNewIntent(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        JitsiMeetActivityDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onResume() {
        super.onResume()

        JitsiMeetActivityDelegate.onHostResume(this)
    }

    override fun onStop() {
        super.onStop()

        JitsiMeetActivityDelegate.onHostPause(this)
    }
}
