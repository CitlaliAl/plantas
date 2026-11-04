package com.upchiapas.plantas

import android.app.Application
import com.upchiapas.plantas.di.AppContainer

class PlantasApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
