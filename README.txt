Library Management System (Phase 1) - README

How to build & test:
  mvn test

Jacoco report generated at:
  target/site/jacoco/index.html

Generate Javadoc:
  mvn javadoc:javadoc

Notes:
 - Tests are deterministic and mock time and email where needed.
 - All storage is in-memory.
 - Main class is minimal and demonstrates a tiny scenario.
