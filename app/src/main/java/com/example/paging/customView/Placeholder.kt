package com.example.paging.customView

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.paging.databinding.CustomPlaceholderBinding

class Placeholder(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {

    val binding: CustomPlaceholderBinding = CustomPlaceholderBinding.inflate(
        LayoutInflater.from(context),
        this
    )
}
