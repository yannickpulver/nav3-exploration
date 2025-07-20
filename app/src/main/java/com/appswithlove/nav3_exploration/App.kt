package com.appswithlove.nav3_exploration

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.appswithlove.nav3_exploration.ui.detail.DetailViewModel
import com.appswithlove.nav3_exploration.ui.home.HomeViewModel
import com.appswithlove.nav3_exploration.ui.login.LoginViewModel
import com.appswithlove.nav3_exploration.ui.navigation.RouteViewModel
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

class App : Application() {


    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            val module = module {
                viewModelOf(::RouteViewModel)
                viewModelOf(::HomeViewModel)
                viewModelOf(::DetailViewModel)
                viewModelOf(::LoginViewModel)
                single<ObservableSettings> { SharedPreferencesSettings(provideSettingsPreferences(get())) }
            }

            modules(module)
        }


    }
}

private const val PREFERENCES_FILE_KEY = "com.appswithlove.nav3_exploration.preferences"
private fun provideSettingsPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences(PREFERENCES_FILE_KEY, Context.MODE_PRIVATE)
}