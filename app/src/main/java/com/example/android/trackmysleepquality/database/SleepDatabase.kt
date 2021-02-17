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

package com.example.android.trackmysleepquality.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// An abstract class that extends RoomDatabase(), annotated with @Database and passing in args that declare entity end set the version number
// Can have multiple tables
// Whenever you change the schema, you have to up the version number
// esportSchema saves the schema of the database to a folder which provides you with a version history of your DB (useful for complex DBs that change often)
@Database(entities = [SleepNight::class], version = 1, exportSchema = false)
abstract class SleepDatabase : RoomDatabase() {

    // Need to tell it about the DAO associated with our entity so that we can interact with the DB
    // Associating with the DAO (data access object), by creating an abstract property that returns the DAO
    // Can have multiple DAOs
    abstract val sleepDatabaseDao: SleepDatabaseDao

    // Define a companion object
    // Companion objects allows clients to access the methods creating or getting the DB, without instantiating the class
    // This class is only to provide the DB so no need to instantiate it
    companion object {

        // Declare private nullable variable for the database
        // Will keep a reference to the DB once we have one
        // Will keep us from repeatedly opening up connections to DB (expensive)
        // Volatile makes sure value of instance is up to date, and the same to all execution threads (will never be cached, and all read/writes done on main memory)
        @Volatile
        private var INSTANCE: SleepDatabase? = null

        // Get a reference to the database
        // This method will return a reference fo the SleepDataBase
        // Going to use a DB builder and it will require a context
        fun getInstance(context: Context): SleepDatabase {

            // Multiple threads can ask for a DB instance at the same time
            // Wrapping code in synchronized means only one thread of execution at a time can enter this block of code
            // Ensures DB only gets initialised once
            // Need to pass 'this' in so that we have access to the context
            synchronized(this) {

                // Copy the current value of instance to a local variable
                // smartcast is only available to local variables, not class variables
                var instance = INSTANCE

                // Use Room's Database building to create the database only if it doesn't exist, otherwise return the existing database
                if (instance == null) {

                    // Create the DB with rooms DB builder
                    // Pass in a reference to the SleepDatabase class to tell it what DB to build
                    // Give the DB a name
                    instance = Room.databaseBuilder(
                            context.applicationContext,
                            SleepDatabase::class.java,
                            "sleep_history_database"
                    )
                            // Normally when creating DB have to pass in a migration object with a migration strategy
                            // If we change the DB schema, need a way to convert the existing object to the new schema
                            // Migration object defines how you will take all rows with the old schema, and convert them to rows with the new schema
                            // So that when user upgrades to a new version of the app, their data would be lost (we wipe the data and create new)
                            // Required migration strategy to the builder
                            // For more info see https://developer.android.com/training/data-storage/room/migrating-db-versions
                            .fallbackToDestructiveMigration()

                            // Build the DB
                            .build()

                    // Assign instance to the newly created DB
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
