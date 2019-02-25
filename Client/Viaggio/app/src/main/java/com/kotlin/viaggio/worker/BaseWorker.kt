package com.kotlin.viaggio.worker

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.kotlin.viaggio.android.ClearCache
import com.kotlin.viaggio.android.TimeHelper
import com.kotlin.viaggio.ioc.module.common.AndroidWorkerInjection
import com.kotlin.viaggio.model.TravelModel
import javax.inject.Inject

abstract class BaseWorker(context: Context, params: WorkerParameters):Worker(context, params){
    @Inject
    lateinit var timeHelper: TimeHelper
    @Inject
    lateinit var travelModel: TravelModel
    @Inject
    lateinit var clearCache: ClearCache

    override fun doWork(): Result {
        AndroidWorkerInjection.inject(this)
        return Result.success()
    }
}