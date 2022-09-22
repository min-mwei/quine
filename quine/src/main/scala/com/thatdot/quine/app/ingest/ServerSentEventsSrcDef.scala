package com.thatdot.quine.app.ingest

import akka.NotUsed
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.sse.ServerSentEvent
import akka.stream.alpakka.sse.scaladsl.EventSource
import akka.stream.contrib.SwitchMode
import akka.stream.scaladsl.Source

import com.thatdot.quine.app.ingest.serialization.ImportFormat
import com.thatdot.quine.graph.CypherOpsGraph
import com.thatdot.quine.graph.MasterStream.IngestSrcExecToken

final case class ServerSentEventsSrcDef(
  url: String,
  format: ImportFormat,
  initialSwitchMode: SwitchMode,
  parallelism: Int,
  maxPerSecond: Option[Int]
)(implicit graph: CypherOpsGraph)
    extends RawValuesIngestSrcDef(
      format,
      initialSwitchMode,
      parallelism,
      maxPerSecond,
      "SSE"
    ) {

  type InputType = ServerSentEvent

  override def ingestToken: IngestSrcExecToken = IngestSrcExecToken(s"$name: $url")

  def source(): Source[ServerSentEvent, NotUsed] = EventSource(uri = Uri(url), send = Http().singleRequest(_))

  def rawBytes(event: ServerSentEvent): Array[Byte] = event.data.getBytes

}
