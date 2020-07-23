package my.project.trashguideline

import android.app.Activity
import android.app.FragmentManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.firebase.geofire.GeoQueryEventListener
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.tedpark.tedonactivityresult.rx2.TedRxOnActivityResult
import io.reactivex.annotations.NonNull
import kotlinx.android.synthetic.main.activity_markermap_page.*
import my.project.trashguideline.auth.LoginActivity
import my.project.trashguideline.auth.SignUpActivity
import my.project.trashguideline.listener.LocationCallback
import my.project.trashguideline.model.Board
import my.project.trashguideline.model.BoardLocation
import my.project.trashguideline.utils.Const
import my.project.trashguideline.utils.L


class MakerMap : Fragment(), LocationListener, OnMapReadyCallback {

    var Tlatitude: Double = 0.0
    var Tlongitude: Double = 0.0

    private lateinit var mfragmentActivity: FragmentActivity
    private lateinit var mfragmentManager: FragmentManager
    private lateinit var mapFragment: MapFragment
    private lateinit var mMap: GoogleMap
    private lateinit var geoQuery: GeoQuery
    private val LOCATION_PERMISSION_REQUEST_CODE = 2000
    private var activity: Activity? = null
    private var mainActivity: MainActivity? = null

    private var mCurrentLocation: Location? = null
    private val mBoardLocationList: ArrayList<BoardLocation> = ArrayList()


    override fun onAttach(activity: Activity) {
        super.onAttach(activity)
        this.activity = activity
        mainActivity = activity as MainActivity?
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_markermap_page, container, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // 위치 권한이 없을 때는 위치 권한을 얻고 끝난다
                // 그렇기 때문에 onActivityResult를 통해서
                // 위치정보를 얻은 즉시 getLocationinfo()의 권한이 있을때의 코드부분이 실행된다
                getLocationinfo(null)

            }

        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // activity를 획득한 상태 (onAttach의 수행 값)에서 fragmentActivity를 얻고
        //  fragmentActivity를 통해서 supportFragmentManager를 얻을 수 있음 그걸 mfragmentManager에 저장
        mfragmentActivity = activity as FragmentActivity
        mfragmentManager = mfragmentActivity.fragmentManager
        mapFragment = mfragmentManager.findFragmentById(R.id.googleMap) as MapFragment
        mapFragment.getMapAsync(this)
        latestRadiusQuery()
        // fragment -> Activity Intent 시에 getActivity() 사용해야함
        intent_posting.setOnClickListener {
            val nextIntent = Intent(getActivity(), PostingBoard::class.java)
            nextIntent.putExtra("latitude", mCurrentLocation?.latitude)
            nextIntent.putExtra("longitude", mCurrentLocation?.longitude)
            startActivity(nextIntent)
        }

