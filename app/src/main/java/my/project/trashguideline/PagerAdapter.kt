package my.project.trashguideline

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

class PagerAdapter(fm: FragmentManager, fragments: Array<Fragment>) :
    FragmentStatePagerAdapter(fm) {
    var tabFragments = fragments
    //    var tabCount: Int = tabCount
    override fun getItem(position: Int): Fragment = tabFragments[position]
//        return when (position) {
//            0 -> {
//                MakerMap()
//            }
//            1 -> {
//                GuideLine()
//            }
//            else -> {
//                throw IllegalStateException("position $position is invalid for this viewpager")
//
//            }
//        }


    override fun getCount(): Int = tabFragments.size


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
    }

}