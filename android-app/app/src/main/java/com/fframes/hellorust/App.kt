package com.fframes.hellorust
import android.app.Application

class App : Application() {
    override fun onCreate() {
        super.onCreate()
            System.loadLibrary("hello_rust_lib")
    }
}
