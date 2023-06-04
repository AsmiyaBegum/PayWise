package com.ab.bankingapplication.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.rxbinding.view.clicks
import rx.android.schedulers.AndroidSchedulers

class GenericAdapter<Data, DataBinding,AdditionalData>(@LayoutRes val layoutId: Int, private val interaction: GenericAdapterInteraction<Data, DataBinding, AdditionalData>) :
    RecyclerView.Adapter<GenericAdapter.GenericViewHolder<Data, DataBinding, AdditionalData>>() {

    private val items = mutableListOf<Data>()
    private var additionalData : AdditionalData? = null
    private var inflater: LayoutInflater? = null
    private var adapterInteraction: GenericAdapterInteraction<Data, DataBinding, AdditionalData>? = null

    init {
        this.adapterInteraction = interaction
    }

    fun addItems(items: List<Data>) {
        this.items.clear()
        this.items.addAll(items)
        notifyDataSetChanged()
    }


    // we can add any additional data needed for displaying list if needed
    fun updateAdditionData(additionalData: AdditionalData) {
        this.additionalData = additionalData
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenericViewHolder<Data, DataBinding, AdditionalData> {
        val layoutInflater = inflater ?: LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(layoutInflater, layoutId, parent, false)
        return GenericViewHolder(binding,interaction)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GenericViewHolder<Data, DataBinding, AdditionalData>, position: Int) {
        val itemViewModel = items[position]
        holder.bind(itemViewModel)
        adapterInteraction!!.bindingViewHolder(holder.viewBinding as DataBinding,items[position],holder,additionalData)

        holder.itemView.clicks()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                adapterInteraction!!.onClicked(itemViewModel,holder.viewBinding as DataBinding)
            }
    }


    class GenericViewHolder<Data,DataBinding,AdditionalData>(private val binding: ViewDataBinding,private val interaction: GenericAdapterInteraction<Data, DataBinding, AdditionalData>) : RecyclerView.ViewHolder(binding.root) {
        var viewBinding : ViewDataBinding = binding
        private lateinit var itemData : Any
        fun bind(itemViewModel: Data) {
            itemData = itemViewModel!!
            binding.executePendingBindings()
        }


    }

}

abstract class GenericAdapterInteraction<Data,DataBinding,AdditionalData> {
    abstract fun onClicked(data : Data,binding: DataBinding)
    abstract fun bindingViewHolder(binding : DataBinding,data : Data,holder : GenericAdapter.GenericViewHolder<Data, DataBinding, AdditionalData>,additionalData: AdditionalData?)
}
