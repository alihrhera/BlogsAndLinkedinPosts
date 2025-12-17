package hrhera.ali.knowledgesharing.domain

import hrhera.ali.annotations.ToRequestMapFailed

data class QueryRequest2(
    val  name: String,
    @ToRequestMapFailed("query_name")
    val namedProperty: String,
    val nullValue: String?
)