-- This CLP file was created using DB2LOOK Version 9.5
-- Timestamp: 4/1/2009 10:44:11 PM
-- Database Name: SAMPLE         
-- Database Manager Version: DB2/NT Version 9.5.0          
-- Database Codepage: 1208
-- Database Collating Sequence is: IDENTITY


CONNECT TO SAMPLE;

------------------------------------------------
-- DDL Statements for table "DB2ADMIN"."STRINGLONGPROPERTY"
------------------------------------------------
 

CREATE TABLE "DB2ADMIN"."STRINGLONGPROPERTY"  (
		  "KEY" VARCHAR(64) NOT NULL , 
		  "VALUE" BIGINT NOT NULL )   
		 IN "USERSPACE1" ; 


-- DDL Statements for primary key on Table "DB2ADMIN"."STRINGLONGPROPERTY"

ALTER TABLE "DB2ADMIN"."STRINGLONGPROPERTY" 
	ADD CONSTRAINT "CC1237843256813" PRIMARY KEY
		("KEY");



------------------------------------------------
-- DDL Statements for table "DB2ADMIN"."STRINGSTRINGPROPERTY"
------------------------------------------------
 

CREATE TABLE "DB2ADMIN"."STRINGSTRINGPROPERTY"  (
		  "KEY" VARCHAR(64) NOT NULL , 
		  "VALUE" VARCHAR(192) NOT NULL )   
		 IN "USERSPACE1" ; 


-- DDL Statements for primary key on Table "DB2ADMIN"."STRINGSTRINGPROPERTY"

ALTER TABLE "DB2ADMIN"."STRINGSTRINGPROPERTY" 
	ADD CONSTRAINT "CC1237906601194" PRIMARY KEY
		("KEY");



------------------------------------------------
-- DDL Statements for table "DB2ADMIN"."LONGLONGPROPERTY"
------------------------------------------------
 

CREATE TABLE "DB2ADMIN"."LONGLONGPROPERTY"  (
		  "KEY" BIGINT NOT NULL , 
		  "VALUE" BIGINT NOT NULL )   
		 IN "USERSPACE1" ; 


-- DDL Statements for primary key on Table "DB2ADMIN"."LONGLONGPROPERTY"

ALTER TABLE "DB2ADMIN"."LONGLONGPROPERTY" 
	ADD CONSTRAINT "CC1237906655335" PRIMARY KEY
		("KEY");



------------------------------------------------
-- DDL Statements for table "DB2ADMIN"."LONGSTRINGPROPERTY"
------------------------------------------------
 

CREATE TABLE "DB2ADMIN"."LONGSTRINGPROPERTY"  (
		  "KEY" BIGINT NOT NULL , 
		  "VALUE" VARCHAR(192) NOT NULL )   
		 IN "USERSPACE1" ; 


-- DDL Statements for primary key on Table "DB2ADMIN"."LONGSTRINGPROPERTY"

ALTER TABLE "DB2ADMIN"."LONGSTRINGPROPERTY" 
	ADD CONSTRAINT "CC1237906703382" PRIMARY KEY
		("KEY");



------------------------------------------------
-- DDL Statements for table "DB2ADMIN"."LISTHEADSTRINGLONG"
------------------------------------------------
 

CREATE TABLE "DB2ADMIN"."LISTHEADSTRINGLONG"  (
		  "KEY" VARCHAR(64) NOT NULL , 
		  "LISTID" BIGINT NOT NULL , 
		  "MIN" BIGINT NOT NULL , 
		  "MAX" BIGINT NOT NULL )   
		 IN "USERSPACE1" ; 


-- DDL Statements for primary key on Table "DB2ADMIN"."LISTHEADSTRINGLONG"

ALTER TABLE "DB2ADMIN"."LISTHEADSTRINGLONG" 
	ADD CONSTRAINT "CC1237993146483" PRIMARY KEY
		("KEY");



------------------------------------------------
-- DDL Statements for table "DB2ADMIN"."LISTITEMSTRINGLONG"
------------------------------------------------
 

CREATE TABLE "DB2ADMIN"."LISTITEMSTRINGLONG"  (
		  "KEY" VARCHAR(64) NOT NULL , 
		  "POS" BIGINT NOT NULL , 
		  "VALUE" BIGINT NOT NULL )   
		 IN "USERSPACE1" ; 


-- DDL Statements for primary key on Table "DB2ADMIN"."LISTITEMSTRINGLONG"

ALTER TABLE "DB2ADMIN"."LISTITEMSTRINGLONG" 
	ADD CONSTRAINT "CC1237993237171" PRIMARY KEY
		("KEY",
		 "POS");



------------------------------------------------
-- DDL Statements for table "DB2ADMIN"."SETITEMSTRINGLONG"
------------------------------------------------
 

CREATE TABLE "DB2ADMIN"."SETITEMSTRINGLONG"  (
		  "KEY" VARCHAR(64) NOT NULL , 
		  "POS" BIGINT NOT NULL , 
		  "VALUE" BIGINT NOT NULL )   
		 IN "USERSPACE1" ; 


-- DDL Statements for primary key on Table "DB2ADMIN"."SETITEMSTRINGLONG"

ALTER TABLE "DB2ADMIN"."SETITEMSTRINGLONG" 
	ADD CONSTRAINT "CC1238622121395" PRIMARY KEY
		("KEY",
		 "POS");



------------------------------------------------
-- DDL Statements for table "DB2ADMIN"."SETHEADSTRINGLONG"
------------------------------------------------
 

CREATE TABLE "DB2ADMIN"."SETHEADSTRINGLONG"  (
		  "KEY" VARCHAR(64) NOT NULL , 
		  "POS" BIGINT NOT NULL )   
		 IN "USERSPACE1" ; 


-- DDL Statements for primary key on Table "DB2ADMIN"."SETHEADSTRINGLONG"

ALTER TABLE "DB2ADMIN"."SETHEADSTRINGLONG" 
	ADD CONSTRAINT "CC1238622181348" PRIMARY KEY
		("KEY");









COMMIT WORK;

CONNECT RESET;

TERMINATE;

-- Generate statistics for all creators 
-- The db2look utility will consider only the specified tables 
-- Creating DDL for table(s)
-- Binding package automatically ... 
-- Bind is successful
-- Binding package automatically ... 
-- Bind is successful
;