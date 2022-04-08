package math.lang

import math.lang.common.Differentiate
import math.lang.common.ExpressionConstants.Companion.a
import math.lang.common.ExpressionConstants.Companion.add
import math.lang.common.ExpressionConstants.Companion.constIn
import math.lang.common.ExpressionConstants.Companion.cos
import math.lang.common.ExpressionConstants.Companion.cosh
import math.lang.common.ExpressionConstants.Companion.cot
import math.lang.common.ExpressionConstants.Companion.csc
import math.lang.common.ExpressionConstants.Companion.d
import math.lang.common.ExpressionConstants.Companion.dfx1
import math.lang.common.ExpressionConstants.Companion.dfx2
import math.lang.common.ExpressionConstants.Companion.div
import math.lang.common.ExpressionConstants.Companion.e
import math.lang.common.ExpressionConstants.Companion.eq
import math.lang.common.ExpressionConstants.Companion.func
import math.lang.common.ExpressionConstants.Companion.fx1
import math.lang.common.ExpressionConstants.Companion.fx2
import math.lang.common.ExpressionConstants.Companion.icos
import math.lang.common.ExpressionConstants.Companion.icot
import math.lang.common.ExpressionConstants.Companion.icsc
import math.lang.common.ExpressionConstants.Companion.inv
import math.lang.common.ExpressionConstants.Companion.isec
import math.lang.common.ExpressionConstants.Companion.isin
import math.lang.common.ExpressionConstants.Companion.itan
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
import math.lang.common.Operand
import math.lang.common.Operation
import math.lang.common.Operators

private val DIFFERENTIATION_FORMULA : MutableList<Formula> = getAllDifferentialEquation()

private val RULES : MutableList<Rule> = getAllRule()

fun getAllDifferentialEquation():MutableList<Formula> {
    val list:MutableList<Formula> = mutableListOf()

    list.add(Formula(null, a, zero))
    list.add(Formula(null, x, one))
    list.add(Formula(null, pow(x, a), mul(a, pow(x, sub(a, one)))))
    list.add(Formula(null, pow(a, x), mul(pow(a,x), ln(a))))
    list.add(Formula(null, pow(e, x), pow(e, x)))
    list.add(Formula(null, log(x, a), inv(mul(x, ln(a)))))
    list.add(Formula(null, ln(x), inv(x)))
    list.add(Formula(null, sin(x), cos(x)))
    list.add(Formula(null, cos(x), neg(sin(x))))
    list.add(Formula(null, tan(x), sqr(sec(x))))
    list.add(Formula(null, cot(x), neg(sqr(csc(x)))))
    list.add(Formula(null, sec(x), mul(sec(x), tan(x))))
    list.add(Formula(null, csc(x), inv(mul(csc(x), cot(x)))))
    list.add(Formula(null, isin(x), inv(sqrt(sub(one, sqr(x))))))
    list.add(Formula(null, icos(x), neg(inv(sqrt(sub(one, sqr(x)))))))
    list.add(Formula(null, itan(x), inv(add(one, sqr(x)))))
    list.add(Formula(null, isec(x), inv(mul(x, sqrt(sub(sqr(x), one))))))
    list.add(Formula(null, icsc(x), neg(inv(mul(x, sqrt(sub(sqr(x), one)))))))
    list.add(Formula(null, icot(x), neg(inv(add(one, sqr(x))))))
    list.add(Formula(null, sinh(x), cosh(x)))
    list.add(Formula(null, cosh(x), sinh(x)))

    return list
}

