package com.example.detectingbeacons.extensions

import androidx.annotation.DrawableRes
import com.example.detectingbeacons.R
import org.altbeacon.beacon.Beacon

//Funcion que asocia una imagen a un Beacon a partir de su mayor(id2) y su minor(id3)
@DrawableRes
fun Beacon.getImage(): Int {
    return when {
        this.id2.toInt() == 16207 && this.id3.toInt() == 16704 ->
            R.drawable.ivam
        this.id2.toInt() == 48375 && this.id3.toInt() == 1347 ->
            R.drawable.okto
        this.id2.toInt() == 22107 && this.id3.toInt() == 46023 ->
            R.drawable.veles
        else ->
            0
    }
}