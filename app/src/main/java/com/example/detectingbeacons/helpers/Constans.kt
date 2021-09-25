package com.example.detectingbeacons.helpers

import org.altbeacon.beacon.BeaconParser

object Constans {

    //Protocolos Beacons
   /*const val ALTBEACON_LAYOUT = BeaconParser.ALTBEACON_LAYOUT
    const val ALBEACON_LAYOUT2 = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"*/
    const val RUUVI_LAYOUT = "m:0-2=0499,i:4-19,i:20-21,i:22-23,p:24-24" // TBD
    const val IBEACON_LAYOUT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"
    const val EDDYSTONE_UID_LAYOUT = BeaconParser.EDDYSTONE_UID_LAYOUT
    const val EDDYSTONE_URL_LAYOUT = BeaconParser.EDDYSTONE_URL_LAYOUT
    const val EDDYSTONE_TLM_LAYOUT = BeaconParser.EDDYSTONE_TLM_LAYOUT

    //Key Region
    const val ALL_BEACONS_REGION = "AllBeaconsRegion"

    //Log TAG
    const val TAG = "BeaconFragment"

    //Scand period
    const val DEFAULT_SCAN_PERIOD_MS = 6000L

    //Permissions
    const val REQUEST_ENABLE_BT = 1

    //bundle
    const val BUNDLE_REFERENCE_IMAGE = "beaconImage"


}