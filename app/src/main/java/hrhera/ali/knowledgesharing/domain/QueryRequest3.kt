package hrhera.ali.knowledgesharing.domain

import hrhera.ali.annotations.ToRequestMap

@ToRequestMap
data class QueryRequest3(
    val  name: String,
    val namedProperty: String,
    val nullValue: String?
)