package com.bac.homelink.core.extensions
import androidx.lifecycle.Lifecycle; import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope; import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow; import kotlinx.coroutines.launch
fun <T> Flow<T>.collectWithLifecycle(owner:LifecycleOwner, state:Lifecycle.State=Lifecycle.State.STARTED, action:suspend (T)->Unit) {
    owner.lifecycleScope.launch { owner.lifecycle.repeatOnLifecycle(state) { collect { action(it) } } }
}
