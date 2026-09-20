package com.organizer.adapter.security.jwt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.springframework.stereotype.Component;

@Component
public class RsaKeyPairHolder {
  private final KeyPair keyPair;

  public RsaKeyPairHolder() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
      generator.initialize(2048);
      this.keyPair = generator.generateKeyPair();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("RSA not available on this JVM", e);
    }
  }

  public PrivateKey privateKey() {
    return this.keyPair.getPrivate();
  }

  public PublicKey publicKey() {
    return this.keyPair.getPublic();
  }
}
