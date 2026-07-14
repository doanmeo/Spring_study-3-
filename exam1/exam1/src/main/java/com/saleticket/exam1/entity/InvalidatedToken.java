package com.saleticket.exam1.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@Entity
public class InvalidatedToken {
    @Id
    String id;
    java.util.Date expiryTime;

}
