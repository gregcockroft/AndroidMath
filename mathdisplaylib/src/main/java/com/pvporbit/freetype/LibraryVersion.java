package com.pvporbit.freetype;

/**
 * This is a simple class wich contains the version information about FreeType.
 */
public class LibraryVersion {

    private int major, minor, patch; // Example: 2.6.0

    public LibraryVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    @Override
    public String toString() {
        return major + "." + minor + "." + patch;
    }
}