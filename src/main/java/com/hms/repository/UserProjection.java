package com.hms.repository;

public interface UserProjection {

    Long getId();

    String getEmail();

    String getFirstName();

    String getLastName();

    String getMobileNumber();

    boolean isEnabled();

    boolean isPasswordChangeRequired();

    String getRoleName();
}