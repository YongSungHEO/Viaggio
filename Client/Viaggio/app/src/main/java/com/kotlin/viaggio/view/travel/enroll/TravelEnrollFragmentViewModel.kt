package com.kotlin.viaggio.view.travel.enroll

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.google.gson.Gson
import com.kotlin.viaggio.android.WorkerName
import com.kotlin.viaggio.data.`object`.Area
import com.kotlin.viaggio.data.`object`.PermissionError
import com.kotlin.viaggio.data.`object`.Travel
import com.kotlin.viaggio.data.`object`.TravelingError
import com.kotlin.viaggio.data.source.AndroidPrefUtilService
import com.kotlin.viaggio.event.Event
import com.kotlin.viaggio.model.TravelLocalModel
import com.kotlin.viaggio.view.common.BaseViewModel
import com.kotlin.viaggio.worker.TimeCheckWorker
import com.kotlin.viaggio.worker.UploadTravelWorker
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.text.DateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class TravelEnrollFragmentViewModel @Inject constructor() : BaseViewModel() {
    @Inject
    lateinit var travelLocalModel: TravelLocalModel
    @Inject
    lateinit var gson: Gson

    val goToCamera: MutableLiveData<Event<Any>> = MutableLiveData()
    val permissionRequestMsg: MutableLiveData<Event<PermissionError>> = MutableLiveData()
    val errorMsg: MutableLiveData<Event<TravelingError>> = MutableLiveData()
    val completeLiveData = MutableLiveData<Event<Any>>()

    private var travelThemeList: List<String> = listOf()

    val themeExist = ObservableBoolean(false)
    val countryExist = ObservableBoolean(false)
    val travelingStartOfDay = ObservableField<String>("")
    val travelingStartOfCountry = ObservableField<String>("")
    val travelThemes = ObservableField<String>("")

    private var startDate = Date()
    var endDate:Date? = null
    private val chooseCountry = mutableListOf<Area>()
    var travelKind: Int= 0

    override fun initialize() {
        super.initialize()
        if(TextUtils.isEmpty(travelingStartOfDay.get())){
            val cal = Calendar.getInstance()
            startDate = cal.time
            travelingStartOfDay.set(
                DateFormat.getDateInstance(DateFormat.LONG).format(startDate)
            )
        }
        travelKind = prefUtilService.getInt(AndroidPrefUtilService.Key.TRAVEL_KINDS).blockingGet()

        val themeDisposable = rxEventBus.travelOfTheme
            .subscribe { t ->
                if (t.isNotEmpty()) {
                    themeExist.set(true)
                    travelThemeList = t.map {
                        it.theme
                    }
                    travelThemes.set(travelThemeList.joinToString(", "))
                }
            }
        addDisposable(themeDisposable)
        val travelingStartOfDayDisposable = rxEventBus.travelingStartOfDay
            .subscribe({
                if(it.isNotEmpty()){
                    if(it.size > 1){
                        startDate = it[0]
                        endDate = it[1]
                        travelingStartOfDay.set(
                            "${DateFormat.getDateInstance(DateFormat.LONG).format(startDate)} ~ ${DateFormat.getDateInstance(DateFormat.LONG).format(endDate)}"
                        )
                        rxEventBus.travelingStartOfDay.onNext(listOf())
                    }else{
                        startDate = it[0]
                        travelingStartOfDay.set(
                            DateFormat.getDateInstance(DateFormat.LONG).format(startDate)
                        )
                        rxEventBus.travelingStartOfDay.onNext(listOf())
                    }
                }
            }) {
                Timber.d(it)
            }
        addDisposable(travelingStartOfDayDisposable)
        val countryDisposable = rxEventBus.travelSelectedCity.subscribe { t ->
            if(t.isNotEmpty()){
                countryExist.set(true)
                chooseCountry.clear()
                chooseCountry.addAll(t)
                val cities = chooseCountry.map {
                    "${it.country}_${it.city}"
                }
                travelingStartOfCountry.set(cities.joinToString(","))
            }else{
                countryExist.set(false)
                travelingStartOfCountry.set("")
            }
        }
        addDisposable(countryDisposable)
    }

    fun permissionCheck(request: Observable<Boolean>?) {
        val disposable = request?.subscribe { t ->
            if (t) {
                goToCamera.value = Event(Any())
            } else {
                permissionRequestMsg.value = Event(PermissionError.NECESSARY_PERMISSION)
            }
        }
        disposable?.let { addDisposable(it) }
    }

    @SuppressLint("RestrictedApi")
    fun travelStart(): Boolean {
        val travel = Travel(
            area = chooseCountry,
            startDate = startDate,
            endDate = endDate,
            theme = travelThemeList.toMutableList(),
            travelKind = travelKind
        )
        prefUtilService.putString(AndroidPrefUtilService.Key.TRAVELING_LAST_COUNTRIES, "${chooseCountry[0].country}_${chooseCountry[0].city}")
            .observeOn(Schedulers.io()).blockingAwait()

        val disposable = travelLocalModel.createTravel(travel)
            .flatMap { t ->
                if(endDate == null){
                    travelingSetting()
                    prefUtilService.putLong(AndroidPrefUtilService.Key.TRAVELING_ID, t).observeOn(Schedulers.io())
                        .blockingAwait()
                }
                travel.id = t
                prefUtilService.getString(AndroidPrefUtilService.Key.TOKEN_ID)
            }
            .subscribe({ token ->
                val mode = prefUtilService.getInt(AndroidPrefUtilService.Key.UPLOAD_MODE).blockingGet()
                if (TextUtils.isEmpty(token).not() && mode != 2) {
                    val constraints =
                        if (mode == 0) {
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build()
                        } else {
                            Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .setRequiresCharging(true)
                                .build()
                        }

                    val resultJsonTravel = gson.toJson(travel)

                    val data = Data.Builder()
                        .putString(WorkerName.TRAVEL.name, resultJsonTravel)
                        .build()
                    val travelWork = OneTimeWorkRequestBuilder<UploadTravelWorker>()
                        .setConstraints(constraints)
                        .setInputData(data)
                        .build()

                    WorkManager.getInstance().enqueue(travelWork)
                }
                completeLiveData.postValue(Event(Any()))
            }) {
                Timber.e(it)
            }
        addDisposable(disposable)

        val timeCheckWork = PeriodicWorkRequestBuilder<TimeCheckWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance().enqueueUniquePeriodicWork(WorkerName.TRAVELING_OF_DAY_CHECK.name, ExistingPeriodicWorkPolicy.KEEP, timeCheckWork)
        return true
    }

    private fun travelingSetting(){
        val cal = Calendar.getInstance()
        prefUtilService.putBool(AndroidPrefUtilService.Key.TRAVELING, true).observeOn(Schedulers.io()).blockingAwait()
        val currentConnectOfDay = cal.get(Calendar.DAY_OF_MONTH)
        prefUtilService.putInt(AndroidPrefUtilService.Key.LAST_CONNECT_OF_DAY, currentConnectOfDay)
            .observeOn(Schedulers.io()).blockingAwait()
        val day = Math.floor(
            ((cal.time.time - startDate.time).toDouble() / 1000) / (24 * 60 * 60)
        ).toInt()
        cal.time = startDate

        prefUtilService.putInt(AndroidPrefUtilService.Key.TRAVELING_OF_DAY_COUNT, day + 1)
            .observeOn(Schedulers.io()).blockingAwait()
    }

    override fun onCleared() {
        super.onCleared()
        rxEventBus.travelOfTheme.onNext(listOf())
    }
}
