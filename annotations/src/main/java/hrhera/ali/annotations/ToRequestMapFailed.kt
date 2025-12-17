package hrhera.ali.annotations
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FIELD)
annotation class ToRequestMapFailed(
    val name: String
)