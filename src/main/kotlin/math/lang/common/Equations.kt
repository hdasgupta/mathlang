package math.lang.common

import math.lang.common.ExpressionConstants.Companion.add
import math.lang.common.ExpressionConstants.Companion.div
import math.lang.common.ExpressionConstants.Companion.getConst
import math.lang.common.ExpressionConstants.Companion.getValue
import math.lang.common.ExpressionConstants.Companion.hasValue
import math.lang.common.ExpressionConstants.Companion.isConst
import math.lang.common.ExpressionConstants.Companion.mul
import math.lang.common.ExpressionConstants.Companion.neg
import math.lang.common.ExpressionConstants.Companion.one
import math.lang.common.ExpressionConstants.Companion.pow
import math.lang.common.ExpressionConstants.Companion.sub
import math.lang.common.ExpressionConstants.Companion.x
import math.lang.common.ExpressionConstants.Companion.y
import math.lang.common.ExpressionConstants.Companion.zero
import math.lang.d
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand

//fun main() {
//    println(div(x, mul(x.new(), y.new())))
//
//    TokenNode.getTree(Token.getTokens("x+-(2)"))
//    val str = "1"
//    val list = Token.getTokens(str)
//    val tokenNode = TokenNode.getTree(list)
//    var operand = getOperand(tokenNode)
//
//    //println(d(operand))
//
//    var results = simp(operand)
//    println(results)
//
//}

val solution: MutableMap<String, Results> = mutableMapOf()

fun simp(operand: Operand): Results {
    var op: Operand = operand
    var r: Results? = solution[operand.toString()]
    if(r!=null) {
        return r
    }
    r = Results()
    for(formula in getAllSimplificationEquation()) {
        if(op is Operation) {
            if (formula.firstPriority) {
                val result = formula.simplify(op)
                if (result != null && !op.deepEquals(result.operand)) {
                    r.add(result)
                    op = result.operand
                    val rs: Results = simp(op)
                    if (rs.isNotEmpty()) {
                        r.addAll(rs)
                        op = rs.last().operand as Operation
                    }
                }
            } else {
                val results: List<Results> = op.operands.map { simp(it) }
                val ops = op.operands.toMutableList()
                results.indices.forEach { i ->
                    results[i].forEach { res ->
                        ops[i] = res.operand

                        var o: Operand = op((op as Operation).operator, ops)
                        r.add(
                            Result(
                                o,
                                res.fx,
                                res.dFx,
                                res.formulaName
                            )
                        )
                    }
                }
                val result = formula.simplify(
                    op(
                        op.operator,
                        *results.indices.map { if (results[it].isNotEmpty()) results[it].last().operand else (op as Operation).operands[it] })
                )
                if (result != null && !op.deepEquals(result.operand)) {
                    r.add(result)
                    op = result.operand
                    val rs: Results = simp(op)
                    if (rs.isNotEmpty()) {
                        r.addAll(rs)
                        op = rs.last().operand
                    }
                }
            }
        }
    }
    solution[operand.toString()] = r
    return r
}


fun op(operator: Operators, ops: List<Operand>) :Operand {
    if(operator.operandCount>1 && ops.size==1) {
        return ops[0]
    }
    return try {
        ExpressionConstants.Companion::class.java.getDeclaredMethod(operator.name, Array<Operand>::class.java).invoke(ExpressionConstants.Companion, ops.toTypedArray()) as Operand
    } catch (t:Throwable) {
        ExpressionConstants.Companion::class.java.getDeclaredMethod(operator.name, Operand::class.java).invoke(ExpressionConstants.Companion, ops[0]) as Operand
    }
}

