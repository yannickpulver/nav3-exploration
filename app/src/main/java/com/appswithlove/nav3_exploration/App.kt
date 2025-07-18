package com.appswithlove.nav3_exploration

import android.app.Application
import com.appswithlove.nav3_exploration.ui.detail.DetailViewModel
import com.appswithlove.nav3_exploration.ui.home.HomeViewModel
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class App : Application() {


    override fun onCreate() {
        super.onCreate()

        startKoin {
            val module = module {
                viewModelOf(::HomeViewModel)
                viewModelOf(::DetailViewModel)
            }

            modules(module)
        }


    }
}