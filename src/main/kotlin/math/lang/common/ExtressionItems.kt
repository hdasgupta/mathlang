package math.lang.common

import com.numericalmethod.suanshu.number.big.BigDecimalUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import math.lang.Results
import math.lang.common.ExpressionConstants.Companion.varIn
import math.lang.diff
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.math.BigInteger
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.pow

class NoOperandOperatorException : Exception("Operator with no argument is not allowed")

class OperandCountMisMatchException(actualCount: Int, expectedCount: String) : Exception("Operator with $actualCount number of arguments is not allowed ($expectedCount expected)")

class Counter(start:Int = 0) {
    private var id:Int = start
    fun getNext(): Int {
        val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())
        return coroutineScope.let {
            return id++
        }
    }
}
enum class Operators(val symbol: String, val reduce: (numbers:List<BigDecimal>, isReal:Boolean)->String, val operandCount: Int = -1) {
    add("+", {numbers, isReal->
        run {
            val num = numbers.reduce { number1, number2 ->number1 + number2}
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }}) ,
    sub("-", {numbers, isReal->
        run {
            val num = numbers[0] - numbers[1]
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }},2),
    mul("*", {numbers, isReal->
        run {
            val num = numbers.reduce { number1, number2 ->number1 * number2}
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }}),
    div("/", {numbers, isReal->
        run {
            val num = numbers[0] / numbers[1]
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }}, 2),
    mod("%", {numbers, isReal->
        run {
            val num = numbers[0] % numbers[1]
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }}, 2),
    pow("^", {numbers, isReal->
        run {
            val num = BigDecimalUtils.pow(numbers[0], numbers[1])
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }}, 2),
    eq("=", {numbers, isReal->
        run {
            val num = numbers[0] == numbers[1]
            num.toString()
        }}, 2),
    gt(">", {numbers, isReal->
        run {
            val num = numbers[0] > numbers[1]
            num.toString()
        }}, 2),
    gte(">=", {numbers, isReal->
        run {
            val num = numbers[0] >= numbers[1]
            num.toString()
        }}, 2),
    lt("<", {numbers, isReal->
        run {
            val num = numbers[0] < numbers[1]
            num.toString()
        }}, 2),
    lte("<=", {numbers, isReal->
        run {
            val num = numbers[0] <= numbers[1]
            num.toString()
        }}, 2),
    pos("+", {numbers, isReal->
        run {
            val num = numbers[0]
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }}, 1),
    neg("-", {numbers, isReal->
        run {
            val num = java.math.BigDecimal.ZERO - numbers[0]
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }}, 1),
    ln("ln", {numbers, isReal->
        run {
            val num = BigDecimalUtils.log(numbers[0]) / BigDecimalUtils.log(BigDecimal(kotlin.math.E))
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }}, 1),
    log("log", {numbers, isReal->
        run {
            val num = BigDecimalUtils.log(numbers[0])
            if (isReal) num.toPlainString() else num.toBigIntegerExact().toString()
        }}, 2),
    sin("sin", {numbers, isReal->
        run {
            val num = BigDecimal(kotlin.math.sin(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    cos("cos", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.cos(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    tan("tan", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.tan(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    cot("cot", {numbers, isReal->
        run {
            val num = BigDecimal.ONE / BigDecimal(kotlin.math.tan(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    sec("sec", {numbers, isReal->
        run {
            val num = BigDecimal.ONE / BigDecimal(kotlin.math.cos(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    csc("cosec", {numbers, isReal->
        run {
            val num = java.math.BigDecimal.ONE / java.math.BigDecimal(kotlin.math.sin(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    rad2deg("radianToDegree", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(java.lang.Math.toRadians(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    deg2rad("degreeToRadian", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(java.lang.Math.toDegrees(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    isin("invsin", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.asin(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    icos("invcos", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.acos(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    itan("invtan", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.atan(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    icot("invcot", {numbers, isReal->
        run {
            val num = BigDecimal.ONE / java.math.BigDecimal(kotlin.math.atan(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    isec("invsec", {numbers, isReal->
        run {
            val num = java.math.BigDecimal.ONE / java.math.BigDecimal(kotlin.math.acos(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    icsc("invcosec", {numbers, isReal->
        run {
            val num = java.math.BigDecimal.ONE / java.math.BigDecimal(kotlin.math.asin(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    sinh("sinh", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.sinh(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    cosh("cosh", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.cosh(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    tanh("tanh", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.tanh(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    coth("coth", {numbers, isReal->
        run {
            val num = BigDecimal.ONE / java.math.BigDecimal(kotlin.math.tanh(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    sech("sech", {numbers, isReal->
        run {
            val num = java.math.BigDecimal.ONE / java.math.BigDecimal(kotlin.math.cosh(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
    csch("cosech", {numbers, isReal->
        run {
            val num = java.math.BigDecimal.ONE / java.math.BigDecimal(kotlin.math.sinh(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
 }

abstract class Operand(val leaf: Boolean = true) : Comparable<Operand> {
    protected val id:Int = counter.getNext()
    override fun compareTo(other: Operand): Int {
        return id.compareTo(other.id)
    }

    private companion object {
         val counter: Counter = Counter()
    }

    open fun funcOf(): Set<Variable> = setOf()

    abstract override fun toString(): String

    fun toHtmlString(): String {
        return toHtmlString(false)
    }

    abstract fun toHtmlString(root:Boolean): String

    open fun toTypeString(): String = this.javaClass.simpleName
    
    abstract fun calc(): Number
}

class Operation(@NotNull operator: Operators, @NotNull vararg operands: Operand) : Operand(false) {
    val operator: Operators = operator
    val operands: Array<out Operand> = operands
    init {
        if(operands.isEmpty()) {
            throw NoOperandOperatorException()
        }
        if(operator.operandCount!=-1) {
            if(operands.size != operator.operandCount) {
                throw OperandCountMisMatchException(operands.size, operator.operandCount.toString())
            }
        } else {
            if(operands.size<2) {
                throw OperandCountMisMatchException(operands.size, "Greater than or equals 2")
            }
        }
    }

    override fun funcOf(): Set<Variable> {
        val variables = mutableSetOf<Variable>()
        for (operand: Operand in operands) {
            variables.addAll(operand.funcOf())
        }
        return variables
    }

    override fun toString(): String {
        return when(operands.size) {
            1 -> "${operator.symbol}(${operands[0]})"
            else -> "(${operands.joinToString(separator = operator.symbol) { o -> o.toString() }})"
        }
    }

    override fun toHtmlString(root:Boolean): String {
        return when(operands.size) {
            1 -> "<table border=\"0\" cellpadding=\"0\">" +
                    "<tr>" +
                    "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                    "<b>" +
                    "<i>" +
                    "${operator.symbol}" +
                    "</i>" +
                    "</b>" +
                    "</td>" +
                    "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                    "${if(operands[0] !is Operation || (operands[0] is Operation && ((operands[0] as Operation).operands.size == 1)||(operands[0] as Operation).operator==Operators.pow)) "<b color=\"blue\">(</b></td><td style=\"vertical-align: middle;padding: 0;margin: 0;\">${operands[0].toHtmlString()}</td><td style=\"vertical-align: middle;padding: 0;margin: 0;\"><b color=\"blue\">)</b>" else operands[0].toHtmlString() }</td></tr></table>"
            else -> if(operator== Operators.div)
                "<table border=\"0\" cellpadding=\"0\">" +
                        "<tr>" +
                        "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                        "<center>" +
                        "${operands[0].toHtmlString()}" +
                        "</center>" +
                        "</td>" +
                        "</tr>" +
                        "<tr>" +
                        "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                        "<hr style=\"height: 2px; padding: 0;margin: 0; color: black;\"/>" +
                        "</td>" +
                        "</tr>" +
                        "<tr>" +
                        "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                        "<center>" +
                        "${operands[1].toHtmlString()}" +
                        "</center>" +
                        "</td>" +
                        "</tr>" +
                        "</table>"
            else if(operator== Operators.pow)
                "<table border=\"0\" cellpadding=\"0\">" +
                        "<tr>" +
                        "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                        "${operands[0].toHtmlString()}" +
                        "</td>" +
                        "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                        "<sup>${operands[1].toHtmlString()}</sup>" +
                        "</td>" +
                        "</tr>" +
                        "</table>"
            else
                "<table border=\"0\" cellpadding=\"0\">" +
                        "<tr>" +
                        (
                        if(!root && (operands[0] !is Operation || (operands[0] is Operation && (operands[0] as Operation).operands.size == 1)))
                            "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                            "<b color=\"blue\">" +
                            "(" +
                            "</b>" +
                            "</td>"
                        else
                            ""
                        ) +
                        "${operands.joinToString(
                            separator = "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                                    "<b color=\"blue\">" +
                                    "${if(operator== Operators.mul) "×" else operator.symbol}" +
                                    "</b>" +
                                    "</td>"
                        ) 
                        { 
                                o -> "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                                "${o.toHtmlString()}" +
                                "</td>" 
                        }
                        }" +
                        (if(!root && (operands[0] !is Operation || (operands[0] is Operation && (operands[0] as Operation).operands.size == 1)))
                        "<td style=\"vertical-align: middle;padding: 0;margin: 0;\">" +
                        "<b color=\"blue\">" +
                        ")" +
                        "</b>" +
                        "</td>" +
                        "</tr>"
                        else
                            "") +
                        "</table>"
        }
    }

    override fun toTypeString() : String {
        return when(operands.size) {
            1 -> "${operator.name}(${operands[0].toTypeString()})"
            else -> "(${operands.joinToString(separator = operator.name) { o -> o.toTypeString() }})"
        }
    }

    override fun calc(): Number {
        val numbers:List<Number> = operands.map { it.calc() }.toList()
        val isReal: Boolean = numbers.any { it is BigDecimal }

        val result = operator.reduce(numbers.map { BigDecimal(it.toString()) }, isReal)

        return if(isReal) BigDecimal(result) else BigInteger(result)
    }
}

open abstract class UnitOperand(val name: String?, @NotNull val isVar: Boolean) : Operand()

class Constant(name: String): UnitOperand(name, false) {
    var lit: Literal<out Object>? = null

    constructor(lit: IntegerLiteral) : this("a"+ counter.getNext()){
        this.lit = lit as Literal<out Object>
    }
    constructor(lit: DecimalLiteral) : this("a"+ counter.getNext()){
        this.lit = lit as Literal<out Object>
    }
    constructor(lit: StringLiteral) : this("a"+ counter.getNext()){
        this.lit = lit as Literal<out Object>
    }
    constructor(lit: BooleanLiteral) : this("a"+ counter.getNext()){
        this.lit = lit as Literal<out Object>
    }
    override fun toString(): String = "${lit ?: name}"
    override fun toHtmlString(root:Boolean): String = "<i color=\"#800080\">${lit ?: name}</i>"
    override fun calc(): Number {
        return lit?.calc() ?: throw Exception("No constant value")
    }

    private companion object {
        val counter: Counter = Counter()
    }

}

class Variable(name: String): UnitOperand(name, true) {
    override fun funcOf(): Set<Variable> {
        return setOf(this)
    }

    override fun toString(): String = name ?: ""
    override fun toHtmlString(root:Boolean): String = if(name==null) "" else "<b>$name</b>"
    override fun calc(): Number {
        throw Exception("No constant value")
    }
}

class Function(private val nm: String, val variables: Set<Variable>? = null, val function: Function? = null, private val index: Int = counter.getNext()): UnitOperand("${nm}${index}", true) {

    private val matcher : Matcher = numericPattern.matcher(nm)
    private val find = matcher.find()

    val nam: String = if(find) matcher.group("name")  else nm
    private val idx: String = if(find) matcher.group("num") else index.toString()
    override fun funcOf(): Set<Variable> {
        return variables ?: function?.variables ?: setOf()
    }

    override fun toString(): String = "$name(${variables?.joinToString(",") { variable -> variable.name ?: "" } ?: function})"
    override fun toHtmlString(root:Boolean): String = "<b><i color=\"green\">$nam<sub>$idx</sub></i></b><b color=\"blue\">(</b>${variables?.joinToString { variable -> if(variable.name==null) "<b>x</b>" else "<b>${variable.name}</b>" } ?: function?.toHtmlString() ?: "<b>x</b>"}<b color=\"blue\">)</b>"
    override fun calc(): Number {
        throw Exception("No constant value")
    }

    private companion object {
        val counter: Counter = Counter(3)
        val numericPattern: Pattern = Pattern.compile("^(?<name>.*)(?<num>[0-9]+)$")
    }

}

class Differentiate(val function: Function? = null, val operand: Operand? = null): Operand() {
    override fun funcOf(): Set<Variable> {
        return if(function ==null && operand != null) varIn(operand) else function?.variables ?: setOf()
    }

    override fun toString(): String = "(d(${operand ?: function})/d${func() ?: "x"})"
    override fun toHtmlString(root:Boolean): String {
        var of:String = "${operand?.toHtmlString() ?: function?.toHtmlString()}"
        if(operand!=null || function !=null) {
            of = "<td rowspan=\"3\" style=\"vertical-align: middle;padding: 0;margin: 0;\"><b color=\"blue\">(</b></td><td rowspan=\"3\" style=\"vertical-align: middle;padding: 0;margin: 0;\">$of</td><td rowspan=\"3\" style=\"vertical-align: middle;padding: 0;margin: 0;\"><b color=\"blue\">)</b></td>"
        } else {
            of = "<td rowspan=\"3\" style=\"vertical-align: middle;padding: 0;margin: 0;\">$of</td>"
        }
        return "<table border=\"0\" cellpadding=\"0\"><tr><td style=\"vertical-align: middle;padding: 0;margin: 0;\"><b color=\"#FF00FF\">d</b></td>$of</tr><tr><td style=\"vertical-align: middle;padding: 0;margin: 0;\"><hr style=\"height: 2px; padding: 0;margin: 0; color: black;\"/></td></tr><tr><td style=\"vertical-align: middle;padding: 0;margin: 0;\"><b color=\"#FF00FF\">d${func() ?: "x"}</b></td></tr></table>"
    }

    override fun calc(): Number {
        throw Exception("No constant value")
    }

    fun func(): String? {
        return if(operand==null) {
            if (function?.variables?.isNotEmpty() == true) function?.variables?.toList()
                ?.get(0)?.name else "x"
        } else {
            if(varIn(operand).isNotEmpty())
                varIn(operand).toList()[0].name
            else
                "x"
        }
    }

    fun apply(): Results = if(operand != null) diff(operand) else Results()
}

open abstract class Literal<out Object>(val obj: Object, name: String?) : UnitOperand(name, false)

class IntegerLiteral(obj: BigInteger, name: String?) : Literal<BigInteger>(obj, name) {
    constructor(obj: BigInteger) : this(obj, null)

    override fun toString(): String = obj.toString()
    override fun toHtmlString(root:Boolean): String = "<b  color=\"#800080\">${obj}</b>"
    override fun calc(): Number {
        return obj
    }
}

class DecimalLiteral(obj: BigDecimal, name: String?) : Literal<BigDecimal>(obj, name) {
    constructor(obj: BigDecimal) : this(obj, null)

    override fun toString(): String = obj.toString()
    override fun toHtmlString(root:Boolean): String = "<b  color=\"#800080\">${obj}</b>"
    override fun calc(): Number {
        return obj
    }
}

class StringLiteral(obj: String, name: String?) : Literal<String>(obj, name) {
    constructor(obj: String) : this(obj, null)

    override fun toString(): String = "\"$obj\""
    override fun toHtmlString(root:Boolean): String = "\"$obj\""
    override fun calc(): Number {
        throw Exception("No constant value")
    }
}

class BooleanLiteral(obj: Boolean, name: String?) : Literal<Boolean>(obj, name) {
    constructor(obj: Boolean) : this(obj, null)

    override fun toString(): String = obj.toString()
    override fun toHtmlString(root:Boolean): String = obj.toString()
    override fun calc(): Number {
        throw Exception("No constant value")
    }
}

class Undefined: Operand() {
    override fun toString(): String = throw ArithmeticException()
    override fun toHtmlString(root:Boolean): String = ArithmeticException().toString()
    override fun calc(): Number {
        throw Exception("No constant value")
    }
}