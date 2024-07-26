# Gropius Adapter for DinoDev

This repository contains the adapter that connects DinoDev to Gropius.

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=MEITREX_dinodev_gropius_adapter&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=MEITREX_dinodev_gropius_adapter)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=MEITREX_dinodev_gropius_adapter&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=MEITREX_dinodev_gropius_adapter)

##  GraphQL Code Generator

Like all other DinoDev repos, the adapter uses the 
[GraphQL Code Generator Plugin](https://github.com/kobylynskyi/graphql-java-codegen-gradle-plugin)
to generate Java classes from the GraphQL schema. In this case, the schema is the Gropius schema
and the generated classes are used to interact with the Gropius API.

The plugin is configured in the `build.gradle` file. The classes are generated on every build, so you don't need to run the generator manually. 

### Updating the Gropius Schema

To update the Gropius Schema, you can run the gradle task `refreshGropiusSchema`. The URL of the Gropius backend can be adjusted in the `build.gradle` file.

### Limitations of generated client classes

The advantage of using the generated classes is that you can use them to interact with the Gropius API in a type-safe way. However, the generated classes are not perfect and there are some limitations:
- To few fields are generated if the field has parameters. In this case, add the type name to `fieldsWithoutResolvers` in the `build.gradle` file.
- There are serialization issues with `OffsetDateTime` and `LocalDateTime`. The workaround is to use `String` fields instead. This can be achieved using a `customTypeMapping` in the `build.gradle` file.
- Serialization of interfaces is not supported. If interfaces are queried, manually map them to a concrete subclass in the `customTypeMapping`.`