fun getAllRule() : MutableList<Rule> {
    val list:MutableList<Rule> = mutableListOf()

    val funcOfFunc = func(fx2)

    list.add(AdditionRule(add(fx1, fx2), add(dfx1, dfx2)))
    list.add(SubtractionRule(sub(fx1, fx2), sub(dfx1, dfx2)))
    list.add(ProductiveRule(mul(fx1, fx2), add(mul(fx1, dfx2), mul(fx2, dfx2))))
    list.add(QuotientRule(div(fx1, fx2), div(sub(mul(fx2, dfx2), mul(fx1, dfx2)), sqr(fx2))))
    list.add(ChainRule(funcOfFunc, mul(d(funcOfFunc), dfx2)))

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

open class Formula(val name: String?, val fx: Operand, val dFx: Operand) {
    open fun differentiate(operand: Operand): Results {
        val results: Results = Results()
        return if(fx.toTypeString() == operand.toTypeString()) {
            val usedConst = constIn(operand)
            if(usedConst != null) {
                results.add(Result(replace(dFx, a, usedConst), this))
            } else {
                results.add(Result(dFx, this))
            }
            results
        } else {
            results
        }
    }

    override fun toString(): String {
        return "d($fx)/dx = $dFx ${if(name==null) "" else "[$name]"}".trim()
    }
}

open abstract class Rule(name: String, fx: Operand, dFx: Operand) : Formula(name, fx, dFx)

class AdditionRule(fx: Operand, dFx: Operand): Rule("Addition Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if(operand is Operation) {
            if(operand.operator== Operators.add) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val result: Results = Results()
                var ops:MutableList<Operand> = operand.operands.map { o-> Differentiate(operand = o) }.toMutableList()
                result.add(Result(add(*ops.toTypedArray()), this))
                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        ops[i] = r.operand
                        result.add(Result(add(*ops.toTypedArray()), r.formulaApplied, r.assumption))
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

class SubtractionRule(fx: Operand, dFx: Operand): Rule("Subtraction Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if(operand is Operation) {
            if(operand.operator== Operators.sub) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val result: Results = Results()
                var ops:MutableList<Operand> = operand.operands.map { o-> Differentiate(operand = o) }.toMutableList()
                result.add(Result(sub(ops[0], ops[1]), this))
                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        ops[i] = r.operand
                        result.add(Result(sub(ops[0], ops[1]), r.formulaApplied, r.assumption))
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

class ProductiveRule(fx: Operand, dFx: Operand): Rule("Productive Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if(operand is Operation) {
            if(operand.operator== Operators.mul) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val result: Results = Results()
                var ops:MutableList<Operand> = operand.operands.indices.map { i-> run {
                    val l:MutableList<Operand> = operand.operands.toMutableList()
                    l[i] = Differentiate(operand = operand.operands[i])
                    mul(*l.toTypedArray())
                } }.toMutableList()
                result.add(Result(add(*ops.toTypedArray()), this))

                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        val innerOperands: MutableList<Operand> = (ops[i] as Operation).operands.toMutableList()
                        innerOperands[i] = r.operand
                        ops[i] = mul(*innerOperands.toTypedArray())
                        result.add(Result(add(*ops.toTypedArray()), r.formulaApplied, r.assumption))
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

class QuotientRule(fx: Operand, dFx: Operand): Rule("Quotient Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results{
        if(operand is Operation) {
            if(operand.operator== Operators.div) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val result: Results = Results()
                var ops:MutableList<Operand> = operand.operands.indices.map { i-> run {
                    val l:MutableList<Operand> = operand.operands.toMutableList()
                    l[i] = Differentiate(operand = operand.operands[i])
                    mul(*l.toTypedArray())
                } }.toMutableList()
                result.add(Result(div(sub(*ops.toTypedArray()), sqr(operand.operands[1])), this))

                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        val innerOperands: MutableList<Operand> = (ops[i] as Operation).operands.toMutableList()
                        innerOperands[i] = r.operand
                        ops[i] = mul(*innerOperands.toTypedArray())
                        result.add(Result(div(sub(*ops.toTypedArray()), sqr(operand.operands[1])), r.formulaApplied, r.assumption))
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

class ChainRule(fx: Operand, dFx: Operand): Rule("Chain Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if(operand is Operation) {
            if(operand.operator.operandCount==1) {
                val result1:Results = diff(operand.operands[0])
                val result2:List<Result> = diff(Operation(operand.operator, x)).map { result-> Result(replace(result.operand, x, operand.operands[0]), result.formulaApplied) }
                val results:List<List<Result>> = listOf(result1, result2)
                val result: Results = Results()

                var ops:MutableList<Operand> = mutableListOf()
                ops.add(Differentiate(operand = operand.operands[0]))
                ops.add(Differentiate(operand = Operation(operand.operator, y)))
                result.add(Result(mul(ops[0], ops[1]), this, eq(y, operand.operands[0])))


                for(i:Int in results.indices) {
                    for(r:Result in results[i]) {
                        ops[i] = r.operand
                        result.add(Result(mul(ops[0], ops[1]), r.formulaApplied, r.assumption))
                    }
                }
                return result
            }
        }
        return Results()
    }
}

class Result(val operand: Operand, val formulaApplied: Formula, val assumption: Operation? = null ) {
    var maxOperandSize: Int = 0
    var str: String = operand.toString()

    override fun toString(): String {

        val initStr = "$preAppeneder$operand${" ".repeat(maxOperandSize-str.length)}$postAppeneder"
        return "$initStr${formulaApplied}${if(assumption != null) " | assuming $assumption" else ""}"
    }
    private companion object {
        const val preAppeneder = "= "
        const val postAppeneder = "| applying "
    }
}

class Results: ArrayList<Result>() {
    override fun toString(): String {
        val maxOperandSize = maxOf { result->result.str.length } + 1
        forEach { result -> result.maxOperandSize = maxOperandSize }
        return joinToString("\r\n")
    }
}