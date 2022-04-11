package math.lang

import math.lang.common.*
import math.lang.common.ExpressionConstants.Companion.a
import math.lang.common.ExpressionConstants.Companion.acos
import math.lang.common.ExpressionConstants.Companion.acosec
import math.lang.common.ExpressionConstants.Companion.acot
import math.lang.common.ExpressionConstants.Companion.add
import math.lang.common.ExpressionConstants.Companion.asec
import math.lang.common.ExpressionConstants.Companion.asin
import math.lang.common.ExpressionConstants.Companion.atan
import math.lang.common.ExpressionConstants.Companion.constIn
import math.lang.common.ExpressionConstants.Companion.cos
import math.lang.common.ExpressionConstants.Companion.cosec
import math.lang.common.ExpressionConstants.Companion.cosh
import math.lang.common.ExpressionConstants.Companion.cot
import math.lang.common.ExpressionConstants.Companion.d
import math.lang.common.ExpressionConstants.Companion.dfx2
import math.lang.common.ExpressionConstants.Companion.div
import math.lang.common.ExpressionConstants.Companion.e
import math.lang.common.ExpressionConstants.Companion.eq
import math.lang.common.ExpressionConstants.Companion.func
import math.lang.common.ExpressionConstants.Companion.fx2
import math.lang.common.ExpressionConstants.Companion.inv
import math.lang.common.ExpressionConstants.Companion.isConst
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
import math.lang.common.ExpressionConstants.Companion.varIn
import math.lang.common.ExpressionConstants.Companion.x
import math.lang.common.ExpressionConstants.Companion.y
import math.lang.common.ExpressionConstants.Companion.zero

private val DIFFERENTIATION_Differentiation_FORMULA: MutableList<DifferentiationFormula> = getAllDifferentialEquation()

private val RULES: MutableList<Rule> = getAllRule()

fun getAllDifferentialEquation(): MutableList<DifferentiationFormula> {
    val list: MutableList<DifferentiationFormula> = mutableListOf()

    list.add(DifferentiationFormula("Constant Derivative is zero", { a }, { zero }))
    list.add(DifferentiationFormula("derivative of x is one", { x }, { one }))
    list.add(DifferentiationFormula("x power constant", { pow(x, a) }, { mul(a, pow(x, sub(a, one))) }))
    list.add(DifferentiationFormula("constant power x", { pow(a, x) }, { mul(pow(a, x), ln(a)) }))
    list.add(DifferentiationFormula("e power x", { pow(e, x) }, { pow(e, x) }))
    list.add(DifferentiationFormula("log x base a", { log(x, a) }, { inv(mul(x, ln(a))) }))
    list.add(DifferentiationFormula("log x base e", { ln(x) }, { inv(x) }))
    list.add(DifferentiationFormula("sin x", { sin(x) }, { cos(x) }))
    list.add(DifferentiationFormula("cos x", { cos(x) }, { neg(sin(x)) }))
    list.add(DifferentiationFormula("tan x", { tan(x) }, { sqr(sec(x)) }))
    list.add(DifferentiationFormula("cot x", { cot(x) }, { neg(sqr(cosec(x))) }))
    list.add(DifferentiationFormula("sec x", { sec(x) }, { mul(sec(x), tan(x)) }))
    list.add(DifferentiationFormula("cosec x", { cosec(x) }, { inv(mul(cosec(x), cot(x))) }))
    list.add(DifferentiationFormula("arc sin x", { asin(x) }, { inv(sqrt(sub(one, sqr(x)))) }))
    list.add(DifferentiationFormula("arc cos x", { acos(x) }, { neg(inv(sqrt(sub(one, sqr(x))))) }))
    list.add(DifferentiationFormula("arc tan x", { atan(x) }, { inv(add(one, sqr(x))) }))
    list.add(DifferentiationFormula("arc sec x", { asec(x) }, { inv(mul(x, sqrt(sub(sqr(x), one)))) }))
    list.add(DifferentiationFormula("arc cosec x", { acosec(x) }, { neg(inv(mul(x, sqrt(sub(sqr(x), one))))) }))
    list.add(DifferentiationFormula("arc cot x", { acot(x) }, { neg(inv(add(one, sqr(x)))) }))
    list.add(DifferentiationFormula("hyperbolic sin x", { sinh(x) }, { cosh(x) }))
    list.add(DifferentiationFormula("hyperbolic cos x", { cosh(x) }, { sinh(x) }))

    return list
}

