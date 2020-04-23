package com.example.networkcamera

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.get
import androidx.fragment.app.FragmentActivity
import com.facebook.react.modules.core.PermissionListener
import org.jitsi.meet.sdk.*
import java.net.URL
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.networkcamera.dataType.CameraDeleteReq
import com.example.networkcamera.dataType.CameraDeleteRes
import com.facebook.react.ReactRootView
import com.facebook.react.views.view.ReactViewGroup
import kotlinx.android.synthetic.main.activity_meet.*
import java.util.*


class MeetActivity : FragmentActivity(), JitsiMeetActivityInterface, Observer {
    private var cameraId: String = ""
    private var isWatch: Boolean = true

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
        if (isWatch)
            JitsiMeetActivityDelegate.onBackPressed()
        else
            showLeaveDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meet)
        APIManager.instance.addObserver(this)

        intent?.extras?.let {
            view = JitsiMeetView(this)

            cameraId = it.getString("id") ?: return@let
            isWatch = it.getBoolean("isWatch")

            val room = it.getString("room") ?: ""
            val options = JitsiMeetConferenceOptions.Builder()
                .setServerURL(URL("https://meet.jit.si"))
                .setRoom("mmslab406mmslab406${cameraId}")
                .setSubject(room)
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
                    cl_panel.visibility = if (isWatch) View.VISIBLE else View.GONE
                    react_view.addView(view)
                    setListen()
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
        APIManager.instance.addObserver(this)
        JitsiMeetActivityDelegate.onHostResume(this)
    }

    override fun onStop() {
        super.onStop()
        APIManager.instance.deleteObserver(this)
        JitsiMeetActivityDelegate.onHostPause(this)
    }

    override fun update(o: Observable?, arg: Any?) {
        when(arg) {
            is CameraDeleteRes -> {
                runOnUiThread {
                    DialogManager.instance.cancelLoading()
                    if (arg.status == 0)
                        view?.leave()
                }
            }
        }
    }

    private fun showLeaveDialog() {
        DialogManager.instance.showMessage(this, "離開後將關閉攝影機", true)?.let {
            it.setOnClickListener {
                DialogManager.instance.dismissDialog()
                DialogManager.instance.showLoading(this)
                APIManager.instance.doCameraDelete(CameraDeleteReq(cameraId))
            }
        }
    }

    private fun setListen() {
        img_volume.isActivated = true

        img_volume.setOnClickListener {
            it.isActivated = !it.isActivated
            setSpeaker(it.isActivated)

            val res = if (it.isActivated) R.drawable.volume else R.drawable.mute
            img_volume.setImageResource(res)
        }

        img_close.setOnClickListener {
            view?.leave()
        }
    }

    private fun setSpeaker(isOn: Boolean) {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        am.isSpeakerphoneOn = isOn

        val volume = if (isOn) am.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL) else 0
        am.setStreamVolume(AudioManager.STREAM_VOICE_CALL, volume, 0)

        Log.e("volume", "isSpeaker: ${am.isSpeakerphoneOn}, volume: $volume")
    }
}
