package com.hasan.badhabit

import androidx.annotation.DrawableRes

data class Avatar(
    val id: String,
    @DrawableRes val resId: Int
)

object AvatarHelper {
    val avatars = listOf(
        Avatar("default", R.drawable.aslan),
        Avatar("aslan", R.drawable.aslan),
        Avatar("baykus", R.drawable.baykus),
        Avatar("boga", R.drawable.boga),
        Avatar("kedi", R.drawable.kedi),
        Avatar("sincap", R.drawable.sincap)
    )

    fun getIconById(id: String): Int {
        return avatars.find { it.id == id }?.resId ?: R.drawable.aslan
    }
}
