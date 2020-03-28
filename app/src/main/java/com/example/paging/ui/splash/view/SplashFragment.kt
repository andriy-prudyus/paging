package com.example.paging.ui.splash.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.paging.R
import com.example.paging.architecture.scope.LifecycleAwareMainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashFragment : Fragment(R.layout.fragment_splash) {

    companion object {
        private const val DELAY = 1000L // ms
    }

    private val mainScope = LifecycleAwareMainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycle.addObserver(mainScope)
    }

    override fun onResume() {
        super.onResume()

        mainScope.launch {
            delay(DELAY)
            findNavController().navigate(SplashFragmentDirections.toItemListFragment())
        }
    }
}