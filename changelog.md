# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## [3.1.0 - 2019-07-02]
### Changed
- Cleaner code
- Removed unused model classes (Note and UserConsent)

## [3.0.0 - 2019-07-01]
### Changed
- Refactored Flyway scripts
- Moved SimpleQueryDto2ShareXmlTransformer from UI to REST
- Split table Inquiry into Inquiry and InquiryDetails

## [2.2.0 - 2019-06-06]
### Changed
- Update version of samply.common.config 3.0.3 -> 3.1.0 
- Update version of samply.share.common 3.1.3 -> 3.2.0 
- Update version of samply.common.mailing 2.1.3 -> 2.2.0 

## [2.1.0 - 2018-05-07]
### Changed
- Update share.common: 3.0.0 -> 3.1.0 (including new samply.mdrfaces without jquery)
- Update other samply dependencies
- Partially Update Jersey 1.x -> 2.26 (due to problems with multipart, asm and Java 8)
- Use webjar for fileinput.js
- Reupdate JQuery 1.11.1 -> 3.3.1-1
- Webjar bootstrap-datetimepicker: 4.17.43 -> 4.17.47
- Webjar select2: 4.0.3 -> 4.0.5
- Update other general and webjar dependencies

## [2.0.0 - 2018-03-19]
### Added
- Parent POM 10.1 (Java 8 )
- Introduce profiles for setting type of project (osse, dktk, gbn)
- Update some library versions (e.g. JQuery 1.11.1 => 3.3.1-1)

## [1.3.2 - 201y-mm-dd]
### Added
- Accept "samply-xml-namespace" header with desired namespace, e.g. "common"
### Changed
- Use xml namespace "common" instead of "osse" or "ccp" for queries
### Deprecated
### Removed
- Deleted unused table user_bank from database
- Observer endpoint
### Fixed
### Security


## [1.3.1 - 2017-12-06]
### Changed
- Upgrade samply.share.common to 1.2.2-SNAPSHOT

### Removed
- Don't convert date values when receiving queries from central mds database

### Fixed
- Erroneous access right warnings removed
- Reword some misleading labels

## [1.3.0 - 2017-11-08]
### Added
- Flyway for DB migrations
- Jooq Codegeneration via maven plugin
- Allow to specify desired viewfields for an inquiry

### Changed
- Switch Java language level to 1.8 (was 1.7)
- Adapt to updated samply auth version (roles changed)

## [1.2.6 - 2017-08-24]
### Added
- Report version information to icinga
- Allow to send telemetric data to icinga
- Provide a reference query for monitoring purposes
- README.md
- CHANGELOG.md

### Changed
- When a user logs in with a different samply auth id, but the same email address, change the user record to the new auth id
- Use library for Bootstrap Style Messages