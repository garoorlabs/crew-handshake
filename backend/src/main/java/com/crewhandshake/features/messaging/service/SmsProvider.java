package com.crewhandshake.features.messaging.service;

public interface SmsProvider {
  void sendOtp(String phoneE164, String code);

  default void sendCrewCall(String phoneE164, String message) {
    // default no-op
  }

  default void sendCrewCallUpdate(String phoneE164, String message) {
    // default no-op
  }

  default void sendStandbyClosure(String phoneE164, String message) {
    // default no-op
  }
}
