package com.jaca.codelib.rxjava

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SwitchMapDemo {

    fun switchMapDemo(list: List<Int>, isSync: Boolean): Observable<String> {
        return Observable
            .fromIterable(list)
            .switchMap {
                if (isSync) {
                    Observable.just("zjnTest: $it")
                        .subscribeOn(Schedulers.newThread())
                } else {
                    Observable.just("zjnTest: $it")
                }
            }
    }
}