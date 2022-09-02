package es.eriktorr.library
package shared.infrastructure

import shared.infrastructure.KafkaConfig.{BootstrapServer, ConsumerGroup, SchemaRegistry, Topic}

import cats.data.NonEmptyList

final case class KafkaConfig(
    bootstrapServers: NonEmptyList[BootstrapServer],
    consumerGroup: ConsumerGroup,
    schemaRegistry: SchemaRegistry,
    topic: Topic,
):
  def bootstrapServersAsString: String = bootstrapServers.toList.mkString(",")

  def asString: String =
    s"""bootstrap-servers=$bootstrapServersAsString, 
       |consumer-group=$consumerGroup, 
       |schema-registry=$schemaRegistry,
       |topic=$topic""".stripMargin.replaceAll("\\R", "").nn

object KafkaConfig:
  opaque type BootstrapServer = String

  object BootstrapServer:
    def unsafeFrom(value: String): BootstrapServer = value

    def from(value: String): Option[BootstrapServer] =
      if value.nonEmpty then Some(unsafeFrom(value)) else Option.empty[BootstrapServer]

    extension (bootstrapServer: BootstrapServer) def value: String = bootstrapServer

  opaque type ConsumerGroup = String

  object ConsumerGroup:
    def unsafeFrom(value: String): ConsumerGroup = value

    def from(value: String): Option[ConsumerGroup] =
      if value.nonEmpty then Some(unsafeFrom(value)) else Option.empty[ConsumerGroup]

    extension (consumerGroup: ConsumerGroup) def value: String = consumerGroup

  opaque type SchemaRegistry = String

  object SchemaRegistry:
    def unsafeFrom(value: String): SchemaRegistry = value

    def from(value: String): Option[SchemaRegistry] =
      if value.nonEmpty then Some(unsafeFrom(value)) else Option.empty[SchemaRegistry]

    extension (schemaRegistry: SchemaRegistry) def value: String = schemaRegistry

  opaque type Topic = String

  object Topic:
    def unsafeFrom(value: String): Topic = value

    def from(value: String): Option[Topic] =
      if value.nonEmpty then Some(unsafeFrom(value)) else Option.empty[Topic]

    extension (topic: Topic) def value: String = topic
