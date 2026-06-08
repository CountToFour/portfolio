package com.example.AI.Payloads.Responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class GroqResponse {
    private String output;
}
