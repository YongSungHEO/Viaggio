package com.kotlin.viaggio.view.travel.enroll

import android.annotation.SuppressLint
import android.text.TextUtils
import androidx.databinding.ObservableBoolean
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.work.*
import com.google.gson.Gson
import com.kotlin.viaggio.android.WorkerName
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

    var travelType: String = ""
    override fun initialize() {
        super.initialize()
        val cal = Calendar.getInstance()
        travelingStartOfDay.set(
            DateFormat.getDateInstance(DateFormat.LONG).format(cal.time)
        )

        val themeDisposable = rxEventBus.travelOfTheme
            .subscribe { t ->
                if (t.isNotEmpty()) {
                    themeExist.set(true)
                    travelThemeList = t
                    travelThemes.set(travelThemeList.joinToString(", "))
                }
            }
        addDisposable(themeDisposable)
        val travelingStartOfDayDisposable = rxEventBus.travelingStartOfDay
            .subscribe({
                if (TextUtils.isEmpty(it).not()) {
                    travelingStartOfDay.set(it)
                    rxEventBus.travelingStartOfDay.onNext("")
                }
            }) {

            }
        addDisposable(travelingStartOfDayDisposable)
        val countryDisposable = rxEventBus.travelOfCountry.subscribe { t ->
            if (TextUtils.isEmpty(t).not()) {
                countryExist.set(true)
                travelingStartOfCountry.set(t)
                rxEventBus.travelOfCountry.onNext("")
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

    fun changeStartOfDay(startOfDay: String) {
        travelingStartOfDay.set(startOfDay)
        rxEventBus.travelingStartOfDay.onNext(startOfDay)
    }



    @SuppressLint("RestrictedApi")
    fun travelStart(): Boolean {
        when {
            TextUtils.isEmpty(travelingStartOfCountry.get()) -> {
                errorMsg.value = Event(TravelingError.COUNTRY_EMPTY)
                return false
            }
            themeExist.get().not() -> {
                errorMsg.value = Event(TravelingError.THEME_EMPTY)
                return false
            }
        }

        val cal = Calendar.getInstance()
        prefUtilService.putBool(AndroidPrefUtilService.Key.TRAVELING, true).observeOn(Schedulers.io()).blockingAwait()
        val currentConnectOfDay = cal.get(Calendar.DAY_OF_MONTH)
        prefUtilService.putInt(AndroidPrefUtilService.Key.LAST_CONNECT_OF_DAY, currentConnectOfDay)
            .observeOn(Schedulers.io()).blockingAwait()

        val travel = Travel(
            entireCountries = arrayListOf(travelingStartOfCountry.get()!!),
            startDate = DateFormat.getDateInstance(DateFormat.LONG).parse(
                travelingStartOfDay.get()!!
            ),
            theme = travelThemeList.toMutableList() as ArrayList<String>,
            travelType = travelType
        )
        prefUtilService.putString(AndroidPrefUtilService.Key.TRAVELING_LAST_COUNTRIES, travelingStartOfCountry.get()!!)
            .observeOn(Schedulers.io()).blockingAwait()

        val disposable = travelLocalModel.createTravel(travel)
            .flatMap { t ->
                prefUtilService.putLong(AndroidPrefUtilService.Key.TRAVELING_ID, t).observeOn(Schedulers.io())
                    .blockingAwait()
                val day = Math.floor(
                    ((cal.time.time - DateFormat.getDateInstance(DateFormat.LONG).parse(
                        travelingStartOfDay.get()!!
                    ).time).toDouble() / 1000) / (24 * 60 * 60)
                ).toInt()
                cal.time =
                    DateFormat.getDateInstance(DateFormat.LONG).parse(travelingStartOfDay.get()!!)

                prefUtilService.putInt(AndroidPrefUtilService.Key.TRAVELING_OF_DAY_COUNT, day + 1)
                    .observeOn(Schedulers.io()).blockingAwait()
                travel.id = t
                prefUtilService.getString(AndroidPrefUtilService.Key.TOKEN_ID)
            }
            .subscribe({ token ->
                if (TextUtils.isEmpty(token).not()) {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()

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
        WorkManager.getInstance().enqueueUniquePeriodicWork(
            WorkerName.TRAVELING_OF_DAY_CHECK.name,
            ExistingPeriodicWorkPolicy.KEEP,
            timeCheckWork
        )

        return true
    }
}
