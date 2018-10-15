package com.ogma.here

import android.Manifest
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.github.buchandersenn.android_permission_manager.PermissionManager
import com.github.buchandersenn.android_permission_manager.PermissionRequest
import com.github.buchandersenn.android_permission_manager.callbacks.OnPermissionCallback
import com.here.android.mpa.common.GeoCoordinate
import com.here.android.mpa.common.OnEngineInitListener
import com.here.android.mpa.guidance.NavigationManager
import com.here.android.mpa.mapping.Map
import com.here.android.mpa.mapping.MapFragment
import com.here.android.mpa.mapping.MapMarker
import com.here.android.mpa.mapping.MapRoute
import com.here.android.mpa.routing.*
import java.io.File


class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val REQUEST_PERMISSION = 121
    private val permissionManager = PermissionManager.create(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions()
    }

    private fun requestPermissions() {
        // Start building a new request using the with() method.
        // The method takes either a single permission or a list of permissions.
        // Specify multiple permissions in case you need to request both
        // read and write access to the contacts at the same time, for example.
        permissionManager.with(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                // Optionally, specify a request code value for the request
//                .usingRequestCode(REQUEST_PERMISSION)
                // Optionally, specify a callback handler for all three callbacks
                .onCallback(object : OnPermissionCallback {
                    override fun onPermissionShowRationale(permissionRequest: PermissionRequest) {
                        Log.i(TAG, "Permission show rationale")
                        val snackbar = Snackbar.make(findViewById(R.id.coordinator_layout), "Permission is required", Snackbar.LENGTH_INDEFINITE)
                        snackbar.setAction("OK") { permissionRequest.acceptPermissionRationale() }
                        snackbar.show()
                    }

                    override fun onPermissionGranted() {
                        Log.i(TAG, "Permission granted")
                        initMap()
                    }

                    override fun onPermissionDenied() {
                        Log.i(TAG, "Permission denied")
                    }
                })
                // Finally, perform the request
                .request()
    }

    private fun initMap() {
        // Set up disk cache path for the map service for this application
        // It is recommended to use a path under your application folder for storing the disk cache
        val success = com.here.android.mpa.common.MapSettings.setIsolatedDiskCacheRootPath(
                applicationContext.getExternalFilesDir(null).path + File.separator + ".here-maps",
                "com.ogma.here.mapservice") /* ATTENTION! Do not forget to update {YOUR_INTENT_NAME} */

        if (!success) {
            Toast.makeText(this, "Unable to set isolated disk cache path.", Toast.LENGTH_LONG).show()
        } else {
            // Search for the Map Fragment
            val mapFragment = fragmentManager.findFragmentById(R.id.mapFragment) as MapFragment
            // initialize the Map Fragment and
            // retrieve the map that is associated to the fragment
            mapFragment.init { error ->
                if (error == OnEngineInitListener.Error.NONE) {
                    // now the map is ready to be used
                    val map = mapFragment.map

                    // Set the map center to Vancouver, Canada.
                    map.setCenter(GeoCoordinate(22.558339, 88.406328), Map.Animation.BOW)
                    // ...

                    val origin = MapMarker()
                    origin.coordinate = GeoCoordinate(22.558339, 88.406328)
                    map.addMapObject(origin)

                    val destination = MapMarker()
                    destination.coordinate = GeoCoordinate(22.652805, 88.446138)
                    map.addMapObject(destination)

                    // Declare the variable (the CoreRouter)
                    val router = CoreRouter()

                    // Create the RoutePlan and add two waypoints
                    val routePlan = RoutePlan()
                    routePlan.addWaypoint(RouteWaypoint(GeoCoordinate(22.558339, 88.406328)))
                    routePlan.addWaypoint(RouteWaypoint(GeoCoordinate(22.652805, 88.446138)))

                    // Create the RouteOptions and set its transport mode & routing type
                    val routeOptions = RouteOptions()
                    routeOptions.transportMode = RouteOptions.TransportMode.TRUCK
                    routeOptions.routeType = RouteOptions.Type.FASTEST

                    routePlan.routeOptions = routeOptions

                    // Calculate the route
                    router.calculateRoute(routePlan, RouteListener(map))
                } else {
                    println("ERROR: Cannot initialize MapFragment")
                    Log.e("details", error.details)
                    Log.e("stacktrace", error.stackTrace)
                }
            }
        }
    }

    private inner class RouteListener constructor(val map: Map) : CoreRouter.Listener {

        // Method defined in Listener
        override fun onProgress(percentage: Int) {
            // Display a message indicating calculation progress
        }

        // Method defined in Listener
        override fun onCalculateRouteFinished(routeResult: List<RouteResult>, error: RoutingError) {
            // If the route was calculated successfully
            if (error == RoutingError.NONE) {
                // Render the route on the map
                val mapRoute = MapRoute(routeResult[0].route)
                map.addMapObject(mapRoute)

                //start navigation
                startNavigation(map, routeResult[0].route)
            } else {
                // Display a message indicating route calculation failure
                Log.e("error", error.toString())
            }
        }
    }

    private fun startNavigation(map: Map, route: Route) {
        val navigationManager = NavigationManager.getInstance()

        //set the map where the navigation will be performed
        navigationManager.setMap(map)

//        //set Tilt to at some angle that you can look in 3d mode
//        map.zoomLevel = 12.0
        map.mapScheme = Map.Scheme.TRUCKNAV_DAY
        navigationManager.mapUpdateMode = NavigationManager.MapUpdateMode.ROADVIEW
        map.setTilt(60f, com.here.android.mpa.mapping.Map.Animation.NONE)


        // if user wants to start real navigation, submit calculated route
        // for more information on calculating a route, see the "Directions" section
        val error: NavigationManager.Error = navigationManager.startNavigation(route)
        Log.e("error", error.toString())
    }
}
