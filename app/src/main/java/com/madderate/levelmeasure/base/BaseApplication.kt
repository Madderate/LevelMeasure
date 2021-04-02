package com.madderate.levelmeasure.base

import android.app.Application
import android.content.Context

/**
 * @author      madderate
 * @date        4/2/21 9:35 PM
 * @description
 */
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }


    companion object {
        lateinit var appContext: Context
    }
}