fun getAllRule(): MutableList<Rule> {
    val list: MutableList<Rule> = mutableListOf()

    val funcOfFunc = func(fx2)

    list.add(AdditionRule({ add(it[0], it[1]) }, { add(d(it[0]), d(it[1])) }))
    list.add(SubtractionRule({ sub(it[0], it[1]) }, { sub(d(it[0]), d(it[1])) }))
    list.add(ProductiveRule({ mul(it[0], it[1]) }, { add(mul(it[0], d(it[1])), mul(it[1], d(it[0]))) }))
    list.add(QuotientRule({ div(it[0], it[1]) }, { div(sub(mul(it[1], d(it[0])), mul(it[0], d(it[1]))), sqr(it[1])) }))
    list.add(
        PowerRule(
            { pow(it[0], it[1]) },
            { mul(add(mul(ln(it[0]), d(it[1])), mul(div(it[1], it[0]), d(it[0]))), pow(it[0], it[1])) })
    )
    list.add(ChainRule({ funcOfFunc }, { mul(d(funcOfFunc), dfx2) }))

    return list
}

fun d(operand: Operand): String {
    val results: Results = diff(operand)
    return "d($operand)/dx\r\n${results}"
}

fun diff(operand: Operand): Results {
    for (differentiationFormula: DifferentiationFormula in DIFFERENTIATION_Differentiation_FORMULA) {
        val result: Results = differentiationFormula.differentiate(operand)
        if (result.isNotEmpty()) {
            return result
        }
    }
    for (differentiationFormula: DifferentiationFormula in RULES) {
        val result: Results = differentiationFormula.differentiate(operand)
        if (result.isNotEmpty()) {
            return result
        }
    }
    return Results()
}

open class DifferentiationFormula(
    name: String?,
    fx: (v: List<Variable>) -> Operand,
    dFx: (v: List<Variable>) -> Operand
) : Formula<Variable, List<Variable>>(
    name,
    fx as (List<out Operand>) -> Operand, dFx as (List<out Operand>) -> Operand
) {
    open fun differentiate(operand: Operand): Results {
        val results: Results = Results()
        val f = fx(listOf())
        if (f is Operation && operand is Operation) {
            if (f.operator == operand.operator) {
                if (f.operands.size == operand.operands.size) {
                    if (f.operands.indices.all {
                            isConst(f[it]) == isConst(operand[it]) &&
                                    varIn(f[it]).minus(varIn(operand[it])).isEmpty() &&
                                    !f[it].deepEquals(operand[it])
                        }) {
                        val usedConst = f.operands.indices.filter {
                            isConst(f[it])
                        }.map { operand[it] }
                        val usedVar: Operand = f.operands.indices.filter {
                            varIn(f[it]).minus(varIn(operand[it])).isEmpty()
                        }.map { operand[it] }.first()
                        val results: Results = Results()
                        val diffOfVar = ArrayList(diff(usedVar))
                        val assume = y.new()
                        val assumption = eq(assume, usedVar)
                        val newF = replace(f, x, assume)
                        val diff = Differentiate(operand = newF)
                        results.add(
                            DifferentiationResult(
                                mul(Differentiate(operand = assume), diff),
                                replace(fx(listOf()), x, assume),
                                mul(replace(dFx(listOf()), x, assume), Differentiate(operand = assume)),
                                name ?: "",
                                listOf(assumption)
                            )
                        )
                        for (i in diffOfVar.indices) {
                            val r = diffOfVar[i]

                            results.add(
                                DifferentiationResult(
                                    mul(r.operand, diff),
                                    r.fx,
                                    r.dFx,
                                    r.formulaName,
                                    r.assumptions,
                                    r.derive

                                )
                            )
                        }
                        val replacedVar = replace(dFx(listOf()), x, usedVar)
                        if (usedConst.isNotEmpty()) {
                            val replacedConst = replace(replacedVar, a, usedConst[0])
                            results.add(
                                DifferentiationResult(
                                    mul(diffOfVar.last().operand, replacedConst),
                                    fx(listOf()),
                                    dFx(listOf()),
                                    name ?: ""
                                )
                            )
                        } else {
                            results.add(
                                DifferentiationResult(
                                    mul(diffOfVar.last().operand, replacedVar),
                                    fx(listOf()),
                                    dFx(listOf()),
                                    name ?: ""
                                )
                            )
                        }
                        return results
                    }
                }
            }
        }
        return if (f.toTypeString() == operand.toTypeString()) {
            val usedConst = constIn(operand)
            if (usedConst != null) {
                results.add(
                    DifferentiationResult(
                        replace(dFx(listOf()), a, usedConst),
                        fx(listOf()),
                        dFx(listOf()),
                        name ?: ""
                    )
                )
            } else {
                results.add(DifferentiationResult(dFx(listOf()), fx(listOf()), dFx(listOf()), name ?: ""))
            }
            results
        } else {
            results
        }
    }

    override fun toString(): String {
        return "d(${fx(listOf())})/dx = ${dFx(listOf())} ${if (name == null) "" else "[$name]"}".trim()
    }
}

