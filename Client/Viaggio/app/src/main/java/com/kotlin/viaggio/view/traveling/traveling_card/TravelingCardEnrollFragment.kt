package com.kotlin.viaggio.view.traveling.traveling_card

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kotlin.viaggio.R
import com.kotlin.viaggio.databinding.ItemTravelingCardImageBinding
import com.kotlin.viaggio.view.common.BaseFragment
import kotlinx.android.synthetic.main.fragment_traveling_card_enroll.*
import kotlinx.android.synthetic.main.item_traveling_card_image.view.*
import org.jetbrains.anko.imageView
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.verticalLayout
import java.text.SimpleDateFormat
import java.util.*


class TravelingCardEnrollFragment : BaseFragment<TravelingCardEnrollFragmentViewModel>() {
    lateinit var binding: com.kotlin.viaggio.databinding.FragmentTravelingCardEnrollBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_traveling_card_enroll, container, false)
        binding.viewModel = getViewModel()
        binding.viewHandler = ViewHandler()
        return binding.root
    }

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        travelCardEnrollImageAllList.layoutManager = GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false)
        getViewModel().imagePathList.observe(this, Observer {
            it.getContentIfNotHandled()?.let { list ->
                travelCardEnrollImageAllList.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
                    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                        TravelingCardImgViewHolder(
                            LayoutInflater.from(parent.context).inflate(
                                R.layout.item_traveling_card_image,
                                parent,
                                false
                            )
                        )

                    override fun getItemCount() = list.size
                    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                        holder as TravelingCardImgViewHolder
                        holder.imageBinding(list[position])
                        holder.binding?.viewHandler = holder.TravelingCardImgViewHandler()
                        holder.binding?.chooseCount = getViewModel().chooseCountList[position]
                    }
                }
                Glide.with(context!!)
                    .load(list[0])
                    .into(travelCardEnrollImg1)

                val width = context!!.resources.displayMetrics.widthPixels
                val layoutParams = travelCardEnrollImg1.layoutParams
                layoutParams.width = width/2
                travelCardEnrollImg1.layoutParams = layoutParams
            }
        })
    }

    inner class ViewHandler {
        fun next() {
            getViewModel().additional.set(getViewModel().additional.get().not())
        }

        fun back() {
            if (getViewModel().additional.get()) {
                getViewModel().additional.set(getViewModel().additional.get().not())
            } else {
                fragmentPopStack()
            }
        }

        fun save() {
            if (getViewModel().checkAdditional()) {
                showLoading()
                getViewModel().saveTravelCard()
            } else {
                toast(resources.getString(R.string.enterTravelCardNotice))
            }
        }

        @SuppressLint("SimpleDateFormat")
        fun enrollOfTime() {
            val cal = Calendar.getInstance()
            TimePickerDialog(context!!, TimePickerDialog.OnTimeSetListener { timePicker, i, i1 ->
                cal.set(Calendar.HOUR_OF_DAY, timePicker.hour)
                cal.set(Calendar.MINUTE, timePicker.minute)
                getViewModel().time.set(SimpleDateFormat(resources.getString(R.string.dateTimeFormat)).format(cal.time))
            }, Calendar.HOUR_OF_DAY, Calendar.MINUTE, true).show()
        }

        fun transportation() {
            TravelingTransportationBottomSheetDialogFragment().show(
                fragmentManager!!,
                TravelingTransportationBottomSheetDialogFragment.TAG
            )
        }
    }

    inner class TravelingCardImgViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = DataBindingUtil.bind<ItemTravelingCardImageBinding>(itemView)
        private lateinit var fileNamePath: String

        fun imageBinding(string: String) {
            fileNamePath = string
            val layoutParams = itemView.travelingCardContainer.layoutParams
            layoutParams.width = width / 4
            layoutParams.height = width / 4
            itemView.travelingCardContainer.layoutParams = layoutParams

            binding?.let {
                Glide.with(itemView)
                    .load(string)
                    .into(itemView.travelingCardListImage)
            }
        }

        inner class TravelingCardImgViewHandler {
            fun imagePicker() {
                if (binding?.chooseCount?.get() == 0) {
                    getViewModel().entireChooseCount += 1
                    binding.chooseCount?.set(getViewModel().entireChooseCount)
                    getViewModel().imageChooseList.add(fileNamePath)
                    when (getViewModel().entireChooseCount) {
                        2 -> {
                            travelCardEnrollImg2.visibility = View.VISIBLE
                            val layoutParams = travelCardEnrollImg2.layoutParams
                            layoutParams.width = width/2
                            travelCardEnrollImg2.layoutParams = layoutParams
                            Glide.with(context!!)
                                .load(fileNamePath)
                                .into(travelCardEnrollImg2)
                        }
                        3 -> {
                            travelCardEnrollImg3.visibility = View.VISIBLE
                            val layoutParams = travelCardEnrollImg3.layoutParams
                            layoutParams.width = width/2
                            travelCardEnrollImg3.layoutParams = layoutParams
                            Glide.with(context!!)
                                .load(fileNamePath)
                                .into(travelCardEnrollImg3)
                        }
                        4 -> {
                            travelCardEnrollImg4Container.visibility = View.VISIBLE
                            val layoutParams = travelCardEnrollImg4Container.layoutParams
                            layoutParams.width = width/2
                            travelCardEnrollImg4Container.layoutParams = layoutParams
                            Glide.with(context!!)
                                .load(fileNamePath)
                                .into(travelCardEnrollImg4)
                        }
                        else -> {
                            getViewModel().overImageCount.set(getViewModel().overImageCount.get().plus(1))
                        }
                    }
                } else {
                    if (getViewModel().entireChooseCount != 1) {
                        getViewModel().imageChooseList.remove(fileNamePath)
                        getViewModel().entireChooseCount -= 1
                        binding?.chooseCount?.set(0)
                        for ((i, s) in getViewModel().imageChooseList.withIndex()) {
                            val index = getViewModel().imageAllList.indexOf(s)
                            getViewModel().chooseCountList[index].set(i + 1)
                        }
                        when(getViewModel().entireChooseCount){
                            3 ->{
                                travelCardEnrollImg4Container.visibility = View.GONE
                            }
                            2->{
                                travelCardEnrollImg3.visibility = View.GONE
                            }
                            1->{
                                travelCardEnrollImg2.visibility = View.GONE
                            }
                            else ->{
                                getViewModel().overImageCount.set(getViewModel().overImageCount.get().minus(1))
                            }
                        }

                        for ((i,s) in getViewModel().imageChooseList.withIndex()) {
                            if(i > 3){ break }
                            Glide.with(context!!)
                                .load(s)
                                .into(when(i){
                                    0->travelCardEnrollImg1
                                    1->travelCardEnrollImg2
                                    2->travelCardEnrollImg3
                                    3->travelCardEnrollImg4
                                    else ->travelCardEnrollImg1
                                })
                        }
                    }
                }
            }
        }
    }
}