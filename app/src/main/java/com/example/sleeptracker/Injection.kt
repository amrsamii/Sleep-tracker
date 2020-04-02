package com.example.sleeptracker

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.example.sleeptracker.database.SleepDatabaseDao
import com.example.sleeptracker.sleepquality.SleepQualityViewModelFactory
import com.example.sleeptracker.sleeptracker.SleepTrackerViewModelFactory

object Injection {

    fun provideSleepTrackerViewModelFactory(
        dataSource: SleepDatabaseDao,
        application: Application
    ): ViewModelProvider.Factory {
        return SleepTrackerViewModelFactory(dataSource, application)
    }

    fun provideSleepQualityViewModelFactory(
        sleepNightKey: Long,
        dataSource: SleepDatabaseDao
    ): ViewModelProvider.Factory {
        return SleepQualityViewModelFactory(sleepNightKey,dataSource)
    }
}