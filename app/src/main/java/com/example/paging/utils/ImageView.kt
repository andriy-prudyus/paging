package com.example.paging.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.paging.R

fun ImageView.load(url: String?) {
    Glide.with(this)
        .setDefaultRequestOptions(
            RequestOptions()
                .error(R.drawable.ic_error)
                .placeholder(R.drawable.ic_no_data)
        )
        .load(url)
        .into(this)
}