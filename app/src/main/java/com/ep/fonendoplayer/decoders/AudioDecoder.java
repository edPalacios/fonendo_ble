package com.ep.fonendoplayer.decoders;

public class AudioDecoder
{
    public static final int RANGE_HIGH = 32767;
    public static final int RANGE_LOW = 32768;
    private static final short[] ima_index_table;
    private static final short[] ima_step_table;
    private int predictor;
    private int step_index;

    static {
        final short[] array;
        final short[] ima_index_table2 = array = new short[16];
        array[1] = (array[0] = -1);
        array[3] = (array[2] = -1);
        array[4] = 2;
        array[5] = 4;
        array[6] = 6;
        array[7] = 8;
        array[9] = (array[8] = -1);
        array[11] = (array[10] = -1);
        array[12] = 2;
        array[13] = 4;
        array[14] = 6;
        array[15] = 8;
        ima_index_table = ima_index_table2;
        final short[] array2;
        final short[] ima_step_table2 = array2 = new short[89];
        array2[0] = 7;
        array2[1] = 8;
        array2[2] = 9;
        array2[3] = 10;
        array2[4] = 11;
        array2[5] = 12;
        array2[6] = 13;
        array2[7] = 14;
        array2[8] = 16;
        array2[9] = 17;
        array2[10] = 19;
        array2[11] = 21;
        array2[12] = 23;
        array2[13] = 25;
        array2[14] = 28;
        array2[15] = 31;
        array2[16] = 34;
        array2[17] = 37;
        array2[18] = 41;
        array2[19] = 45;
        array2[20] = 50;
        array2[21] = 55;
        array2[22] = 60;
        array2[23] = 66;
        array2[24] = 73;
        array2[25] = 80;
        array2[26] = 88;
        array2[27] = 97;
        array2[28] = 107;
        array2[29] = 118;
        array2[30] = 130;
        array2[31] = 143;
        array2[32] = 157;
        array2[33] = 173;
        array2[34] = 190;
        array2[35] = 209;
        array2[36] = 230;
        array2[37] = 253;
        array2[38] = 279;
        array2[39] = 307;
        array2[40] = 337;
        array2[41] = 371;
        array2[42] = 408;
        array2[43] = 449;
        array2[44] = 494;
        array2[45] = 544;
        array2[46] = 598;
        array2[47] = 658;
        array2[48] = 724;
        array2[49] = 796;
        array2[50] = 876;
        array2[51] = 963;
        array2[52] = 1060;
        array2[53] = 1166;
        array2[54] = 1282;
        array2[55] = 1411;
        array2[56] = 1552;
        array2[57] = 1707;
        array2[58] = 1878;
        array2[59] = 2066;
        array2[60] = 2272;
        array2[61] = 2499;
        array2[62] = 2749;
        array2[63] = 3024;
        array2[64] = 3327;
        array2[65] = 3660;
        array2[66] = 4026;
        array2[67] = 4428;
        array2[68] = 4871;
        array2[69] = 5358;
        array2[70] = 5894;
        array2[71] = 6484;
        array2[72] = 7132;
        array2[73] = 7845;
        array2[74] = 8630;
        array2[75] = 9493;
        array2[76] = 10442;
        array2[77] = 11487;
        array2[78] = 12635;
        array2[79] = 13899;
        array2[80] = 15289;
        array2[81] = 16818;
        array2[82] = 18500;
        array2[83] = 20350;
        array2[84] = 22385;
        array2[85] = 24623;
        array2[86] = 27086;
        array2[87] = 29794;
        array2[88] = 32767;
        ima_step_table = ima_step_table2;
    }

    public AudioDecoder() {
        this.predictor = 0;
        this.step_index = 0;
    }

