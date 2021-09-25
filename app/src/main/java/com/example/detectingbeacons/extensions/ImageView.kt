package com.example.detectingbeacons.extensions

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners

fun ImageView.roundCorners(reference: Int){
    Glide.with(this)
        .load(reference)
        .centerCrop()
        .transform(RoundedCorners(20))
        .into(this)
}