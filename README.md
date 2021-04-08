# Asciidoctor Syntaxes

Parsers and Renderers for Asciidoctor syntaxes.

* Project Lead: [Cédric Champeau](https://www.xwiki.org/xwiki/bin/view/XWiki/melix)
* Documentation & Downloads
* Issue Tracker
* Communication: [Mailing List](http://dev.xwiki.org/xwiki/bin/view/Community/MailingLists), [IRC](http://dev.xwiki.org/xwiki/bin/view/Community/IRC)
* [Development Practices](http://dev.xwiki.org)
* Minimal XWiki version supported: XWiki 11.10.5
* License: LGPL 2.1
* Translations: N/A
* Sonar Dashboard: N/A
* Continuous Integration
  Status: [![Build Status](http://ci.xwiki.org/job/XWiki%20Contrib/job/syntax-asciidoctor/job/master/badge/icon)](http://ci.xwiki.org/job/XWiki%20Contrib/job/syntax-asciidoctor/job/master/)

## Building

This project uses an experimental Gradle build, in addition to the Maven build.

|What do you want to do?                                 |    Execute        | Comment |
|--------------------------------------------------------|-------------------|---------|
|Run unit tests                                          | `./gradlew test`  | |
|Run all verification                                    | `./gradlew check` | |
|Build the jar                                           | `./gradlew jar`   | |
|Check what is going to be published on Maven repository | `./gradlew publishMavenPublicationToTestRepository` | Then check the result in `build/repo` |

