package com.thatdot.quine.app.routes

import org.apache.pekko.http.scaladsl.model.Uri
import org.apache.pekko.http.scaladsl.server.Route

import endpoints4s.openapi.model._

import com.thatdot.quine.app.BuildInfo
import com.thatdot.quine.app.config.QuineConfig
import com.thatdot.quine.app.util.OpenApiRenderer
import com.thatdot.quine.graph.BaseGraph
import com.thatdot.quine.model.QuineIdProvider
import com.thatdot.quine.routes._
import com.thatdot.quine.routes.exts.OpenApiServer

/** The OpenAPI docs for our API
  *
  * @param idProvider the Quine ID provider (relevant for serialization of IDs and examples)
  */
final class QuineAppOpenApiDocs(val idProvider: QuineIdProvider)
    extends DebugOpsRoutes
    with AlgorithmRoutes
    with AdministrationRoutes
    with QueryUiRoutes
    with QueryUiConfigurationRoutes
    with IngestRoutes
    with StandingQueryRoutes
    with endpoints4s.openapi.Endpoints
    with endpoints4s.openapi.JsonEntitiesFromSchemas
    with com.thatdot.quine.app.routes.exts.ServerQuineEndpoints
    with com.thatdot.quine.routes.exts.OpenApiEntitiesWithExamples
    with com.thatdot.quine.routes.exts.OpenApiAnySchema {

  private[this] val endpoints = List(
    buildInfo,
    config(QuineConfig().loadedConfigJson),
    readinessProbe,
    livenessProbe,
    metrics,
    shutdown,
    shardSizes,
    requestNodeSleep,
    graphHashCode,
    debugOpsGet,
    debugOpsVerbose,
    debugOpsEdgesGet,
    debugOpsHalfEdgesGet,
    debugOpsPropertyGet,
    //    non-readonly debugOps (intentionally left registered but undocumented, QU-1045:
    //    debugOpsPost,
    //    debugOpsDelete,
    //    debugOpsEdgesPut,
    //    debugOpsEdgeDelete,
    //    debugOpsPropertyPut,
    //    debugOpsPropertyDelete,
    algorithmSaveRandomWalks,
    algorithmRandomWalk,
    cypherPost,
    cypherNodesPost,
    cypherEdgesPost,
    gremlinPost,
    gremlinNodesPost,
    gremlinEdgesPost,
    queryUiSampleQueries,
    updateQueryUiSampleQueries,
    queryUiQuickQueries,
    updateQueryUiQuickQueries,
    queryUiAppearance,
    updateQueryUiAppearance,
    updateQueryUiAppearance,
    ingestStreamList,
    ingestStreamStart,
    ingestStreamStop,
    ingestStreamLookup,
    ingestStreamPause,
    ingestStreamUnpause,
    standingList,
    standingIssue,
    standingAddOut,
    standingRemoveOut,
    standingCancel,
    standingGet,
    standingList,
    standingPropagate
  )

  val api: OpenApi =
    openApi(
      Info(title = "Quine API", version = BuildInfo.version).withDescription(
        Some(
          """The following is autogenerated from the OpenAPI specification `openapi.json` 
            |and is included in Quine as fully interactive documentation. When running 
            |Quine, you can issue API calls directly from the embedded documentation pages.
            | 
            |For docs, guides, and tutorials, please visit <https://quine.io>""".stripMargin
        )
      )
    )(
      endpoints: _*
    )

}

/** The Pekko HTTP implementation of routes serving up the OpenAPI specification
  * of our API
  *
  * @param graph the Quine graph
  */
final case class QuineAppOpenApiDocsRoutes(graph: BaseGraph, uri: Uri)
    extends endpoints4s.pekkohttp.server.Endpoints
    with endpoints4s.pekkohttp.server.JsonEntitiesFromEncodersAndDecoders {

  val doc = new QuineAppOpenApiDocs(graph.idProvider)

  val route: Route = {
    val docEndpoint = endpoint(
      get(path / "docs" / "openapi.json"),
      ok(
        jsonResponse[endpoints4s.openapi.model.OpenApi](
          OpenApiRenderer(isEnterprise = false).stringEncoder(Some(Seq(OpenApiServer(uri.toString))))
        )
      )
    )

    docEndpoint.implementedBy(_ => doc.api)
  }
}
