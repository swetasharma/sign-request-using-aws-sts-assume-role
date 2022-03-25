package com.example.signrequestusingawssignature.pojo.aws;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AWSToken {
	private String date;
    private String token;
    private String authorization;
}
