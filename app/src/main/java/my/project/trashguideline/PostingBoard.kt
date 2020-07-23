package my.project.trashguideline

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.location.Location
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.firebase.geofire.*
import com.github.ybq.android.spinkit.SpinKitView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_posting_board.*
import my.project.trashguideline.MYApplication.Companion.auth
import my.project.trashguideline.base.BaseActivity
import my.project.trashguideline.model.Board
import my.project.trashguideline.utils.Const
import my.project.trashguideline.utils.L
import my.project.trashguideline.utils.bindView
import java.io.File


class PostingBoard : BaseActivity() {

    private val CAMERA_PERMISSION_REQUEST = 1000
    private val GALLERY_PERMISSION_REQUEST = 1001
    private val FILE_NAME = "picture.jpg"
    private var uploadChooser: uploadchooser? = null
    //초기값 0 관련이슈 90으로 변경적용
    private var mDegree: Int = 90
    private var uri: Uri? = null
    private var isCamera: Boolean = true
    private var isSeleted: Boolean = false
    private var getEditText: String? = null


    //이미지 서버 관련 초기화.
    private lateinit var imageStorageReference: StorageReference

    //게시글 데이터 베이스 관련
    private lateinit var boardDatabaseReference: DatabaseReference

    //위치정보 데이터 베이스 관련련
    private lateinit var boardLocationDatabase: DatabaseReference

    private val ivImage: ImageView by bindView(R.id.uploaded_image)
    private val etContent: EditText by bindView(R.id.edit_text)
    private val loadingProgress: SpinKitView by bindView(R.id.loading)
    private lateinit var currentLocation: Location

    override fun getLayoutId(): Int = R.layout.activity_posting_board

    override fun onInitView() {
        var intent = intent
        var latitude = intent.getDoubleExtra("latitude", 0.0);
        var longitude = intent.getDoubleExtra("longitude", 0.0);
        currentLocation = Location("Current").apply {
            this.latitude = latitude
            this.longitude = longitude
        }


    }

    override fun setListener() {
        setupListener()
    }


    // Activity가 꺼졌을 때 isSeleted가 false로 바뀌게 해서, bitmap 이미지를 다시 못 불러오게 함
    override fun onStop() {
        super.onStop()
        isSeleted = false
    }

    private fun setupListener() {
        uploaded_image.setOnClickListener {
            uploadChooser = uploadchooser().apply {
                // 매개변수인 interface 타입의 listener를 매개 변수 안에서
                // interface를 바로 불러와 implements 해준다
                addNotifier(object : uploadchooser.UploadChooserNotifierInterface {
                    override fun cameraOnClick() {
                        checkCameraPermission()

                    }

                    override fun galleryOnClick() {
                        checkGalleryPermission()
                    }
                })
            }
            uploadChooser!!.show(supportFragmentManager, "")
        }

        rotate_image.setOnClickListener {
            rotate()
        }

        regist_btn.setOnClickListener {
            L.i("[등록 버튼 누를시]")
            // db를 통한 구현
            auth = FirebaseAuth.getInstance()
            var uid = auth.currentUser!!.uid
            var content = etContent.text.toString()
            var boardKey = FirebaseDatabase.getInstance().reference.child(Const.DB_BOARD).push().key
            var imageKey = FirebaseDatabase.getInstance().reference.child(Const.DB_BOARD).push().key
            var recommendMap = HashMap<String, String>()
            var reportMap = HashMap<String, String>()

            //게시글에 올릴 Board 객체 셋팅
            var board = Board(
                uid,
                imageKey!!,
                content,
                currentLocation.latitude.toString(),
                currentLocation.longitude.toString(),
                boardKey!!, reportMap, recommendMap, 0, 0
            )

            //서버 등록 순서..
            //1.이미지 업로드 2.이미지 업로드 완료 확인  3.이미지업로드 확인되면 RealTimeDatabase에 관련 게시글 데이터 삽입
            imageStorageReference =
                FirebaseStorage.getInstance().getReferenceFromUrl(Const.TRASHGUIDE_FIREBASE_STORAGE)
                    .child(Const.IMAGE_BOARD).child(uid)
            //이미지 서버 경로 등록
            //이미지 경로 내부 각 글쓴이 uid 폴더 지정

            //게시판 DB 경로 등록
            boardDatabaseReference =
                FirebaseDatabase.getInstance().reference.child(Const.DB_BOARD).child(boardKey)

            uri?.let {
                loadingProgress.visibility = View.VISIBLE
                imageStorageReference.child(imageKey).putFile(it)
                    .addOnCompleteListener { taskSnapshot ->
                        if (taskSnapshot.isSuccessful) {
                            loadingProgress.visibility = View.GONE
                            boardDatabaseReference.setValue(board).addOnCompleteListener { task ->
                                loadingProgress.visibility = View.GONE
                                Handler().postDelayed({
                                    simpleToast("업로드에 성공 하였습니다.")
                                    finish()
                                }, 200)
                            }

                            //위치정보 db에 저장..
                            val geoFire =
                                GeoFire(FirebaseDatabase.getInstance().reference.child("board_location"))

                            geoFire.setLocation(
                                boardKey,
                                GeoLocation(currentLocation.latitude, currentLocation.longitude)
                            )
                        }
                    }.addOnFailureListener { _ ->
                        simpleToast("업로드 도중 문제가 발생하였습니다.")
                        loadingProgress.visibility = View.GONE
                    }.addOnProgressListener { progress ->
                        //업로드 프로그래스 상황.
                        val progress =
                            (100 * progress.bytesTransferred / progress.totalByteCount) as Long
                        L.i("업로드 중.. $progress")
                    }
            }
        }

        cancel_btn.setOnClickListener {
            onBackPressed()
        }

    }

