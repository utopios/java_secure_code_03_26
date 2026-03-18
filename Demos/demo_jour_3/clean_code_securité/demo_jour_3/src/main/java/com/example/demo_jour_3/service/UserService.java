package com.example.demo_jour_3.service;

import com.example.demo_jour_3.demo_aspect.annotation.Loggable;
import com.example.demo_jour_3.demo_aspect.annotation.Tracked;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Loggable
    @Tracked
    public void methodA() {

    }
}
