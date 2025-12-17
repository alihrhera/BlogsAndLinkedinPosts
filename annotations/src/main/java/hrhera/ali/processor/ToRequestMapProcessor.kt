package hrhera.ali.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import hrhera.ali.annotations.IgnoreFailed
import hrhera.ali.annotations.ToRequestMap
import hrhera.ali.annotations.ToRequestMapFailed
import java.io.BufferedWriter
import java.io.OutputStream

class ToRequestMapProcessor(
    private val codeGenerator: CodeGenerator,  // بيستخدم الـ CodeGenerator عشان يولد ملفات كود جديدة
) : SymbolProcessor {

    // الدالة الرئيسية اللي KSP بتتنادي عليها في كل بيلد
    override fun process(resolver: Resolver): List<KSAnnotated> {

        // بيجيب كل الكلاسات اللي عليها annotation @ToRequestMap
        // Resolver.getSymbolsWithAnnotation بيلاقي الرموز حسب اسم الـ annotation الكامل :contentReference[oaicite:0]{index=0}
        val classesWithMap = resolver.getSymbolsWithAnnotation(ToRequestMap::class.qualifiedName!!)
            .filterIsInstance<KSClassDeclaration>()  // بفلترهم عشان ناخد بس declarations بتاعت الكلاسات

        // هنا بنجمع كل الـ properties اللي عليها @ToRequestMapFailed أو @IgnoreFailed
        val propsWithAnnotations = listOf(
            ToRequestMapFailed::class,
            IgnoreFailed::class
        ).flatMap { annClass ->
            resolver.getSymbolsWithAnnotation(annClass.qualifiedName!!)
                .filterIsInstance<KSPropertyDeclaration>()  // ناخد فقط declarations بتاعت الـ properties
                .toList()                                   // نحولها لـ List عشان نقدر نشتغل عليها
        }

        // بنجهّز خريطة (Map) عشان نجمع الكلاسات وخصائصها
        val grouped = mutableMapOf<KSClassDeclaration, MutableList<KSPropertyDeclaration>>()

        // لو الكلاس عليه @ToRequestMap نضيفه في الخريطة حتى لو لسه مفيش أي property ليه
        classesWithMap.forEach { clazz ->
            grouped.getOrPut(clazz) { mutableListOf() }
        }

        // نضمّ كل property لكلاس الأب بتاعه
        propsWithAnnotations.forEach { prop ->
            val parent = prop.parentDeclaration as? KSClassDeclaration  // بنجيب الكلاس الأب
            if (parent != null) {
                grouped.getOrPut(parent) { mutableListOf() }.add(prop)
            }
        }

        // ندّي لكل زوج (كلاس + properties) الدالة اللي بتولّد الكود
        grouped.forEach { (clazz, props) ->
            generateExtension(clazz, props)
        }

        // هنا بنرجّع emptyList معناه مفيش symbols محتاجة
        // defer أو إعادة معالجة في جولة تانية :contentReference[oaicite:1]{index=1}
        return emptyList()
    }

    // دي الدالة اللي بتولّد فعليًا كود الـ extension لكل كلاس
    private fun generateExtension(
        clazz: KSClassDeclaration,
        properties: List<KSPropertyDeclaration>
    ) {
        // اسم الكلاس كـ String
        val className = clazz.simpleName.asString()

        // نشوف إذا الكلاس نفسه عليه @ToRequestMap
        val classHasToRequestMap = clazz.annotations.any { it.shortName.asString() == "ToRequestMap" }

        // نشوف إذا أي property عنده annotation مهم (علشان نقرر لو نولّد حاجة)
        val anyPropertyHasAnnotation = properties.any { prop ->
            prop.annotations.any { ann ->
                ann.shortName.asString() in listOf("ToRequestMapFailed", "IgnoreFailed")
            }
        }

        // لو لا الكلاس ولا أي property مهتم بيه، نخرج من الدالة من غير توليد
        if (!classHasToRequestMap && !anyPropertyHasAnnotation) return

        // نخلق ملف جديد في build/generated/ksp (KSP بيقدّمه تلقائي) علشان نكتب فيه الكود المتولّد
        val file = createNeaFile(clazz, className)

        mapCreation(file, className, properties)

        file.close()  // نقفل الملف ونسلّمه
    }

    private fun mapCreation(
        file: OutputStream,
        className: String,
        properties: List<KSPropertyDeclaration>
    ) {
        file.bufferedWriter().use { writer ->  // نفتح writer للكتابة
            // نولّد دالة extension باسم toRequestMap
            writer.appendLine("fun $className.toRequestMap(): Map<String, String> {")
            writer.appendLine("    val map = mutableMapOf<String, String>()")

            // نمرّ على كل الخصائص اللي جمعناها
            properties.forEach { prop ->
                // نتجاهل لو عليه @IgnoreFailed
                val ignore = prop.annotations.any { it.shortName.asString() == "IgnoreFailed" }
                if (ignore) return@forEach

                // لو فيه @ToRequestMapFailed ناخد الاسم اللي اتكتب جوّا annotation
                val toRequestMapFailed =
                    prop.annotations.find { it.shortName.asString() == "ToRequestMapFailed" }
                val key = if (toRequestMapFailed != null) {
                    toRequestMapFailed.arguments.first().value as String
                } else {
                    // غير كده ناخد اسم الخاصية نفسها
                    prop.simpleName.asString()
                }

                // اسم الخاصية كـ String
                val propName = prop.simpleName.asString()

                addNewFailedToMap(prop = prop, propName = propName, writer = writer, key = key)
            }

            // نرجّع الـ map في الآخر
            writer.appendLine("    return map")
            writer.appendLine("}")
        }
    }

    private fun createNeaFile(
        clazz: KSClassDeclaration,
        className: String
    ): OutputStream {
        val file = codeGenerator.createNewFile(
            Dependencies(false, clazz.containingFile!!), // هنربط الاعتماديات بـ الملف الأصلي
            clazz.packageName.asString(),               // package نفسه
            "G_${className}ToRequestMap"                 // اسم الملف المتولد
        )
        return file
    }

    private fun addNewFailedToMap(
        prop: KSPropertyDeclaration,
        writer: BufferedWriter,
        propName: String,
        key: String
    ) {
        val type = prop.type.resolve()
        val isNullable = type.isMarkedNullable
        if (isNullable) {
            writer.appendLine(
                """    this.$propName?.let { map["$key"] = it.toString() }"""
            )
        } else {
            writer.appendLine("""    map["$key"] = this.$propName.toString()""")
        }
    }
}