    // 사진 회전을 위한 메소드로 90도 씩 계속 회전함
    private fun rotate() {
        if (isSeleted) {
            val matrix = Matrix()
            mDegree += 90
            matrix.postRotate(mDegree.toFloat())
            val scaledBitmap =
                Bitmap.createScaledBitmap(getbitmp(), getbitmp().width, getbitmp().height, true)
            val rotatedBitmap = Bitmap.createBitmap(
                scaledBitmap,
                0,
                0,
                scaledBitmap.width,
                scaledBitmap.height,
                matrix,
                true
            )

            Glide.with(this).load(rotatedBitmap).into(ivImage)
            click_image_text.setText("")
            ivImage.setBackgroundColor(Color.WHITE)
        } else {
            Toast.makeText(this, "사진을 먼저 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkCameraPermission() {
        //requestPermission은 true를 return하기 때문에 카메라 권한을 얻으면 openCamera()를 실행한다

        //fragment 내에서 Context를 얻으려면 Attach 내에서 context?를 받거나 requireContext()를 지정한다
        //fragment : Fragment 자체는 this로 할당함
        if (PermissionUtil().requestPermission(
                this, CAMERA_PERMISSION_REQUEST, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            openCamera()
        }
    }

    private fun checkGalleryPermission() {
        if (PermissionUtil().requestPermission(
                this, GALLERY_PERMISSION_REQUEST, Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            openGallery()
        }

    }

    private fun openCamera() {
        // Uri는 경로를 지칭함, photoUri는 카메라로 찍은 사진이 저장될 위치
        // 해당 Uri를 만들기 위해서는 FileProvider.getUriForFile()이라는 파일을 위한 Uri를 생성하는 함수를 사용
        // 매개변수는 Context, authority(authority는 해당 패키지명 + provider임), 파일이 필요함

        // FileProvider를 Manifest에 등록해줘야 사용 가능함
        val photoUri = FileProvider.getUriForFile(
            this,
            applicationContext.packageName + ".provider",
            createCameraFile()
        )

        startActivityForResult(
            Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                // 찍은 사진 (MediaStore.EXTRA_OUTPUT)을 photoUri에 넣어준다
                putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }, CAMERA_PERMISSION_REQUEST
        )
    }

    private fun openGallery() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            setType("image/*")
            setAction(Intent.ACTION_GET_CONTENT)
        }
        startActivityForResult(Intent.createChooser(intent, "-"), GALLERY_PERMISSION_REQUEST)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // data에 사용자가 지정한 사진의 Uri를 지니고 있음
            GALLERY_PERMISSION_REQUEST ->

                data?.let {
                    uploadImage(it.data)
                    // it.data가 Intent.ACTION_GET_CONTENT 된 data인데 그걸 전역변수 uri에 담아서
                    // getbitmp()에서 Bitmap 변수를 얻을 때 사용할 거임
                    // 그래서  getbitmp()에서 구분 가능하게 isCamera의 boolean 값을 변경함
                    uri = it.data
                    isCamera = false
                    isSeleted = true

                }
            CAMERA_PERMISSION_REQUEST -> {
                isCamera = true
                isSeleted = true
                // 작업의 결과물을 떠나 작업이 잘 수행되어 있는지 RESULT_OK로 확인해줘야 함
                // 작업이 잘 수행되지 않으면 바로 return함
                if (resultCode != Activity.RESULT_OK) return
                // openCamera()에서 찍은 결과물을 저장한 파일의 Uri를 다시 가져옴
                val photoUri = FileProvider.getUriForFile(
                    this,
                    applicationContext.packageName + ".provider",
                    createCameraFile()
                )
                uri = photoUri
                uploadImage(photoUri)


            }
        }
    }


