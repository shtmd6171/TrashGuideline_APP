package my.project.trashguideline.utils.rx

interface RXCall<T> {
    fun onCall(data: T?)
    fun onError(error : Throwable)
}