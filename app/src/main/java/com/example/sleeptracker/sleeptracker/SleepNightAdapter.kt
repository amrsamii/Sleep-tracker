package com.example.sleeptracker.sleeptracker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.sleeptracker.database.SleepNight
import com.example.sleeptracker.databinding.ListItemSleepNightBinding

// ListAdapter builds RecyclerView Adapter backed by List
// it will keep track of the list
class SleepNightAdapter(val clickListener: SleepNightListener) :
    ListAdapter<SleepNight, SleepNightAdapter.SleepNightViewHolder>(SleepNightDiffCallback()) {
    /**
     * This function creates new viewHolder for RecyclerView
     * RecyclerView needs a new viewHolder if it just started up and it's displaying the first items or
     * if the number of views on the screen increased
     * @param parent -> viewGroup into which the new view will be added after it's bounded to an adapter position
     * viewGroup is type of view that holds a group of views. in reality, this will always be the RecyclerView
     * @param viewType -> used when there are multiple different views in the same RecyclerView
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepNightViewHolder {
        return SleepNightViewHolder.from(parent)
    }

    /**
     *  This function tells RecyclerView how to draw items into viwHolder
     *  It is called for items that are either on screen, or just about to scroll onto the screen
     *  @param position -> the position in the list to bind
     */
    override fun onBindViewHolder(holder: SleepNightViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    // A viewHolder describes an item view and metadata about its place within the RecyclerView
    class SleepNightViewHolder private constructor(val binding: ListItemSleepNightBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SleepNight,
                 clickListener: SleepNightListener) {
            binding.sleepNight = item
            binding.clickListener = clickListener
            // ask dataBinding to execute our pending bindings right away. It is a good idea when using binding adapters
            // in the recyclerView
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): SleepNightViewHolder {
                /**
                 *  to make a viewHolder, we need a view for it to hold
                 *  to inflate layout from XML, we use layoutInflater
                 *  parent.context means we will create a layoutInflater based on the parent view
                 *  false means we don't attach the viewHolder to recyclerView now since RecyclerView will add this item when it's time
                 */
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemSleepNightBinding.inflate(layoutInflater, parent, false)
                return SleepNightViewHolder(binding)
            }
        }
    }
}

class SleepNightDiffCallback : DiffUtil.ItemCallback<SleepNight>() {
    // used to know if item is added, removed, or moved
    override fun areItemsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
        return oldItem.nightId == newItem.nightId
    }

    // used to know if item contents have changed
    override fun areContentsTheSame(oldItem: SleepNight, newItem: SleepNight): Boolean {
        return oldItem == newItem
    }

}

// this class will listen for clicks and pass on the related data for processing those clicks to the fragment
// when user clicks an item, the onClick method in this listener will be triggered with the selected item
class SleepNightListener(val clickListener: (sleepId: Long) -> Unit) {
    // we will call onClick whenever the user clicks on an item
    fun onClick(night: SleepNight) = clickListener(night.nightId)
}