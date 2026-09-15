package com.hms.repository;

public interface BillItemProjection {

    Long getBillId();

    String getItemName();

    Integer getQuantity();

    Double getPrice();

    Double getTotal();
}