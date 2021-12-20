package com.ep.fonendoplayer.utils

import android.content.Context
import android.media.*
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception


val track by lazy {
    val attributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
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


fun playtrack() {
    try {
        track.play()
    } catch (ex: Exception) {
        println("//////// NOT WORKING")
        ex.printStackTrace()
    }

}

fun playbackPrepare(byteArray: ByteArray) {
    track.write(byteArray, 0, byteArray.size)
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
