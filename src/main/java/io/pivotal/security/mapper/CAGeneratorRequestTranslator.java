package io.pivotal.security.mapper;

import com.jayway.jsonpath.DocumentContext;
import io.pivotal.security.controller.v1.CertificateSecretParameters;
import io.pivotal.security.data.CertificateAuthorityDataService;
import io.pivotal.security.entity.NamedCertificateAuthority;
import io.pivotal.security.generator.BCCertificateGenerator;
import io.pivotal.security.view.CertificateAuthorityView;
import io.pivotal.security.view.CertificateAuthorityBody;
import io.pivotal.security.view.ParameterizedValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;

@Component
public class CAGeneratorRequestTranslator implements RequestTranslator<NamedCertificateAuthority> {
  @Autowired
  BCCertificateGenerator certificateGenerator;

  @Autowired
  CertificateGeneratorRequestTranslator certificateGeneratorRequestTranslator;

  @Autowired
  public CertificateAuthorityDataService certificateAuthorityDataService;

  @Override
  public void populateEntityFromJson(NamedCertificateAuthority namedCA, DocumentContext documentContext) {
    if (!"root".equals(documentContext.read("$.type"))) {
      throw new ParameterizedValidationException("error.bad_authority_type");
    }

    CertificateSecretParameters parameters =
        certificateGeneratorRequestTranslator.validCertificateAuthorityParameters(documentContext);

    CertificateAuthorityView certificateAuthorityView;
    try {
      certificateAuthorityView = certificateGenerator.generateCertificateAuthority(parameters);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    CertificateAuthorityBody caBody = certificateAuthorityView.getCertificateAuthorityBody();
    namedCA
        .setType(certificateAuthorityView.getType())
        .setCertificate(caBody.getCertificate())
        .setPrivateKey(caBody.getPrivateKey());
  }

  @Override
  public Set<String> getValidKeys() {
    return of("$['type']",
        "$['name']",
        "$['parameters']",
        "$['parameters']['common_name']",
        "$['parameters']['organization']",
        "$['parameters']['organization_unit']",
        "$['parameters']['locality']",
        "$['parameters']['state']",
        "$['parameters']['country']",
        "$['parameters']['key_length']",
        "$['parameters']['duration']"
    );
  }
}
