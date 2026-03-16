package com.example.springsecurityjwt.dto;

import lombok.Data;

@Data
public class BaseResponseDto {

    private Object message;
    private Object data;


    public BaseResponseDto(Object message) {
        this.message = message;
    }

    public BaseResponseDto(Object message, Object data) {
        this.message = message;
        this.data = data;
    }




}
