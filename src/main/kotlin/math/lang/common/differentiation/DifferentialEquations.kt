package math.lang

import math.lang.common.*
import math.lang.common.ExpressionConstants.Companion.a
import math.lang.common.ExpressionConstants.Companion.add
import math.lang.common.ExpressionConstants.Companion.constIn
import math.lang.common.ExpressionConstants.Companion.cos
import math.lang.common.ExpressionConstants.Companion.cosh
import math.lang.common.ExpressionConstants.Companion.cot
import math.lang.common.ExpressionConstants.Companion.cosec
import math.lang.common.ExpressionConstants.Companion.d
import math.lang.common.ExpressionConstants.Companion.dfx1
import math.lang.common.ExpressionConstants.Companion.dfx2
import math.lang.common.ExpressionConstants.Companion.div
import math.lang.common.ExpressionConstants.Companion.e
import math.lang.common.ExpressionConstants.Companion.eq
import math.lang.common.ExpressionConstants.Companion.func
import math.lang.common.ExpressionConstants.Companion.fx1
import math.lang.common.ExpressionConstants.Companion.fx2
import math.lang.common.ExpressionConstants.Companion.acos
import math.lang.common.ExpressionConstants.Companion.acot
import math.lang.common.ExpressionConstants.Companion.acosec
import math.lang.common.ExpressionConstants.Companion.inv
import math.lang.common.ExpressionConstants.Companion.asec
import math.lang.common.ExpressionConstants.Companion.asin
import math.lang.common.ExpressionConstants.Companion.atan
import math.lang.common.ExpressionConstants.Companion.ln
import math.lang.common.ExpressionConstants.Companion.log
import math.lang.common.ExpressionConstants.Companion.mul
import math.lang.common.ExpressionConstants.Companion.neg
import math.lang.common.ExpressionConstants.Companion.one
import math.lang.common.ExpressionConstants.Companion.pow
import math.lang.common.ExpressionConstants.Companion.replace
import math.lang.common.ExpressionConstants.Companion.sec
import math.lang.common.ExpressionConstants.Companion.sin
import math.lang.common.ExpressionConstants.Companion.sinh
import math.lang.common.ExpressionConstants.Companion.sqr
import math.lang.common.ExpressionConstants.Companion.sqrt
import math.lang.common.ExpressionConstants.Companion.sub
import math.lang.common.ExpressionConstants.Companion.tan
import math.lang.common.ExpressionConstants.Companion.x
import math.lang.common.ExpressionConstants.Companion.y
import math.lang.common.ExpressionConstants.Companion.zero

private val DIFFERENTIATION_FORMULA : MutableList<Formula> = getAllDifferentialEquation()

private val RULES : MutableList<Rule> = getAllRule()

fun getAllDifferentialEquation():MutableList<Formula> {
    val list:MutableList<Formula> = mutableListOf()

    list.add(Formula("Constant Derivative is zero", { a }, { zero }))
    list.add(Formula("derivative of x is one", { x }, { one }))
    list.add(Formula("x power constant", { pow(x, a) }, { mul(a, pow(x, sub(a, one))) }))
    list.add(Formula("constant power x", { pow(a, x) }, { mul(pow(a, x), ln(a)) }))
    list.add(Formula("e power x", { pow(e, x) }, { pow(e, x) }))
    list.add(Formula("log x base a", { log(x, a) }, { inv(mul(x, ln(a))) }))
    list.add(Formula("log x base e", { ln(x) }, { inv(x) }))
    list.add(Formula("sin x", { sin(x) }, { cos(x) }))
    list.add(Formula("cos x", { cos(x) }, { neg(sin(x)) }))
    list.add(Formula("tan x", { tan(x) }, { sqr(sec(x)) }))
    list.add(Formula("cot x", { cot(x) }, { neg(sqr(cosec(x))) }))
    list.add(Formula("sec x", { sec(x) }, { mul(sec(x), tan(x)) }))
    list.add(Formula("cosec x", { cosec(x) }, { inv(mul(cosec(x), cot(x))) }))
    list.add(Formula("arc sin x", { asin(x) }, { inv(sqrt(sub(one, sqr(x)))) }))
    list.add(Formula("arc cos x", { acos(x) }, { neg(inv(sqrt(sub(one, sqr(x))))) }))
    list.add(Formula("arc tan x", { atan(x) }, { inv(add(one, sqr(x))) }))
    list.add(Formula("arc sec x", { asec(x) }, { inv(mul(x, sqrt(sub(sqr(x), one)))) }))
    list.add(Formula("arc cosec x", { acosec(x) }, { neg(inv(mul(x, sqrt(sub(sqr(x), one))))) }))
    list.add(Formula("arc cot x", { acot(x) }, { neg(inv(add(one, sqr(x)))) }))
    list.add(Formula("hyperbolic sin x", { sinh(x) }, { cosh(x) }))
    list.add(Formula("hyperbolic cos x", { cosh(x) }, { sinh(x) }))

    return list
}

