CREATE TABLE Group
(
	Id BIGINT NOT NULL,
	Name VARCHAR(200) UNIQUE,
	Code VARCHAR(50),
	Description VARCHAR(255),
	Type_Id BIGINT REFERENCES Lookup_Value (Id),
	Parent_Group_Id BIGINT,
  Fhir_Resource_Id UUID,
	Creation_Date DATETIME NOT NULL,
	Created_By BIGINT REFERENCES User (Id),
	Last_Update_Date DATETIME NOT NULL,
	Last_Updated_By BIGINT REFERENCES User (Id),
	PRIMARY KEY (Id)
)
;


CREATE TABLE Role
(
	Id BIGINT NOT NULL,
	Name VARCHAR(50) NOT NULL UNIQUE,
  Description VARCHAR(255),
	Creation_Date DATETIME NOT NULL,
	Created_By BIGINT NOT NULL REFERENCES User (Id),
	Last_Update_Date DATETIME,
	Last_Updated_By BIGINT REFERENCES User (Id),
	PRIMARY KEY (Id)
)
;

CREATE TABLE User
(
	Id BIGINT NOT NULL,
	Username VARCHAR(50) NOT NULL UNIQUE,
	Password VARCHAR(100) NOT NULL,
	Change_Password BOOL NOT NULL,
	Locked BOOL NOT NULL,
  Fhir_Resource_Id UUID UNIQUE,
  Email VARCHAR(200) NOT NULL,
  Name VARCHAR (200) NOT NULL,
	Start_Date DATE NOT NULL,
	End_Date DATE,
	Creation_Date TIMESTAMP NOT NULL,
	Created_By BIGINT NOT NULL REFERENCES User (Id),
	Last_Update_Date TIMESTAMP,
	Last_Updated_By BIGINT REFERENCES User (Id),
	PRIMARY KEY (Id)
)
;


CREATE TABLE User_Group
(
	Id BIGINT NOT NULL,
	User_Id BIGINT NOT NULL REFERENCES User (Id),
	Group_Id INTEGER REFERENCES Group (Id),
	Role_Id BIGINT NOT NULL REFERENCES Role (Id),
	Start_Date DATE,
	End_Date DATE,
	Creation_Date TIMESTAMP NOT NULL,
	Created_By BIGINT REFERENCES User (Id),
	Last_Update_Date TIMESTAMP,
	Last_Updated_By BIGINT REFERENCES User (Id),
	PRIMARY KEY (Id)
)
;

