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

package com.example.android.trackmysleepquality.sleepquality

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Need to record the sleep quality
// Then navigate back to sleep tracker fragment
// The display should update automatically to show us the displayed value
// Need a ViewModel to go with the sleep quality
// A ViewModelFactory
// And to update the fragment

// Passing in the sleepnight key we got from the navigation
// Passing in the database from the factory
class SleepQualityViewModel(private val  sleepNightKey: Long = 0L, val database: SleepDatabaseDao) : ViewModel()
{
    // Needed to navigate back to the SleepTrackerFragment after recording the quality
    // Create the event variable with the backend property
    private val _navigateToSleepTracker = MutableLiveData<Boolean?>()
    val navigateToSleepTracker: LiveData<Boolean?>
    get() = _navigateToSleepTracker

    fun doneNavigating()
    {
        _navigateToSleepTracker.value = null
        Log.i("SleepQualityViewModel", "done navigating")
    }

    // Used for the click handler to set sleep quality
    fun onSetSleepQuality(quality: Int)
    {
        Log.i("SleepQualityViewModel", "Setting sleep quality")
        // Launch a coroutine
        viewModelScope.launch {

            // Get tonight from the database
            val tonight = database.get(sleepNightKey) ?: return@launch

            // Set the sleep quality to the quality passed in
            tonight.sleepQuality = quality

            // Update the database
            database.update(tonight)

            // Set the state to navigate back to the sleep tracker
            _navigateToSleepTracker.value = true

            Log.i("SleepQualityViewModel", "Navigating back to the sleep tracker")
        }
    }
}
