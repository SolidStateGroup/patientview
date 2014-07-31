CREATE TABLE PV_User
(
  Id               BIGINT       NOT NULL,
  Username         VARCHAR(50)  NOT NULL UNIQUE,
  Password         VARCHAR(100) NOT NULL,
  Change_Password  BOOL         NOT NULL,
  Locked           BOOL         NOT NULL,
  Fhir_Resource_Id UUID UNIQUE,
  Email            VARCHAR(200) NOT NULL,
  Fullname             VARCHAR(200) NOT NULL,
  Verification_Code    VARCHAR(200),
  Verified         BOOL         NOT NULL DEFAULT FALSE,
  Contact_Number   VARCHAR(50),
  Last_Login       TIMESTAMP,
  Start_Date       DATE         NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP    NOT NULL,
  Created_By       BIGINT       NOT NULL REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_Lookup_Type (
  Id               BIGINT    NOT NULL,
  Lookup_Type      VARCHAR(50) UNIQUE,
  Description      TEXT,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Lookup_Value (
  Id               BIGINT       NOT NULL,
  Lookup_Type_Id   BIGINT       NOT NULL REFERENCES PV_Lookup_Type (Id),
  Value            VARCHAR(100) NOT NULL,
  Description      TEXT,
  Creation_Date    TIMESTAMP    NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Group
(
  Id               BIGINT    NOT NULL,
  Group_Name       VARCHAR(200) UNIQUE,
  Code             VARCHAR(50),
  Sftp_User        VARCHAR(255),
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Parent_Group_Id  BIGINT,
  Fhir_Resource_Id UUID,
  Visible          BOOLEAN,
  Visible_To_Join  BOOLEAN,
  Address_1        TEXT,
  Address_2        TEXT,
  Address_3        TEXT,
  Postcode         VARCHAR(255),
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Contact_Point
(
  Id               BIGINT    NOT NULL,
  User_Id          BIGINT    REFERENCES PV_User (Id),
  Group_Id         BIGINT    REFERENCES PV_Group (Id),
  Type_Id          BIGINT    REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Content          TEXT      NOT NULL,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Group_Relationship
(
  Id               BIGINT    NOT NULL,
  Source_Group_Id         BIGINT    NOT NULL,
  Object_Group_Id  BIGINT    REFERENCES PV_Group (Id) NOT NULL,
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Start_Date       DATE         NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_Role
(
  Id               BIGINT      NOT NULL,
  Role_Name        VARCHAR(50) NOT NULL UNIQUE,
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Level            INTEGER   NOT NULL,
  Visible          BOOLEAN,
  Description      VARCHAR(255),
  Creation_Date    TIMESTAMP   NOT NULL,
  Created_By       BIGINT      NOT NULL REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_User_Group_Role
(
  Id               BIGINT    NOT NULL,
  User_Id          BIGINT    NOT NULL REFERENCES PV_User (Id),
  Group_Id         BIGINT REFERENCES PV_Group (Id),
  Role_Id          BIGINT    NOT NULL REFERENCES PV_Role (Id),
  Start_Date       DATE,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Feature
(
  Id                BIGINT    NOT NULL,
  Feature_Name      VARCHAR(50) NOT NULL UNIQUE,
  Description       VARCHAR(100),
  Parent_Feature_Id BIGINT,
  Start_Date        DATE,
  End_Date          DATE,
  Creation_Date     TIMESTAMP NOT NULL,
  Created_By        BIGINT REFERENCES PV_User (Id),
  Last_Update_Date  TIMESTAMP,
  Last_Updated_By   BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Feature_Feature_Type
(
  Id                BIGINT NOT NULL,
  Feature_Id        BIGINT REFERENCES PV_Feature (Id) NOT NULL,
  Type_Id           BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Feature_User
(
  Id               BIGINT    NOT NULL,
  User_Id          BIGINT    NOT NULL REFERENCES PV_User (Id),
  Feature_Id       BIGINT    NOT NULL REFERENCES PV_Feature (Id),
  Start_Date       DATE,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Feature_Group
(
  Id               BIGINT    NOT NULL,
  Group_Id         BIGINT    NOT NULL REFERENCES PV_Group (Id),
  Feature_Id       BIGINT    NOT NULL REFERENCES PV_Feature (Id),
  Start_Date       DATE      NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_User_Token
(
  Id              BIGINT      NOT NULL,
  User_Id         BIGINT      NOT NULL REFERENCES PV_User (Id),
  Token           VARCHAR(50) NOT NULL UNIQUE,
  Parent_Token_Id BIGINT      REFERENCES PV_User_Token (Id),
  Creation_Date   TIMESTAMP   NOT NULL,
  Expiration_Date TIMESTAMP,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_News_Item (
  Id               BIGINT    NOT NULL,
  Heading          VARCHAR(100),
  Story            TEXT      NOT NULL,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_News_Link (
  Id            BIGINT    NOT NULL,
  News_Id       BIGINT    NOT NULL REFERENCES PV_News_Item (Id),
  Group_Id      BIGINT REFERENCES PV_Group (Id),
  Role_Id       BIGINT REFERENCES PV_Role (Id),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By    BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_Conversation (
  Id               BIGINT       NOT NULL,
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id),
  Image_Data       TEXT,
  Rating           INTEGER,
  Status           INTEGER,
  Open             BOOL         NOT NULL,
  Title            VARCHAR(200) NOT NULL,
  Creation_Date    TIMESTAMP    NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (id)
);

CREATE TABLE PV_Message (
  Id              BIGINT    NOT NULL,
  Conversation_Id BIGINT    NOT NULL  REFERENCES PV_Conversation (Id),
  Type_Id         BIGINT REFERENCES PV_Lookup_Value (Id),
  Message         TEXT      NOT NULL,
  Creation_Date   TIMESTAMP NOT NULL,
  Created_By      BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Conversation_Participant (
  Id              BIGINT    NOT NULL,
  Conversation_Id BIGINT    NOT NULL REFERENCES PV_Conversation (Id),
  User_Id         BIGINT    NOT NULL REFERENCES PV_User (Id),
  Anonymous       BOOL      NOT NULL,
  Creation_Date   TIMESTAMP NOT NULL,
  Created_By      BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Message_Read_Receipt (
  Id            BIGINT    NOT NULL,
  Message_Id    BIGINT    NOT NULL  REFERENCES PV_Message (Id),
  User_Id       BIGINT    NOT NULL REFERENCES PV_User (Id),
  Creation_Date TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_User_Information (
  Id            BIGINT    NOT NULL,
  User_Id       BIGINT    NOT NULL REFERENCES PV_User (Id),
  Type_Id       BIGINT    NOT NULL REFERENCES PV_Lookup_Value (Id),
  Value         TEXT      NOT NULL,
  Creation_Date TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Code (
  Id               BIGINT    NOT NULL,
  Code             VARCHAR(100),
  Type_Id          BIGINT    NOT NULL REFERENCES PV_Lookup_Value (Id),
  Display_Order    INTEGER   ,
  Description      VARCHAR(100),
  Standard_Type_Id BIGINT    NOT NULL REFERENCES PV_Lookup_Value (Id),
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Join_Request (
  Id            BIGINT       NOT NULL,
  Forename      VARCHAR(200) NOT NULL,
  Surname       VARCHAR(200) NOT NULL,
  DOB           DATE         NOT NULL,
  Nhs_Number    VARCHAR(10),
  Join_Group_Id BIGINT       NOT NULL  REFERENCES PV_Group (Id),
  Creation_Date TIMESTAMP    NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Location (
  Id            BIGINT    NOT NULL,
  Group_Id      BIGINT    NOT NULL  REFERENCES PV_Group (Id),
  Label         TEXT      NOT NULL,
  Name          TEXT      NOT NULL,
  Phone         TEXT,
  Address       TEXT,
  Web           TEXT,
  Email         TEXT,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Log (
  Id            BIGINT    NOT NULL,
  User_Id       BIGINT REFERENCES PV_User (Id),
  Source        VARCHAR(50),
  Message       VARCHAR(500),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By    BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Audit (
  Id            BIGINT    NOT NULL,
  Action        VARCHAR(200),
  Source_Object_Type    VARCHAR(50),
  Source_Object_Id      BIGINT,
  Pre_Value     VARCHAR(500),
  Post_Value    VARCHAR(500),
  Action_Date   TIMESTAMP NOT NULL,
  Actor_Id      BIGINT    NOT NULL REFERENCES PV_User (Id),
  Creation_Date TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Observation_Heading (
  Id               BIGINT    NOT NULL,
  Type_Id          BIGINT    NOT NULL  REFERENCES PV_Lookup_Value (Id),
  Colour           VARCHAR(7),
  Link             VARCHAR(2048),
  Name             VARCHAR(200),
  Description      VARCHAR(200),
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Module (
  Id               BIGINT    NOT NULL,
  Name             VARCHAR(200),
  Description      VARCHAR(200),
  Route            VARCHAR(2048),
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_Module_Group (
  Id               BIGINT    NOT NULL,
  Module_Id        BIGINT REFERENCES PV_Module (Id),
  Group_Id         BIGINT REFERENCES PV_Group (Id),
  Start_Date       DATE      NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Module_Role (
  Id               BIGINT    NOT NULL,
  Module_Id        BIGINT REFERENCES PV_Module (Id),
  Role_Id          BIGINT    NOT NULL REFERENCES PV_Role (Id),
  Start_Date       DATE      NOT NULL,
  End_Date         DATE,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Link (
  Id               BIGINT    NOT NULL,
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id),
  Code_Id          BIGINT REFERENCES PV_Code (Id),
  Group_Id         BIGINT REFERENCES PV_Group (Id),
  Link             VARCHAR(2048),
  Name             VARCHAR(200),
  Display_Order    INTEGER       NOT NULL,
  Creation_Date    TIMESTAMP NOT NULL,
  Created_By       BIGINT REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Shared_Thought (
  Id                   BIGINT    NOT NULL,
  Conversation_Id      BIGINT    REFERENCES PV_Conversation (Id),
  Positive             BOOL      NOT NULL,
  Anonymous            BOOL      NOT NULL,
  Submitted            BOOL      NOT NULL,
  Patient              BOOL      NOT NULL,
  Principal_Carer      BOOL      NOT NULL,
  Relative             BOOL      NOT NULL,
  Friend               BOOL      NOT NULL,
  Other                BOOL      NOT NULL,
  Other_Specify        VARCHAR(255),
  About_Me             BOOL      NOT NULL,
  About_Other          BOOL      NOT NULL,
  Ongoing              BOOL      NOT NULL,
  Location             VARCHAR(2048),
  When_Occurred        VARCHAR(2048),
  Description          VARCHAR(2048),
  Suggested_Action     VARCHAR(2048),
  Concern_Reason       VARCHAR(2048),
  Recurrence           INTEGER,
  Recurrence_Specify   VARCHAR(2048),
  Serious              INTEGER,
  Submit_Date          TIMESTAMP,
  Creation_Date        TIMESTAMP NOT NULL,
  Created_By           BIGINT    REFERENCES PV_User (Id),
  Last_Update_Date     TIMESTAMP,
  Last_Updated_By      BIGINT    REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Shared_Thought_Audit (
  Id                   BIGINT       NOT NULL,
  Shared_Thought_Id    BIGINT       NOT NULL REFERENCES PV_Shared_Thought (Id),
  User_Id              BIGINT       NOT NULL REFERENCES PV_User (Id),
  Group_Id             BIGINT       NOT NULL REFERENCES PV_Group (Id),
  Message_Id           BIGINT       REFERENCES PV_Message (Id),
  Responder_Id         BIGINT       REFERENCES PV_User (Id),
  Action               VARCHAR(255) NOT NULL,
  Creation_Date        TIMESTAMP    NOT NULL,
  Created_By           BIGINT       REFERENCES PV_User (Id),
  Last_Update_Date     TIMESTAMP,
  Last_Updated_By      BIGINT       REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE TABLE PV_Route (
  Id                   BIGINT        NOT NULL,
  Type_Id              BIGINT        NOT NULL  REFERENCES PV_Lookup_Value (Id),
  Display_Order        INTEGER       NOT NULL,
  Url                  VARCHAR(2048) NOT NULL,
  Controller           VARCHAR(255)  NOT NULL,
  Template_Url         VARCHAR(2048) NOT NULL,
  Title                VARCHAR(255)  NOT NULL,
  Creation_Date        TIMESTAMP     NOT NULL,
  Created_By           BIGINT        REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_Route_Link (
  Id                   BIGINT        NOT NULL,
  Route_Id             BIGINT        NOT NULL  REFERENCES PV_Route (Id),
  Group_Id             BIGINT        REFERENCES PV_Group (Id),
  Role_Id              BIGINT        REFERENCES PV_Role (Id),
  Feature_Id           BIGINT        REFERENCES PV_Feature (Id),
  Creation_Date        TIMESTAMP     NOT NULL,
  Created_By           BIGINT        REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);


CREATE TABLE PV_Identifier
(
  Id               BIGINT      NOT NULL,
  User_Id          BIGINT REFERENCES PV_User (Id) NOT NULL,
  Type_Id          BIGINT REFERENCES PV_Lookup_Value (Id) NOT NULL,
  Identifier       VARCHAR(200)   NOT NULL,
  Start_Date       DATE,
  End_Date         DATE,
  Creation_Date    TIMESTAMP   NOT NULL,
  Created_By       BIGINT      NOT NULL REFERENCES PV_User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By  BIGINT REFERENCES PV_User (Id),
  PRIMARY KEY (Id)
);

CREATE SEQUENCE hibernate_sequence
INCREMENT 1
MINVALUE 1
MAXVALUE 9223372036854775807
START 1
CACHE 1;
ALTER TABLE hibernate_sequence
OWNER TO patientview;
