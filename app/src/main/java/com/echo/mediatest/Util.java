package com.echo.mediatest;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Util {
    public static byte[] intToByteArray(int data){
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    public static byte[] shortToByteArray(short data){
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }

    public static int byteArrayToInt(byte[] b){
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    public static short byteArrayToShort(byte[] b){
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    public static byte[] byteSplit(byte[] src,int begin,int count){
        byte[] desbyte=new byte[count];
        System.arraycopy(src,begin,desbyte,0,count);
        return desbyte;
    }
}
