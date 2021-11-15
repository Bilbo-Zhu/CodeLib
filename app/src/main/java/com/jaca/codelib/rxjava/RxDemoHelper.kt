package com.jaca.codelib.rxjava

import android.annotation.SuppressLint
import com.jaca.codelib.TestCaseVersion
import io.reactivex.functions.Consumer

class RxDemoHelper {

    @SuppressLint("CheckResult")
    fun runDemo() {
        val transformOperatorsDemo = TransformOperatorsDemo()
        val list = listOf(1, 2, 3, 4, 5, 6)
        when (TestCaseVersion.RX_VERSION) {
            1 -> {
                transformOperatorsDemo
                    .switchMap(list, true)
                    .subscribe { println(it) }

                transformOperatorsDemo
                    .switchMap(list, false)
                    .subscribe { println(it) }
            }
            2 -> {
                transformOperatorsDemo
                    .collect(list)
                    ?.subscribe(Consumer {
                        println(it)
                    })

                transformOperatorsDemo
                    .collectInto(list)
                    ?.subscribe(Consumer {
                        println(it)
                    })
            }
        }
    }
}