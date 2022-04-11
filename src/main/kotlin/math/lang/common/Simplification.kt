package math.lang.common


fun simple(operand: Operand): List<Result> {
    return when (operand.javaClass) {
        Operand::class.java -> {
            val results: List<List<Result>> = (operand as Operation).operands.map { simple(it) }
            val newResults: List<Result> = simplify(
                Operation(
                    operand.operator,
                    *results.filter { it.isNotEmpty() }.map { it.last().operand }.toTypedArray()
                )
            )
            val updatedResult: MutableList<Result> = results.flatten().toMutableList()
            updatedResult.addAll(0, newResults)
            updatedResult
        }
        Constant::class.java -> simplify(operand as Constant)
        Variable::class.java -> simplify(operand as Variable)
        IntegerLiteral::class.java -> simplify(operand as IntegerLiteral)
        DecimalLiteral::class.java -> simplify(operand as DecimalLiteral)
        else -> listOf()
    }
}

fun simplify(operation: Operation): List<Result> {
    TODO()
}

fun simplify(operation: Constant): List<Result> {
    TODO()
}

fun simplify(operation: Variable): List<Result> {
    TODO()
}

fun simplify(operation: IntegerLiteral): List<Result> {
    TODO()
}

fun simplify(operation: DecimalLiteral): List<Result> {
    TODO()
}