        back_to_loginpage_setting.setOnClickListener {
            startAccountSetting()
        }

    }

    private fun startAccountSetting() {
        TedRxOnActivityResult.with(getActivity())
            .startActivityForResult(Intent(getActivity(), AccountSettingActivity::class.java))
            .subscribe({ activityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK) {
                    mainActivity?.finish()
                    startActivity(Intent(getActivity(), LoginActivity::class.java))
                }

            }, { error -> L.e("error " + error.message) })
    }


    private fun startPingDetail(boardLocation: BoardLocation) {

        val boardIntent = Intent(getActivity(), PingDetailActivity::class.java)
        boardIntent.putExtra("selected_board", boardLocation)

        TedRxOnActivityResult.with(getActivity())
            .startActivityForResult(boardIntent)
            .subscribe({ activityResult ->
                if (activityResult.resultCode == Activity.RESULT_OK) {
                    mMap.clear()
                    latestRadiusQuery()
                }

            }, { error -> L.e("error " + error.message) })
    }

    private fun latestRadiusQuery() {

        getLocationinfo(object : LocationCallback {
            override fun callback(location: Location) {
                L.i("::::현재 위치 좌표 : $location")
                mCurrentLocation = location
                //서버에 현 위치 기준 반경 1km에 있는 Board 데이터를 가져온다.
                val geoFire =
                    GeoFire(FirebaseDatabase.getInstance().reference.child("board_location"))
                geoQuery =
                    geoFire.queryAtLocation(GeoLocation(location.latitude, location.longitude), 1.0)
                geoQuery.addGeoQueryEventListener(object : GeoQueryEventListener {
                    override fun onGeoQueryReady() {
                        //현재 서버에 있는 모든 데이터가 로도가 됬을시 호출
                        L.i("[onGeoQueryReady]All initial data has been loaded and events have been fired!")
                    }

                    override fun onKeyEntered(@NonNull key: String, location: GeoLocation?) {
                        //쿼리시 반경안에 들어오는 위치정보가 있을경우.
                        L.i(
                            String.format(
                                "Key %s entered the search area at [%f,%f]",
                                key, location?.latitude, location?.longitude
                            )
                        )

                        location?.let {

                            FirebaseDatabase.getInstance().reference.child(Const.DB_BOARD)
                                .child(key)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        val board =
                                            dataSnapshot.getValue(Board::class.java) ?: return
                                        if (board.reportCount < 10) {
                                            //리포트 카운트가 10 이상 일시 블라인드 처리 됬으므로 5이하 인것만 활성화.
                                            mBoardLocationList.add(BoardLocation(board.addedByUser, key, location.latitude,location.longitude))

                                            val markerOptions = MarkerOptions()
                                            markerOptions.position(
                                                LatLng(
                                                    it.latitude,
                                                    it.longitude
                                                )
                                            ).icon(getMarkgerIcon(board.recommentCount, board.reportCount))

                                            mMap.addMarker(markerOptions)
                                        }
                                    }

                                    override fun onCancelled(p0: DatabaseError) {

                                    }

                                })
                        }

                    }

                    override fun onKeyMoved(key: String?, location: GeoLocation?) {
                        L.i(
                            String.format(
                                "Key %s moved within the search area to [%f,%f]",
                                key, location?.latitude, location?.longitude
                            )
                        )
                    }

                    override fun onKeyExited(key: String?) {
                        L.i(String.format("Key %s is no longer in the search area", key))
                    }

                    override fun onGeoQueryError(error: DatabaseError?) {
                        L.e("There was an error with this query: $error")
                    }


                })

            }
        })

    }

    private fun getMarkgerIcon(likeCount: Int, reportCount: Int): BitmapDescriptor {
        var drawable = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)

        if (likeCount >= 5) {
            drawable = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)
        }

        if (reportCount >= 5) {
            drawable = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)
        }

        return drawable
    }

    /*
    ★ fragment일 때는
    private fun checkCameraPermission() {
        //requestPermission은 true를 return하기 때문에 카메라 권한을 얻으면 openCamera()를 실행한다

        //fragment 내에서 Context를 얻으려면 Attach 내에서 context?를 받거나 requireContext()를 지정한다
        //fragment : Fragment 자체는 this로 할당함
        if(PermissionUtil().requestPermission(requireContext(),this,CAMERA_PERMISSION_REQUEST, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            openCamera()
        }
    }
    private fun checkGalleryPermission() {
        if(PermissionUtil().requestPermission(requireContext(),this,GALLERY_PERMISSION_REQUEST, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE))
        {
            openGallery()
        }

    }

    private fun openCamera() {
        // Uri는 경로를 지칭함, photoUri는 카메라로 찍은 사진이 저장될 위치
        // 해당 Uri를 만들기 위해서는 FileProvider.getUriForFile()이라는 파일을 위한 Uri를 생성하는 함수를 사용
        // 매개변수는 Context, authority(authority는 해당 패키지명 + provider임), 파일이 필요함

        // FileProvider를 Manifest에 등록해줘야 사용 가능함
        val context : Context = requireContext()
        val photoUri = FileProvider.getUriForFile(context,context.packageName + ".provider",createCameraFile())

        startActivityForResult(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply{
                // 찍은 사진 (MediaStore.EXTRA_OUTPUT)을 photoUri에 넣어준다
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) },CAMERA_PERMISSION_REQUEST)
    }

    private fun openGallery() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            setType("image/*")
            setAction(Intent.ACTION_GET_CONTENT)
        }
        startActivityForResult(Intent.createChooser(intent,"-"),GALLERY_PERMISSION_REQUEST)


    }

    private fun uploadImage(imageUri : Uri) {
        val bitmap : Bitmap = MediaStore.Images.Media.getBitmap(activity!!.contentResolver, imageUri)
    }

    private fun createCameraFile() : File {

        val context : Context = requireContext()
        // getExternalFilesDir()을 통해 디렉터리를 만드는데, 매개변수는 디렉터리로 삼을 저장소를 의미한다
        // 여기서 Environment.DIRECTORY_PICTURES란, 핸드폰 내부의 사진첩폴더를 디렉터리로 삼는 다는 것
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // 파일을 만들기 위해서는 디렉터리와 파일 이름이 필요함, 파일 네임(저장될 사진 이름)은 위에 지정했음
        return File(dir, FILE_NAME)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            GALLERY_PERMISSION_REQUEST -> {
                if (PermissionUtil().permissionGranted(requestCode, GALLERY_PERMISSION_REQUEST, grantResults))
                {
                    openGallery()
                }
            }
            CAMERA_PERMISSION_REQUEST -> {
                if (PermissionUtil().permissionGranted(requestCode, CAMERA_PERMISSION_REQUEST, grantResults))
                {
                    openCamera()
                }
            }
        }
    }
    */
     */

    private fun getLocationinfo(locationCallback: LocationCallback?) {
        val context: Context = requireContext()
        if (Build.VERSION.SDK_INT >= 23
            && ContextCompat.checkSelfPermission
                (
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // 권한이 있어 즉시 위도 경도를 수집한다
            val locationManager =
                requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
            // getLastKnownLocation으로 가장 최근 위치정보를 수집한 내용을 가져온다
            val location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                locationCallback?.callback(location)
                val latitude = location.latitude
                val longitude = location.longitude
                getLocationLatLng(latitude, longitude)


                // 만약 가장 최근 위치정보를 수집한 내용이 없다면
                // requestLocationUpdates를 통해 새롭게 위치정보를 업데이트 해야한다
            } else {
                // requestLocationUpdates에 필요한 각 인수들을 작성하고
                // LocationListener를 마지막에 삽입하는데 현재 클래스인
                // OpenWeatherActivity가 LocationListener를 구현하는 클래스로 나타내고
                // LocationListener의 필요한 메소드를 현재 클래스에 구현해 this가 곧 LocationListener가 될 수 있다

                // 이외에 따로 class를 LocationListener를 구현해 삽입하거나
                // object : LocationListener {impliments ...} 형식으로 구현할 수 있다
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    3000L, 0F, this
                )
                // 한 번 업데이트 후 위치 정보가 지속적으로 갱신되는 것을 종료함
                locationManager.removeUpdates(this)
            }

        }
    }

    override fun onLocationChanged(location: Location?) {
        val latitude = location?.latitude
        val longitude = location?.longitude
        getLocationLatLng(latitude, longitude)
    }

    private fun getLocationLatLng(latitude: Double?, longitude: Double?) {
        Tlatitude = latitude!!.toDouble()
        Tlongitude = longitude!!.toDouble()

    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

    }

    override fun onProviderEnabled(provider: String?) {

    }

    override fun onProviderDisabled(provider: String?) {

    }

    override fun onMapReady(p0: GoogleMap) {
        mMap = p0
        var location: LatLng = LatLng(Tlatitude, Tlongitude)
        var markerOptions: MarkerOptions = MarkerOptions()
        markerOptions.title("현재위치입니다")
        markerOptions.snippet("세부설명 옵션")
        markerOptions.position(location)
        p0.addMarker(markerOptions)

        mMap.setOnMarkerClickListener { marker ->
            if (marker.isInfoWindowShown) {
                marker.hideInfoWindow()
            } else {
                for (item in mBoardLocationList) {
                    L.i("::::key $item")
                    if (getSelectedMarker(marker, item)) {
                        L.i("click item : $item")
                        startPingDetail(item)
                        break
                    }
                }
                marker.showInfoWindow()
            }
            true
        }



        p0.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 16F))
    }


    fun getSelectedMarker(marker: Marker, boardLocation: BoardLocation): Boolean {
        if (marker.position.latitude == boardLocation.latitude &&
            marker.position.longitude == boardLocation.longitude
        ) {
            return true
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::geoQuery.isInitialized) {
            geoQuery.removeAllListeners()
        }
    }


    fun afterMoveToBard(board: Board) {
        var boardLocation = BoardLocation().apply {
            addedByUser = board.addedByUser
            key = board.childKey
            latitude = board.latitude.toDouble()
            longitude = board.longitude.toDouble()
        }
        startPingDetail(boardLocation)
    }

    fun moveToMarker(location: Location?){
        var latLng: LatLng = LatLng(Tlatitude, Tlongitude)
        var markerOptions = MarkerOptions()
        markerOptions.position(latLng)
        mMap.addMarker(markerOptions)

    }

    fun updateLocation(location: Location?, zoom: Boolean) {
        //카메라 위치를 이동시킨다.
        mMap.clear()
        location?.let {
            if (zoom) {
                val position = CameraPosition.Builder()
                    .target(LatLng(it.latitude, it.longitude))
                    .zoom(16f)
                    .bearing(0f)
                    .build()
                mMap.animateCamera(
                    CameraUpdateFactory.newCameraPosition(position),
                    Math.max(30, 1),
                    null
                )
            } else {
                mMap.animateCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            it.latitude,
                            it.longitude
                        )
                    )
                )

            }
            moveToMarker(location)

        }
    }
}