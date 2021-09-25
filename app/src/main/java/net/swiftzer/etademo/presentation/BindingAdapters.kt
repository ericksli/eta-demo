package net.swiftzer.etademo.presentation

import android.view.View
import androidx.databinding.BindingAdapter

@BindingAdapter("isVisible")
fun View.setVisible(visible: Boolean?) {
    visibility = (if (visible == true) View.VISIBLE else View.GONE)
}
