package com.pvporbit.freetype;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.pvporbit.freetype.Utils.Pointer;

/**
 * Each library is completely independent from the others; it is the root of a set of objects like fonts, faces, sizes, etc.
 */
public class Library extends Pointer {

    public Library(long pointer) {
        super(pointer);
    }

    /**
     * Destroy the library object and all of it's childrens, including faces, sizes, etc.
     */
    public boolean delete() {
        return FreeType.FT_Done_FreeType(pointer);
    }

    /**
     * Create a new Face object from file<br>
     * It will return null in case of error.
     */
    public Face newFace(String file, int faceIndex) {
        try {
            return newFace(Utils.loadFileToByteArray(file), faceIndex);
        } catch (IOException e) {
        }
        return null;
    }

    /**
     * Create a new Face object from a byte[]<br>
     * It will return null in case of error.
     */
    public Face newFace(byte[] file, int faceIndex) {
        ByteBuffer buffer = Utils.newBuffer(file.length);
        buffer.order(ByteOrder.nativeOrder());
        buffer.limit(buffer.position() + file.length);
        Utils.fillBuffer(file, buffer, file.length);
        return newFace(buffer, faceIndex);
    }

    /**
     * Create a new Face object from a ByteBuffer.<br>
     * It will return null in case of error.<br>
     * Take care that the ByteByffer must be a direct buffer created with Utils.newBuffer and filled with Utils.fillBuffer.
     */
    public Face newFace(ByteBuffer file, int faceIndex) {
        long face = FreeType.FT_New_Memory_Face(pointer, file, file.remaining(), faceIndex);
        if (face <= 0) {
            Utils.deleteBuffer(file);
            return null;
        }
        return new Face(face, file);
    }

    /**
     * Returns a LibraryVersion object containing the information about the version of FreeType
     */
    public LibraryVersion getVersion() {
        return FreeType.FT_Library_Version(pointer);
    }
}