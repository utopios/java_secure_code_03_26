package com.example.demo_jour_3.entity;

import java.util.ArrayList;
import java.util.List;

public class Demo {
    public static void main(String[] args) {
        List<A> liste = new ArrayList<>();
        liste.add(new A());
        liste.add(new B());
        liste.add(new C());

        for (A a : liste) {
            a.methodeA();
        }
    }
}
