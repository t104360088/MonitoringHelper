package com.example.networkcamera

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.networkcamera.dataType.CameraListRes

class ListCameraAdapter(context: Context, list: Array<CameraListRes.Camera>):
    ArrayAdapter<CameraListRes.Camera>(context, R.layout.item_camera, list) {

    private class ViewHolder(v: View) {
        val tv_name: TextView = v.findViewById(R.id.tv_name)
    }

    override fun getView(position: Int, convertView: View?, viewGroup: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if(convertView == null){
            view = View.inflate(context, R.layout.item_camera, null)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
            view = convertView
        }

        val item = getItem(position) ?: return view

        holder.tv_name.text = item.name

        return view
    }
}