open abstract class Rule(name: String, fx: (v: List<Variable>) -> Operand, dFx: (v: List<Variable>) -> Operand) :
    DifferentiationFormula(name, fx, dFx)

class AdditionRule(fx: (v: List<Variable>) -> Operand, dFx: (v: List<Variable>) -> Operand) :
    Rule("Addition Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if (operand is Operation) {
            if (operand.operator == Operators.add) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val vars: List<Variable> = operand.operands.map { y.new() }
                val result: Results = Results()
                var ops: MutableList<Operand> = operand.operands.map { o -> Differentiate(operand = o) }.toMutableList()
                result.add(
                    DifferentiationResult(
                        add(*ops.toTypedArray()),
                        this.fx(vars),
                        this.dFx(vars),
                        this.name ?: "",
                        vars.indices.map { eq(vars[it], operand.operands[it]) })
                )
                for (i: Int in results.indices) {
                    for (r: Result in results[i]) {
                        ops[i] = r.operand
                        result.add(
                            DifferentiationResult(
                                add(*ops.toTypedArray()),
                                r.fx,
                                r.dFx,
                                r.formulaName,
                                r.assumptions,
                                r.derive
                            )
                        )
                    }
                }
                return result
            } else {
                return Results()
            }
        } else {
            return Results()
        }
    }
}

class SubtractionRule(fx: (v: List<Variable>) -> Operand, dFx: (v: List<Variable>) -> Operand) :
    Rule("Subtraction Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if (operand is Operation) {
            if (operand.operator == Operators.sub) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val vars: List<Variable> = operand.operands.map { y.new() }
                val result: Results = Results()
                var ops: MutableList<Operand> = operand.operands.map { o -> Differentiate(operand = o) }.toMutableList()
                result.add(
                    DifferentiationResult(
                        sub(ops[0], ops[1]),
                        fx(vars),
                        dFx(vars),
                        name ?: "",
                        vars.indices.map { eq(vars[it], operand.operands[it]) })
                )
                for (i: Int in results.indices) {
                    for (r: Result in results[i]) {
                        ops[i] = r.operand
                        result.add(
                            DifferentiationResult(
                                sub(ops[0], ops[1]),
                                r.fx,
                                r.dFx,
                                r.formulaName,
                                r.assumptions,
                                r.derive
                            )
                        )
                    }
                }
                return result
            } else {
                return Results()
            }
        } else {
            return Results()
        }
    }
}

