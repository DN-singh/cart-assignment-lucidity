package com.springboot.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor 
public class ApplyOfferResponse {
	@JsonProperty("cart_value")
    private int cart_value;
}
