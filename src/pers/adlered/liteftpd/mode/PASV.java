package pers.adlered.liteftpd.mode;

import jdk.internal.util.xml.impl.Input;
import pers.adlered.liteftpd.analyze.PrivateVariable;
import pers.adlered.liteftpd.main.PauseListen;
import pers.adlered.liteftpd.main.Send;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class PASV extends Thread {
    private ServerSocket serverSocket = null;
    private Socket socket = null;
    private Send send = null;
    private PrivateVariable privateVariable = null;
    private PauseListen pauseListen = null;

    private String listening = null;
    private File file = null;

    public PASV(int port, Send send, PrivateVariable privateVariable, PauseListen pauseListen) {
        System.out.println("Listening " + port + "...");
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            this.serverSocket = serverSocket;
            this.send = send;
            this.privateVariable = privateVariable;
            this.pauseListen = pauseListen;
        } catch (IOException IOE) {
            //TODO
            IOE.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("WAITING...");
            Socket socket = serverSocket.accept();
            this.socket = socket;
            System.out.println("Connected. Waiting for " + socket.getRemoteSocketAddress() + "...");
            while (listening == null && file == null) {
                if (!pauseListen.isRunning()) {
                    System.out.println("Passive mode listener paused.");
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException IE) {
                }
            }
            if (pauseListen.isRunning()) {
                System.out.print("\r\nService has response.");
                long startTime = System.nanoTime();
                double kb = 0;
                long bts = 0;
                if (listening != null) {
                    //To avoid bare line feeds.
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(socket.getOutputStream());
                    listening = listening.replaceAll("\r\n", "\n");
                    listening = listening.replaceAll("\n", "\r\n");
                    bufferedOutputStream.write(listening.getBytes(privateVariable.encode));
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                    bts = (listening.getBytes(privateVariable.encode)).length;
                    kb = bts / 1000;
                } else if (file != null) {
                    InputStream inputStream = new FileInputStream(file);
                    OutputStream outputStream = new DataOutputStream(socket.getOutputStream());
                    int temp;
                    while ((temp = inputStream.read()) != -1) {
                        outputStream.write(temp);
                    }
                    inputStream.close();
                    outputStream.close();
                    //outputStream.write("\r\n".getBytes());
                    bts = file.length();
                    kb = bts / 1000;
                }
                socket.close();
                serverSocket.close();
                long stopTime = System.nanoTime();
                long nanoEndTime = stopTime - startTime;
                double endTime = nanoEndTime / 1000000000;
                double perSecond = 0;
                if (endTime == 0) {
                    perSecond = kb;
                } else {
                    perSecond = kb / endTime;
                }
                send.send("226 Complete! " + bts + " bytes in " + nanoEndTime + " nanosecond transferred. " + perSecond + " KB/sec.\r\n");
            }
        } catch (IOException IOE) {
            //TODO
            IOE.printStackTrace();
        } finally {
            if (pauseListen.isRunning()) {
                privateVariable.setTimeoutLock(false);
            }
        }
    }

    public void hello(String message) {
        listening = message;
    }

    public void hello(File file) {
        this.file = file;
    }
}
