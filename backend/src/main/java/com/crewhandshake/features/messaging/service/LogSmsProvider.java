package com.crewhandshake.features.messaging.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogSmsProvider implements SmsProvider {
  private static final Logger logger = LoggerFactory.getLogger(LogSmsProvider.class);

  @Override
  public void sendOtp(String phoneE164, String code) {
    logger.info("OTP for {}: {}", phoneE164, code);
  }

  @Override
  public void sendCrewCall(String phoneE164, String message) {
    logger.info("Crew call SMS to {}: {}", phoneE164, message);
  }

  @Override
  public void sendCrewCallUpdate(String phoneE164, String message) {
    logger.info("Crew call update SMS to {}: {}", phoneE164, message);
  }

  @Override
  public void sendStandbyClosure(String phoneE164, String message) {
    logger.info("Standby closure SMS to {}: {}", phoneE164, message);
  }
}
