package com.example.demo_jour_3.entity;

import java.nio.ByteBuffer;

public class A {

    public void methodeA() {
        System.out.println("Methode Class A");
        ByteBuffer buffer = ByteBuffer.allocateDirect(100000);
        buffer.clear();
        buffer = null;
    }
}
