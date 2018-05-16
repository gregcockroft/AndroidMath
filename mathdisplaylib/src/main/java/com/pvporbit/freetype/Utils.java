package com.pvporbit.freetype;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class Utils {

    public static class Pointer {
        protected long pointer;

        public Pointer(long pointer) {
            this.pointer = pointer;
        }

        public long getPointer() {
            return pointer;
        }
    }

    public static byte[] loadFileToByteArray(String filepath) throws IOException, FileNotFoundException {
        File file = new File(filepath);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
        buf.read(bytes, 0, bytes.length);
        buf.close();
        return bytes;
    }

    /* Buffer helpers */

    public static native ByteBuffer newBuffer(int size);

    public static native void fillBuffer(byte[] bytes, ByteBuffer buffer, int length);

    public static native void deleteBuffer(ByteBuffer buffer);

}