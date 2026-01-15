
import android.app.Application
import android.location.Location
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch

class MyLocationViewModel(application: Application) : AndroidViewModel(application) {

    var uiState by mutableStateOf<Location?>(null)
        private set

    private var collecting = false

    fun start() {
        if (collecting) return
        collecting = true

        viewModelScope.launch {
            LocationMonitor(getApplication()).currentLocation.collect {
                uiState = it
            }
        }
    }

    companion object {
        fun Factory(application: Application): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MyLocationViewModel(application)
            }
        }
    }
}

