package io.pivotal.security.view;

import com.greghaskins.spectrum.Spectrum;
import io.pivotal.security.domain.Encryptor;
import io.pivotal.security.domain.NamedSshSecret;
import io.pivotal.security.service.Encryption;
import org.junit.runner.RunWith;
import org.springframework.test.util.JsonExpectationsHelper;

import java.time.Instant;
import java.util.UUID;

import static com.greghaskins.spectrum.Spectrum.beforeEach;
import static com.greghaskins.spectrum.Spectrum.it;
import static io.pivotal.security.helper.SpectrumHelper.json;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Spectrum.class)
public class SshViewTest {

  private static final JsonExpectationsHelper jsonExpectationsHelper = new JsonExpectationsHelper();

  private NamedSshSecret entity;

  private String secretName;

  private UUID uuid;

  private Encryptor encryptor;

  {
    beforeEach(() -> {
      secretName = "foo";
      uuid = UUID.randomUUID();
      encryptor = mock(Encryptor.class);
      when(encryptor.encrypt("my-private-key")).thenReturn(new Encryption("encrypted".getBytes(), "nonce".getBytes()));
      when(encryptor.decrypt(any(UUID.class), any(byte[].class), any(byte[].class))).thenReturn("my-private-key");
      entity = new NamedSshSecret(secretName)
          .setEncryptor(encryptor)
          .setPublicKey("my-public-key")
          .setPrivateKey("my-private-key");
      entity.setUuid(uuid);
    });

    it("creates a view from entity", () -> {
      final SecretView subject = SshView.fromEntity(entity);
      jsonExpectationsHelper.assertJsonEqual("{" +
          "\"id\":\"" + uuid.toString() + "\"," +
          "\"type\":\"ssh\"," +
          "\"name\":\"foo\"," +
          "\"version_created_at\":null," +
          "\"value\":{" +
            "\"public_key\":\"my-public-key\"," +
            "\"private_key\":\"my-private-key\"" +
          "}" +
        "}", json(subject), true);
    });

    it("sets updated-at time on generated view", () -> {
      Instant now = Instant.now();
      entity.setVersionCreatedAt(now);
      final SshView subject = (SshView) SshView.fromEntity(entity);
      assertThat(subject.getVersionCreatedAt(), equalTo(now));
    });

    it("sets uuid on generated view", () -> {
      SshView subject = (SshView) SshView.fromEntity(entity);
      assertThat(subject.getUuid(), equalTo(uuid.toString()));
    });
  }
}
