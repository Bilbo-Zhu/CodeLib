package com.jaca.codelib.rxjava

import android.annotation.SuppressLint
import com.jaca.codelib.TestCaseVersion

class RxDemoHelper {

    @SuppressLint("CheckResult")
    fun runDemo() {
        when (TestCaseVersion.RX_VERSION) {
            1 -> {
                val switchMap = SwitchMapDemo()
                val list = listOf(1, 2, 3, 4, 5, 6)
                switchMap
                    .switchMapDemo(list,true)
                    .subscribe { println(it) }

                switchMap
                    .switchMapDemo(list, false)
                    .subscribe { println(it) }
            }
        }
    }
}