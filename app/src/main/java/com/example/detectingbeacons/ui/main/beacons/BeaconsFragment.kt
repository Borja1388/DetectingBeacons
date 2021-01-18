package com.example.detectingbeacons.ui.main.beacons

import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.os.RemoteException
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.detectingbeacons.R
import com.example.detectingbeacons.databinding.FragmentBeaconsBinding
import org.altbeacon.beacon.*
import java.math.BigDecimal
import java.math.RoundingMode


private const val ALL_BEACONS_REGION = "AllBeaconsRegion"
private const val REQUEST_ENABLE_BT = 1
private const val DEFAULT_SCAN_PERIOD_MS = 6000L
private const val TAG = "BeaconFragment"
const val RUUVI_LAYOUT = "m:0-2=0499,i:4-19,i:20-21,i:22-23,p:24-24" // TBD
const val IBEACON_LAYOUT = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"
const val ALTBEACON_LAYOUT = BeaconParser.ALTBEACON_LAYOUT
const val ALBEACON_LAYOUT2 = "m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
const val EDDYSTONE_UID_LAYOUT = BeaconParser.EDDYSTONE_UID_LAYOUT
const val EDDYSTONE_URL_LAYOUT = BeaconParser.EDDYSTONE_URL_LAYOUT
const val EDDYSTONE_TLM_LAYOUT = BeaconParser.EDDYSTONE_TLM_LAYOUT

class BeaconsFragment : Fragment(), BeaconConsumer, RangeNotifier {

    private var _binding: FragmentBeaconsBinding? = null
    private val binding: FragmentBeaconsBinding
        get() = _binding!!
    private var mBeaconManager: BeaconManager? = null
    private var mRegion: Region? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBeaconsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //Se inicializa el objeto BeaconManager con la configuracion AltBeacon y la Region
        mBeaconManager = BeaconManager.getInstanceForApplication(requireContext())
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(IBEACON_LAYOUT))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(EDDYSTONE_UID_LAYOUT))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(EDDYSTONE_URL_LAYOUT))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(EDDYSTONE_TLM_LAYOUT))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(ALBEACON_LAYOUT2))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(RUUVI_LAYOUT))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(ALTBEACON_LAYOUT))

        val identifiers = ArrayList<Identifier>()
        mRegion = Region(ALL_BEACONS_REGION, identifiers)

        //Permisos de localizacion
        if (ActivityCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(),
                        android.Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }


        //Eventos onClik
        binding.play.setOnClickListener {
            checkIfLocationEnabled()
        }

        binding.stop.setOnClickListener {
            stopDetectingBeacons()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun checkIfLocationEnabled() {
        val lm: LocationManager = requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val networkLocationEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val gpsLocationEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (networkLocationEnabled && gpsLocationEnabled) {
            checkIsBluetoothEnabled()
        } else {
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
        }
    }

    private fun checkIsBluetoothEnabled() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        if (mBluetoothAdapter == null) {

            showToastMessage(getString(R.string.not_support_bluetooth_msg))

        } else if (mBluetoothAdapter.isEnabled) {

            startDetectingBeacons()

        } else {

            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }
    }

    private fun startDetectingBeacons() {
        mBeaconManager?.foregroundBetweenScanPeriod = DEFAULT_SCAN_PERIOD_MS
        mBeaconManager?.bind(this)

    }

    private fun stopDetectingBeacons() {
        try {
            mRegion?.let {
                mBeaconManager?.stopMonitoringBeaconsInRegion(it)
                showToastMessage(getString(R.string.stopDetectingBeacons))
            }

        } catch (e: RemoteException) {
            Log.d(TAG, "Se ha producido una excepción al parar de buscar beacons ${e.message}")
        }
        mBeaconManager?.removeAllRangeNotifiers()
        mBeaconManager?.unbind(this)
        binding.cardView.visibility = View.GONE
    }

    private fun showToastMessage(message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onBeaconServiceConnect() {

        try {
            // Empezar a buscar los beacons que encajen con el el objeto Región pasado, incluyendo
            // actualizaciones en la distancia estimada
            mRegion?.let { region ->
                mBeaconManager?.startRangingBeaconsInRegion(region)
                showToastMessage(getString(R.string.start_looking_for_beacons))
            } ?: run { showToastMessage("mregion null") }

        } catch (e: RemoteException) {
            Log.d(TAG, "Se ha producido una excepción al empezar a buscar beacons ${e.message}")
        }

        mBeaconManager?.addRangeNotifier(this)
    }

    override fun getApplicationContext(): Context {
        return requireContext().applicationContext
    }

    override fun unbindService(p0: ServiceConnection?) {
        TODO("Not yet implemented")
    }

    override fun bindService(p0: Intent?, p1: ServiceConnection?, p2: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        if (beacons.isNullOrEmpty()) {
            showToastMessage(getString(R.string.no_beacons_detected))
        } else {
            val nearestBeacon = beacons.minByOrNull {
                it.distance
            }
            val distanceRound = nearestBeacon?.distance

            val uuid = try {
                nearestBeacon?.id1?.toString()
            } catch (e: IndexOutOfBoundsException) {
                "0"
            }
            val major = try {
                nearestBeacon?.id2?.toString()
            } catch (e: IndexOutOfBoundsException) {
                "0"
            }
            val minor = try {
                nearestBeacon?.id3?.toString()
            } catch (e: IndexOutOfBoundsException) {
                "0"
            }

            //Asignar datos al CardView
            binding.cardView.visibility = View.VISIBLE
            binding.distanceText.text = if (distanceRound != null) {
                "${BigDecimal(distanceRound).setScale(2, RoundingMode.HALF_EVEN)} m"
            } else {
                "Error al leer el beacon"
            }

            binding.bluetoothAddress.text = nearestBeacon?.bluetoothAddress.toString()
            binding.uuidText.text = "UUID: $uuid"
            binding.majorText.text = "Mayor: $major"
            binding.minorTxt.text = "Minor: $minor"
            binding.rssi.text = "RSSI: ${nearestBeacon?.rssi} dBm"
            binding.tx.text = "Tx: ${nearestBeacon?.txPower} dBm"

        }
    }

}
