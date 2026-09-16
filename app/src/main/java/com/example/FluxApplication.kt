package com.example

import android.app.Application

class FluxApplication : Application() {
    lateinit var container: FluxAppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = FluxAppContainer(this)
    }
}
