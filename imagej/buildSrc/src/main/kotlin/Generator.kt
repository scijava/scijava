import java.io.File

abstract class Generator {

    val builder = StringBuilder()

    operator fun String.unaryPlus() {
        val text = lines().drop(1).joinToString("\n")
        val maybeNewline = if (builder.isEmpty()) "" else "\n"
        builder.append(maybeNewline + text)
    }

    operator fun invoke(src: File) {
        builder.clear()
        generate()
        val `package` = builder.lines().first { it.startsWith("package ") }
                .drop(8).dropLast(1).replace('.', '/')
        val classname = this::class.simpleName!! + ".java"
        val path = src.resolve(`package`).apply { mkdirs() }
        path.resolve(classname).apply { createNewFile() }.writeText(builder.toString())
    }

    abstract fun generate()

    // Globals.list
    var arity = 0
    var maxArity = 16
    val numConsumers get() = maxArity + 1
    open val arities get() = 0..maxArity

    inline fun forEachArity(arities: IntRange = this.arities, block: (arity: Int) -> Unit) {
        for (a in arities) {
            arity = a
            block(a)
        }
    }

    val functionName
        get() = when (arity) {
            1 -> "Function"
            2 -> "BiFunction"
            in 3..16 -> "Functions.Arity$arity"
            else -> "Error"
        }

    fun arityName(arity: Int) = when (arity) {
        1 -> "first"
        2 -> "second"
        3 -> "third"
        4 -> "fourth"
        5 -> "fifth"
        6 -> "sixth"
        7 -> "seventh"
        8 -> "eighth"
        9 -> "ninth"
        10 -> "tenth"
        11 -> "eleventh"
        12 -> "twelfth"
        13 -> "thirteenth"
        14 -> "fourteenth"
        15 -> "fifteenth"
        16 -> "sixteenth"
        else -> error("invalid max arity: $maxArity")
    }

    fun inplaceSuffix(num: Int) = if (arity == 1) "1" else "${arity}_$num"

    fun simplifiedInplace(num: Int) = if (arity == 1) "Arity1" else "Arity${inplaceSuffix(num)}"

    fun inplaceType(num: Int) = "Inplaces.${simplifiedInplace(num)}"

    val computerArity get() = "Computers.Arity${arity}"

    val consumerArity
        get() = when (arity) {
            0 -> "Consumer"
            1 -> "BiConsumer"
            else -> "Consumers.Arity${arity + 1}"
        }

    val functionArity
        get() = when (arity) {
            0 -> "Producer"
            1 -> "Function"
            2 -> "BiFunction"
            else -> "Functions.Arity$arity"
        }

    val genericParamTypes
        get() = when (arity) {
            0 -> listOf("O")
            1 -> listOf("I", "O")
            else -> (1..arity).map { "I$it" } + "O"
        }

    open val generics get() = '<' + genericParamTypes.joinToString() + '>'

    open val genericsNamesList
        get() = genericParamTypes.map {
            when (it) {
                "O" -> "out"
                "I" -> "in"
                else -> "in${it.substring(1)}"
            }
        }

    open val nilNames get() = genericsNamesList.map { "${it}Type" }

    val typeArgs get() = nilNames.joinToString { "$it.getType()" }

    open val typeParamsList: List<String>
        get() {
            val gpt = genericParamTypes
            val names = genericsNamesList
            return (0..arity).map { "${gpt[it]} ${names[it]}" }
        }

    val typeParamsListWithoutOutput: List<String>
        get() {
            val gpt = genericParamTypes
            val names = genericsNamesList
            return (0 until arity).map { "${gpt[it]} ${names[it]}" }
        }

    val applyParams get() = typeParamsList.dropLast(1).joinToString()

    val applyArgs get() = genericsNamesList.dropLast(1).joinToString()

    val computeParams: String
        get() {
            val typeParams = typeParamsList as ArrayList
            typeParams[arity] = "@Container " + typeParams[arity]
            return typeParams.joinToString()
        }

    val acceptParams get() = typeParamsList.joinToString { "final $it" }

    val computeArgs get() = genericsNamesList.joinToString()

    val acceptArgs get() = computeArgs
}