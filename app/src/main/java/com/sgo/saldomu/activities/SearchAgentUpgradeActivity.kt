package com.sgo.saldomu.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.sgo.saldomu.BuildConfig
import com.sgo.saldomu.R
import com.sgo.saldomu.coreclass.*
import com.sgo.saldomu.coreclass.Singleton.MyApiClient
import com.sgo.saldomu.coreclass.Singleton.RetrofitService
import com.sgo.saldomu.interfaces.ObjListeners
import com.sgo.saldomu.models.ShopDetail
import com.sgo.saldomu.widgets.BaseActivity
import kotlinx.android.synthetic.main.activity_search_agent_upgrade.*
import org.json.JSONObject
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.util.*

class SearchAgentUpgradeActivity : BaseActivity(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        OnMapReadyCallback,
        LocationListener,
        GoogleMap.OnMarkerClickListener {

    private var categoryId: String? = ""
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var isZoomedAlready: Boolean = false
    private var shopDetails = ArrayList<ShopDetail>()
    private var hashMapMarkers: HashMap<String?, Marker?>? = null
    private var googleMap: GoogleMap? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mapFrag: SupportMapFragment? = null
    private var mLocationRequest: LocationRequest? = null
    private var markerCurrent: Marker? = null
    private var markerShop: Marker? = null
    private var bitmap: Bitmap? = null
    private var roundedImage: RoundImageTransformation? = null
    private lateinit var mLastLocation: Location
    private val RC_GPS_REQUEST = 1
    override fun getLayoutResource(): Int {
        return R.layout.activity_search_agent_upgrade
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initialize()
    }

    private fun initialize() {
        actionBarTitle = getString(R.string.menu_item_title_upgrade_via_agent)
        setActionBarIcon(R.drawable.ic_arrow_left)
        sp = CustomSecurePref.getInstance().getmSecurePrefs()
        categoryId = sp.getString(DefineValue.CATEGORY_ID_UPG, "")
        cardDetail.visibility = View.GONE
        hashMapMarkers = HashMap()
        mapFrag = supportFragmentManager.findFragmentById(R.id.agentMap) as SupportMapFragment?
        mapFrag?.getMapAsync(this)
        if (isHasAppPermission) {
            if (!GlobalSetting.isLocationEnabled(this)) {
                showAlertEnabledGPS()
            } else {
                runningApp()
            }
        } else {
            //Do not have permissions, request them now
            EasyPermissions.requestPermissions(this, getString(R.string.rationale_location), RC_LOCATION_PERM, *perms)
        }

        bitmap = BitmapFactory.decodeResource(Objects.requireNonNull<Context>(this).resources, R.drawable.user_unknown_menu)
        roundedImage = RoundImageTransformation(bitmap)
    }

    private fun runningApp() {
        if (checkPlayServices()) {
            buildGoogleApiClient()
            createLocationRequest()
        }

        Timber.d("GPS Test googleapiclient : " + mGoogleApiClient.toString())
        if (mGoogleApiClient != null) {
            mGoogleApiClient?.connect()
            Timber.d("GPS Test googleapiclient connect : " + mGoogleApiClient.toString())
        }
    }

    fun initializeToolbar(title: String) {
        setActionBarIcon(R.drawable.ic_arrow_left)
        actionBarTitle = title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        //listener ketika button back di action bar diklik
        if (id == android.R.id.home) {
            //kembali ke activity sebelumnya
            onBackPressed()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onConnected(bundle: Bundle?) {
        try {
            if (mGoogleApiClient != null) {

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)

                if (mLastLocation == null) {

                } else {
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
                    latitude = mLastLocation.getLatitude()
                    longitude = mLastLocation.getLongitude()

                    Timber.d("GPS TEST Onconnected : Latitude : $latitude, Longitude : $longitude")

                    if (googleMap != null) {

                        //disable map gesture untuk sementara sampai camera position selesai
                        googleMap?.uiSettings?.setAllGesturesEnabled(true)
                        googleMap?.uiSettings?.isMapToolbarEnabled = false
                        googleMap?.isIndoorEnabled = false
                        googleMap?.isMyLocationEnabled = false

                        if (latitude != null && longitude != null) {
                            val latLng = LatLng(latitude, longitude)
                            googleMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))

                            //add camera position and configuration
                            val cameraPosition = CameraPosition.Builder()
                                    .target(latLng) // Center Set
                                    .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                                    .build() // Creates a CameraPosition from the builder

                            googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), object : GoogleMap.CancelableCallback {
                                override fun onFinish() {
                                    //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                                    googleMap?.uiSettings?.setAllGesturesEnabled(true)
                                    isZoomedAlready = true
                                }

                                override fun onCancel() {}
                            })


                            val markerOptions = MarkerOptions()
                                    .position(latLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)))
                            markerCurrent = googleMap?.addMarker(markerOptions)

                        }
                    }
                    getAgentLoc()
                }
            }
        } catch (se: SecurityException) {
            se.printStackTrace()
        }

    }

    override fun onMapReady(map: GoogleMap?) {
        googleMap = map

        if (googleMap != null) {

            //disable map gesture untuk sementara sampai camera position selesai
            googleMap?.uiSettings?.setAllGesturesEnabled(true)
            googleMap?.uiSettings?.isMapToolbarEnabled = false
            googleMap?.isIndoorEnabled = false
            //            googleMap.setMyLocationEnabled(false);

            if (latitude != null && longitude != null) {
                val latLng = LatLng(latitude, longitude)
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))

                //add camera position and configuration
                val cameraPosition = CameraPosition.Builder()
                        .target(latLng) // Center Set
                        .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
                        .build() // Creates a CameraPosition from the builder

                googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), object : GoogleMap.CancelableCallback {
                    override fun onFinish() {
                        //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
                        googleMap?.uiSettings?.setAllGesturesEnabled(true)
                    }

                    override fun onCancel() {}
                })

                val markerOptions = MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)))
                markerCurrent = googleMap?.addMarker(markerOptions)

                googleMap?.setOnMarkerClickListener(this)
            }

        }
    }

    override fun onLocationChanged(location: Location) {
        try {
            longitude = location.longitude
            latitude = location.latitude

            Timber.d("GPS TEST OnChanged : Latitude : $latitude, Longitude : $longitude")

            if (googleMap != null) {

                if (latitude != null && longitude != null) {
                    val latLng = LatLng(latitude, longitude)
//                    googleMap?.moveCamera(CameraUpdateFactory.newLatLng(latLng))
//
//                    isZoomedAlready = false
//                    if (!isZoomedAlready) {
//
//                        //add camera position and configuration
//                        val cameraPosition = CameraPosition.Builder()
//                                .target(latLng) // Center Set
//                                .zoom(DefineValue.ZOOM_CAMERA_POSITION) // Zoom
//                                .build() // Creates a CameraPosition from the builder
//
//                        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), object : GoogleMap.CancelableCallback {
//                            override fun onFinish() {
//                                //mengaktifkan kembali gesture map yang sudah dimatikan sebelumnya
//                                googleMap?.uiSettings?.setAllGesturesEnabled(true)
//                                isZoomedAlready = true
//                            }
//
//                            override fun onCancel() {}
//                        })
//                    }

                    if (markerCurrent != null) markerCurrent?.remove()

                    val markerOptions = MarkerOptions()
                            .position(latLng)
                            .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.search_location, 70, 90)))
                    markerCurrent = googleMap?.addMarker(markerOptions)
                }

            }
            if (shopDetails.size == 0) {
                getAgentLoc()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onConnectionSuspended(p0: Int) {
        mGoogleApiClient?.connect()
        Timber.d("GPS Test Connection Failed")
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        Timber.d("GPS Test Connection Failed")
    }

    /**
     * Creating google api client object
     */
    @Synchronized
    protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build()
    }

    /**
     * Creating location request object
     */
    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = (2 * 8000).toLong()
        mLocationRequest?.fastestInterval = (1 * 8000).toLong()
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /**
     * Method to verify google play services on the device
     */
    private fun checkPlayServices(): Boolean {

        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        Timber.d("GPS Test checkPlayServices : $result")
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                Toast.makeText(this, "GOOGLE API LOCATION CONNECTION FAILED", Toast.LENGTH_SHORT).show()
            }

            return false
        }

        return true
    }

    private fun getAgentLoc() {
        try {
//            showProgressDialog()

            var params = RetrofitService.getInstance().getSignature(MyApiClient.LINK_SEARCH_AGENT, categoryId)
            params[WebParams.SENDER_ID] = DefineValue.BBS_SENDER_ID
            params[WebParams.RECEIVER_ID] = DefineValue.BBS_RECEIVER_ID
            params[WebParams.APP_ID] = BuildConfig.APP_ID
            params[WebParams.USER_ID] = userPhoneID
            params[WebParams.CATEGORY_ID] = categoryId
            Timber.d("Params search agent upgrade :$params")

            RetrofitService.getInstance().PostJsonObjRequest(MyApiClient.LINK_SEARCH_AGENT, params,
                    object : ObjListeners {
                        override fun onResponses(response: JSONObject?) {
                            Timber.d("Respon search agent upgrade :$response")

                            var code = response?.getString(WebParams.ERROR_CODE)
                            if (code!! == WebParams.SUCCESS_CODE) {
                                var shops = response?.getJSONArray("shop")
                                shopDetails.clear()
                                if (shops!!.length() > 0) {
                                    for (i in 0 until shops.length()) {
                                        var jsonObject = shops.getJSONObject(i)
                                        var shopDetail = ShopDetail()

                                        shopDetail.shopId = jsonObject.getString("shop_id")
                                        shopDetail.memberCust = jsonObject.getString("member_cust")
                                        shopDetail.memberId = jsonObject.getString("member_id")
                                        shopDetail.shopLatitude = jsonObject.getDouble("shop_latitude")
                                        shopDetail.shopLongitude = jsonObject.getDouble("shop_longitude")
                                        shopDetail.memberName = jsonObject.getString("member_name")
                                        shopDetail.shopAddress = jsonObject.getString("shop_address")
                                        shopDetail.shopDistrict = jsonObject.getString("shop_district")
                                        shopDetail.shopProvince = jsonObject.getString("shop_province")
                                        shopDetail.shopCountry = jsonObject.getString("shop_country")
                                        shopDetail.urlSmallProfilePicture = jsonObject.getString("shop_picture")
                                        shopDetail.lastActivity = jsonObject.getString("shop_lastactivity")
                                        shopDetail.shopMobility = jsonObject.getString("shop_mobility")
                                        shopDetails.add(shopDetail)
                                    }
                                    for (i in 0 until shopDetails.size) {
                                        if (shopDetails[i].shopLatitude != null && shopDetails[i].shopLongitude != null) {
                                            var latlng = LatLng(shopDetails[i].shopLatitude, shopDetails[i].shopLongitude)
                                            if (hashMapMarkers!!.containsKey(shopDetails[i].shopId)) {
                                                markerShop = hashMapMarkers?.get(shopDetails[i].shopId)
                                                markerShop?.position = latlng
                                                hashMapMarkers?.remove(shopDetails[i].shopId)
                                                hashMapMarkers?.put(shopDetails[i].shopId, markerShop!!)
                                            } else {
                                                val markerOptions = MarkerOptions().position(latlng)
                                                if (shopDetails[i].shopMobility == DefineValue.STRING_YES) {
                                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_person, 90, 90)))
                                                } else {
                                                    markerOptions.icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.drawable.map_home, 90, 90)))
                                                }
                                                markerShop = googleMap?.addMarker(markerOptions)
                                                markerShop!!.tag = i
                                                hashMapMarkers!![shopDetails[i].shopId] = markerShop
                                            }
                                        }
                                    }
                                }
                            } else {
                                shopDetails.clear()
                            }
                        }

                        override fun onError(throwable: Throwable?) {

                        }

                        override fun onComplete() {
                            dismissProgressDialog()
                        }

                    })
        } catch (e: Exception) {
            Timber.d("httpclient:" + e.message)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onMarkerClick(marker: Marker?): Boolean {
        if (marker!! != markerCurrent) {
            cardDetail.visibility = View.VISIBLE
            var position = marker.tag.toString().toInt()
            var urlProfilePicture = shopDetails[position].urlSmallProfilePicture
            tvMemberName.text = shopDetails[position].memberName
            tvMemberDetail.text = NoHPFormat.formatTo08(shopDetails[position].memberCust) + "\n" +
                    shopDetails[position].shopAddress + ", " + shopDetails[position].shopDistrict + ", " +
                    shopDetails[position].shopProvince + ", " + shopDetails[position].shopCountry + "."

            if (urlProfilePicture != null && urlProfilePicture.isEmpty()) {
                GlideManager.sharedInstance().initializeGlide(this, R.drawable.user_unknown_menu, roundedImage, ivMemberPhoto)
            } else {
                GlideManager.sharedInstance().initializeGlide(this, urlProfilePicture, roundedImage, ivMemberPhoto)
            }

            btnCall.setOnClickListener {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + NoHPFormat.formatTo08(shopDetails[position].memberCust)))
                startActivity(callIntent)
            }

            btnDirection.setOnClickListener {
                val directionIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr=" + shopDetails[position].shopLatitude + "," + shopDetails[position].shopLongitude))
                startActivity(directionIntent)
            }
            var animation = AnimationUtils.loadAnimation(this,R.anim.slide_up)
            cardDetail.startAnimation(animation)
        }
        return false
    }

    fun resizeMapIcons(image: Int, width: Int, height: Int): Bitmap {
        var imageBitmap = BitmapFactory.decodeResource(resources, image)
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    private fun showAlertEnabledGPS() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(getString(R.string.alertbox_gps_warning))
                .setCancelable(false)
                .setPositiveButton("Yes") { dialog, id ->
                    val ilocation = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivityForResult(ilocation, RC_GPS_REQUEST)
                }
                .setNegativeButton("No") { dialog, id ->
                    dialog.cancel()
                    startActivity(Intent(applicationContext, MainPage::class.java))
                }
        val alert = builder.create()
        alert.show()
    }
}
