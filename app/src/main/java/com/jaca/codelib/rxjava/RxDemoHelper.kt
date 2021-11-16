package com.jaca.codelib.rxjava

import android.annotation.SuppressLint
import com.jaca.codelib.TestCaseVersion
import io.reactivex.functions.Consumer

class RxDemoHelper {

    @SuppressLint("CheckResult")
    fun runDemo() {
        val transformOperatorsDemo = TransformOperatorsDemo()
        val list = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
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
            3 -> {
                /**
                 * OutPut:
                 * zjnTest [1, 4, 7, 10]
                 * zjnTest [2, 5, 8]
                 * zjnTest [3, 6, 9]
                 */
                transformOperatorsDemo
                    .group(list)
                    ?.subscribe {
                        println("zjnTest: $it")
                    }
            }
            4 -> {
                /**
                 * OutPut:
                 * zjnTest 1
                 * zjnTest 2
                 * zjnTest 3
                 * zjnTest 4
                 */
                transformOperatorsDemo
                    .amb()
                    .subscribe {
                        println("zjnTest $it")
                    }
            }
        }
    }
}