package com.example.casalista.ui.common

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.casalista.R

private val slide = navOptions {
    anim {
        enter = R.anim.slide_in_right
        exit = R.anim.slide_out_left
        popEnter = R.anim.slide_in_left
        popExit = R.anim.slide_out_right
    }
}

fun Fragment.goTo(destinationId: Int, args: Bundle? = null) =
    findNavController().navigate(destinationId, args, slide)
