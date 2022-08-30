
Considerations:

[DDD Part 1: Strategic Domain-Driven Design](https://vaadin.com/blog/ddd-part-1-strategic-domain-driven-design)
[The perils of shared code](https://www.innoq.com/en/blog/the-perils-of-shared-code/)

## Build native image

```shell
sbt catalogue/nativeImageRunAgent

sbt catalogue/nativeImage

sbt catalogue/nativeImageRun
```

```shell
apps/lending/target/native-image/lending -b da3e0fcd-0494-4a58-8247-d6dd79dca54d
```