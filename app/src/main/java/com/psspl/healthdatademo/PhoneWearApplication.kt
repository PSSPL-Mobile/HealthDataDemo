package com.psspl.healthdatademo

import android.app.Application
import androidx.room.Room
import com.psspl.healthdatademo.di.AppDatabase
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/***
 * Name : PhoneWearApplication.kt
 * Author : Prakash Software Pvt Ltd
 * Date : 27 Jun 2025
 * Desc : Application class for the Weather Dashboard app.
 * */
@HiltAndroidApp
class PhoneWearApplication : Application()