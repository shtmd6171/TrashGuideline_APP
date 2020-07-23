package my.project.trashguideline

import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.android.material.tabs.TabLayout.TabLayoutOnPageChangeListener
import kotlinx.android.synthetic.main.activity_main.*
import my.project.trashguideline.model.Board
import my.project.trashguideline.model.Report
import my.project.trashguideline.utils.L
import my.project.trashguideline.utils.isMaterUser
import my.project.trashguideline.utils.rx.RXHelper

class MainActivity : AppCompatActivity() {

    private lateinit var backPressHolder: onBackPressHolder
    private lateinit var adapter: my.project.trashguideline.PagerAdapter

    private lateinit var mapFragment: MakerMap
    private lateinit var reportFragment: GuideLine
    private lateinit var myPingFragment: MyPingFragment
    private lateinit var voteFragment: VoteFragment

    private lateinit var tabFragment: Array<Fragment>

    override fun getSupportFragmentManager(): FragmentManager {
        return super.getSupportFragmentManager()
    }

    fun setFragment() {


        if (isMaterUser()) {
            mapFragment = newInstance(MakerMap())
            reportFragment = newInstance(GuideLine())
            voteFragment = newInstance(VoteFragment())
            tabFragment = arrayOf(mapFragment, reportFragment, voteFragment)

            tab_layout.addTab(tab_layout!!.newTab().setText("맵"))
            tab_layout.addTab(tab_layout!!.newTab().setText("리포트"))
            tab_layout.addTab(tab_layout!!.newTab().setText("투표"))
        } else {
            mapFragment = newInstance(MakerMap())
            myPingFragment = newInstance(MyPingFragment())
            voteFragment = newInstance(VoteFragment())
            tabFragment = arrayOf(mapFragment, myPingFragment, voteFragment)

            tab_layout.addTab(tab_layout!!.newTab().setText("맵"))
            tab_layout.addTab(tab_layout!!.newTab().setText("나의 핑"))
            tab_layout.addTab(tab_layout!!.newTab().setText("투표"))
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)


        //checkPreviewLogin()

        setFragment()

        adapter = PagerAdapter(supportFragmentManager, tabFragment)
        view_pager.offscreenPageLimit = adapter.count
        view_pager!!.adapter = adapter





        tab_layout!!.addOnTabSelectedListener(object : OnTabSelectedListener {

            override fun onTabSelected(tab: TabLayout.Tab) {
                //tab.getPosition()은 각 탭에 해당하는 번호를 의미하고
                //setCurrentItem()은 viewPager에게 각 값에 해당하는 숫자에 맞는 Fragment를 보여주라는 의미
                view_pager!!.currentItem = tab.position

                var pos = view_pager.currentItem;

                if (isMaterUser()) {
                    if (adapter.getItem(pos) is GuideLine) {
                        reportFragment.onReportLoad()
                    }
                } else {
                    if (adapter.getItem(pos) is MyPingFragment) {
                        myPingFragment.onMyPingLoad()
                    }
                }

                if (adapter.getItem(pos) is VoteFragment) {
                    voteFragment.onVoteItemsLoad()
                }


            }

            // 탭이 선택 안됐을 때
            override fun onTabUnselected(tab: TabLayout.Tab) {}

            // 탭이 다시 선택 됐을 때
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
        view_pager!!.addOnPageChangeListener(TabLayoutOnPageChangeListener(tab_layout))

        backPressHolder = onBackPressHolder()
    }
    // 이미 로그인한 적이 있으면 (user값이 존재 하면) startActivity를 수행하고
    // user값이 없다면 showLoginWindow()를 통해 로그인 창을 띄움
    /*private fun checkPreviewLogin() {
        val user = FirebaseAuth.getInstance().currentUser
        if(user == null )
        {

            showLoginWindow()

        }
        else{

            moveToOpenWeatherActivity()

        }
    }

    private fun moveToOpenWeatherActivity() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun showLoginWindow() {
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.GreenTheme)
                .build(),
            RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                val user = FirebaseAuth.getInstance().currentUser
                moveToOpenWeatherActivity()
                // google 사용시 프로필 사진 user.photoUrl
            } else {
                Toast.makeText(this,"로그인 실패, 로그인을 다시 시도해주세요.",Toast.LENGTH_SHORT).show()
            }
        }
    }*/


    override fun onBackPressed() {
        backPressHolder.onBackPressed()
    }

    inner class onBackPressHolder() {
        private var backPressHolder: Long = 0

        fun onBackPressed() {
            if (System.currentTimeMillis() > backPressHolder + 2000) {
                backPressHolder = System.currentTimeMillis()
                showBackToast()
                return
            }
            if (System.currentTimeMillis() <= backPressHolder + 2000) {
                finishAffinity()
            }
        }

        fun showBackToast() {
            Toast.makeText(
                getApplicationContext(),
                "한번 더 누르시면 종료됩니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    companion object {
        fun <T : Fragment?> newInstance(fragment: T): T {
            val args = Bundle()
            fragment!!.arguments = args
            return fragment
        }
    }

    fun pushNewFragment(item: Any) {
        //아이템뷰 클릭시 해당 좌표로 이동시킨다.

        tab_layout.getTabAt(0)?.select()
        var moveLocation: Location? = null
        if (item is Report) {
            moveLocation = Location("report_ping").apply {
                latitude = item.latitude
                longitude = item.longitude
            }
            mapFragment.updateLocation(moveLocation, false)
        } else if (item is Board) {
            moveLocation = Location("my_ping").apply {
                latitude = item.latitude.toDouble()
                longitude = item.longitude.toDouble()
            }
            mapFragment.updateLocation(moveLocation, false)
            mapFragment.afterMoveToBard(item)
        }


    }
}
