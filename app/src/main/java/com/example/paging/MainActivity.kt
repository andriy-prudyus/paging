package com.example.paging

import androidx.appcompat.app.AppCompatActivity
import com.example.paging.architecture.view.AppFragmentFactory
import javax.inject.Inject

class MainActivity : AppCompatActivity(R.layout.activity_main) {

    @Inject
    lateinit var fragmentFactory: AppFragmentFactory
}
