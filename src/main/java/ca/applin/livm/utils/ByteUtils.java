package ca.applin.livm.utils;

import java.util.Arrays;
import java.util.function.Supplier;

public class ByteUtils {

    // big endian
    public static byte[] to_bytes_big(double f) {
        throw new RuntimeException("TODO: to_bytes_big(double f)");
    }

    // big endian
    public static byte[] to_bytes_big(float f) {
        throw new RuntimeException("TODO: to_bytes_big(float f)");
    }

    // big endian
    public static byte[] to_bytes_big(short s) {
        return new byte[] {
                (byte) ((s >> 8) & 0x00FF),
                (byte) ((s     ) & 0x00FF)
        };
    }

    // big endian
    public static byte[] to_bytes_big(int i) {
        return new byte[] {
                (byte) ((i >> 24) & 0x00FF),
                (byte) ((i >> 16) & 0x00FF),
                (byte) ((i >>  8) & 0x00FF),
                (byte) ((i      ) & 0x00FF)
        };
    }

    // big endian
    public static byte[] to_bytes_big(long l) {
        return new byte[] {
                (byte) ((l >> 56) & 0x00FF),
                (byte) ((l >> 48) & 0x00FF),
                (byte) ((l >> 40) & 0x00FF),
                (byte) ((l >> 32) & 0x00FF),
                (byte) ((l >> 24) & 0x00FF),
                (byte) ((l >> 16) & 0x00FF),
                (byte) ((l >>  8) & 0x00FF),
                (byte) ((l      ) & 0x00FF)
        };
    }

    // byte[]{ high_bytes, ..., ..., low_bytes }
    public static int from_byte_int_big(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8 ) |
               ((bytes[3] & 0xFF) << 0 );
    }

    public static byte[] flat(byte[][] bytes) {
        int totalLength = 0;
        for (byte[] b : bytes) {
            totalLength += b.length;
        }
        byte[] res = new byte[totalLength];
        int index = 0;
        for (byte[] b : bytes) {
            System.arraycopy(b, 0, res, index, b.length);
            index += b.length;
        }
        return res;
    }

    public static byte[] flat(Supplier<byte[]>[] bytes) {
        int totalLength = 0;
        for (Supplier<byte[]> b : bytes) {
            totalLength += b.get().length;
        }
        byte[] res = new byte[totalLength];
        int index = 0;
        for (Supplier<byte[]> b : bytes) {
            byte[] arr = b.get();
            System.arraycopy(arr, 0, res, index, arr.length);
            index += arr.length;
        }
        return res;
    }

}