fun getAllSimplificationEquation():MutableList<SimplificationFormula> {
    val list:MutableList<SimplificationFormula> = mutableListOf()

    list.add(SimplificationFormula("Distributive Division", { div(x, mul(x.new(), y.new())) }, { ExpressionConstants.mul(
        *listOf(listOf(it[0]), (it[1] as Operation).operands.map { it1->it1.invert() }.toList()).flatten().toTypedArray()) }, true))

    list.add(SimplificationFormula("Calculating Numeric Values", { x }, { calc(it.operator,
        it.operands.toList()
    ) }, true))

    list.add(SimplificationFormula("Convert to Addition from Subtraction", { ExpressionConstants.sub(x, y) }, { applySub(it.operands.toList()) }))

    list.add(SimplificationFormula("Convert to Multiplication from Division", { ExpressionConstants.div(x, y) }, {
        ExpressionConstants.mul(
        it[0], it[1].invert()) }))

    list.add(SimplificationFormula("Communicative Addition", { add(y.new(), add(x, y))}, { applyCommAdd(it.operands.toList()) }))

    list.add(SimplificationFormula("Communicative Multiplication", { mul(y.new(), mul(x, y))}, { applyCommMul(it.operands.toList()) }))

    list.add(SimplificationFormula("Distributive Multiplication", { filterMulAdd(it) }, { applyMulAdd(it.operands.toList()) }))

    list.add(SimplificationFormula("Common Negation", { ExpressionConstants.mul(x, y) }, {
        if(it.operands.count { it1->it1.isNegative() }.mod(2)==1)  mul(
            *it.operands.map { it1->it1.positive() }.toTypedArray()).negate() else mul(
            *it.operands.map { it1->it1.positive() }.toTypedArray())}))

    list.add(SimplificationFormula("Power of Power", { ExpressionConstants.pow(x.new(), pow(x, y)) }, { applyPowPow(it.operands.toList()) }))

    list.add(SimplificationFormula("Power from Multiplication", { ExpressionConstants.mul(x, y) }, { applyPowMul(it.operands.toList()) }))

    list.add(SimplificationFormula("Simplify Addition", { add(x, y) }, { applySimAdd(
        it.operands.toList()
    ) }))

    list.add(SimplificationFormula("Simplify Power, Multiplication and Division", { div(x, pow(x.new(), y.new())) }, { applyDivPowMul(
        it.operands.toList()
    ) }))

    list.add(SimplificationFormula("Simplify Power, Multiplication and Division", { mul(x, pow(x.new(), y.new())) }, { applyDivPowMul(
        it.operands.toList()
    ) }))

    list.add(SimplificationFormula("Simplify Power, Multiplication and Division", { mul(x, div(x.new(), y.new())) }, { applyDivPowMul(
        it.operands.toList()
    ) }))

    return list
}

fun calc(op:Operators, list:List<out Operand>): Operand {
    val consts = list.filter { hasValue(it) }
    val vars = list.filter { !hasValue(it) }.toMutableList()

    return if(consts.isNotEmpty()) {

        when (val num = getValue(
            when(consts.size) {
                1 -> consts[0]
                else -> Operation(op, *consts.toTypedArray())
            }
        )) {
            is Int -> vars.add(IntegerLiteral(num))
            is Double -> vars.add(DecimalLiteral(num))
        }
        if(vars.size == 1) {
            vars[0]
        } else {
            op(op, vars)
        }
    } else {
        op(op, list)
    }
}

fun filterMulAdd(it:List<out Operand>): Operand {
    return mul(y.new(), add(x, y))
}

fun applySub(it:List<out Operand>): Operand {
    return add(
        it[0], it[1].negate())
}

fun applyCommAdd(it:List<out Operand>): Operand {
    return add(
        *getFlatOperands(add(*it.toTypedArray()) as Operation).toTypedArray() )
}

fun applyCommMul(it:List<out Operand>): Operand {
    return mul(
        *getFlatOperands(mul(*it.toTypedArray()) as Operation).toTypedArray() )
}

