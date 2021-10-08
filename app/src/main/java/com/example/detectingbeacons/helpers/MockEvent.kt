package com.example.detectingbeacons.helpers

import com.example.detectingbeacons.R
import com.example.detectingbeacons.models.Event

object MockEvent {
    val nearbySites = listOf(
        Event(R.drawable.ayuntamiento, "Ayuntamiento de Valencia"),
        Event(R.drawable.miguelete, "El Miguelete"),
        Event(R.drawable.estacion, "Estación del Norte"),
        Event(R.drawable.ciudad_ciencias, "Ciudad de las Artes y las Ciencias"),
    )
    val museums = listOf(
        Event(R.drawable.museo_blasco, "Museo Blasco Ibáñez"),
        Event(R.drawable.museo_ciencias, "Museo Municipal de Ciencias Naturales"),
        Event(R.drawable.museo_fallero, "Museo Fallero"),
        Event(R.drawable.museo_historia, "Museo de historia de Valencia"),
    )

}