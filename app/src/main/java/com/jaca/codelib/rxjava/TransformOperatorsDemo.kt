package com.jaca.codelib.rxjava

import android.annotation.SuppressLint
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiConsumer
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Callable

class TransformOperatorsDemo {

    fun switchMap(list: List<Int>, isSync: Boolean): Observable<String> {
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

    fun collectInto(list: List<Int>): Single<MutableList<Int>>? {
        return Observable
            .fromIterable(list).collectInto(mutableListOf(), { list, item -> list.add(item) })
    }

    fun collect(list: List<Int>): Single<MutableList<Int>>? {
        return Observable
            .fromIterable(list).collect({ mutableListOf() }, { list, item -> list.add(item) })
    }

    fun group(list: List<Int>): Observable<MutableList<Int>>? {
        return Observable
            .fromIterable(list)
            .groupBy { it % 3 }
            .concatMapSingle { it.toList() }
    }
}