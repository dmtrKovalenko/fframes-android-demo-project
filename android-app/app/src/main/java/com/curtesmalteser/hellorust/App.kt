package com.curtesmalteser.hellorust

import android.app.Application

/**
 * Created by António Bastião on 26.11.2024
 * Refer to <a href="https://github.com/CurtesMalteser">CurtesMalteser github</a>
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
            System.loadLibrary("hello_rust_lib")
    }
}