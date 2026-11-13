package net.prototux.aare.car.obd

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.util.Log
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.prototux.aare.car.CarData
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * Connects to a standard Bluetooth ELM327 / OBD-II adapter over RFCOMM (SPP)
 * and streams live engine RPM, vehicle speed, coolant temp, and battery voltage
 * into [CarData].
 */
class ObdManager(private val context: Context, private val scope: CoroutineScope) {

    private var job: Job? = null
    @Volatile private var socket: BluetoothSocket? = null

    fun start(targetAddress: String = "") {
        stop()
        job = scope.launch(Dispatchers.IO) {
            runLoop(targetAddress)
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        runCatching { socket?.close() }
        socket = null
    }

    @SuppressLint("MissingPermission")
    private suspend fun runLoop(targetAddress: String) {
        val adapter = context.getSystemService(android.bluetooth.BluetoothManager::class.java)?.adapter ?: return
        while (scope.isActive) {
            try {
                val device = findObdDevice(adapter, targetAddress)
                if (device == null) {
                    Log.d(TAG, "No paired ELM327 / OBD-II device found; retrying in 10s")
                    delay(10_000)
                    continue
                }
                Log.i(TAG, "Connecting to OBD device: ${device.name} (${device.address})")
                val s = device.createRfcommSocketToServiceRecord(SPP_UUID)
                socket = s
                s.connect()
                Log.i(TAG, "OBD connected, initializing ELM327")

                val input = s.inputStream
                val output = s.outputStream
                initElm327(input, output)

                pollLoop(input, output)
            } catch (e: CancellationException) {
                break
            } catch (e: Exception) {
                Log.w(TAG, "OBD connection error: ${e.message}, retrying in 5s")
                runCatching { socket?.close() }
                socket = null
                delay(5_000)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun findObdDevice(adapter: BluetoothAdapter, targetAddress: String): BluetoothDevice? {
        val bonded = adapter.bondedDevices ?: return null
        if (targetAddress.isNotBlank()) {
            bonded.firstOrNull { it.address.equals(targetAddress, ignoreCase = true) }?.let { return it }
        }
        val keywords = listOf("obd", "elm327", "vgate", "konnwei", "scan", "carista", "viecar")
        return bonded.firstOrNull { dev ->
            val name = dev.name?.lowercase() ?: ""
            keywords.any { name.contains(it) }
        }
    }

    private suspend fun initElm327(input: InputStream, output: OutputStream) {
        sendCommand(input, output, "ATZ")    // Reset
        delay(500)
        sendCommand(input, output, "ATE0")   // Echo off
        sendCommand(input, output, "ATL0")   // Linefeed off
        sendCommand(input, output, "ATS0")   // Spaces off
        sendCommand(input, output, "ATSP0")  // Auto protocol
    }

    private suspend fun pollLoop(input: InputStream, output: OutputStream) {
        var tick = 0
        while (scope.isActive) {
            tick++
            // 010C: Engine RPM (every 100ms)
            val rpmResp = sendCommand(input, output, "010C")
            parseRpm(rpmResp)?.let { CarData.updateObdData(rpm = it) }

            // 010D: Vehicle Speed
            val speedResp = sendCommand(input, output, "010D")
            parseSpeed(speedResp)?.let { CarData.updateObdData(speedKmh = it) }

            // Poll coolant and voltage every 10 cycles (~1.5s)
            if (tick % 10 == 0) {
                val coolantResp = sendCommand(input, output, "0105")
                parseCoolant(coolantResp)?.let { CarData.updateObdData(coolantC = it) }

                val voltResp = sendCommand(input, output, "ATRV")
                parseVoltage(voltResp)?.let { CarData.updateObdData(voltage = it) }
            }

            delay(100)
        }
    }

    private fun sendCommand(input: InputStream, output: OutputStream, cmd: String): String {
        output.write("$cmd\r".toByteArray())
        output.flush()

        val sb = StringBuilder()
        val buffer = ByteArray(128)
        val start = System.currentTimeMillis()
        while (System.currentTimeMillis() - start < 1_000) {
            if (input.available() > 0) {
                val count = input.read(buffer)
                if (count > 0) {
                    val str = String(buffer, 0, count)
                    sb.append(str)
                    if (str.contains(">")) break
                }
            } else {
                Thread.sleep(15)
            }
        }
        return sb.toString().replace(">", "").replace(" ", "").trim()
    }

    private fun parseRpm(resp: String): Float? {
        val idx = resp.indexOf("410C")
        if (idx == -1 || resp.length < idx + 8) return null
        return try {
            val hexA = resp.substring(idx + 4, idx + 6).toInt(16)
            val hexB = resp.substring(idx + 6, idx + 8).toInt(16)
            ((hexA * 256) + hexB) / 4f
        } catch (_: Exception) { null }
    }

    private fun parseSpeed(resp: String): Float? {
        val idx = resp.indexOf("410D")
        if (idx == -1 || resp.length < idx + 6) return null
        return try {
            resp.substring(idx + 4, idx + 6).toInt(16).toFloat()
        } catch (_: Exception) { null }
    }

    private fun parseCoolant(resp: String): Float? {
        val idx = resp.indexOf("4105")
        if (idx == -1 || resp.length < idx + 6) return null
        return try {
            resp.substring(idx + 4, idx + 6).toInt(16) - 40f
        } catch (_: Exception) { null }
    }

    private fun parseVoltage(resp: String): Float? {
        val clean = resp.replace("V", "").trim()
        return clean.toFloatOrNull()
    }

    companion object {
        private const val TAG = "AareOBD"
        private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}