    fun uploadImage(imageUri: Uri) {
        // Bitmap 형태를 가져오기 위해서 contentResolver와 이미지의 Uri가 필요함
        // contentResolver는 Activity 자체에서 상속되어있는 메소드

        var bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        // 이미지를 설정한 후에 uploadChooser를 닫게 해줌
        // uploadImage가 카메라,갤러리 어느 것을 선택해도 이것을 실행하기 때문에 여기에 적어뒀음
        uploadChooser?.dismiss()

//        findViewById<ImageView>(R.id.uploaded_image).setImageBitmap(bitmap)
        L.i("imageUri $imageUri")
        Glide.with(this).load(imageUri).into(ivImage)
        click_image_text.text = ""
        uploaded_image.setBackgroundColor(Color.WHITE)

    }

    fun getbitmp(): Bitmap {
        var bitmap: Bitmap
        // isCamera의 값에 따라서 true면 imageUri를 사진캡쳐의 비트맵으로 변환해주고
        if (isCamera) {
            val imageUri = FileProvider.getUriForFile(
                this,
                applicationContext.packageName + ".provider",
                createCameraFile()
            )
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
        } else {
            // 아니면 intent 할 때 담겼던 uri를 통해서 비트맵으로 변환해줌
            bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)


        }
        return bitmap
    }


    private fun createCameraFile(): File {

        // getExternalFilesDir()을 통해 디렉터리를 만드는데, 매개변수는 디렉터리로 삼을 저장소를 의미한다
        // 여기서 Environment.DIRECTORY_PICTURES란, 핸드폰 내부의 사진첩폴더를 디렉터리로 삼는 다는 것
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // 파일을 만들기 위해서는 디렉터리와 파일 이름이 필요함, 파일 네임(저장될 사진 이름)은 위에 지정했음
        return File(dir, FILE_NAME)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GALLERY_PERMISSION_REQUEST -> {
                if (PermissionUtil().permissionGranted(
                        requestCode,
                        GALLERY_PERMISSION_REQUEST,
                        grantResults
                    )
                ) {
                    openGallery()
                }
            }
            CAMERA_PERMISSION_REQUEST -> {
                // 여기서 requestCode는 startActivityForResult를 수행할 때 넘겨준 RequestCode이고
                // permissionGranted() 내부에서 RequestCode === CAMERA_PERMISSION_REQUEST인지 확인한다
                // grantResults는 권한을 얻은 IntArray형태로 저장한 것
                if (PermissionUtil().permissionGranted(
                        requestCode,
                        CAMERA_PERMISSION_REQUEST,
                        grantResults
                    )
                ) {
                    openCamera()
                }
            }
        }
    }

}
