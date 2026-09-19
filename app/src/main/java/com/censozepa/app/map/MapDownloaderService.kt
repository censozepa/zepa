package com.censozepa.app.map

import android.content.Context
import org.osmdroid.tileprovider.cachemanager.CacheManager
import org.osmdroid.util.BoundingBox
import org.osmdroid.views.MapView

/**
 * Service class to download map tiles for offline usage using OSMDroid's CacheManager.
 */
class MapDownloaderService(
    private val context: Context,
    private val mapView: MapView
) {

    private val cacheManager = CacheManager(mapView)

    /**
     * Downloads map tiles for the given bounding box and zoom levels without showing default UI.
     * 
     * @param north Northern latitude of the bounding box
     * @param south Southern latitude of the bounding box
     * @param east Eastern longitude of the bounding box
     * @param west Western longitude of the bounding box
     * @param minZoom Minimum zoom level to download
     * @param maxZoom Maximum zoom level to download
     * @param callback Callback to monitor the download progress and completion
     */
    fun downloadMapArea(
        north: Double,
        south: Double,
        east: Double,
        west: Double,
        minZoom: Int,
        maxZoom: Int,
        callback: DownloadCallback
    ) {
        // BoundingBox parameters: north, east, south, west
        val boundingBox = BoundingBox(north, east, south, west)

        // Using downloadAreaAsyncNoUI to avoid OSMDroid's default AlertDialog
        // and allow the app to handle its own UI/Progress reporting.
        cacheManager.downloadAreaAsyncNoUI(
            context,
            boundingBox,
            minZoom,
            maxZoom,
            object : CacheManager.CacheManagerCallback {
                override fun onTaskComplete() {
                    callback.onSuccess()
                }

                override fun onTaskFailed(errors: Int) {
                    callback.onFailure(errors)
                }

                override fun updateProgress(
                    progress: Int,
                    currentZoomLevel: Int,
                    zoomMin: Int,
                    zoomMax: Int
                ) {
                    callback.onProgress(progress, currentZoomLevel)
                }

                override fun downloadStarted() {
                    callback.onStart()
                }

                override fun setPossibleTilesInArea(total: Int) {
                    callback.onTotalTilesDetermined(total)
                }
            }
        )
    }

    /**
     * Callback interface to monitor map downloading progress.
     */
    interface DownloadCallback {
        fun onStart()
        fun onTotalTilesDetermined(totalTiles: Int)
        fun onProgress(progress: Int, currentZoomLevel: Int)
        fun onSuccess()
        fun onFailure(errors: Int)
    }
}