fun getAllRule() : MutableList<Rule> {
    val list:MutableList<Rule> = mutableListOf()

    val funcOfFunc = func(fx2)

    list.add(AdditionRule({ add(it[0], it[1]) }, { add(d(it[0]), d(it[1])) }))
    list.add(SubtractionRule({ sub(it[0], it[1]) }, { sub(d(it[0]), d(it[1])) }))
    list.add(ProductiveRule({ mul(it[0], it[1]) }, { add(mul(it[0], d(it[1])), mul(it[1], d(it[0]))) }))
    list.add(QuotientRule({ div(it[0], it[1]) }, { div(sub(mul(it[1], d(it[0])), mul(it[0], d(it[1]))), sqr(it[1])) }))
    list.add(PowerRule({ pow(it[0], it[1]) }, { mul(add(mul(ln(it[0]), d(it[1])), mul(div(it[1], it[0]), d(it[0]))), pow(it[0], it[1])) }))
    list.add(ChainRule({ funcOfFunc }, { mul(d(funcOfFunc), dfx2) }))

    return list
}

fun d(operand: Operand): String {
    val results: Results = diff(operand)
    return "d($operand)/dx\r\n${results}"
}

fun diff(operand: Operand): Results {
    for(formula:Formula in DIFFERENTIATION_FORMULA) {
        val result : Results = formula.differentiate(operand)
        if(result.isNotEmpty()) {
            return result
        }
    }
    for(formula:Formula in RULES) {
        val result : Results = formula.differentiate(operand)
        if(result.isNotEmpty()) {
            return result
        }
    }
    return Results()
}

open class Formula(val name: String?, val fx:(v:List<Variable>) -> Operand, val dFx:(v:List<Variable>) ->  Operand) {
    open fun differentiate(operand: Operand): Results {
        val results: Results = Results()
        return if(fx(listOf()).toTypeString() == operand.toTypeString()) {
            val usedConst = constIn(operand)
            if(usedConst != null) {
                results.add(Result(replace(dFx(listOf()), a, usedConst), fx(listOf()), dFx(listOf()), name ?: ""))
            } else {
                results.add(Result(dFx(listOf()), fx(listOf()), dFx(listOf()), name ?: ""))
            }
            results
        } else {
            results
        }
    }

    override fun toString(): String {
        return "d(${fx(listOf())})/dx = ${dFx(listOf())} ${if(name==null) "" else "[$name]"}".trim()
    }
}

open abstract class Rule(name: String, fx:(v:List<Variable>) ->  Operand, dFx: (v:List<Variable>) -> Operand) : Formula(name, fx, dFx)

class AdditionRule(fx:(v:List<Variable>) ->  Operand, dFx:(v:List<Variable>) ->  Operand): Rule("Addition Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if(operand is Operation) {
            if(operand.operator== Operators.add) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val vars: List<Variable> = operand.operands.map { y.new() }
                val result: Results = Results()
                var ops:MutableList<Operand> = operand.operands.map { o-> Differentiate(operand = o) }.toMutableList()
                result.add(Result(add(*ops.toTypedArray()), this.fx(vars), this.dFx(vars), this.name ?: "", vars.indices.map { eq(vars[it], operand.operands[it]) }))
                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        ops[i] = r.operand
                        result.add(Result(add(*ops.toTypedArray()), r.fx, r.dFx, r.formulaName, r.assumptions, r.derive))
                    }
                }
                return result
            } else {
                return Results()
            }
        }else {
            return Results()
        }
    }
}

