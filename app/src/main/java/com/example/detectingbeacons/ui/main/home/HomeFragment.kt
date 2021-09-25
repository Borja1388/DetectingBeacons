package com.example.detectingbeacons.ui.main.home

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
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.example.detectingbeacons.R
import com.example.detectingbeacons.databinding.FragmentHomeBinding
import com.example.detectingbeacons.extensions.getImage
import com.example.detectingbeacons.helpers.Constans
import org.altbeacon.beacon.*


class HomeFragment : Fragment(), BeaconConsumer, RangeNotifier {

    private var _binding: FragmentHomeBinding? = null
    private val binding: FragmentHomeBinding
        get() = _binding!!
    private var mBeaconManager: BeaconManager? = null
    private var mRegion: Region? = null
    private var currentBeacon: Beacon? = null
    private var dialog: HomeDialogFragment? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Se inicializa el objeto BeaconManager y la Region
        mBeaconManager = BeaconManager.getInstanceForApplication(requireContext())
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(Constans.IBEACON_LAYOUT))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(Constans.EDDYSTONE_UID_LAYOUT))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(Constans.EDDYSTONE_URL_LAYOUT))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(Constans.EDDYSTONE_TLM_LAYOUT))
        mBeaconManager?.beaconParsers?.add(BeaconParser().setBeaconLayout(Constans.RUUVI_LAYOUT))

        val identifiers = ArrayList<Identifier>()
        mRegion = Region(Constans.ALL_BEACONS_REGION, identifiers)

        //Permisos de localizacion
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ), 1
            )
        }

        checkIfLocationEnabled()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    //Metodo para comprobar si la localizazion esta encendida.
    private fun checkIfLocationEnabled() {
        val lm: LocationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val networkLocationEnabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        val gpsLocationEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER)

        if (networkLocationEnabled && gpsLocationEnabled) {
            checkIsBluetoothEnabled()
        } else {
            val myIntent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(myIntent)
            checkIsBluetoothEnabled()
        }
    }

    //Metodo para comprobar si el bluetooth esta encendido
    private fun checkIsBluetoothEnabled() {
        val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        when {
            mBluetoothAdapter == null -> {
                showToastMessage(getString(R.string.not_support_bluetooth_msg))
            }
            mBluetoothAdapter.isEnabled -> {
                startDetectingBeacons()
            }
            else -> {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, Constans.REQUEST_ENABLE_BT)
                startDetectingBeacons()
            }
        }
    }

    //Metodo para empezar a buscoar los beacons
    private fun startDetectingBeacons() {
        mBeaconManager?.foregroundBetweenScanPeriod = Constans.DEFAULT_SCAN_PERIOD_MS
        mBeaconManager?.bind(this)
    }

    //Metodo para parar la busqueda de beacons
    private fun stopDetectingBeacons() {
        try {
            mRegion?.let {
                mBeaconManager?.stopMonitoringBeaconsInRegion(it)
                Log.d(
                    Constans.TAG,
                    getString(R.string.stopDetectingBeacons)
                )
            }

        } catch (e: RemoteException) {
            Log.d(
                Constans.TAG,
                getString(R.string.exception_beacon)
            )
        }
        mBeaconManager?.removeAllRangeNotifiers()
        mBeaconManager?.unbind(this)
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
                Log.d(Constans.TAG, getString(R.string.start_looking_for_beacons))
            } ?: run { Log.d(Constans.TAG, getString(R.string.region_void)) }

        } catch (e: RemoteException) {
            Log.d(
                Constans.TAG,
                getString(R.string.exception_beacon)
            )
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

    //Metodo para procesar los beacons detectados.
    override fun didRangeBeaconsInRegion(beacons: MutableCollection<Beacon>?, region: Region?) {
        if (beacons.isNullOrEmpty()) {
            Log.d(Constans.TAG, getString(R.string.no_beacons_detected))
        } else {

            val nearestBeacon = beacons.sortedBy {

                it.distance

            }.firstOrNull {
                val major = try {
                    it.id2
                } catch (e: IndexOutOfBoundsException) {
                    null
                }
                val minor = try {
                    it.id3
                } catch (e: IndexOutOfBoundsException) {
                    null
                }
                major != null && minor != null
            }

            if (nearestBeacon != null &&
                nearestBeacon.id2 != currentBeacon?.id2 &&
                nearestBeacon.id3 != currentBeacon?.id3
            ) {
                currentBeacon = nearestBeacon
                createDialog(nearestBeacon)
            }
        }
    }

    //Metodo para cargar la popUp donde se muestra el evento detectado
    private fun createDialog(beacon: Beacon) {
        if (beacon.getImage() != 0) {
            dialog?.dismiss()
            dialog = HomeDialogFragment().apply {
                arguments = bundleOf(
                    Constans.BUNDLE_REFERENCE_IMAGE to beacon.getImage()
                )
            }
            dialog?.isCancelable = false
            dialog?.show(childFragmentManager, "")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopDetectingBeacons()
        _binding = null
    }
}
