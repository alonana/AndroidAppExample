package com.example.myapplication.bouncer;

import com.example.myapplication.MyAppException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

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
            return buffers.toString(StandardCharsets.UTF_8.toString());
        } catch (Exception e) {
            throw new MyAppException(e);
        }
    }
}
