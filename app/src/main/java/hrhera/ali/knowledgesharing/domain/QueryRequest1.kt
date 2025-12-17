package hrhera.ali.knowledgesharing.domain

import hrhera.ali.annotations.IgnoreFailed

data class QueryRequest1(
    val name: String,
    @IgnoreFailed
    val ignoredValue: String,
    val nullValue: String?
)