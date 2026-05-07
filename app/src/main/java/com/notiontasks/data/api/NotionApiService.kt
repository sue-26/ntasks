package com.notiontasks.data.api

import com.notiontasks.data.model.*
import okhttp3.RequestBody
import retrofit2.http.*

interface NotionApiService {

    // ─── OAuth ────────────────────────────────────────────────────────────────

    @POST
    suspend fun exchangeToken(
        @Url url: String,
        @Header("Authorization") basicAuth: String,
        @Body body: RequestBody
    ): NotionTokenResponse

    // ─── Search ───────────────────────────────────────────────────────────────

    @POST("search")
    suspend fun search(
        @Body request: NotionSearchRequest
    ): NotionListResponse<NotionPage>

    // ─── Blocks ───────────────────────────────────────────────────────────────

    @GET("blocks/{block_id}/children")
    suspend fun getBlockChildren(
        @Path("block_id") blockId: String,
        @Query("start_cursor") startCursor: String? = null,
        @Query("page_size") pageSize: Int = 100
    ): NotionListResponse<NotionBlock>

    @PATCH("blocks/{block_id}")
    suspend fun updateBlock(
        @Path("block_id") blockId: String,
        @Body request: NotionUpdateBlockRequest
    ): NotionBlock

    // ─── Databases ────────────────────────────────────────────────────────────

    @GET("databases/{database_id}")
    suspend fun getDatabase(
        @Path("database_id") databaseId: String
    ): NotionPage

    @POST("databases/{database_id}/query")
    suspend fun queryDatabase(
        @Path("database_id") databaseId: String,
        @Body request: NotionDatabaseQueryRequest
    ): NotionListResponse<NotionPage>

    // ─── Pages ────────────────────────────────────────────────────────────────

    @GET("pages/{page_id}")
    suspend fun getPage(
        @Path("page_id") pageId: String
    ): NotionPage

    @PATCH("pages/{page_id}")
    suspend fun updatePage(
        @Path("page_id") pageId: String,
        @Body request: NotionUpdatePageRequest
    ): NotionPage
}
