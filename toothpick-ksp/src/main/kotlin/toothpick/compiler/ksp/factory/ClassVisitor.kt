package toothpick.compiler.ksp.factory

import com.google.devtools.ksp.closestClassDeclaration
import com.google.devtools.ksp.isAnnotationPresent
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitorVoid
import toothpick.ProvidesReleasable
import toothpick.ProvidesSingletonInScope
import toothpick.Releasable
import toothpick.compiler.ksp.common.ParamInjectionTarget
import toothpick.compiler.ksp.common.containsInject
import toothpick.compiler.ksp.common.isMethod
import toothpick.compiler.ksp.common.superClass
import toothpick.compiler.ksp.factory.targets.ConstructorInjectionTarget
import javax.inject.Singleton

class ClassVisitor(
    private val logger: KSPLogger
) : KSVisitorVoid() {

    private val _targets = mutableListOf<ConstructorInjectionTarget>()
    val targets: List<ConstructorInjectionTarget>
        get() = _targets

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        val classDeclaration = function.parentDeclaration as KSClassDeclaration
        logger.info("#OPZ# Visiting function declaration: $classDeclaration")

        _targets += ConstructorInjectionTarget(
            classDeclaration = classDeclaration,
            constructorDeclaration = function,
            scopeName = classDeclaration.getScopeName(),
            hasSingletonAnnotation = classDeclaration.isAnnotationPresent(Singleton::class),
            hasReleasableAnnotation = classDeclaration.isAnnotationPresent(Releasable::class),
            hasProvidesSingletonInScopeAnnotation = classDeclaration
                .isAnnotationPresent(ProvidesSingletonInScope::class),
            hasProvidesReleasableAnnotation = classDeclaration
                .isAnnotationPresent(ProvidesReleasable::class),
            superClassThatNeedsMemberInjection = classDeclaration.getMostDirectSuperClassWithInjectedMembers(),
            parameters = function.parameters.toParameterTarget()
        )
    }

}

private fun List<KSValueParameter>.toParameterTarget(): List<ParamInjectionTarget> {
    return this.map {
        ParamInjectionTarget(
            memberType = it.type.asMemberType(),
            memberName = it.name!!.asString(),
            kind = it.type.toKind(),
            kindParamClass = it.type,
            name = it.annotatedName
        )
    }
}

private fun KSTypeReference.toKind(): ParamInjectionTarget.Kind {
    return when (this.resolve().declaration.qualifiedName?.asString()) {
        "toothpick.Lazy" -> ParamInjectionTarget.Kind.LAZY
        "javax.inject.Provider" -> ParamInjectionTarget.Kind.PROVIDER
        else -> ParamInjectionTarget.Kind.INSTANCE
    }
}

private fun KSTypeReference.asMemberType(): KSTypeReference {
    return when (this.resolve().declaration.qualifiedName?.asString()) {
        "toothpick.Lazy" -> this.element?.typeArguments?.firstOrNull()?.type ?: this
        "javax.inject.Provider" -> this.element?.typeArguments?.firstOrNull()?.type ?: this
        else -> this
    }
}

private val KSValueParameter.annotatedName: String?
    get() = this.name?.asString()

private fun KSClassDeclaration.getMostDirectSuperClassWithInjectedMembers(): KSClassDeclaration? {
    var klass: KSClassDeclaration? = this
    do {
        if (klass!!.declarations.any { (it is KSPropertyDeclaration || (it.isMethod())) && it.annotations.containsInject() }) {
            return klass
        }

        klass = klass.superClass
    } while (klass != null)

    return null
}

private fun KSClassDeclaration.getScopeName(): String? {
    return null
}
