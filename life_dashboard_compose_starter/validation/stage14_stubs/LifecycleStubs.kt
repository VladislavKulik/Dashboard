package androidx.lifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
open class ViewModel
class ViewModelProvider {
    interface Factory { fun <T : ViewModel> create(modelClass: Class<T>): T }
}
val ViewModel.viewModelScope: CoroutineScope
    get() = CoroutineScope(SupervisorJob() + Dispatchers.Default)
