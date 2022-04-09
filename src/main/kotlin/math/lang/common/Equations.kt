package math.lang.common

import math.lang.Results
import math.lang.common.ExpressionConstants.Companion.add
import math.lang.common.ExpressionConstants.Companion.mul
import math.lang.common.ExpressionConstants.Companion.x
import math.lang.common.ExpressionConstants.Companion.y
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand

/*fun main() {
    val str = "((x-2)*x)/(x*(2*-x-3))"
    val list = Token.getTokens(str)
    val tokenNode = TokenNode.getTree(list)
    val operand = getOperand(tokenNode)

    val results = simp(operand)
    if(results.isNotEmpty())
        println(results)
}*/

fun simp(operand: Operand): Results {
    if(operand is Operation) {
        val results: List<Results> = operand.operands.map { simp(it) }
        val r: Results = Results()
        val ops = operand.operands.toMutableList()
        for(formula in getAllSimplificationEquation()) {
            val result = formula.simplify(Operation(operand.operator, *results.indices.map { if(results[it].isNotEmpty()) results[it].last().operand else operand.operands[it] }.toTypedArray()))
            if(result!=null) {
                results.indices.forEach { i ->
                    results[i].forEach { res ->
                        ops[i] = res.operand
                        r.add(
                            Result(
                                Operation(operand.operator, *ops.toTypedArray()),
                                res.fx,
                                res.dFx,
                                res.formulaName
                            )
                        )
                    }

                }
                r.add(result)
            }
        }
    }
    return Results()
}


fun getAllSimplificationEquation():MutableList<SimplificationFormula> {
    val list:MutableList<SimplificationFormula> = mutableListOf()

    list.add(SimplificationFormula("Convert to Addition from Subtraction", { ExpressionConstants.sub(x, y) }, { applySub(it) }))

    list.add(SimplificationFormula("Convert to Multiplication from Division", { ExpressionConstants.div(x, y) }, { ExpressionConstants.mul(
        it[0], it[1].invert()) }))

    list.add(SimplificationFormula("Communicative Addition", { add(y.new(), add(x, y))}, { ExpressionConstants.add(
        *it.flatMap { it2-> if(it2 is Operation) it2.operands.toList() else listOf(it2) }.toTypedArray() ) }))

    list.add(SimplificationFormula("Communicative Multiplication", { mul(y.new(), mul(x, y))}, { ExpressionConstants.mul(
        *it.flatMap { it2-> if(it2 is Operation) it2.operands.toList() else listOf(it2) }.toTypedArray()) }))

    list.add(SimplificationFormula("Distributive Multiplication", { filterMulAdd(it) }, { applyMulAdd(it) }))

    list.add(SimplificationFormula("Common Negation", { ExpressionConstants.mul(x, y) }, {
        if(it.count { it1->it1.isNegative() }.mod(2)==1)  mul(
            *it.map { it1->it1.positive() }.toTypedArray()).negate() else mul(
            *it.map { it1->it1.positive() }.toTypedArray())}))

    return list
}

fun filterMulAdd(it:List<out Operand>): Operand {
    return mul(y.new(), add(x, y))
}

fun applySub(it:List<out Operand>): Operand {
    return add(
        it[0], it[1].negate())
}

fun applyMulAdd(it:List<out Operand>): Operand {
    val adds = it.filterIsInstance<Operation>()
        .filter { op->op.operator==Operators.add }
    val exceptAdds = it
        .filter { op-> op !is Operation || op.operator!=Operators.add }

    return add(*adds.reduce { a, o ->dot(a, o, exceptAdds) }.operands.map {
        ad -> when(exceptAdds.size) {
            0 -> ad
            1-> mul(ad, exceptAdds[0])
            else -> mul(*listOf(listOf(ad), exceptAdds).flatten().toTypedArray())
        }
    }.toTypedArray())

}

fun dot(o1:Operation, o2:Operation, multiplier: List<Operand> ) : Operation{
    return add(*o1.operands.map {
            it1 -> add(*o2.operands.map {
            it2-> mul(*listOf(it1, it2).toTypedArray()) as Operation

    }.toTypedArray()) as Operation
    }.toTypedArray()) as Operation
}

open class Formula(val name: String?, val fx:(v:List<out Operand>) -> Operand, val dFx:(v:List<out Operand>) -> Operand)

open class SimplificationFormula(val name: String?, val fx:(v:List<out Operand>) -> Operand, val dFx:(v:List<out Operand>) -> Operand) {
    fun simplify(operand: Operand) : Result? {
        if(operand is Operation) {
            val f = fx(operand.operands.toList())
            val level = getLevel(f, -1)
            if(operand.toOperatorString(level) == f.toOperatorString(level)) {
                val d = dFx(operand.operands.toList())
                return name?.let { Result(d, f, d, it) }
            }
        }
        return null
    }

    private fun getLevel(f: Operand, i: Int): Int {
        return if(f is Operation) {
            f.operands.maxOf { getLevel(it, i + 1) }
        } else {
            i
        }
    }
}

open class Result(val operand: Operand, val fx: Operand, val dFx: Operand, val formulaName: String, val assumptions: List<Operation> = listOf(), val derive: List<Derive> = listOf()) {
    var maxOperandSize: Int
    var str: String

    init {
        try {
            maxOperandSize= 0
            str = operand.string()
        } catch (t:Throwable) {
            t.printStackTrace()
            throw t
        }
    }

    override fun toString(): String {
        return "$operand $formulaName"
    }

}

class Derive(val left: Operand, val right: Operand, val desc: String)