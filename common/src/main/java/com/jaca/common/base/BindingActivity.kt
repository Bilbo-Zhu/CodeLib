package com.jaca.common.base

import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BindingActivity<T : ViewDataBinding>: AppCompatActivity() {

    lateinit var bindWrapper: BindingWrapper<T>
    val binding: T
        get() = bindWrapper.binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindWrapper = BindingWrapper(
            DataBindingUtil.setContentView(this, getLayoutResource())
        )
        binding.lifecycleOwner = this
    }

    @LayoutRes
    abstract fun getLayoutResource(): Int
}

/**
 * why need this Wrapper https://youtrack.jetbrains.com/issue/KT-19080
 */
class BindingWrapper<out T> constructor(val binding: T)