class ProductiveRule(fx: (v: List<Variable>) -> Operand, dFx: (v: List<Variable>) -> Operand) :
    Rule("Productive Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if (operand is Operation) {
            if (operand.operator == Operators.mul) run {
                val results: List<Results> = operand.operands.map { diff(it) }
                val result: Results = Results()
                val vars: List<Variable> = operand.operands.map { y.new() }
                var ops: MutableList<Operand> = operand.operands.indices.map {
                    run {
                        val l: MutableList<Operand> = operand.operands.toMutableList()
                        l[it] = Differentiate(operand = operand.operands[it])
                        mul(*l.toTypedArray())
                    }
                }.toMutableList()
                result.add(
                    DifferentiationResult(
                        add(*ops.toTypedArray()),
                        fx(vars),
                        dFx(vars),
                        name ?: "",
                        vars.indices.map { eq(vars[it], operand.operands[it]) })
                )

                for (i: Int in results.indices) {
                    for (r: Result in results[i]) {
                        val innerOperands: MutableList<Operand> = (ops[i] as Operation).operands.toMutableList()
                        innerOperands[i] = r.operand
                        ops[i] = mul(*innerOperands.toTypedArray())
                        result.add(
                            DifferentiationResult(
                                add(*ops.toTypedArray()),
                                r.fx,
                                r.dFx,
                                r.formulaName,
                                r.assumptions,
                                r.derive
                            )
                        )
                    }
                }
                return result
            } else {
                return Results()
            }
        } else {
            return Results()
        }
    }
}

class QuotientRule(fx: (v: List<Variable>) -> Operand, dFx: (v: List<Variable>) -> Operand) :
    Rule("Quotient Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if (operand is Operation) {
            if (operand.operator == Operators.div) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val vars: List<Variable> = operand.operands.map { y.new() }
                val result: Results = Results()
                var ops: MutableList<Operand> = operand.operands.indices.map { i ->
                    run {
                        val l: MutableList<Operand> = operand.operands.toMutableList()
                        l[i] = Differentiate(operand = operand.operands[i])
                        mul(*l.toTypedArray())
                    }
                }.toMutableList()
                result.add(
                    DifferentiationResult(
                        div(sub(*ops.toTypedArray()), sqr(operand.operands[1])),
                        fx(vars),
                        dFx(vars),
                        name ?: "",
                        vars.indices.map { eq(vars[it], operand.operands[it]) })
                )

                for (i: Int in results.indices) {
                    for (r: Result in results[i]) {
                        val innerOperands: MutableList<Operand> = (ops[i] as Operation).operands.toMutableList()
                        innerOperands[i] = r.operand
                        ops[i] = mul(*innerOperands.toTypedArray())
                        result.add(
                            DifferentiationResult(
                                div(sub(*ops.toTypedArray()), sqr(operand.operands[1])),
                                r.fx,
                                r.dFx,
                                r.formulaName,
                                r.assumptions,
                                r.derive
                            )
                        )
                    }
                }
                return result
            } else {
                return Results()
            }
        } else {
            return Results()
        }
    }
}

class PowerRule(fx: (v: List<Variable>) -> Operand, dFx: (v: List<Variable>) -> Operand) : Rule("Power Rule", fx, dFx) {

    override fun differentiate(operand: Operand): Results {
        if (operand is Operation) {
            if (operand.operator == Operators.pow) run {
                val results: List<Results> = operand.operands.map { operand -> diff(operand) }
                val additionOp1Multiplier: Operation = div(operand.operands[1], operand.operands[0]) as Operation
                val additionOp2Multiplier: Operation = ln(operand.operands[0])
                val multipliers: List<Operation> = listOf(additionOp1Multiplier, additionOp2Multiplier)
                val result: Results = Results()

                val newVar1: Variable = y.new()
                val newVar2: Variable = y.new()
                var ops: MutableList<Operand> = mutableListOf()
                ops.add(mul(additionOp1Multiplier, Differentiate(operand = operand.operands[0])))
                ops.add(mul(additionOp2Multiplier, Differentiate(operand = operand.operands[1])))


                result.add(
                    DifferentiationResult(
                        mul(add(*ops.toTypedArray()), operand),
                        fx(listOf(newVar1, newVar2)),
                        dFx(listOf(newVar1, newVar2)),
                        name ?: "",
                        listOf(eq(newVar1, operand.operands[0]), eq(newVar2, operand.operands[1])),
                        calc(newVar1, newVar2)
                    )
                )

                for (i: Int in results.indices) {
                    for (r: Result in results[i]) {

                        ops[i] = mul(multipliers[i], r.operand)
                        result.add(
                            DifferentiationResult(
                                mul(add(*ops.toTypedArray()), operand),
                                r.fx,
                                r.dFx,
                                r.formulaName,
                                r.assumptions,
                                r.derive
                            )
                        )
                    }
                }
                return result
            } else {
                return Results()
            }
        } else {
            return Results()
        }
    }

