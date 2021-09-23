package tech.watanave.fulla.compiler

import com.google.auto.common.BasicAnnotationProcessor
import com.google.auto.service.AutoService
import com.google.common.collect.SetMultimap
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import tech.watanave.fulla.annotation.State
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.lang.model.element.Element
import javax.lang.model.element.ElementKind
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

@AutoService(Processor::class)
class FullaProcessor: BasicAnnotationProcessor() {

    override fun initSteps(): MutableIterable<ProcessingStep> {
        return mutableListOf(
            FullaProcessorStep(processingEnv.filer, processingEnv)
        )
    }

}

class FullaProcessorStep(private val filer: Filer, private val processingEnvironment: ProcessingEnvironment) : BasicAnnotationProcessor.ProcessingStep {

    override fun annotations(): MutableSet<out Class<out Annotation>> {
        return mutableSetOf(State::class.java)
    }

    override fun process(elementsByAnnotation: SetMultimap<Class<out Annotation>, Element>?): MutableSet<Element> {
        elementsByAnnotation ?: return mutableSetOf()
        elementsByAnnotation[State::class.java].forEach(this::generateActionAndReducer)
        return mutableSetOf()
    }

    private fun generateActionAndReducer(element: Element) {
        val packageName = processingEnvironment.elementUtils.getPackageOf(element).qualifiedName.toString() +
            "." + element.simpleName.toString().lowercase()
        val actionClassName = "Action"

        val actionClassBuilder = TypeSpec
            .classBuilder(actionClassName)
            .addModifiers(KModifier.SEALED)

        val actionSubClasses = mutableListOf<TypeSpec>()
        element.enclosedElements
            .filter { it.kind == ElementKind.FIELD }
            .forEach { field ->
                val constructorSpec = FunSpec.constructorBuilder()
                    .addParameter("value", field.asType().asTypeName().javaToKotlinType())
                    .build()

                TypeSpec
                    .classBuilder(field.simpleName.toString().toUpperCamel())
                    .superclass(ClassName(packageName, actionClassName))
                    .addModifiers(KModifier.DATA)
                    .primaryConstructor(constructorSpec)
                    .addProperty(
                        PropertySpec.builder("value", field.asType().asTypeName().javaToKotlinType())
                            .initializer("value")
                            .build()
                    )
                    .build()
                    .also {
                        actionClassBuilder.addType(it)
                        actionSubClasses.add(it)
                    }
            }

        FileSpec.builder(packageName, actionClassName)
            .addType(actionClassBuilder.build())
            .build()
            .writeTo(filer)

        ////////////////////////////////////////////////////////////

        val reducerFunSpec = FunSpec.builder("reducer")
            //.receiver(viewModelElement.asType().asTypeName())
            .addParameter("state", element.asType().asTypeName())
            .addParameter("action", ClassName(packageName, actionClassName))
            .returns(element.asType().asTypeName())
            .addCode(
                CodeBlock.of("""
    return when (action) {
        ${actionSubClasses
                .map { "is $actionClassName.${it.name} -> { state.copy(${it.name!!.toLowerCamel()} = action.value) }" }
                .joinToString(separator = "\n        ")}
    }
                """.trimIndent()))
            .build()

        val stateName = element.simpleName.toString()
        FileSpec.builder(packageName, "${stateName}Reducer")
            .addFunction(reducerFunSpec)
            .build()
            .writeTo(filer)
    }

    private fun String.toUpperCamel() : String {
        val initial = this.first().uppercase()
        return this.replaceFirst(initial, initial, true)
    }

    private fun String.toLowerCamel() : String {
        val initial = this.first().lowercase()
        return this.replaceFirst(initial, initial, true)
    }

    private fun TypeName.javaToKotlinType(): TypeName {
        return if (this is ParameterizedTypeName) {
            val converted = this.rawType.javaToKotlinType()
            val typeArguments = this.typeArguments.map { it.javaToKotlinType() }
            (converted as ClassName).parameterizedBy(*typeArguments.toTypedArray())
        } else {
            val className =
                JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(toString()))
                    ?.asSingleFqName()?.asString()

            return if (className == null) {
                this
            } else {
                ClassName.bestGuess(className)
            }
        }
    }
}
