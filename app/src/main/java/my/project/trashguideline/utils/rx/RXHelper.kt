package my.project.trashguideline.utils.rx

import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable

class RXHelper {
    companion object {
        fun <T> runOnUiThread(@NonNull call: RXCall<T>): Disposable =
            Flowable.just(call)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ t ->
                    t.onCall(null)
                }, { error ->
                    call.onError(error)
                });


    }


}

