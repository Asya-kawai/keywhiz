/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package keywhiz.service.daos;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;
import keywhiz.KeywhizTestRunner;
import keywhiz.api.ApiDate;
import keywhiz.api.model.SecretContent;
import keywhiz.api.model.SecretSeries;
import keywhiz.api.model.SecretSeriesAndContent;
import keywhiz.service.crypto.ContentCryptographer;
import keywhiz.service.crypto.CryptoFixtures;
import keywhiz.service.daos.SecretDAO.SecretDAOFactory;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.exception.DataAccessException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static keywhiz.jooq.tables.Secrets.SECRETS;
import static keywhiz.jooq.tables.SecretsContent.SECRETS_CONTENT;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(KeywhizTestRunner.class)
public class SecretDAOTest {
  @Inject DSLContext jooqContext;
  @Inject ObjectMapper objectMapper;
  @Inject SecretDAOFactory secretDAOFactory;

  final static ContentCryptographer cryptographer = CryptoFixtures.contentCryptographer();
  final static ApiDate date = ApiDate.now();
  ImmutableMap<String, String> emptyMetadata = ImmutableMap.of();

  SecretSeries series1 = SecretSeries.of(1, "secret1", "desc1", date, "creator", date, "updater", null, null, 101L);
  String content = "c2VjcmV0MQ==";
  String encryptedContent = cryptographer.encryptionKeyDerivedFrom(series1.name()).encrypt(content);
  SecretContent content1 = SecretContent.of(101, 1, encryptedContent, date, "creator", date, "updater", emptyMetadata);
  SecretSeriesAndContent secret1 = SecretSeriesAndContent.of(series1, content1);

  SecretSeries series2 = SecretSeries.of(2, "secret2", "desc2", date, "creator", date, "updater", null, null, 102L);
  SecretContent content2a = SecretContent.of(102, 2, encryptedContent, date, "creator", date, "updater", emptyMetadata);
  SecretSeriesAndContent secret2a = SecretSeriesAndContent.of(series2, content2a);

  SecretContent content2b = SecretContent.of(103, 2, "some other content", date, "creator", date, "updater", emptyMetadata);
  SecretSeriesAndContent secret2b = SecretSeriesAndContent.of(series2, content2b);

  SecretSeries series3 = SecretSeries.of(3, "secret3", "desc3", date, "creator", date, "updater", null, null, null);
  SecretContent content3 = SecretContent.of(104, 3, encryptedContent, date, "creator", date, "updater", emptyMetadata);
  SecretSeriesAndContent secret3 = SecretSeriesAndContent.of(series3, content3);

  SecretDAO secretDAO;

