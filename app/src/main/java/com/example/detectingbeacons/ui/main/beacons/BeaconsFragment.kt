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


private const val ALL_BEACONS_REGION = "AllBeaconsRegion"
private const val REQUEST_ENABLE_BT = 1
private const val DEFAULT_SCAN_PERIOD_MS = 6000L
private const val TAG = "BeaconFragment"

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
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT))
        //"m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"
        //m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"
        //"m:0-3=2d24bf16"
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
            binding.cardView.visibility = View.VISIBLE
            binding.distanceText.text = nearestBeacon?.distance.toString()
            binding.uuidText.text = nearestBeacon?.id1.toString()
            binding.majorText.text = nearestBeacon?.bluetoothAddress.toString()
            binding.minorTxt.text = nearestBeacon?.rssi.toString()
           // showToastMessage("Distancia: ${nearestBeacon?.distance.toString()}")
           // binding.textViewBeacon.text = "${nearestBeacon?.distance.toString()} ${nearestBeacon?.bluetoothAddress.toString()}"
        }
    }

}
