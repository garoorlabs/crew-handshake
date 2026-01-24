package com.crewhandshake.features.messaging.service;

public class NoopSmsProvider implements SmsProvider {
  @Override
  public void sendOtp(String phoneE164, String code) {
    // intentionally no-op for development
  }

  @Override
  public void sendCrewCall(String phoneE164, String message) {
    // intentionally no-op for development
  }

  @Override
  public void sendCrewCallUpdate(String phoneE164, String message) {
    // intentionally no-op for development
  }

  @Override
  public void sendStandbyClosure(String phoneE164, String message) {
    // intentionally no-op for development
  }
}
