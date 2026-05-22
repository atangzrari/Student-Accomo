package com.bac.homelink.core.extensions
import android.view.View; import android.widget.Toast; import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
fun View.show()  { visibility = View.VISIBLE }
fun View.hide()  { visibility = View.GONE }
fun Fragment.showToast(msg:String) = Toast.makeText(requireContext(),msg,Toast.LENGTH_SHORT).show()
fun View.showSnack(msg:String, action:String?=null, onAction:(()->Unit)?=null) {
    val s = Snackbar.make(this,msg,Snackbar.LENGTH_LONG)
    if(action!=null&&onAction!=null) s.setAction(action){onAction()}
    s.show()
}
