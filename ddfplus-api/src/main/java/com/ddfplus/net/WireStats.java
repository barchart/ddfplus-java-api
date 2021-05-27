package com.ddfplus.net;

public class WireStats {
    private static final int MB = 1000 * 1000;
    private volatile double bytesReceived;
    private volatile double bitsReceived;
    private volatile double bitsReceivedMax;

    public void update(long bytesReceived) {
        this.bytesReceived += bytesReceived;
        this.bitsReceived += bytesReceived * 8;
        if (this.bitsReceived > this.bitsReceivedMax) {
            this.bitsReceivedMax = this.bitsReceived;
        }
    }

    public void reset() {
        this.bytesReceived = this.bitsReceived = 0;
    }

    public double getBytesReceived() {
        return bytesReceived;
    }

    public double getBitsReceived() {
        return bitsReceived;
    }

    public String toString() {
        return "Wire: Kbytes/sec = " + bytesReceived / 1000 + ", Mbps = " +
                this.bitsReceived / MB + " Max Mbps = " + bitsReceivedMax / MB;
    }
}
