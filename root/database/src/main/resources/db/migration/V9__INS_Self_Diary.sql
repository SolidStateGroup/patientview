INSERT INTO pv_feature (id, feature_name, description, start_date, creation_date, created_by)
VALUES (16, 'INS_DIARY', 'INS Diary Recording', now(), now(), 1);

INSERT INTO pv_feature_feature_type (id, feature_id, type_id) values (22, 16, 14);

CREATE TABLE pv_relapse
(
  id                          BIGINT NOT NULL,
  user_id                     BIGINT NOT NULL REFERENCES pv_user (id),
  relapse_date                DATE,
  remission_date              DATE,
  viral_infection             VARCHAR(200),
  common_cold                 BOOLEAN      NOT NULL,
  hay_fever                   BOOLEAN      NOT NULL,
  allergic_reaction           BOOLEAN      NOT NULL,
  allergic_skin_rash          BOOLEAN      NOT NULL,
  food_intolerance            BOOLEAN      NOT NULL,
  created_by                  BIGINT       REFERENCES pv_user (id) NOT NULL,
  creation_date               TIMESTAMP    NOT NULL,
  last_update_date            TIMESTAMP,
  last_updated_by             BIGINT REFERENCES pv_user (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE pv_ins_diary
(
  id                          BIGINT NOT NULL,
  user_id                     BIGINT NOT NULL REFERENCES pv_user (id),
  entry_date                  TIMESTAMP,
  is_relapse                  BOOLEAN      NOT NULL,
  relapse_id                  BIGINT    REFERENCES pv_relapse (id),
  urine_protein_dipstick_type VARCHAR(50),
  systolic_bp                 INTEGER,
  systolic_bp_exclude         BOOLEAN,
  systolic_bp_resource_id     UUID,
  diastolic_bp                INTEGER,
  diastolic_bp_exclude        BOOLEAN,
  diastolic_bp_resource_id    UUID,
  weight                      NUMERIC(19, 2),
  weight_exclude              BOOLEAN,
  weight_resource_id          UUID,
  oedema                      JSONB,
  created_by                  BIGINT       REFERENCES pv_user (id) NOT NULL,
  creation_date               TIMESTAMP    NOT NULL,
  last_update_date            TIMESTAMP,
  last_updated_by             BIGINT REFERENCES pv_user (Id),
  PRIMARY KEY (Id)
);



CREATE TABLE pv_relapse_medication
(
  id                          BIGINT NOT NULL,
  name                        VARCHAR(100),
  other                       VARCHAR(200),
  dose_quantity               INTEGER,
  dose_units                  VARCHAR(10),
  dose_frequency              VARCHAR(20),
  route                       VARCHAR(20),
  started                     DATE,
  stopped                     DATE,
  relapse_id                  BIGINT NOT NULL REFERENCES pv_relapse (id),
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