fun getFlatOperands(operation: Operation): List<Operand> {
    return operation.operands.flatMap { if(it is Operation && it.operator==operation.operator) getFlatOperands(it) else listOf(it) }
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


fun applyPowPow(list:List<out Operand>): Operand {
    val power = list[0] as Operation
    val powOut = mul(power[1], list[1])
    return  pow(power[0], powOut)
}

fun applyPowMul(it:List<out Operand>): Operand {
    val map: MutableMap<Operand, Operand> = mutableMapOf()
    for(operand in it) {
        val validKey = map.keys.filter { key->operand.deepEquals(key) }
        if(validKey.isNotEmpty()) {
            map[validKey[0]] = map[validKey[0]]?.let { it1 -> add(it1 ,one)}!!
        } else {
            map[operand] = one
        }
    }
    return map.keys.map { if(map[it] ==null) it else if(getConst<Int>(map[it]!!) != 1) map[it]
        ?.let { it2 ->
            pow(it,
                it2
            )
        } else it }.toTypedArray()?.let { it3 -> mul(*it3 as Array<out Operand>) }
}

fun applySimAdd(list:List<out Operand>): Operand {
    val map: MutableMap<Operand, Operand> = mutableMapOf()
    val operation = list.filterIsInstance<Operation>()

    val mults = operation.filter { it.operator == Operators.mul }

    val negBases = operation.filter { !mults.contains(it) && it.operator==Operators.neg }

    val negs = negBases.map { it.operands[0] }

    val negOps = negs.filterIsInstance<Operation>()

    val negMults = negOps.filter { it.operator == Operators.mul }

    val negOther = negs.filter { !negMults.contains(it) }

    val other = list.filter { !mults.contains(it) && !negBases.contains(it) }

    for(o in other) {
        val validKey = map.keys.filter { key->o.deepEquals(key) }
        if(validKey.isEmpty()) {
            map[o] = one
        } else {
            val list = if((map[validKey[0]] is Operation))
                ArrayList((map[validKey[0]] as Operation).operands.toMutableList())
            else
                mutableListOf(map[validKey[0]])
            list.add(one)
            map[validKey[0]] = add(*list.toTypedArray())
        }
    }

    for(o in negOther) {
        val validKey = map.keys.filter { key->o.deepEquals(key) }
        if(validKey.isEmpty()) {
            map[o] = one.negate()
        } else {
            val list = if((map[validKey[0]] is Operation))
                ArrayList((map[validKey[0]] as Operation).operands.toMutableList())
            else
                mutableListOf(map[validKey[0]])
            list.add(one.negate())
            map[validKey[0]] = add(*list.toTypedArray())
        }
    }

    for(m in mults) {
        val conts = m.operands.filter { isConst(it) }
        val vars = m.operands.filter { !isConst(it) }
        if(vars.isEmpty()) {
            if(map[one]==null) {
                map[one] = mul(*conts.toTypedArray())
            } else {
                val list = if(map[one] is Operation)
                    ArrayList((map[one] as Operation).operands.toMutableList())
                else
                    mutableListOf(map[one])
                list.addAll(conts)
                map[one] = add(*list.toTypedArray())
            }
        } else {
            val op = if(vars.size==1) vars[0] else mul(*vars.toTypedArray())
            val validKey = map.keys.filter { key->op.deepEquals(key) }
            if(validKey.isEmpty()) {
                map[op] = mul(*conts.toTypedArray())
            } else {
                val list = if((map[validKey[0]] is Operation))
                    ArrayList((map[validKey[0]] as Operation).operands.toMutableList())
                else
                    mutableListOf(map[validKey[0]])
                list.addAll(conts)
                map[validKey[0]] = add(*list.toTypedArray())
            }
        }
    }

    for(m in negMults) {
        val conts = m.operands.filter { isConst(it) }
        val vars = m.operands.filter { !isConst(it) }
        if(vars.isEmpty()) {
            if(map[one]==null) {
                map[one] = mul(*conts.toTypedArray()).negate()
            } else {
                val list = if(map[one] is Operation)
                    ArrayList((map[one] as Operation).operands.toMutableList())
                else
                    mutableListOf(map[one])
                list.addAll(conts)
                map[one] = add(*list.toTypedArray()).negate()
            }
        } else {
            val op = if(vars.size==1) vars[0] else mul(*vars.toTypedArray())
            val validKey = map.keys.filter { key->op.deepEquals(key) }
            if(validKey.isEmpty()) {
                map[op] = mul(*conts.toTypedArray()).negate()
            } else {
                val list = if((map[validKey[0]] is Operation))
                    ArrayList((map[validKey[0]] as Operation).operands.toMutableList())
                else
                    mutableListOf(map[validKey[0]])
                list.addAll(conts.map { it.negate() })
                map[validKey[0]] = add(*list.toTypedArray())
            }
        }
    }


    return map.keys.map { if(map[it] ==null) it else if(getConst<Int>(map[it]!!) != 1) map[it]
        ?.let { it2 ->
            mul(it,
                it2
            )
        } else it }?.let { it3 -> add(*it3.toTypedArray() as Array<out Operand>) }

}


fun applyDivPowMul(list:List<out Operand>): Operand {
    val map: MutableMap<Operand, Operand> = mutableMapOf()

    val operation = list.filterIsInstance<Operation>()
    val powers = operation.filter { it.operator==Operators.pow }
    val inverts = operation.filter { it.operator==Operators.div }.filter { isConst(it.operands[0], 1) }
    val invertsPow = inverts.filter { it.operands[1] is Operation && (it.operands[1] as Operation).operator == Operators.pow }
    val invertOther = inverts.filter { !invertsPow.contains(it) }
    val other = list.filter { !powers.contains(it) && !inverts.contains(it) }

    powers.forEach {
        val validKey = map.keys.filter { key->(it.operands[0]).deepEquals(key) }
        if(validKey.isEmpty()) {
            map[it[0]] = it[1]
        } else {
            val list: MutableList<Operand> = if(map[validKey[0]] is Operation)
                    (map[validKey[0]] as Operation).operands.toMutableList()
            else
                map[validKey[0]]?.let { it1 -> mutableListOf(it1) } ?: mutableListOf()
            list.add(IntegerLiteral(getConst(it.operands[1]) ?: 1) as Operand)
            map[validKey[0]] = add(*list.toTypedArray())
        }
    }

    for(op in invertsPow) {
        val validKey = map.keys.filter { key->(op.operands[1] as Operation).operands[0].deepEquals(key) }
        if(validKey.isNotEmpty()) {
            map[validKey[0]] = map[validKey[0]]?.let {sub(it,(op.operands[1] as Operation).operands[1])} ?: zero
        } else {
            map[(op.operands[1] as Operation).operands[0]] = (op.operands[1] as Operation).operands[1]?.negate()
        }
    }

    for(operand in invertOther) {
        val validKey = map.keys.filter { key->operand[1].deepEquals(key) }
        if(validKey.isNotEmpty()) {
            map[validKey[0]] = map[validKey[0]]?.let { sub(it, one) } ?: zero
        } else {
            map[operand.operands[1]] = IntegerLiteral(-1)
        }
    }

    for(operand in other) {
        val validKey = map.keys.filter { key->operand.deepEquals(key) }
        if(validKey.isNotEmpty()) {
            map[validKey[0]] = map[validKey[0]]?.let { add(it, one) } ?: zero
        } else {
            map[operand] = one
        }
    }

    return map.keys.map { if(map[it] ==null) it else if(getConst<Int>(map[it]!!) != 1) map[it]
        ?.let { it2 ->
            pow(it,
                it2
            )
        } else it }?.let { it3 -> mul(*it3.toTypedArray() as Array<out Operand> ) }

}

fun dot(o1:Operation, o2:Operation, multiplier: List<Operand> ) : Operation{
    return add(*o1.operands.map {
            it1 -> add(*o2.operands.map {
            it2-> mul(*listOf(it1, it2).toTypedArray()) as Operation

    }.toTypedArray()) as Operation
    }.toTypedArray()) as Operation
}

open class Formula<out T: Operand, out U>(val name: String?, val fx:(v:List<@UnsafeVariance T>) -> Operand, val dFx:(v: @UnsafeVariance U) -> Operand)

open class SimplificationFormula(name: String?, fx:(v:List<out Operand>) -> Operand, dFx:(operation: Operation) -> Operand, val firstPriority: Boolean = false): Formula<Operand, Operation>(name, fx, dFx) {
    fun simplify(operand: Operand) : Result? {
        if(operand is Operation) {
            val f = fx(operand.operands.toList())
            val level = getLevel(f, 0)
            val maxLevel = level.keys.maxOf { it }
            if(operand.toOperatorString(maxLevel-1, level) == f.toOperatorString(maxLevel)) {
                val d = dFx(operand)
                if(d.toString() == operand.toString()) return null
                return name?.let { Result(d, operand, d, it) }
            }
        }
        return null
    }

    private fun getLevel(f: Operand, i: Int): Map<Int, List<Operators>> {
        val map: MutableMap<Int, MutableList<Operators>> = mutableMapOf(Pair(i, mutableListOf()))
        if(f is Operation) {
            map[i]?.add(f.operator)
            f.operands.map { getLevel(it, i + 1) }.forEach { it.keys.forEach { key->if(map.containsKey(key)) it[key]?.let { it1 ->
                map[key]?.addAll(
                    it1
                )
            } else map[key] = it[key]?.toMutableList()!!
            } }
        }
        return map
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Result

        if (operand.toString() != other.operand.string()) return false

        return true
    }

    override fun hashCode(): Int {
        return operand.toString().hashCode()
    }

}

class Derive(val left: Operand, val right: Operand, val desc: String)
class Results: LinkedHashSet<Result>() {
    override fun toString(): String {
        if(isEmpty()) return ""
        val maxOperandSize = maxOf { result->result.str.length } + 1
        forEach { result -> result.maxOperandSize = maxOperandSize }
        return joinToString("\r\n")
    }
}