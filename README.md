# Custom Annotations with KSP

In this branch, we explored how to create custom annotations and a corresponding annotation processor using **Kotlin Symbol Processing (KSP)**. The goal was to automate the generation of `toMap()` functions for data classes, which is a common requirement when preparing data for API requests.

## 🚀 Overview
We implemented a system where simply annotating a data class automatically generates an extension function to convert that class into a `Map<String, String>`. This eliminates the need to manually write `toMap` functions for every request model.

## 🎯 Importance of Annotations
Annotations provide a powerful way to add metadata to our code. Instead of writing repetitive boilerplate code, we can tag our classes and properties with annotations, and let a processor handle the heavy lifting.

**Key Advantages:**
*   **Cleaner Code:** Business logic is not cluttered with utility mapping code.
*   **Consistency:** The generated code always follows the same pattern, reducing human error.
*   **Maintainability:** Changes to the mapping logic only need to be made in the processor, not in every data class.

## 💎 Benefits of KSP (Kotlin Symbol Processing)
*   **Performance:** KSP is significantly faster than KAPT (Kotlin Annotation Processing Tool) because it doesn't require parsing Java code and is optimized for Kotlin.
*   **Kotlin-First:** It understands Kotlin-specific features (like nullability and properties) better than standard Java annotation processors.
*   **Type Safety:** Errors can be caught at compile-time rather than runtime.

## 🛠️ Implementation Steps

### 1. Module Setup
We created a separate Java/Kotlin Library module named `annotations` to contain our annotations and processor. This separation is best practice to avoid including the processor code in the final app APK.

### 2. Defining Annotations
We defined three key annotations in `hrhera.ali.annotations`:
*   **`@ToRequestMap`**: Marks the class that needs a map generation.
*   **`@ToRequestMapFailed`**: Customizes the key name for a property in the map (e.g., mapping `userName` property to `"user_name"` key).
*   **`@IgnoreFailed`**: Excludes a specific property from being added to the map.

### 3. Building the Processor
We implemented the `ToRequestMapProcessor` which extends `SymbolProcessor`.
*   **Discovery**: It uses `resolver.getSymbolsWithAnnotation` to find all classes annotated with `@ToRequestMap`.
*   **Processing**: It iterates through the properties of these classes.
*   **Logic**:
    *   Checks for `@IgnoreFailed` to skip properties.
    *   Checks for `@ToRequestMapFailed` to use custom keys.
    *   Handles nullable types safely.

### 4. Code Generation
Using `codeGenerator`, the processor writes a new Kotlin file containing an extension function `toRequestMap()` for each annotated class.
*   **Generated File**: `build/generated/ksp/.../G_<ClassName>ToRequestMap.kt`

### 5. Usage Example
In the `app` module, we applied the annotations to `QueryRequest2`:

```kotlin
@ToRequestMap
data class QueryRequest2(
    val name: String,
    @ToRequestMapFailed("query_name") // Maps "namedProperty" to "query_name"
    val namedProperty: String,
    val nullValue: String?
)
```

After building the project, KSP generates the code, allowing us to call:
```kotlin
val request = QueryRequest2(...)
val map = request.toRequestMap() // Auto-generated!
```
