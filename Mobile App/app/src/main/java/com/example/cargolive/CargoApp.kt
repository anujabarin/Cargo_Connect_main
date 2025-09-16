package com.example.cargolive

import android.app.Application
import com.example.cargolive.data.api.RetrofitClient

class CargoApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RetrofitClient.init(this)
    }
}