    companion object {
        val calc: (Variable, Variable) -> List<Derive> = { newVar1: Variable, newVar2: Variable ->
            val newVar: Variable = y.new()
            listOf(
                Derive(newVar, pow(newVar1, newVar2), "Assumption"),
                Derive(ln(newVar), mul(newVar2, ln(newVar1)), "Taking Log in Both Side"),
                Derive(
                    mul(inv(newVar), Differentiate(operand = newVar)),
                    add(
                        mul(div(newVar2, newVar1), Differentiate(operand = newVar1)),
                        mul(ln(newVar1), Differentiate(operand = newVar2))
                    ),
                    "Differentiating Both Side"
                ),
                Derive(
                    Differentiate(operand = newVar),
                    mul(
                        add(
                            mul(div(newVar2, newVar1), Differentiate(operand = newVar1)),
                            mul(ln(newVar1), Differentiate(operand = newVar2))
                        ), newVar
                    ),
                    "Multiplied by $newVar both side"
                )
            )
        }
    }
}


class ChainRule(fx: (v: List<Variable>) -> Operand, dFx: (v: List<Variable>) -> Operand) : Rule("Chain Rule", fx, dFx) {
    override fun differentiate(operand: Operand): Results {
        if (operand is Operation) {
            if (operand.operator.operandCount == 1) {
                val result1: Results = diff(operand.operands[0])
                val differentiationResult2s: Set<DifferentiationResult> =
                    diff(Operation(operand.operator, x)).map { result ->
                        DifferentiationResult(
                            replace(
                                result.operand,
                                x,
                                operand.operands[0]
                            ), result.fx, result.dFx, result.formulaName
                        )
                    }.toSet()
                val differentiationResults: List<Set<Result>> = listOf(result1, differentiationResult2s)
                val result: Results = Results()

                val newVar: Variable = y.new()
                var ops: MutableList<Operand> = mutableListOf()
                ops.add(Differentiate(operand = operand.operands[0]))
                ops.add(Differentiate(operand = Operation(operand.operator, newVar)))
                result.add(
                    DifferentiationResult(
                        mul(ops[0], ops[1]),
                        fx(listOf()),
                        dFx(listOf()),
                        name ?: "",
                        listOf(eq(newVar, operand.operands[0]))
                    )
                )


                for (i: Int in differentiationResults.indices) {
                    for (r: Result in differentiationResults[i]) {
                        ops[i] = r.operand
                        result.add(
                            DifferentiationResult(
                                mul(ops[0], ops[1]),
                                r.fx,
                                r.dFx,
                                r.formulaName,
                                r.assumptions,
                                r.derive
                            )
                        )
                    }
                }
                return result
            }
        }
        return Results()
    }
}

class DifferentiationResult(
    operand: Operand,
    fx: Operand,
    dFx: Operand,
    formulaName: String,
    assumptions: List<Operation> = listOf(),
    derive: List<Derive> = listOf()
) : Result(operand, fx, dFx, formulaName, assumptions, derive) {

    override fun toString(): String {

        val initStr = "$preAppeneder$operand${" ".repeat(maxOperandSize - str.length)}$postAppeneder"
        return "$initStr$fx=$dFx${if (assumptions.isNotEmpty()) " | assuming $assumptions" else ""}"
    }

    private companion object {
        const val preAppeneder = "= "
        const val postAppeneder = "| applying "
    }
}



