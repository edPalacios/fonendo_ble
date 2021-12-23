package com.ep.fonendoplayer.utils

import android.content.Context
import android.media.*
import android.media.AudioManager.STREAM_MUSIC
import android.util.Log
import com.ep.fonendoplayer.R
import com.ep.fonendoplayer.decoders.AudioDecoder
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import kotlin.experimental.and


val track by lazy {
    val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).setLegacyStreamType(STREAM_MUSIC).build()
    val audioFormat = AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(44100)
            .setChannelMask(4).build()
    AudioTrack(attributes, audioFormat, 1024, 1, 0)
}

fun saveFile(byteArray: ByteArray, context: Context): File? {
    return try {
        val Mytemp: File = File.createTempFile("FONENDO_", ".mp3", context.cacheDir)
        Mytemp.deleteOnExit()
        val fos = FileOutputStream(Mytemp)
        fos.write(byteArray)
        fos.close()
        Mytemp
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun playMediaPlayer(file: File) {
    try {
        val mediaPlayer = MediaPlayer()
        val MyFile = FileInputStream(file)
        mediaPlayer.isLooping = true
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
        mediaPlayer.setDataSource(MyFile.fd)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener { mediaPlayer.start() }
    } catch (ex: IOException) {
        ex.printStackTrace()
    }
}


fun AudioTrack.play(byteArray: ByteArray) {
    try {
//        val n = 16
//        val array2 = ByteArray(n)
//        for (i in 0 until n) {
//            array2[i] = byteArray.get(i + 4)
//        }

//        val decode = AudioDecoder().decode(byteArray)
//
//        var n3 = 0
//        val array3 = ShortArray(decode.size / 2)
//        for (j in 0 until decode.size / 2) {
//            val n2 = ((decode[j * 2 + 1] and 0xFF.toByte()).toInt() shl 8 or ((decode[j * 2] and 0xFF.toByte()).toInt()))
//                n3 = n2 * 8
//                if (n3 <= 32767) {
//                    break
//                }
//                n3 = 32767
//                array3[j] = n3.toShort()
//        }

        Log.i("AudioTrack", "writing bytes to play")
        write(byteArray, 0, byteArray.size) // TODO somehow this plays noise with the recorded sound mixed wft!!!
        Log.i("AudioTrack", "play on")
        play()
    } catch (ex: Exception) {
        Log.e("AudioTrack", "Error playing track: $ex")
    }
}

fun Context.playThunder() {
        val wavBytes = resources.openRawResource(R.raw.thunder).readBytes()
        track.play(wavBytes)
}

fun Context.playFonendo() {
    val wavBytes = resources.openRawResource(R.raw.fonendo_hex).readBytes()
    track.play(wavBytes)
}

fun playAudioBase64(base64EncodedString: String) {
    try {
        val url = "data:audio/mp3;base64,$base64EncodedString"
        val mediaPlayer = MediaPlayer()
        mediaPlayer.setDataSource(url)
        mediaPlayer.prepare()
        mediaPlayer.start()
    } catch (ex: Exception) {
        print(ex.message)
    }
}


fun playback(audio: ByteArray) {
    try {
        val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            8000,
            AudioFormat.CHANNEL_CONFIGURATION_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            500000,
            AudioTrack.MODE_STATIC
        )
        audioTrack.write(audio, 0, 500000)
    } catch (t: Throwable) {
        Log.d("Audio", "Playback Failed")
    }
}
