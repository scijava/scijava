import function.Computers
import function.Consumers
import function.Functions
import function.Inplaces
import opsApi.OpBuilder
import opsEngine.main.complexLift.ComputersToFunctionsAndLift
import opsEngine.main.complexLift.FunctionsToComputersAndLift
import opsEngine.main.functional.ComputersToFunctionsViaFunction
import opsEngine.main.functional.ComputersToFunctionsViaSource
import opsEngine.main.functional.FunctionsToComputers
import opsEngine.main.functional.InplacesToFunctions
import opsEngine.main.lift.ComputerToArrays
import opsEngine.main.lift.ComputerToIterables
import opsEngine.main.lift.FunctionToArrays
import opsEngine.main.lift.FunctionToIterables
import opsEngine.main.lift.InplaceToArrays
import opsEngine.main.matcher.OpWrappers
import opsEngine.main.util.FunctionUtils
import opsEngine.test.complexLift.ComputerToFunctionIterablesTest
import opsEngine.test.complexLift.FunctionToComputerIterablesTest
import opsEngine.test.functional.ComputerToFunctionAdaptTest
import opsEngine.test.functional.ComputerToFunctionAdaptTestOps
import opsEngine.test.functional.FunctionToComputerAdaptTest
import opsEngine.test.functional.FunctionToComputerAdaptTestOps
import opsEngine.test.functional.InplaceToFunctionAdaptTest
import opsEngine.test.lift.ComputerToArraysTest
import opsEngine.test.lift.ComputerToIterablesTest
import opsEngine.test.lift.FunctionToArraysTest
import opsEngine.test.lift.FunctionToIterablesTest
import opsEngine.test.lift.InplaceToArraysTest
import opsEngine.test.matcher.OpWrappersTest
import opsEngine.test.OpBuilderTest
import opsEngine.test.OpBuilderTestOps
import opsEngine.test.OpMethodTest
import opsEngine.test.OpMethodTestOps
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

abstract class GenerateScijava : DefaultTask() {

    init {
        group = "build"
    }

    @get:Input
    var isMain: Boolean = true

    @get:Input
    val srcPath get() = "generated/src/" + if (isMain) "main" else "test"

    @get:OutputDirectory
    val target: Provider<Directory>
        get() = project.layout.buildDirectory.dir(srcPath)

    @TaskAction
    fun generate() {
        val src = project.layout.buildDirectory.get().asFile.resolve(srcPath).apply { mkdirs() }
        when (project.name.drop(8)) {
            "function" -> {
                Computers(src)
                Consumers(src)
                Functions(src)
                Inplaces(src)
            }
            "ops-api" -> OpBuilder(src)
            "ops-engine" ->
                if (isMain) {
                    // complexLift
                    ComputersToFunctionsAndLift(src)
                    FunctionsToComputersAndLift(src)
                    // functional
                    ComputersToFunctionsViaFunction(src)
                    ComputersToFunctionsViaSource(src)
                    FunctionsToComputers(src)
                    InplacesToFunctions(src)
                    // lift
                    ComputerToArrays(src)
                    ComputerToIterables(src)
                    FunctionToArrays(src)
                    FunctionToIterables(src)
                    InplaceToArrays(src)
                    // matcher
                    OpWrappers(src)
                    // util
                    FunctionUtils(src)
                } else {
                    // complexLift
                    ComputerToFunctionIterablesTest(src)
                    FunctionToComputerIterablesTest(src)
                    // functional
                    ComputerToFunctionAdaptTest(src)
                    ComputerToFunctionAdaptTestOps(src)
                    FunctionToComputerAdaptTest(src)
                    FunctionToComputerAdaptTestOps(src)
                    InplaceToFunctionAdaptTest(src)
                    // lift
                    ComputerToArraysTest(src)
                    ComputerToIterablesTest(src)
                    FunctionToArraysTest(src)
                    FunctionToIterablesTest(src)
                    InplaceToArraysTest(src)
                    // matcher
                    OpWrappersTest(src)
                    // ../
                    OpBuilderTest().invoke(src)
                    OpBuilderTestOps(src)
                    OpMethodTest(src)
                    OpMethodTestOps(src)
                }
        }
    }
}

fun <T> Iterable<T>.joinToStringComma(transform: ((T) -> CharSequence)? = null) = joinToStringComma(0, transform)
fun <T> Iterable<T>.joinToStringComma(limit: Int, transform: ((T) -> CharSequence)? = null): String {
    val joint = joinTo(StringBuilder(), transform = transform).toString()
    return joint + when {
        count() > limit -> ", "
        else -> ""
    }
}

val license = """/*
 * #%L
 * SciJava Operations: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2016 - 2019 SciJava Ops developers.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */"""

val dontEdit = """/*
* This is autogenerated source code -- DO NOT EDIT. Instead, edit the
* corresponding template in templates/ and rerun bin/generate.groovy.
*/"""