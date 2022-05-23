package com.example.myapplication.rest;

import com.example.myapplication.MyAppException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class InputReader {
    public static String readToString(InputStream in) {
        try {
            ByteArrayOutputStream buffers = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            while (true) {
                int n = in.read(buffer, 0, buffer.length);
                if (n == -1) {
                    break;
                }
                buffers.write(buffer, 0, n);
            }
            return new String(buffers.toByteArray(), "UTF-8");
        } catch (Exception e) {
            throw new MyAppException(e);
        }
    }
}
