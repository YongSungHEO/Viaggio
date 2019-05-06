package com.kotlin.viaggio.view.traveling.country

import androidx.databinding.ObservableArrayList
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kotlin.viaggio.data.`object`.Area
import com.kotlin.viaggio.data.`object`.Country
import com.kotlin.viaggio.event.Event
import com.kotlin.viaggio.model.TravelLocalModel
import com.kotlin.viaggio.view.common.BaseViewModel
import java.io.InputStreamReader
import javax.inject.Inject

class TravelingDomesticsCountryFragmentViewModel @Inject constructor() : BaseViewModel() {
    @Inject
    lateinit var travelLocalModel: TravelLocalModel
    @Inject
    lateinit var gson: Gson

    val domesticsLiveData:MutableLiveData<Event<Any>> = MutableLiveData()

    var groupDomestics:List<Country> = listOf()
    val selectedCities = ObservableArrayList<Area>()
    var check = false
    override fun initialize() {
        super.initialize()

        val inputStream = InputStreamReader(appCtx.get().assets.open("domestics.json"))
        val type = object : TypeToken<List<Country>>() {}.type
        groupDomestics = gson.fromJson(inputStream, type)

        domesticsLiveData.value = Event(Any())

        val disposable = rxEventBus.travelSelectedCity.subscribe {
            if(check.not()){
                selectedCities.clear()
                selectedCities.addAll(it)
                domesticsLiveData.value = Event(Any())
            }
        }
        addDisposable(disposable)
    }
    fun selectedCity() {
        check = true
        rxEventBus.travelSelectedCity.onNext(selectedCities)
    }
}
