package com.ep.fonendoplayer.decoders;

import android.util.Log;

import java.nio.ByteBuffer;

//https://linuxtut.com/en/67a20140fd7c205872d6/
public class JavaDecoder {

    final static String TAG = "JavaDecoder";

    static void decode(byte[] wavData) {

        /**
         * Header analysis
         * The WAV file has a header of about 44 bytes, followed by data.
         * However, it seems that this 44 bytes may be added in various ways,
         * so it seems better to acquire the data while checking the contents of the header properly.
         *
         * Reference external link: About the data structure of the sound file (extension: WAV file)
         *
         * The type of header is called chunk, and it would be nice to know'fmt'and'data'. (For details, please see the external link above.)
         * Therefore, check these'fmt'== 0x6d7420 and'data'== 0x64617461 and save their respective indexes.
         */
        int fmtIdx = 0;
        for(int i = 0; i < wavData.length - 4; i ++){
            if(wavData[i] == 0x66 && wavData[i + 1] == 0x6d
                    && wavData[i + 2] == 0x74 && wavData[i + 3] == 0x20){ // 'fmt ' chunk
                fmtIdx = i;
                Log.i("Test", "fmtIdx:" + fmtIdx);
                break;
            }
        }
        if(fmtIdx == 0){
            Log.e(TAG, "No fmt chunk");
        }

        int dataIdx = 0;
        for(int i = 0; i < wavData.length - 4; i ++){
            if(wavData[i] == 0x64 && wavData[i + 1] == 0x61
                    && wavData[i + 2] == 0x74 && wavData[i + 3] == 0x61){ // 'data' chunk
                dataIdx = i;
                Log.i("Test", "dataIdx:" + dataIdx);
                break;
            }
        }
        if(dataIdx == 0){
            Log.e(TAG, "No data chunk");
        }

        /**
         * Next, let's get the number of channels, sampling rate, number of bits (bytes in this case), and data size.
         * The data size is basically the part excluding the header,
         * but it seems that you can freely add a footer to the WAV format, so get it.
         * The sound source I actually tried contained a fair amount of footer.
         */
        int wavChannel = (int)(wavData[fmtIdx + 10]);
        Log.i("Test", "wavChannel:" + wavChannel);

        //int wavSamplingRate = ((int)(wavData[fmtIdx + 15]) << 24) + ((int)(wavData[fmtIdx + 14]) << 16)
        //        + ((int)(wavData[fmtIdx + 13]) << 8) + (int)(wavData[fmtIdx + 12]);
        //↑ It seems that the writing style is not good, so correct it
        byte[] bytes1 = {wavData[fmtIdx + 15], wavData[fmtIdx + 14], wavData[fmtIdx + 13], wavData[fmtIdx + 12]};
        int wavSamplingRate = ByteBuffer.wrap(bytes1).getInt();

        Log.i("Test", "wavSamplingRate:" + wavSamplingRate);

        int wavByte = (int)(wavData[fmtIdx + 22]) / 8;
        Log.i("Test", "wavByte:" + wavByte);

        //int wavDataSize = ((int)(wavData[dataIdx + 7]) << 24) + ((int)(wavData[dataIdx + 6]) << 16)
        //        + ((int)(wavData[dataIdx + 5]) << 8) + (int)(wavData[dataIdx + 4]);
        byte[] bytes2 = {wavData[dataIdx + 7], wavData[dataIdx + 6], wavData[dataIdx + 5], wavData[dataIdx + 4]};
        int wavDataSize = ByteBuffer.wrap(bytes2).getInt();

        Log.i("Test", "wavDataSize:" + wavDataSize);

        int wavHeaderSize = dataIdx + 8;

        int[] musicDataRight = new int[wavDataSize / wavByte / wavChannel];
        int[] musicDataLeft = new int[wavDataSize / wavByte / wavChannel];

        /**
         * Where it is stored in 4 bytes, it is set to int by ~~ shift operation ~~ ByteBuffer. Also, the header size is calculated for later use.
         * When the channel is 2, the left and right sound sources can be acquired, so put them in musicDataRight and musicDataLeft.
         */
        byte[] bytes_temp = {0, 0, 0, 0}; //If it is not 4 bytes, BufferUnderflowException will occur.
        if(wavByte == 1 && wavChannel == 1){
            for(int i = 0, j = wavHeaderSize; i < musicDataRight.length; i ++, j ++){
                musicDataRight[i] = (int)wavData[j];
                musicDataLeft[i]  = (int)wavData[j];
            }
        } else if(wavByte == 1 && wavChannel == 2){
            for(int i = 0, j = wavHeaderSize; i < musicDataRight.length; i ++, j += 2){
                musicDataRight[i] = (int)wavData[j];
                musicDataLeft[i]  = (int)wavData[j + 1];
            }
        } else if(wavByte == 2 && wavChannel == 1){
            for(int i = 0, j = wavHeaderSize; i < musicDataRight.length; i ++, j += 2){
                //musicDataRight[i] = ((int)wavData[j + 1] << 8) + (int)wavData[j];
                //musicDataLeft[i]  = ((int)wavData[j + 1] << 8) + (int)wavData[j];
                bytes_temp[2] = wavData[j + 1];
                bytes_temp[3] = wavData[j];
                musicDataRight[i] = ByteBuffer.wrap(bytes_temp).getInt();
                musicDataLeft[i] = ByteBuffer.wrap(bytes_temp).getInt();

            }
        } else if(wavByte == 2 && wavChannel == 2){
            for(int i = 0, j = wavHeaderSize; i < musicDataRight.length; i ++, j += 4){
                //musicDataRight[i] = ((int)wavData[j + 1] << 8) + (int)wavData[j];
                //musicDataLeft[i]  = ((int)wavData[j + 3] << 8) + (int)wavData[j + 2];
                bytes_temp[2] = wavData[j + 1];
                bytes_temp[3] = wavData[j];
                musicDataRight[i] = ByteBuffer.wrap(bytes_temp).getInt();
                bytes_temp[2] = wavData[j + 3];
                bytes_temp[3] = wavData[j + 2];
                musicDataLeft[i] = ByteBuffer.wrap(bytes_temp).getInt();

            }
        }
        /**
         * Data is stored by the number of channels and the number of bytes.
         * If I wanted to do it, I could put together the formulas (I actually tried it),
         * but I wrote them separately because they would reduce readability.
         * When the number of channels is 1, the same sound is put on the left and right.
         * Also, if the number of channels or bytes is 3 or more, it is necessary to add them separately.
         *
         * This is the end of the procedure for reading WAV data as a byte array.
         */


    }
}