  @Before
  public void setUp() throws Exception {
    jooqContext.insertInto(SECRETS)
        .set(SECRETS.ID, series1.id())
        .set(SECRETS.NAME, series1.name())
        .set(SECRETS.DESCRIPTION, series1.description())
        .set(SECRETS.CREATEDBY, series1.createdBy())
        .set(SECRETS.CREATEDAT, series1.createdAt().toEpochSecond())
        .set(SECRETS.UPDATEDBY, series1.updatedBy())
        .set(SECRETS.UPDATEDAT, series1.updatedAt().toEpochSecond())
        .set(SECRETS.CURRENT, series1.currentVersion().orElse(null))
        .execute();

    jooqContext.insertInto(SECRETS_CONTENT)
        .set(SECRETS_CONTENT.ID, secret1.content().id())
        .set(SECRETS_CONTENT.SECRETID, secret1.series().id())
        .set(SECRETS_CONTENT.ENCRYPTED_CONTENT, secret1.content().encryptedContent())
        .set(SECRETS_CONTENT.CREATEDBY, secret1.content().createdBy())
        .set(SECRETS_CONTENT.CREATEDAT, secret1.content().createdAt().toEpochSecond())
        .set(SECRETS_CONTENT.UPDATEDBY, secret1.content().updatedBy())
        .set(SECRETS_CONTENT.UPDATEDAT, secret1.content().updatedAt().toEpochSecond())
        .set(SECRETS_CONTENT.METADATA,
            objectMapper.writeValueAsString(secret1.content().metadata()))
        .execute();

    jooqContext.insertInto(SECRETS)
        .set(SECRETS.ID, series2.id())
        .set(SECRETS.NAME, series2.name())
        .set(SECRETS.DESCRIPTION, series2.description())
        .set(SECRETS.CREATEDBY, series2.createdBy())
        .set(SECRETS.CREATEDAT, series2.createdAt().toEpochSecond())
        .set(SECRETS.UPDATEDBY, series2.updatedBy())
        .set(SECRETS.UPDATEDAT, series2.updatedAt().toEpochSecond())
        .set(SECRETS.CURRENT, series2.currentVersion().orElse(null))
        .execute();

    jooqContext.insertInto(SECRETS_CONTENT)
        .set(SECRETS_CONTENT.ID, secret2a.content().id())
        .set(SECRETS_CONTENT.SECRETID, secret2a.series().id())
        .set(SECRETS_CONTENT.ENCRYPTED_CONTENT, secret2a.content().encryptedContent())
        .set(SECRETS_CONTENT.CREATEDBY, secret2a.content().createdBy())
        .set(SECRETS_CONTENT.CREATEDAT, secret2a.content().createdAt().toEpochSecond())
        .set(SECRETS_CONTENT.UPDATEDBY, secret2a.content().updatedBy())
        .set(SECRETS_CONTENT.UPDATEDAT, secret2a.content().updatedAt().toEpochSecond())
        .set(SECRETS_CONTENT.METADATA, objectMapper.writeValueAsString(secret2a.content().metadata()))
        .execute();

    jooqContext.insertInto(SECRETS_CONTENT)
        .set(SECRETS_CONTENT.ID, secret2b.content().id())
        .set(SECRETS_CONTENT.SECRETID, secret2b.series().id())
        .set(SECRETS_CONTENT.ENCRYPTED_CONTENT, secret2b.content().encryptedContent())
        .set(SECRETS_CONTENT.CREATEDBY, secret2b.content().createdBy())
        .set(SECRETS_CONTENT.CREATEDAT, secret2b.content().createdAt().toEpochSecond())
        .set(SECRETS_CONTENT.UPDATEDBY, secret2b.content().updatedBy())
        .set(SECRETS_CONTENT.UPDATEDAT, secret2b.content().updatedAt().toEpochSecond())
        .set(SECRETS_CONTENT.METADATA, objectMapper.writeValueAsString(secret2b.content().metadata()))
        .execute();

    jooqContext.insertInto(SECRETS)
        .set(SECRETS.ID, series3.id())
        .set(SECRETS.NAME, series3.name())
        .set(SECRETS.DESCRIPTION, series3.description())
        .set(SECRETS.CREATEDBY, series3.createdBy())
        .set(SECRETS.CREATEDAT, series3.createdAt().toEpochSecond())
        .set(SECRETS.UPDATEDBY, series3.updatedBy())
        .set(SECRETS.UPDATEDAT, series3.updatedAt().toEpochSecond())
        .set(SECRETS.CURRENT, series3.currentVersion().orElse(null))
        .execute();

    jooqContext.insertInto(SECRETS_CONTENT)
        .set(SECRETS_CONTENT.ID, secret3.content().id())
        .set(SECRETS_CONTENT.SECRETID, secret3.series().id())
        .set(SECRETS_CONTENT.ENCRYPTED_CONTENT, secret3.content().encryptedContent())
        .set(SECRETS_CONTENT.CREATEDBY, secret3.content().createdBy())
        .set(SECRETS_CONTENT.CREATEDAT, secret3.content().createdAt().toEpochSecond())
        .set(SECRETS_CONTENT.UPDATEDBY, secret3.content().updatedBy())
        .set(SECRETS_CONTENT.UPDATEDAT, secret3.content().updatedAt().toEpochSecond())
        .set(SECRETS_CONTENT.METADATA, objectMapper.writeValueAsString(secret3.content().metadata()))
        .execute();


    secretDAO = secretDAOFactory.readwrite();
  }

