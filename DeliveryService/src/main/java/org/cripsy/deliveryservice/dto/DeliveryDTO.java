package org.cripsy.deliveryservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryDTO {
    private  Integer personId;
    private  String name;
    private  String email;
    private  String password;
    private  String contact;
    private  Boolean availability;
}

