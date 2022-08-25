package es.eriktorr.library
package shared.infrastructure

trait KafkaConfigConfigDecoder
    extends BootstrapServerConfigDecoder
    with ConsumerGroupConfigDecoder
    with NonEmptyListConfigDecoder
    with SchemaRegistryConfigDecoder
    with TopicConfigDecoder
