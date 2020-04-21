package com.example.networkcamera.dataType

data class CameraSetReq(val name: String)

class CameraSetRes(val result: Result) : BaseRespond() {
    class Result(val id: String, val name: String)
}