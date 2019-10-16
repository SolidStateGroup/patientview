INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by)
VALUES (16, 'INS_DIARY', 'INS Diary Recording', now(), now(), 1);

INSERT INTO pv_feature_feature_type (id, feature_id, type_id) values (22, 16, 14);

CREATE TABLE pv_ins_diary
(
  id              BIGINT NOT NULL,
  user_id         BIGINT NOT NULL REFERENCES pv_user (id),
  created_by                  BIGINT       REFERENCES pv_user (id) NOT NULL,
  creation_date               TIMESTAMP    NOT NULL,
  last_update_date            TIMESTAMP,
  last_updated_by             BIGINT REFERENCES pv_user (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE pv_immunisation
(
  id                          BIGINT NOT NULL,
  user_id                     BIGINT NOT NULL REFERENCES pv_user (id),
  codelist                    Character Varying( 50 ) NOT NULL,
  other                       Text,
  immunisation_date           TIMESTAMP,
  created_by                  BIGINT       REFERENCES pv_user (id) NOT NULL,
  creation_date               TIMESTAMP    NOT NULL,
  last_update_date            TIMESTAMP,
  last_updated_by             BIGINT REFERENCES pv_user (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE pv_hospitalisation
(
  id                          BIGINT NOT NULL,
  user_id                     BIGINT NOT NULL REFERENCES pv_user (id),
  reason                      Text,
  date_admitted               TIMESTAMP NOT NULL,
  date_discharged             TIMESTAMP,
  created_by                  BIGINT       REFERENCES pv_user (id) NOT NULL,
  creation_date               TIMESTAMP    NOT NULL,
  last_update_date            TIMESTAMP,
  last_updated_by             BIGINT REFERENCES pv_user (Id),
  PRIMARY KEY (Id)
);
