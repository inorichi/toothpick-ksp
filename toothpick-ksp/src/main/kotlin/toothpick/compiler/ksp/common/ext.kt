package toothpick.compiler.ksp.common

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration

fun KSFunctionDeclaration.isInjected(): Boolean {
    return annotations.containsInject()
}

fun KSDeclaration.isMethod(): Boolean {
    return this is KSFunctionDeclaration && !this.isConstructor()
}

fun Sequence<KSAnnotation>.containsInject(): Boolean {
    return any { annotation -> annotation.shortName.getShortName() == "Inject" }
}

val KSClassDeclaration.superClass: KSClassDeclaration?
    get() {
        return superTypes.map { it.resolve() }
            .firstOrNull { (it.declaration as? KSClassDeclaration)?.classKind == ClassKind.CLASS }
            ?.let { it.declaration as KSClassDeclaration }
    }
