/*
 * Copyright 2018, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.sleeptracker.sleeptracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.sleeptracker.database.SleepDatabaseDao
import com.example.sleeptracker.database.SleepNight
import com.example.sleeptracker.formatNights
import kotlinx.coroutines.*

/**
 * ViewModel for SleepTrackerFragment.
 */
class SleepTrackerViewModel(
    private val database: SleepDatabaseDao,
    application: Application
) : AndroidViewModel(application) {

    // Allow us to cancel all coroutines started by this model when viewModel is no longer used and destroyed
    // so that we don't end up with coroutines that have no nowhere to return to
    private var viewModelJob = Job()

    // we need scope for coroutine to run in. scope determines what thread coroutine will run on, and it also needs to know
    // about the job
    // the coroutine launched in this scope will run on mainThread, this is sensible for many coroutines started by viewModel
    // as they will eventually result in update of ui after performing some processing
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var tonight = MutableLiveData<SleepNight?>()

    val nights = database.getAllNights()

    val startButtonVisible = Transformations.map(tonight) {
        it == null
    }

    val stopButtonVisible = Transformations.map(tonight) {
        it != null
    }

    val clearButtonVisible = Transformations.map(nights) {
        it?.isNotEmpty()
    }

    /**
     * Navigate to sleepQualityFragment event
     */
    // value of LiveData is SleepNight object as the fragment we navigate to it needs this object to update it with quality
    private val _eventNavigateToSleepQuality = MutableLiveData<SleepNight?>()

    val eventNavigateToSleepQuality: LiveData<SleepNight?>
        get() = _eventNavigateToSleepQuality

    fun onNavigationToSleepQualityComplete() {
        _eventNavigateToSleepQuality.value = null
    }

    /**
     * Show snackbar event
     */
    private val _eventShowSnackbar = MutableLiveData<Boolean>()

    val eventShowSnackbar: LiveData<Boolean>
        get() = _eventShowSnackbar

    fun onShowSnackbarComplete(){
        _eventShowSnackbar.value = false
    }

    // this transformation map is executed every time nights receives new data from the database
    val nightsString = Transformations.map(nights) { nights ->
        // because this function has nothing to with viewModel and could be used by any other class, it's a utility function
        formatNights(nights, application.resources)
    }

    init {
        _eventShowSnackbar.value = false
        _eventNavigateToSleepQuality.value = null

        initializeTonight()
    }

    private fun initializeTonight() {
        // we use coroutine to get tonight from database
        // we specify scope which is uiScope, in that scope we launch coroutine, this creates coroutine without
        // blocking the current thread in the context defined by the scope
        // the coroutine is scheduled to execute immediately unless you specify otherwise
        uiScope.launch {
            tonight.value = getTonightFromDatabase()
        }
    }

    // suspend because we want to call it from inside coroutine and not block
    private suspend fun getTonightFromDatabase(): SleepNight? {
        // we get data from database using another coroutine in IO context using IO dispatcher
        return withContext(Dispatchers.IO) {
            var night = database.getTonight()
            if (night?.endTimeMilli != night?.startTimeMilli)
                night = null
            night
        }
    }

    // called when start button is pressed
    fun onStartTracking() {
        // we launch coroutine because insert is suspend function, and to call suspend function, we need to call it either
        // from inside another suspend function or from a coroutine
        // we make the scope is uiScope because we need the result to continue and update the UI
        uiScope.launch {
            val newNight = SleepNight()

            insert(newNight)

            tonight.value = getTonightFromDatabase()
        }

    }

    // insert is suspend function because it makes expensive processing (insert into database), so we should ensure that
    // it does not block mainThread by making the insert operation inside a coroutine
    private suspend fun insert(night: SleepNight) {
        withContext(Dispatchers.IO) {
            database.insert(night)
        }
    }

    fun onStopTracking() {
        uiScope.launch {
            val oldTonight = tonight.value ?: return@launch

            oldTonight.endTimeMilli = System.currentTimeMillis()

            update(oldTonight)
            _eventNavigateToSleepQuality.value = oldTonight
        }
    }

    private suspend fun update(oldTonight: SleepNight) {
        withContext(Dispatchers.IO) {
            database.update(oldTonight)
        }
    }

    fun onClear() {
        uiScope.launch {
            clear()
            tonight.value = null
            _eventShowSnackbar.value = true
        }
    }

    private suspend fun clear() {
        withContext(Dispatchers.IO) {
            database.clear()
        }
    }

    // when viewModel is destroyed, onCleared is called. here we can cancel all couroutines started from viewModel
    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}

