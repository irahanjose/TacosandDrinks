package mx.tecnm.misantla.tacosdrinks

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Location
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import mx.tecnm.misantla.tacosdrinks.databinding.ActivityMapsBinding
import androidx.appcompat.app.AlertDialog


private const val LOCATION_PERMISSION_REQUEST_CODE = 2000
private const val DEFAULT_MAP_SCALE = 13.0f

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private val taquerias_Drinks = mutableListOf<TacoDrinks>()
    private lateinit var tacoIcon: BitmapDescriptor
    private val userLocation = Location("")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        taquerias_Drinks.add(TacoDrinks("Tacos Estrella", 19.93462838327143, -96.84889465570451))
        taquerias_Drinks.add(TacoDrinks("Tacos El Compa", 19.931093204855102, -96.8522796034813))
        taquerias_Drinks.add(
            TacoDrinks(
                "Tacos El Misanteco",
                19.93092173912607,
                -96.85191482305528
            )
        )
        taquerias_Drinks.add(
            TacoDrinks(
                "Tacos La Parroquia 2",
                19.928667451797388,
                -96.85336321592332
            )
        )
        taquerias_Drinks.add(TacoDrinks("Tacos Raquel", 19.92851615619422, -96.8533578515053))
        taquerias_Drinks.add(
            TacoDrinks(
                "Tacos La Parroquia",
                19.928198434956112,
                -96.85450047254565
            )
        )

        tacoIcon = getTacoIcon()

        checkLocationPermission()
    }


    private fun checkLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                getUserLocation()
            } else {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            getUserLocation()
        }
    }

    @SuppressLint("MissingPermission")
    private fun getUserLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                userLocation.latitude = location.latitude
                userLocation.longitude = location.longitude
                setupMap()
            }
        }
    }

    private fun setupMap() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getUserLocation()
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showLocationPermissionRationaleDialog()
            } else {
                finish()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showLocationPermissionRationaleDialog() {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.need_location_permission_dialog_title)
            .setMessage(R.string.need_location_permission_dialog_message)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    LOCATION_PERMISSION_REQUEST_CODE)
            }.setNegativeButton(R.string.no) { _, _ ->
                finish()
            }
        dialog.show()
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val userLatLng = LatLng(userLocation.latitude, userLocation.longitude)
        val userMarker = MarkerOptions().position(userLatLng)
        mMap.addMarker(userMarker)

        for (taqueria in taquerias_Drinks) {
            val tacoPosition = LatLng(taqueria.latitud, taqueria.longitude)
            val tacoLocation = Location("")

            tacoLocation.latitude = taqueria.latitud
            tacoLocation.longitude = taqueria.longitude

            val distanceToTaco = tacoLocation.distanceTo(userLocation)

            val tacoMarkerOptions = MarkerOptions()
                .icon(tacoIcon)
                .position(tacoPosition)
                .title(taqueria.name)
                .snippet(getString(R.string.distance_to_format, distanceToTaco))
            mMap.addMarker(tacoMarkerOptions)
        }

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, DEFAULT_MAP_SCALE))


    }

    private fun getTacoIcon(): BitmapDescriptor {
        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_taco)
        drawable?.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        val bitmap = Bitmap.createBitmap(drawable?.intrinsicWidth ?: 0,
            drawable?.intrinsicHeight ?: 0, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable?.draw(canvas)
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }
}
