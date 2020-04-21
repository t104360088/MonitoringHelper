package com.example.networkcamera.dataType

class CameraListRes(val result: Result) : BaseRespond() {
    class Result(val list: Array<Camera>)

    class Camera(val id: String, val name: String)
}