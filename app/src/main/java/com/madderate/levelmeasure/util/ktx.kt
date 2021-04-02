package com.madderate.levelmeasure.util

import android.content.res.Resources
import android.util.TypedValue

/**
 * @author      madderate
 * @date        4/2/21 10:05 PM
 * @description
 */
val Int.dp: Int
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), Resources.getSystem().displayMetrics
    ).toInt()

val Float.dp: Float
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics
    )