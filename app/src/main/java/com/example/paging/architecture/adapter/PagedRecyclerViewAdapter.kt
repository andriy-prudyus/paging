package com.example.paging.architecture.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.paging.architecture.dataSource.BaseDataSource
import com.example.paging.architecture.state.PagingState.After
import com.example.paging.architecture.state.PagingState.Before
import com.example.paging.databinding.ListItemFailureBinding
import com.example.paging.databinding.ListItemLoadingBinding
import com.example.paging.utils.localizedErrorMessage

abstract class PagedRecyclerViewAdapter<T, VH : RecyclerView.ViewHolder>(
    callback: DiffUtil.ItemCallback<T>
) : PagedListAdapter<T, RecyclerView.ViewHolder>(callback) {

    companion object {
        private const val TYPE_REGULAR = 0
        private const val TYPE_LOADING = 1
        private const val TYPE_FAILURE = 2
    }

    var loadAfterState: After<Int, T>? = null
        set(value) {
            val position = itemCount - 1

            when (value) {
                is After.Loading, is After.Failure -> {
                    if (field == null || field is After.Success) {
                        notifyItemInserted(position)
                    } else {
                        notifyItemChanged(position)
                    }
                }
                is After.Success -> notifyItemRemoved(position)
            }

            field = value
        }

    var loadBeforeState: Before<Int, T>? = null
        set(value) {
            val position = 0

            when (value) {
                is Before.Loading, is Before.Failure -> {
                    if (field == null || field is Before.Success) {
                        notifyItemInserted(position)
                    } else {
                        notifyItemChanged(position)
                    }
                }
                is Before.Success -> notifyItemRemoved(position)
            }

            field = value
        }

    protected abstract fun onCreateRegularViewHolder(parent: ViewGroup): VH
    protected abstract fun onBindRegularViewHolder(holder: VH, position: Int)

    override fun onCurrentListChanged(previousList: PagedList<T>?, currentList: PagedList<T>?) {
        loadAfterState = null
        loadBeforeState = null
    }

    override fun getItemCount(): Int {
        return super.getItemCount() +
                loadAfterState.let { if (it != null && it !is After.Success) 1 else 0 } +
                loadBeforeState.let { if (it != null && it !is Before.Success) 1 else 0 }
    }

    override fun getItemViewType(position: Int): Int {
        val itemCount = itemCount

        return when (position) {
            itemCount - 1 -> {
                when (loadAfterState) {
                    null, is After.Success -> TYPE_REGULAR
                    is After.Loading -> TYPE_LOADING
                    is After.Failure -> TYPE_FAILURE
                }
            }
            0 -> {
                when (loadBeforeState) {
                    null, is Before.Success -> TYPE_REGULAR
                    is Before.Loading -> TYPE_LOADING
                    is Before.Failure -> TYPE_FAILURE
                }
            }
            else -> TYPE_REGULAR
        }
    }

    final override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_REGULAR -> onCreateRegularViewHolder(parent)
            TYPE_LOADING -> {
                LoadingViewHolder(
                    ListItemLoadingBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            TYPE_FAILURE -> {
                FailureViewHolder(
                    ListItemFailureBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            }
            else -> throw IllegalArgumentException("Unknown viewType $viewType")
        }
    }

    @Suppress("UNCHECKED_CAST")
    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val viewType = getItemViewType(position)) {
            TYPE_REGULAR -> {
                onBindRegularViewHolder(
                    holder as VH,
                    if (loadBeforeState != null && loadBeforeState !is Before.Success) {
                        position - 1
                    } else {
                        position
                    }
                )
            }
            TYPE_FAILURE -> {
                when (position) {
                    itemCount - 1 -> {
                        (loadAfterState as? After.Failure<Int, T>)?.let {
                            (holder as? PagedRecyclerViewAdapter<T, VH>.FailureViewHolder)?.bind(it.throwable)
                        }
                    }
                    0 -> {
                        (loadBeforeState as? Before.Failure<Int, T>)?.let {
                            (holder as? PagedRecyclerViewAdapter<T, VH>.FailureViewHolder)?.bind(it.throwable)
                        }
                    }
                }
            }
            else -> {
                if (viewType != TYPE_LOADING) {
                    throw IllegalArgumentException("Incorrect viewType at position $position")
                }
            }
        }
    }

    class LoadingViewHolder(
        binding: ListItemLoadingBinding
    ) : RecyclerView.ViewHolder(binding.root)

    inner class FailureViewHolder(
        private val binding: ListItemFailureBinding
    ) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(throwable: Throwable) {
            binding.textView.setText(localizedErrorMessage(throwable))
        }

        override fun onClick(v: View?) {
            reload(adapterPosition)
        }

        private fun reload(position: Int) {
            (currentList?.dataSource as? BaseDataSource<T, *>)?.let { dataSource ->
                when (position) {
                    itemCount - 1 -> {
                        (loadAfterState as? After.Failure<Int, T>)?.let {
                            dataSource.loadAfter(it.params, it.callback)
                        }
                    }
                    0 -> {
                        (loadBeforeState as? Before.Failure<Int, T>)?.let {
                            dataSource.loadBefore(it.params, it.callback)
                        }
                    }
                    else -> throw IllegalArgumentException("Unexpected position $position")
                }
            }
        }
    }
}