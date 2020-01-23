# Changelog

## 2.1.0 - 2020-01-19
### Added
- VerificationStatus added to Diagnosis to allow "negative" Diagnoses for UKRR purposes
- EnteredAt/EncounterNumber added to Diagnosis - These are to support recording information via PatientView
- EncounterNumber added to Diagnoss - This is to support recording information via PatientView
### Changed
- ProgramName/ProgramDescription have been limited to 100 characters in ProgramMembership

## 2.0.0 -2019-09-05
### Changed
- COD CodingStandard has been changed from EDTA to EDTA_COD

## 1.4.0 - 2019-07-12
### Added
- 3847 / 3852 have been added to the EDTA PRD Code List
- 34 has been added to the EDTA COD List. This had been mistakenly omitted.

## 1.3.0 - 2019-07-09
### Changed
- STUDYNO has been added as a possible number source

## 1.2.0 - 2019-06-20
### Changed
- Made TRA fields in Transplant Procedure non-Mandatory
- Made ACC fields in Vascular Access Procedure non-Mandatory
- Made QHD fields in DialysisSession Procedure non-Mandatory
- Reduced Max string lengths in Name to match database
- Reduced COD/Diagnosis/RenalDiagnosis Description Max String lengths to match database
- Restricted Medication.Frequency Max string length to match database

## 1.1.0 - 2019-06-19
### Added
- Add version attribute to schema element
- Add schemaVersion attribute to SendingFacility element

### Changed
- Removed Trailing Spaces / Redundant Comments

## 1.0.0 - 2019-06-19
### Added
- Initial Version

