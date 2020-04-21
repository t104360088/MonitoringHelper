package com.example.networkcamera.dataType

data class CameraDeleteReq(val id: String)

class CameraDeleteRes(val result: Result) : BaseRespond() {
    class Result(val id: String)
}