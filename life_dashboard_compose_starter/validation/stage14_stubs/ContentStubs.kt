package android.content
import android.net.Uri
open class Context {
    val applicationContext: Context get() = this
    val packageName: String = "com.example.lifedashboard"
    fun getSystemService(name: String): Any? = null
    fun checkSelfPermission(permission: String): Int = 0
    companion object { const val ALARM_SERVICE = "alarm"; const val NOTIFICATION_SERVICE = "notification" }
}
class Intent {
    var action: String? = null
    var data: Uri? = null
    var flags: Int = 0
    constructor()
    constructor(context: Context, target: Class<*>)
    fun putExtra(name: String, value: String): Intent = this
    fun getStringExtra(name: String): String? = null
    companion object {
        const val FLAG_ACTIVITY_CLEAR_TOP=1
        const val FLAG_ACTIVITY_SINGLE_TOP=2
    }
}
abstract class BroadcastReceiver {
    abstract fun onReceive(context: Context, intent: Intent?)
    fun goAsync() = PendingResult()
    class PendingResult { fun finish() {} }
}
