package com.kotlin.viaggio.view.setting

import android.text.TextUtils
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableBoolean
import androidx.lifecycle.MutableLiveData
import com.kotlin.viaggio.data.source.AndroidPrefUtilService
import com.kotlin.viaggio.event.Event
import com.kotlin.viaggio.model.UserModel
import com.kotlin.viaggio.view.common.BaseViewModel
import javax.inject.Inject

class SettingLockActionDialogFragmentViewModel @Inject constructor() : BaseViewModel() {
    @Inject
    lateinit var userModel: UserModel

    var enrollMode = ObservableBoolean(false)
    val password = ObservableArrayList<Int>()
    var confirmPassword = ""

    val completeLiveData: MutableLiveData<Event<Boolean>> = MutableLiveData()
    val fingerPrintLiveData: MutableLiveData<Event<Any>> = MutableLiveData()
    var currentPosition = 0
    override fun initialize() {
        super.initialize()
        settingPassword()

        if(prefUtilService.getBool(AndroidPrefUtilService.Key.FINGER_PRINT_LOCK_APP).blockingGet()){
            fingerPrintLiveData.value = Event(Any())
        }
    }
    fun settingPassword(){
        password.clear()
        currentPosition = 0
        for(i in 0 until 4){
            password.add(null)
        }
    }
    fun choose(num:Int){
        password[currentPosition] = num
        currentPosition += 1
        if(currentPosition == 4){
            if(enrollMode.get()){
                if(TextUtils.isEmpty(confirmPassword)){
                    confirmPassword = password.joinToString()
                    settingPassword()
                } else {
                    val pw = password.joinToString()
                    if(pw == confirmPassword){
                        prefUtilService.putString(AndroidPrefUtilService.Key.LOCK_PW, pw).blockingAwait()
                        prefUtilService.putBool(AndroidPrefUtilService.Key.LOCK_APP, true).blockingAwait()
                        rxEventBus.completeLock.onNext(Any())
                        completeLiveData.value = Event(true)
                    } else {
                        settingPassword()
                        completeLiveData.value = Event(false)
                    }

                }
            } else {
                val lockPassword = password.joinToString()
                if(lockPassword == prefUtilService.getString(AndroidPrefUtilService.Key.LOCK_PW).blockingGet()){
                    completeLiveData.value = Event(true)
                } else {
                    settingPassword()
                    completeLiveData.value = Event(false)
                }

            }
        }
    }
    fun cancel() {
        if(currentPosition != 0){
            currentPosition -= 1
            password[currentPosition] = null
        }
    }
}