class SubtractionRule(fx: (v:List<Variable>) -> Operand, dFx: (v:List<Variable>) -> Operand): Rule("Subtraction Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if(operand is Operation) {
            if(operand.operator== Operators.sub) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val vars: List<Variable> = operand.operands.map { y.new() }
                val result: Results = Results()
                var ops:MutableList<Operand> = operand.operands.map { o-> Differentiate(operand = o) }.toMutableList()
                result.add(Result(sub(ops[0], ops[1]), fx(vars), dFx(vars), name ?: "", vars.indices.map { eq(vars[it], operand.operands[it]) }))
                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        ops[i] = r.operand
                        result.add(Result(sub(ops[0], ops[1]), r.fx, r.dFx, r.formulaName, r.assumptions, r.derive))
                    }
                }
                return result
            } else {
                return Results()
            }
        }else {
            return Results()
        }
    }
}

class ProductiveRule(fx: (v:List<Variable>) -> Operand, dFx: (v:List<Variable>) -> Operand): Rule("Productive Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if(operand is Operation) {
            if(operand.operator== Operators.mul) run {
                val results: List<Results> = operand.operands.map { diff(it) }
                val result: Results = Results()
                val vars: List<Variable> = operand.operands.map { y.new() }
                var ops:MutableList<Operand> = operand.operands.indices.map { run {
                    val l:MutableList<Operand> = operand.operands.toMutableList()
                    l[it] = Differentiate(operand = operand.operands[it])
                    mul(*l.toTypedArray())
                } }.toMutableList()
                result.add(Result(add(*ops.toTypedArray()), fx(vars), dFx(vars), name ?: "", vars.indices.map { eq(vars[it], operand.operands[it]) }))

                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        val innerOperands: MutableList<Operand> = (ops[i] as Operation).operands.toMutableList()
                        innerOperands[i] = r.operand
                        ops[i] = mul(*innerOperands.toTypedArray())
                        result.add(Result(add(*ops.toTypedArray()), r.fx, r.dFx, r.formulaName, r.assumptions, r.derive))
                    }
                }
                return result
            } else {
                return Results()
            }
        }else {
            return Results()
        }
    }
}

class QuotientRule(fx: (v:List<Variable>) -> Operand, dFx: (v:List<Variable>) -> Operand): Rule("Quotient Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results{
        if(operand is Operation) {
            if(operand.operator== Operators.div) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val vars: List<Variable> = operand.operands.map { y.new() }
                val result: Results = Results()
                var ops:MutableList<Operand> = operand.operands.indices.map { i-> run {
                    val l:MutableList<Operand> = operand.operands.toMutableList()
                    l[i] = Differentiate(operand = operand.operands[i])
                    mul(*l.toTypedArray())
                } }.toMutableList()
                result.add(Result(div(sub(*ops.toTypedArray()), sqr(operand.operands[1])), fx(vars), dFx(vars), name ?: "", vars.indices.map { eq(vars[it], operand.operands[it]) }))

                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        val innerOperands: MutableList<Operand> = (ops[i] as Operation).operands.toMutableList()
                        innerOperands[i] = r.operand
                        ops[i] = mul(*innerOperands.toTypedArray())
                        result.add(Result(div(sub(*ops.toTypedArray()), sqr(operand.operands[1])), r.fx, r.dFx, r.formulaName, r.assumptions, r.derive))
                    }
                }
                return result
            } else {
                return Results()
            }
        }else {
            return Results()
        }
    }
}

class PowerRule(fx: (v:List<Variable>) -> Operand, dFx: (v:List<Variable>) -> Operand): Rule("Power Rule", fx, dFx) {

