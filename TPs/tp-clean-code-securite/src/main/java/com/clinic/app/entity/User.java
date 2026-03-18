package com.clinic.app.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public String n;
    public String p;
    public String e;
    public String r;
    public String tok;
    public boolean a;
}
