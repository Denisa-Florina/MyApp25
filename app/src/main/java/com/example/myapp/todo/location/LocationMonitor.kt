import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class LocationMonitor(val context: Context) {
    @SuppressLint("MissingPermission")
    val currentLocation: Flow<Location> = callbackFlow {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        // 1. Try to get the last known location immediately
        fusedLocationClient.lastLocation.addOnSuccessListener {
            Log.d("LocationMonitor", "lastLocation $it")
            if (it != null) {
                trySend(it)
            }
        }

        // 2. Define the callback for continuous updates
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d("LocationMonitor", "onLocationResult $location")
                    trySend(location)
                }
            }
        }

        // 3. Create the high-accuracy request
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .build()

        // 4. Start requesting updates
        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )

        // 5. Clean up when the flow is closed (e.g., ViewModel is cleared)
        awaitClose {
            Log.d("LocationMonitor", "removeLocationUpdates")
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}