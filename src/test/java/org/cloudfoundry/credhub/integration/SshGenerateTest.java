package org.cloudfoundry.credhub.integration;

import org.cloudfoundry.credhub.CredentialManagerApp;
import org.cloudfoundry.credhub.util.DatabaseProfileResolver;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.cloudfoundry.credhub.helper.RequestHelper.generateSsh;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.IsNot.not;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@RunWith(SpringRunner.class)
@ActiveProfiles(value = "unit-test", resolver = DatabaseProfileResolver.class)
@SpringBootTest(classes = CredentialManagerApp.class)
@Transactional
public class SshGenerateTest {
  private static final String CREDENTIAL_NAME = "/set_credential";

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @Before
  public void setup() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(springSecurity())
        .build();
  }

  @Test
  public void credentialNotOverwrittenWhenModeIsSetToConvergeAndParametersAreTheSame() throws Exception {
    String firstResponse = generateSsh(mockMvc, CREDENTIAL_NAME, false, 2048, null);
    String originalValue = (new JSONObject(firstResponse)).getString("value");

    String secondResponse = generateSsh(mockMvc, CREDENTIAL_NAME, false, 2048, null);
    String sameValue = (new JSONObject(secondResponse)).getString("value");

    assertThat(originalValue, equalTo(sameValue));
  }

  @Test
  public void credentialNotOverwrittenWhenModeIsSetToConvergeAndParametersAreTheSameAndAreTheDefault() throws Exception {
    String firstResponse = generateSsh(mockMvc, CREDENTIAL_NAME, true, null, null);
    String originalValue = (new JSONObject(firstResponse)).getString("value");

    String secondResponse = generateSsh(mockMvc, CREDENTIAL_NAME, false, null, null);
    String sameValue = (new JSONObject(secondResponse)).getString("value");

    assertThat(originalValue, equalTo(sameValue));
  }

  @Test
  public void credentialNotOverwrittenWhenModeIsSetToConvergeAndSshCommentDoesNotChange() throws Exception {
    String firstResponse = generateSsh(mockMvc, CREDENTIAL_NAME, true, null, "some-comment");
    String originalValue = (new JSONObject(firstResponse)).getString("value");

    String secondResponse = generateSsh(mockMvc, CREDENTIAL_NAME, false, null, "some-comment");
    String sameValue = (new JSONObject(secondResponse)).getString("value");

    assertThat(originalValue, equalTo(sameValue));
  }

  @Test
  public void credentialOverwrittenWhenModeIsSetToConvergeAndParametersNotTheSame() throws Exception {
    String firstResponse = generateSsh(mockMvc, CREDENTIAL_NAME, true, 4096, null);
    String originalValue = (new JSONObject(firstResponse)).getString("value");

    String secondResponse = generateSsh(mockMvc, CREDENTIAL_NAME, false, 2048, null);
    String updatedValue = (new JSONObject(secondResponse)).getString("value");

    assertThat(originalValue, not(equalTo(updatedValue)));
  }
}
