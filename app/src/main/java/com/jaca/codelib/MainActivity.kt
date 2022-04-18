package com.jaca.codelib

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.Toast
import com.jaca.codelib.bView.BViewActivity
import com.jaca.codelib.databinding.ActivityMainBinding
import com.jaca.codelib.rxjava.RxDemoHelper
import com.jaca.common.base.BindingActivity
import com.jaca.tooltips.SimpleTooltip

class MainActivity : BindingActivity<ActivityMainBinding>() {

    private lateinit var rxDemoHelper: RxDemoHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initListener()
        initHelper()
    }

    override fun getLayoutResource(): Int = R.layout.activity_main

    private fun initListener() {
        binding.tvTooltips.setOnClickListener {
            showTips()
        }
        binding.tvRxjava.setOnClickListener {
            rxDemoHelper.runDemo()
        }

        binding.tvBview.setOnClickListener {
            this.startActivity(Intent(this, BViewActivity::class.java))
        }
    }

    private fun initHelper() {
        rxDemoHelper = RxDemoHelper()
    }

    private fun baseTooltipBuilder() =
        SimpleTooltip.Builder(this).gravity(Gravity.START)
            .dismissOnOutsideTouch(true)
            .dismissOnInsideTouch(true)
            .arrowWidth(this.resources.getDimension(R.dimen.grid_2_5))
            .arrowHeight(this.resources.getDimension(R.dimen.grid_2_5))
            .keepInsideScreen(true)
            .margin(R.dimen.grid_1dp)
            .padding(0F)
            .modal(false)
            .arrowColor(this.resources.getColor(R.color.color_33c072))

    private fun showTips() {
        baseTooltipBuilder()
            .gravity(Gravity.BOTTOM)
            .anchorView(findViewById(R.id.tv_tooltips))
            .text(this.resources.getString(R.string.hello_tooltips))
            .contentView(R.layout.tooltip, R.id.tv_tooltip)
            .build()
            .show()
    }
}

fun toast(context: Context) {
    Toast.makeText(context, "我是被插入的Toast", Toast.LENGTH_SHORT).show()
}