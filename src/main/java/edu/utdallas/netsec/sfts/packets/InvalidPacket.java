package edu.utdallas.netsec.sfts.packets;

/**
 * @author Fahad Shaon
 */
public class InvalidPacket extends RuntimeException {

    public InvalidPacket() {
        super();
    }

    public InvalidPacket(String message) {
        super(message);
    }

    public InvalidPacket(Throwable cause) {
        super(cause);
    }

    public InvalidPacket(String message, Throwable cause) {
        super(message, cause);
    }
}
