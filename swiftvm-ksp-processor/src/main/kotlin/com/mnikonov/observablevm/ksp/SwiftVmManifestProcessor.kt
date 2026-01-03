package com.mnikonov.observablevm.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.validate
import kotlinx.serialization.json.Json

internal class SwiftVmManifestProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) : SymbolProcessor {

    // Persist across rounds so we don't lose previously-processed symbols.
    private val collected = linkedMapOf<String, SwiftVmEntry>()
    private var written = false

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (written) return emptyList()

        val deferred = mutableListOf<KSAnnotated>()
        val vmSymbols = resolver.getSymbolsWithAnnotation("com.mnikonov.observablevm.swiftvm.SwiftViewModel")

        for (symbol in vmSymbols) {
            if (!symbol.validate()) {
                deferred += symbol
                continue
            }

            val cls = symbol as? KSClassDeclaration
            if (cls == null) {
                logger.error("@SwiftViewModel can only target classes.", symbol)
                continue
            }

            val fqName = cls.qualifiedName?.asString()
            if (fqName == null) {
                logger.error("Cannot resolve qualified name for @SwiftViewModel class.", cls)
                continue
            }

            collected[fqName] = buildEntry(cls)
        }

        // If anything isn't ready, ask KSP to give it back next round.
        if (deferred.isNotEmpty()) return deferred

        // All done: write aggregated manifest once (sortedBy just to guarantee predictable output)
        writeManifest(collected.values.toList().sortedBy { it.className })
        written = true
        return emptyList()
    }

    private fun buildEntry(cls: KSClassDeclaration): SwiftVmEntry {
        val fqName = cls.qualifiedName!!.asString()
        val simpleName = cls.simpleName.asString()

        val uiState = findUiState(cls, fqName)
        val actions = findActions(cls)

        return SwiftVmEntry(
            className = fqName,
            simpleName = simpleName,
            state = uiState,
            actions = actions
        )
    }

    private fun findUiState(cls: KSClassDeclaration, vmFqName: String): StateEntry? {
        val uiStateProps = cls.getAllProperties()
            .filter { it.hasAnnotation(annotationShortName = "SwiftUiState") }
            .toList()

        return when (uiStateProps.size) {
            0 -> null
            1 -> buildUiStateEntry(uiStateProps.single())
            else -> {
                logger.error(
                    "Multiple @SwiftUiState properties in $vmFqName. Please annotate exactly one.",
                    uiStateProps.first()
                )
                null
            }
        }
    }

    private fun buildUiStateEntry(prop: KSPropertyDeclaration): StateEntry? {
        val propName = prop.simpleName.asString()
        val resolvedType = prop.type.resolve()
        val declaration = resolvedType.declaration as? KSClassDeclaration

        val fqName = declaration?.qualifiedName?.asString()
        if (fqName != "kotlinx.coroutines.flow.StateFlow") {
            logger.error(
                "@SwiftUiState '$propName' must be a StateFlow<T>.",
                prop
            )
            return null
        }

        val inner = resolvedType.arguments.firstOrNull()?.type?.resolve()
        if (inner == null) {
            logger.error(
                "@SwiftUiState '$propName' must be generic (StateFlow<T>).",
                prop
            )
            return null
        }

        return StateEntry(
            property = propName,
            typeSimpleName = inner.declaration.simpleName.asString()
        )
    }

    private fun findActions(cls: KSClassDeclaration): List<ActionEntry> {
        val actionFns = cls.getAllFunctions()
            .filter { it.hasAnnotation(annotationShortName = "SwiftUiAction") }
            .toList()

        return actionFns.map { fn ->
            ActionEntry(
                function = fn.simpleName.asString(),
                params = fn.parameters.mapIndexed { index, parameter ->
                    ActionParamsEntry(
                        paramLabel = parameter.name?.asString() ?: "p$index",
                        typeSimpleName = parameter.type.resolve().declaration.simpleName.asString(),
                    )
                },
            )
        }
    }

    private fun writeManifest(entries: List<SwiftVmEntry>) {
        val manifest = SwiftVmManifest(viewModels = entries)
        val content = json.encodeToString(manifest)

        val file = codeGenerator.createNewFile(
            dependencies = Dependencies(aggregating = true),
            packageName = "",
            fileName = "swiftvm-manifest",
            extensionName = "json"
        )

        file.writer().use { it.write(content) }
        logger.info("SwiftVM manifest generated with ${entries.size} view model(s).")
    }

    private fun KSDeclaration.hasAnnotation(annotationShortName: String): Boolean =
        annotations.any { it.shortName.asString() == annotationShortName }

}
