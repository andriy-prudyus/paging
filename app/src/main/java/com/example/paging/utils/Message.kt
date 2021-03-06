package com.example.paging.utils

import android.view.View
import com.google.android.material.snackbar.Snackbar

fun showErrorSnackbar(view: View, throwable: Throwable) {
    Snackbar.make(view, localizedErrorMessage(throwable), Snackbar.LENGTH_LONG).show()
}