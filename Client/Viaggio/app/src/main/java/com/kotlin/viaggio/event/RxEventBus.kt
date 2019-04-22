@file:Suppress("unused")

package com.kotlin.viaggio.event

import android.graphics.Bitmap
import com.kotlin.viaggio.data.`object`.Area
import com.kotlin.viaggio.data.`object`.Country
import com.kotlin.viaggio.data.`object`.ThemeData
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RxEventBus @Inject constructor() {
    val travelOfCountry:BehaviorSubject<String> = BehaviorSubject.create()
    val travelOfGo:BehaviorSubject<Any> = BehaviorSubject.create()
    val travelOfFirstImage: BehaviorSubject<Bitmap> = BehaviorSubject.create()
    val travelOfTheme: BehaviorSubject<List<ThemeData>> = BehaviorSubject.create()
    val travelOfDayTheme: BehaviorSubject<List<String>> = BehaviorSubject.create()
    val travelOfDayChange: BehaviorSubject<Boolean> = BehaviorSubject.create()
    val travelCardTransportation: BehaviorSubject<String> = BehaviorSubject.create()
    val travelCardUpdate: BehaviorSubject<Any> = BehaviorSubject.create()
    val travelOfDayImage: BehaviorSubject<String> = BehaviorSubject.create()
    val travelOfCountryChange:BehaviorSubject<Any> = BehaviorSubject.create()
    val travelFinish:BehaviorSubject<Boolean> = BehaviorSubject.create()

    val travelCardImages: PublishSubject<List<Bitmap>> = PublishSubject.create()
    val travelingStartOfDay:BehaviorSubject<List<Date>> = BehaviorSubject.create()
    val travelType:BehaviorSubject<Int> = BehaviorSubject.create()
    val travelCountry:BehaviorSubject<Country> = BehaviorSubject.create()
    val travelCity:PublishSubject<List<Area>> = PublishSubject.create()
}