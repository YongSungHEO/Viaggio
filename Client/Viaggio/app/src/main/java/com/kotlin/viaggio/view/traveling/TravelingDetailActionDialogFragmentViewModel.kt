package com.kotlin.viaggio.view.traveling

import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.kotlin.viaggio.android.WorkerName
import com.kotlin.viaggio.data.`object`.TravelCard
import com.kotlin.viaggio.data.source.AndroidPrefUtilService
import com.kotlin.viaggio.event.Event
import com.kotlin.viaggio.model.TravelLocalModel
import com.kotlin.viaggio.view.common.BaseViewModel
import com.kotlin.viaggio.worker.TimeCheckWorker
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TravelingDetailActionDialogFragmentViewModel @Inject constructor() : BaseViewModel() {
    @Inject
    lateinit var travelLocalModel: TravelLocalModel

    val completeLiveDate: MutableLiveData<Event<Any>> = MutableLiveData()

    var travelCard = TravelCard()
    var location:IntArray? = null

    val contents = ObservableField<String>()
    override fun initialize() {
        super.initialize()

        val disposable = travelLocalModel.getTravelCard()
            .subscribe({
                if(it.isNotEmpty()){
                    travelCard = it[0]
                    contents.set(travelCard.content)
                }
            }){
                Timber.d(it)
            }
        addDisposable(disposable)
    }

    fun save() {
        if(travelCard.id != 0L){
            travelCard.content = contents.get()!!
            travelCard.userExist = false
            val disposable = travelLocalModel.updateTravelCard(travelCard)
                .subscribe({
                    rxEventBus.travelCardUpdate.onNext(Any())
                    completeLiveDate.postValue(Event(Any()))
                }){
                    Timber.d(it)
                }
            addDisposable(disposable)
        }
    }
}