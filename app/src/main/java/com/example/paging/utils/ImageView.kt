package com.example.paging.utils

import android.widget.ImageView
import androidx.core.content.ContextCompat
import coil.api.load
import com.example.paging.R

fun ImageView.load(url: String?) {
    load(url) {
        error(
            ContextCompat.getDrawable(context, R.drawable.ic_error)
                ?.apply {
                    setTint(getColor(context, R.attr.colorIconRegular))
                }
        )
        placeholder(
            ContextCompat.getDrawable(context, R.drawable.ic_no_data)
                ?.apply {
                    setTint(getColor(context, R.attr.colorIconRegular))
                }
        )
    }
}