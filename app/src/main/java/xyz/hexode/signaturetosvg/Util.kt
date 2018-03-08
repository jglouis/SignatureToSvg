package xyz.hexode.signaturetosvg

import android.content.Context
import android.widget.Toast

fun Context.toast(msg: String, isShort: Boolean = true) {
    Toast.makeText(this, msg, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
}