CREATE TABLE Feature
(
  Id BIGINT NOT NULL,
  Name BIGINT NOT NULL UNIQUE,
  Description VARCHAR(100),
  Parent_Feature_Id BIGINT,
  Start_Date DATE,
  End_Date DATE,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Feature_User
(
  Id BIGINT NOT NULL,
  User_Id BIGINT NOT NULL REFERENCES User (Id),
  Feature_Id BIGINT NOT NULL REFERENCES Feature (Id),
  Start_Date DATE,
  End_Date DATE,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Feature_Group
(
  Id BIGINT NOT NULL,
  Group_Id BIGINT NOT NULL REFERENCES Group (Id),
  Feature_Id BIGINT NOT NULL REFERENCES Feature (Id),
  Start_Date DATE NOT NULL,
  End_Date DATE,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;


CREATE TABLE User_Token
(
  Id BIGINT NOT NULL,
  User_Id BIGINT NOT NULL REFERENCES User (Id),
  Token VARCHAR(50) NOT NULL UNIQUE,
  Parent_Token VARCHAR(50) NOT NULL,
  Creation_Date TIMESTAMP NOT NULL,
  Expiration_Date TIMESTAMP,
  PRIMARY KEY (Id)
)
;

CREATE TABLE News (
  Id BIGINT NOT NULL,
  News_Heading VARCHAR(100),
  News TEXT NOT NULL,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE News_Group_Role (
  Id BIGINT NOT NULL,
  News_Id BIGINT NOT NULL REFERENCES News (Id),
  Group_Id BIGINT REFERENCES Group (Id),
  Role_Id BIGINT REFERENCES Role (Id),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Message (
  Id BIGINT NOT NUll,
  Conversation_Id BIGINT NOT NULL  REFERENCES Conversation (Id),
  Type_Id BIGINT REFERENCES Lookup_Value (Id),
  Message TEXT NOT NULL,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Message_Read_Receipt (
  Id BIGINT NOT NUll,
  Message_Id BIGINT NOT NULL  REFERENCES Message (Id),
  User_Id BIGINT NOT NULL REFERENCES User (Id),
  Creation_Date TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
)
;

CREATE TABLE Conversation (
  Id BIGINT NOT NULL,
  Title VARCHAR(200) NOT NULL,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (id)
)
;

CREATE TABLE Conversation_Participant (
  Id BIGINT NOT NULL,
  Conversation_Id BIGINT NOT NULL REFERENCES Conversation (Id),
  User_Id BIGINT NOT NULL REFERENCES User (Id),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Lookup_Type (
  Id BIGINT NOT NULL,
  Lookup_Type VARCHAR(50) UNIQUE,
  Description TEXT,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Lookup_Value (
  Id BIGINT NOT NULL,
  Lookup_Type_Id BIGINT NOT NULL REFERENCES Lookup_Type (Id),
  Value VARCHAR(100) NOT NULL,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE User_Information (
  Id BIGINT NOT NULL,
  User_Id BIGINT NOT NULL REFERENCES User (Id),
  Type_Id BIGINT NOT NULL REFERENCES Lookup_Value (Id),
  Value TEXT NOT NULL,
  Creation_Date TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
)
;

CREATE TABLE Medical_Code (
  Id BIGINT NOT NULL,
  Code VARCHAR(20),
  Type_Id BIGINT NOT NULL REFERENCES Lookup_Value (Id),
  Order INTEGER NOT NULL,
  Fhir_Resource_Id BIGINT,
  Description VARCHAR(100),
  External_Mapping VARCHAR(100),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Link (
  Id BIGINT NOT NULL,
  Type_Id BIGINT NOT NULL  REFERENCES Lookup_Value (Id),
  Code_Id BIGINT REFERENCES Code (Id),
  Group_Id BIGINT  REFERENCES Group (Id),
  Link VARCHAR(2048),
  Name VARCHAR(200),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Join_Request (
  Id BIGINT NOT NULL,
  Forename VARCHAR(200) NOT NULL,
  Surname VARCHAR(200) NOT NULL,
  DOB DATE NOT NULL,
  Nhs_Number VARCHAR(10),
  Join_Group_Id BIGINT NOT NULL  REFERENCES Group (Id),
  Creation_Date TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
)
;

CREATE TABLE Log (
  Id BIGINT NOT NULL,
  User_Id BIGINT  REFERENCES User (Id),
  Source VARCHAR(50),
  Message VARCHAR(500),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Audit (
  Id BIGINT NOT NULL,
  Action VARCHAR(200),
  Source VARCHAR(50),
  Object_id BIGINT,
  Pre_Value VARCHAR(500),
  Post_Value VARCHAR(500),
  Action_Date TIMESTAMP NOT NULL,
  Actor_Id BIGINT NOT NULL REFERENCES User (Id),
  Creation_Date TIMESTAMP NOT NULL,
  PRIMARY KEY (Id)
)
;

CREATE TABLE Observation_Heading (
  Id BIGINT NOT NULL,
  Type_Id BIGINT NOT NULL  REFERENCES Lookup_Value (Id),
  Colour VARCHAR(7),
  Link VARCHAR(2048),
  Name VARCHAR(200),
  Description VARCHAR(200),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Module (
  Id BIGINT NOT NULL,
  Name VARCHAR(200),
  Description VARCHAR(200),
  Route VARCHAR(2048),
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;


CREATE TABLE Module_Group (
  Id BIGINT NOT NULL,
  Module_Id BIGINT REFERENCES Module (Id),
  Group_Id BIGINT REFERENCES Group (Id),
  Start_Date DATE NOT NULL,
  End_Date DATE,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;

CREATE TABLE Module_Role (
  Id BIGINT NOT NULL,
  Module_Id BIGINT REFERENCES Module (Id),
  Role_Id BIGINT NOT NULL REFERENCES Role (Id),
  Start_Date DATE NOT NULL,
  End_Date DATE,
  Creation_Date TIMESTAMP NOT NULL,
  Created_By BIGINT REFERENCES User (Id),
  Last_Update_Date TIMESTAMP,
  Last_Updated_By BIGINT REFERENCES User (Id),
  PRIMARY KEY (Id)
)
;






