package com.ep.fonendoplayer.decoders;



public class AudioDecoder2 {
    public static final int RANGE_HIGH = 32767;

    public static final int RANGE_LOW = -32768;

    private static final short[] ima_index_table = new short[] {
            -1, -1, -1, -1, 2, 4, 6, 8, -1, -1,
            -1, -1, 2, 4, 6, 8 };

    private static final short[] ima_step_table = new short[] {
            7, 8, 9, 10, 11, 12, 13, 14, 16, 17,
            19, 21, 23, 25, 28, 31, 34, 37, 41, 45,
            50, 55, 60, 66, 73, 80, 88, 97, 107, 118,
            130, 143, 157, 173, 190, 209, 230, 253, 279, 307,
            337, 371, 408, 449, 494, 544, 598, 658, 724, 796,
            876, 963, 1060, 1166, 1282, 1411, 1552, 1707, 1878, 2066,
            2272, 2499, 2749, 3024, 3327, 3660, 4026, 4428, 4871, 5358,
            5894, 6484, 7132, 7845, 8630, 9493, 10442, 11487, 12635, 13899,
            15289, 16818, 18500, 20350, 22385, 24623, 27086, 29794, Short.MAX_VALUE };

    private int predictor = 0;

    private int step_index = 0;

    private byte[] decodeByte(byte paramByte) {
        byte[] arrayOfByte = new byte[4];
        int[] arrayOfInt = new int[2];
        arrayOfInt[0] = 0;
        arrayOfInt[1] = 1;
        int i = arrayOfInt.length;
        for (byte b = 0; b < i; b++) {
            int j = arrayOfInt[b];
            byte b1 = (byte)(paramByte >> (1 - j) * 4);
            short s = ima_step_table[this.step_index];
            int k = s >> 1;
            int m = k;
            if ((b1 & 0x4) != 0)
                m = k + (s << 2);
            k = m;
            if ((b1 & 0x2) != 0)
                k = m + (s << 1);
            m = k;
            if ((b1 & 0x1) != 0)
                m = k + s;
            m >>= 2;
            if ((b1 & 0x8) != 0) {
                this.predictor -= m;
            } else {
                this.predictor += m;
            }
            if (this.predictor > 32767) {
                this.predictor = 32767;
            } else if (this.predictor < -32768) {
                this.predictor = -32768;
            }
            this.step_index += ima_index_table[b1 & 0xF];
            if (this.step_index < 0) {
                this.step_index = 0;
            } else if (this.step_index > ima_step_table.length - 1) {
                this.step_index = ima_step_table.length - 1;
            }
            arrayOfByte[j * 2 + 1] = (byte)(byte)(this.predictor >> 8);
            arrayOfByte[j * 2] = (byte)(byte)this.predictor;
        }
        return arrayOfByte;
    }

//    private byte encodeShort(int paramInt) {
//        short s = ima_step_table[this.step_index];
//        int i = paramInt - this.predictor;
//        if (i >= 0) {
//            j = 0;
//        } else {
//            j = 8;
//            i = -i;
//        }
//        int k = j;
//        paramInt = i;
//        if (i >= s) {
//            k = (short)(j + 4);
//            paramInt = i - s;
//        }
//        int m = s >> 1;
//        i = k;
//        int n = paramInt;
//        if (paramInt >= m) {
//            i = (short)(k + 2);
//            n = paramInt - m;
//        }
//        int j = i;
//        if (n >= m >> 1)
//            j = (short)(i + 1);
//        paramInt = (short)(s >> 1);
//        k = paramInt;
//        if ((j & 0x4) != 0)
//            k = (short)((s << 2) + paramInt);
//        paramInt = k;
//        if ((j & 0x2) != 0)
//            paramInt = (short)((s << 1) + k);
//        k = paramInt;
//        if ((j & 0x1) != 0)
//            k = (short)(paramInt + s);
//        paramInt = (short)(k >> 2);
//        if ((j & 0x8) != 0) {
//            this.predictor -= paramInt;
//        } else {
//            this.predictor += paramInt;
//        }
//        if (this.predictor > 32767) {
//            this.predictor = 32767;
//        } else if (this.predictor < -32768) {
//            this.predictor = -32768;
//        }
//        this.step_index += ima_index_table[j];
//        if (this.step_index < 0) {
//            this.step_index = 0;
//            return (byte)(j & 0xF);
//        }
//        if (this.step_index > ima_step_table.length - 1)
//            this.step_index = ima_step_table.length - 1;
//        return (byte)(j & 0xF);
//    }

    public byte[] decode(byte[] paramArrayOfbyte) {
        byte[] arrayOfByte = new byte[paramArrayOfbyte.length * 4];
        byte b1 = 0;
        int i = paramArrayOfbyte.length;
        for (byte b2 = 0; b2 < i; b2++) {
            byte[] arrayOfByte1 = decodeByte(paramArrayOfbyte[b2]);
            arrayOfByte[b1 * 4] = (byte)arrayOfByte1[0];
            arrayOfByte[b1 * 4 + 1] = (byte)arrayOfByte1[1];
            arrayOfByte[b1 * 4 + 2] = (byte)arrayOfByte1[2];
            arrayOfByte[b1 * 4 + 3] = (byte)arrayOfByte1[3];
            b1++;
        }
        return arrayOfByte;
    }

//    public byte[] encode(int[] paramArrayOfint) {
//        byte[] arrayOfByte = new byte[paramArrayOfint.length / 2];
//        for (byte b = 0; b < paramArrayOfint.length / 2; b++) {
//            byte b1 = 0;
//            int[] arrayOfInt = new int[2];
//            arrayOfInt[0] = 0;
//            arrayOfInt[1] = 1;
//            int i = arrayOfInt.length;
//            for (byte b2 = 0; b2 < i; b2++) {
//                int j = arrayOfInt[b2];
//                b1 = (byte)((encodeShort(paramArrayOfint[b * 2 + j]) & 0xF) << (1 - j) * 4 | b1);
//            }
//            arrayOfByte[b] = (byte)b1;
//        }
//        return arrayOfByte;
//    }

    public void initialize() {
        this.predictor = 0;
        this.step_index = 0;
    }
}