  @Test public void createSecret() {
    int secretsBefore = tableSize(SECRETS);
    int secretContentsBefore = tableSize(SECRETS_CONTENT);

    String name = "newSecret";
    String content = "c2VjcmV0MQ==";
    String encryptedContent = cryptographer.encryptionKeyDerivedFrom(name).encrypt(content);
    long newId = secretDAO.createSecret(name, encryptedContent, "creator",
        ImmutableMap.of(), 0, "", null, ImmutableMap.of());
    SecretSeriesAndContent newSecret = secretDAO.getSecretByIdOne(newId).get();

    assertThat(tableSize(SECRETS)).isEqualTo(secretsBefore + 1);
    assertThat(tableSize(SECRETS_CONTENT)).isEqualTo(secretContentsBefore + 1);

    newSecret = secretDAO.getSecretByNameOne(newSecret.series().name()).get();
    assertThat(secretDAO.getSecrets()).containsOnly(secret1, secret2a, secret2b, newSecret);
  }

  @Test(expected = DataAccessException.class)
  public void createSecretFailsIfSecretExists() {
    String name = "newSecret";
    secretDAO.createSecret(name, "some secret", "creator",
        ImmutableMap.of(), 0, "", null, ImmutableMap.of());

    secretDAO.createSecret(name, "some secret", "creator",
        ImmutableMap.of(), 0, "", null, ImmutableMap.of());
  }

  @Test(expected = DataAccessException.class)
  public void willNotCreateDuplicateSecret() throws Exception {
    ImmutableMap<String, String> emptyMap = ImmutableMap.of();
    secretDAO.createSecret("secret1", "encrypted1", "creator", emptyMap, 0, "", null, emptyMap);
    secretDAO.createSecret("secret1", "encrypted1", "creator", emptyMap, 0, "", null, emptyMap);
  }

  @Test public void createSecretSucceedsIfCurrentVersionIsNull() {
    String name = "newSecret";
    long firstId = secretDAO.createSecret(name, "some secret", "creator",
        ImmutableMap.of(), 0, "", null, ImmutableMap.of());

    jooqContext.update(SECRETS)
        .set(SECRETS.CURRENT, (Long)null)
        .where(SECRETS.ID.eq(firstId))
        .execute();


    long secondId = secretDAO.createSecret(name, "some secret", "creator",
        ImmutableMap.of(), 0, "", null, ImmutableMap.of());
    assertThat(secondId).isEqualTo(firstId);
  }

  @Test public void getSecretByName() {
    String name = secret1.series().name();
    assertThat(secretDAO.getSecretByNameOne(name)).contains(secret1);
  }

  @Test public void getSecretByIdOne() {
    assertThat(secretDAO.getSecretByIdOne(series2.id())).isEqualTo(Optional.of(secret2a));
  }

  @Test public void getSecretByIdOneReturnsEmptyWhenCurrentVersionIsNull() {
    jooqContext.update(SECRETS)
        .set(SECRETS.CURRENT, (Long)null)
        .where(SECRETS.ID.eq(series2.id()))
        .execute();
    assertThat(secretDAO.getSecretByIdOne(series2.id())).isEmpty();
  }

  @Test(expected = IllegalStateException.class)
  public void getSecretByIdOneThrowsExceptionIfCurrentVersionIsInvalid() {
    jooqContext.update(SECRETS)
        .set(SECRETS.CURRENT, -1234L)
        .where(SECRETS.ID.eq(series2.id()))
        .execute();
    secretDAO.getSecretByIdOne(series2.id());
  }

  @Test public void getNonExistentSecret() {
    assertThat(secretDAO.getSecretByNameOne("non-existent")).isEmpty();
    assertThat(secretDAO.getSecretByIdOne(-1231)).isEmpty();
  }

  @Test public void getSecrets() {
    assertThat(secretDAO.getSecrets()).containsOnly(secret1, secret2a, secret2b);
  }

  @Test public void deleteSecretsByName() {
    secretDAO.createSecret("toBeDeleted_deleteSecretsByName", "encryptedShhh", "creator",
        ImmutableMap.of(), 0, "", null, null);

    int secretsBefore = tableSize(SECRETS);
    int secretContentsBefore = tableSize(SECRETS_CONTENT);

    secretDAO.deleteSecretsByName("toBeDeleted_deleteSecretsByName");

    assertThat(tableSize(SECRETS)).isEqualTo(secretsBefore - 1);
    assertThat(tableSize(SECRETS_CONTENT)).isEqualTo(secretContentsBefore - 1);
    assertThat(secretDAO.getSecretByNameOne("toBeDeleted_deleteSecretsByName"))
        .isEmpty();
  }

  private int tableSize(Table table) {
    return jooqContext.fetchCount(table);
  }
}
