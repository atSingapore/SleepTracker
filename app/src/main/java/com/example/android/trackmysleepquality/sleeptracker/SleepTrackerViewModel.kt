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

package com.example.android.trackmysleepquality.sleeptracker

import android.app.Application
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.android.trackmysleepquality.database.SleepDatabaseDao
import com.example.android.trackmysleepquality.database.SleepNight
import com.example.android.trackmysleepquality.formatNights
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * ViewModel for SleepTrackerFragment.
 */
// Takes in application context as a parameter to access resources
// Needs access to data in DB (so pass in instance of database DAO)
// Need factory to instantiate VM and provide it with the data source
class SleepTrackerViewModel(
        val database: SleepDatabaseDao,
        application: Application) : AndroidViewModel(application) {

    // Will hold the current night
    private var tonight = MutableLiveData<SleepNight?>()

    // Will hold all nights from the database
    private var nights = database.getAllNights()

    // Define a mapping function as calling formatNights, giving it nights and access to our string resources
    val nightsString = Transformations.map(nights) { nights ->
        formatNights(nights, application.resources)
    }

    // Initialize tonight
    init {
        initializeTonight()
    }

    private fun initializeTonight()
    {
        // Start a coroutine in the ViewModelScope to get tonight from the database
        // This is done so that we are not blocking the UI while waiting for the result
        // In the scope we launch the coroutine, it is created in the context defined by the scope
        viewModelScope.launch {
            // Get the value for tonight from the database
            tonight.value = getTonightFromDatabase()
        }

    }

    // We want to make sure that getTonightFromDatabase does not block
    // Marked as 'suspend' because we want to call it from inside the coroutine and not block
    private suspend fun getTonightFromDatabase() : SleepNight? {

        // Get tonight form the database
        var night = database.getTonight()

        // If the start and end times are not the same, meaning, the night has already completed
        // then return null
        // otherwise return night
        if(night?.endTimeMilli != night?.startTimeMilli)
        {
            night = null
        }
        return night
    }

    // Needed to make this suspend because it calls insert
    fun onStartTracking() {

        // Create a new sleep night
        val newNight = SleepNight()

        // Insert it into the database
        runBlocking {
            insert(newNight)
        }

        // Set tonight to the new night
        tonight.value = newNight
    }

    private suspend fun insert(night: SleepNight)
    {
        // Insert the night into the database
        database.insert(night)
    }

    fun onStopTracking()
    {
        // Need to use a coroutine because everything here will be time consuming
        viewModelScope.launch {


            // return@label is used for specifying which function among several nested ones the statement returns from
            // In this case, we are specifying to return from launch
            val oldNight = tonight.value ?: return@launch

            // Set the endTimeMilli to the current system time
            oldNight.endTimeMilli = System.currentTimeMillis()

            // Call update with the night
            update(oldNight)
        }
    }

    private suspend fun update(night: SleepNight)
    {
        database.update(night)
    }

    fun onClear()
    {
        viewModelScope.launch {
            clear()
            tonight.value = null
        }
    }

    suspend fun clear() {
        database.clear()
    }


}

