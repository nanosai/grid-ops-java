package com.nanosai.gridops.tcp;

/**
 * An ISocketManager helps manage the sockets (connections) managed by a TcpSocketsPort.
 */
public interface ISocketManager {


    /**
     * Called when the ISocketManager is first set on TcpSocketsPort.
     *
     * @param tcpSocketsPort The TcpSocketsPort the ISocketManager is added to.
     */
    public void init(TcpSocketsPort tcpSocketsPort);

    /**
     * Called when a new TcpSocket is added to the TcpSocketManager
     *
     * @param tcpSocket The newly added socket.
     */
    public void socketAdded(TcpSocket tcpSocket);


    /**
     * Called after a TcpSocket is closed by the TcpSocketsPort
     * @param tcpSocket The tcpSocket that was just closed the the TcpSocketsPort
     */
    public void socketClosed(TcpSocket tcpSocket);


}