    private byte[] decodeByte(final byte b) {
        final int predictor = 32767;
        final int predictor2 = -32768;
        final byte[] array = new byte[4];
        final int[] array3;
        final int[] array2 = array3 = new int[2];
        array3[0] = 0;
        array3[1] = 1;
        for (int length = array2.length, i = 0; i < length; ++i) {
            final int n = array2[i];
            final byte b2 = (byte)(b >> (1 - n) * 4);
            final short n2 = AudioDecoder.ima_step_table[this.step_index];
            int n3 = n2 >> 1;
            if ((b2 & 0x4) != 0x0) {
                n3 += n2 << 2;
            }
            if ((b2 & 0x2) != 0x0) {
                n3 += n2 << 1;
            }
            if ((b2 & 0x1) != 0x0) {
                n3 += n2;
            }
            final int n4 = n3 >> 2;
            if ((b2 & 0x8) != 0x0) {
                this.predictor -= n4;
            }
            else {
                this.predictor += n4;
            }
            if (this.predictor > predictor) {
                this.predictor = predictor;
            }
            else if (this.predictor < predictor2) {
                this.predictor = predictor2;
            }
            this.step_index += AudioDecoder.ima_index_table[b2 & 0xF];
            if (this.step_index < 0) {
                this.step_index = 0;
            }
            else if (this.step_index > AudioDecoder.ima_step_table.length - 1) {
                this.step_index = AudioDecoder.ima_step_table.length - 1;
            }
            array[n * 2 + 1] = (byte)(this.predictor >> 8);
            array[n * 2] = (byte)this.predictor;
        }
        return array;
    }

    private byte encodeShort(final int n) {
        final int predictor = 32767;
        final int predictor2 = -32768;
        final short n2 = AudioDecoder.ima_step_table[this.step_index];
        int n3 = n - this.predictor;
        int n4;
        if (n3 >= 0) {
            n4 = 0;
        }
        else {
            n4 = 8;
            n3 = -n3;
        }
        final short n5 = n2;
        if (n3 >= n2) {
            n4 = (short)(n4 + 4);
            n3 -= n2;
        }
        final int n6 = n5 >> 1;
        if (n3 >= n6) {
            n4 = (short)(n4 + 2);
            n3 -= n6;
        }
        if (n3 >= n6 >> 1) {
            n4 = (short)(n4 + 1);
        }
        short n7 = (short)(n2 >> 1);
        if ((n4 & 0x4) != 0x0) {
            n7 += (short)(n2 << 2);
        }
        if ((n4 & 0x2) != 0x0) {
            n7 += (short)(n2 << 1);
        }
        if ((n4 & 0x1) != 0x0) {
            n7 += n2;
        }
        final short n8 = (short)(n7 >> 2);
        if ((n4 & 0x8) != 0x0) {
            this.predictor -= n8;
        }
        else {
            this.predictor += n8;
        }
        if (this.predictor > predictor) {
            this.predictor = predictor;
        }
        else if (this.predictor < predictor2) {
            this.predictor = predictor2;
        }
        this.step_index += AudioDecoder.ima_index_table[n4];
        if (this.step_index < 0) {
            this.step_index = 0;
        }
        else if (this.step_index > AudioDecoder.ima_step_table.length - 1) {
            this.step_index = AudioDecoder.ima_step_table.length - 1;
        }
        return (byte)(n4 & 0xF);
    }

    public byte[] decode(final byte[] array) {
        final byte[] array2 = new byte[array.length * 4];
        int n = 0;
        for (int length = array.length, i = 0; i < length; ++i) {
            final byte[] decodeByte = this.decodeByte(array[i]);
            array2[n * 4] = decodeByte[0];
            array2[n * 4 + 1] = decodeByte[1];
            array2[n * 4 + 2] = decodeByte[2];
            array2[n * 4 + 3] = decodeByte[3];
            ++n;
        }
        return array2;
    }

    public byte[] encode(final int[] array) {
        final byte[] array2 = new byte[array.length / 2];
        for (int i = 0; i < array.length / 2; ++i) {
            byte b = 0;
            final int[] array4;
            final int[] array3 = array4 = new int[2];
            array4[0] = 0;
            array4[1] = 1;
            for (int length = array3.length, j = 0; j < length; ++j) {
                final int n = array3[j];
                b |= (byte)((this.encodeShort(array[i * 2 + n]) & 0xF) << (1 - n) * 4);
            }
            array2[i] = b;
        }
        return array2;
    }

    public void initialize() {
        this.predictor = 0;
        this.step_index = 0;
    }
}