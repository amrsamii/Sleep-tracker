package com.example.sleeptracker.sleeptracker

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.sleeptracker.R
import com.example.sleeptracker.convertDurationToFormatted
import com.example.sleeptracker.convertNumericQualityToString
import com.example.sleeptracker.database.SleepNight

// to tell data binding about this binding adapter, we have to annotate it
@BindingAdapter("sleepLengthText")
fun TextView.setSleepLengthText(item: SleepNight?) {
    // bind the data to the view
    item?.let {
        text = convertDurationToFormatted(item.startTimeMilli, item.endTimeMilli, context.resources)
    }
}

@BindingAdapter("qualityText")
fun TextView.setQualityText(item: SleepNight?) {
    item?.let {
        text = convertNumericQualityToString(item.sleepQuality, context.resources)
    }
}

@BindingAdapter("sleepImage")
fun ImageView.setSleepImage(item: SleepNight?) {
    item?.let {
        setImageResource(
            when (item.sleepQuality) {
                0 -> R.drawable.ic_sleep_0
                1 -> R.drawable.ic_sleep_1
                2 -> R.drawable.ic_sleep_2
                3 -> R.drawable.ic_sleep_3
                4 -> R.drawable.ic_sleep_4
                5 -> R.drawable.ic_sleep_5
                else -> R.drawable.ic_sleep_active
            }
        )
    }
}