    override fun differentiate(operand: Operand): Results {
        if(operand is Operation) {
            if(operand.operator== Operators.pow) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val additionOp1Multiplier : Operation = div(operand.operands[1], operand.operands[0]) as Operation
                val additionOp2Multiplier : Operation = ln(operand.operands[0]) as Operation
                val multipliers: List<Operation> = listOf(additionOp1Multiplier, additionOp2Multiplier)
                val result: Results = Results()

                val newVar1: Variable = y.new()
                val newVar2: Variable = y.new()
                var ops:MutableList<Operand> = mutableListOf()
                ops.add(mul(additionOp1Multiplier, Differentiate(operand = operand.operands[0])))
                ops.add(mul(additionOp2Multiplier, Differentiate(operand = operand.operands[1])))

                
                result.add(Result(mul(add(*ops.toTypedArray()), operand), fx(listOf(newVar1, newVar2)), dFx(listOf(newVar1, newVar2)), name ?: "", listOf(eq(newVar1, operand.operands[0]), eq(newVar2, operand.operands[1])), calc(newVar1, newVar2)))

                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {

                        ops[i] = mul(multipliers[i], r.operand)
                        result.add(Result(mul(add(*ops.toTypedArray()), operand), r.fx, r.dFx, r.formulaName, r.assumptions, r.derive))
                    }
                }
                return result
            } else {
                return Results()
            }
        }else {
            return Results()
        }
    }

    companion object {
        val calc:(Variable, Variable)-> List<Derive> = {newVar1:Variable, newVar2:Variable->
            val newVar : Variable = y.new()
            listOf(
                Derive(newVar, pow(newVar1, newVar2), "Assumption"),
                Derive(ln(newVar), mul(newVar2, ln(newVar1)), "Taking Log in Both Side"),
                Derive(mul(inv(newVar), Differentiate(operand = newVar)), add(mul(div(newVar2, newVar1), Differentiate(operand = newVar1)), mul(ln(newVar1), Differentiate(operand = newVar2))), "Differentiating Both Side"),
                Derive(Differentiate(operand = newVar), mul(add(mul(div(newVar2, newVar1), Differentiate(operand = newVar1)), mul(ln(newVar1), Differentiate(operand = newVar2))), newVar), "Multiplied by $newVar both side")
            )
        }
    }
}


class ChainRule(fx: (v:List<Variable>) -> Operand, dFx: (v:List<Variable>) -> Operand): Rule("Chain Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if(operand is Operation) {
            if(operand.operator.operandCount==1) {
                val result1:Results = diff(operand.operands[0])
                val result2:List<Result> = diff(Operation(operand.operator, x)).map { result-> Result(replace(result.operand, x, operand.operands[0]), result.fx, result.dFx, result.formulaName) }
                val results:List<List<Result>> = listOf(result1, result2)
                val result: Results = Results()

                val newVar: Variable = y.new()
                var ops:MutableList<Operand> = mutableListOf()
                ops.add(Differentiate(operand = operand.operands[0]))
                ops.add(Differentiate(operand = Operation(operand.operator, newVar)))
                result.add(Result(mul(ops[0], ops[1]), fx(listOf()), dFx(listOf()), name ?: "", listOf(eq(newVar, operand.operands[0]))))


                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        ops[i] = r.operand
                        result.add(Result(mul(ops[0], ops[1]), r.fx, r.dFx, r.formulaName, r.assumptions, r.derive))
                    }
                }
                return result
            }
        }
        return Results()
    }
}

class Result(val operand: Operand, val fx: Operand, val dFx: Operand, val formulaName: String, val assumptions: List<Operation> = listOf(), val derive: List<Derive> = listOf()) {
    var maxOperandSize: Int
    var str: String

    init {
        try {
            maxOperandSize= 0
            str = operand.toString()
        } catch (t:Throwable) {
            t.printStackTrace()
            throw t
        }
    }

    override fun toString(): String {

        val initStr = "$preAppeneder$operand${" ".repeat(maxOperandSize-str.length)}$postAppeneder"
        return "$initStr$fx=$dFx${if(assumptions.isNotEmpty()) " | assuming $assumptions" else ""}"
    }
    private companion object {
        const val preAppeneder = "= "
        const val postAppeneder = "| applying "
    }
}

class Derive(val left:Operand, val right: Operand, val desc: String)

class Results: ArrayList<Result>() {
    override fun toString(): String {
        val maxOperandSize = maxOf { result->result.str.length } + 1
        forEach { result -> result.maxOperandSize = maxOperandSize }
        return joinToString("\r\n")
    }
}