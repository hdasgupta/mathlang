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

class Counter(val start:Int = 0) {
    private var id: ThreadLocal<Int> = ThreadLocal<Int>()
    fun getNext(): Int {
        val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())
        return coroutineScope.let {
            if(id.get() == null) {
                id.set(1)
            }
            val old:Int = id.get()
            id.set(id.get()+1)
            return old
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
            val num = numbers[0].div(numbers[1])
            num.toPlainString()
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
   asin("asin", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.asin(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
   acos("acos", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.acos(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
   atan("atan", {numbers, isReal->
        run {
            val num = java.math.BigDecimal(kotlin.math.atan(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
   acot("acot", {numbers, isReal->
        run {
            val num = BigDecimal.ONE / java.math.BigDecimal(kotlin.math.atan(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
   asec("asec", {numbers, isReal->
        run {
            val num = java.math.BigDecimal.ONE / java.math.BigDecimal(kotlin.math.acos(numbers[0].toDouble()))
            num.toPlainString()
        }}, 1),
   acsc("acosec", {numbers, isReal->
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
            1 -> "${operator.symbol}${if(operands[0] is Operation) "${operands[0]}" else "(${operands[0]})"}"
            else -> "(${operands.joinToString(separator = operator.symbol) { o -> o.toString() }})"
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

        val dnumbers:List<BigDecimal> = numbers.map { BigDecimal(if(it.toString().contains(".")) it.toString() else "${it.toString()}.${"0".repeat(200)}") }
        val result = operator.reduce(dnumbers, isReal)

        return if(isReal || operator==Operators.div) BigDecimal(result) else BigInteger(result)
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

    override fun calc(): Number {
        return lit?.calc() ?: throw Exception("No constant value")
    }

    private companion object {
        val counter: Counter = Counter()
    }

}

class Variable(name: String): UnitOperand(name(name), true) {
    var index: Int?= num(name)
    private constructor(name: String, index: Int): this(name) {
        this.index = index
    }
    override fun funcOf(): Set<Variable> {
        return setOf(this)
    }

    override fun toString(): String = "${name ?: ""}${index ?: ""}"

    override fun calc(): Number {
        throw Exception("No constant value")
    }

    fun new(): Variable = Variable(this.name ?: "y", counter.getNext())

    private companion object {
        val counter: Counter = Counter()
        val numericPattern: Pattern = Pattern.compile("^(?<name>[a-zA-Z_]+)(?<num>[0-9]*)$")
        fun name(name : String) : String {
            println(name)
            val matcher : Matcher = numericPattern.matcher(name)
            val find = matcher.find()
            println("$name -> ${matcher.group("name")}, ${matcher.group("num")}")
            return matcher.group("name")
        }

        fun num(name: String) : Int? {
            val matcher : Matcher = numericPattern.matcher(name)
            val find = matcher.find()

            return if(find && matcher.group("num").isNotEmpty()) Integer.parseInt(matcher.group("num")) else null
        }
    }
}

class Function(private val nm: String, val variables: Set<Variable>? = null, val function: Function? = null, private val index: Int = counter.getNext()): UnitOperand("${nm}${index}", true) {

    private val matcher : Matcher = numericPattern.matcher(nm)
    private val find = matcher.find()

    val nam: String = if(find) matcher.group("name")  else nm
    val idx: String = if(find) matcher.group("num") else index.toString()
    override fun funcOf(): Set<Variable> {
        return variables ?: function?.variables ?: setOf()
    }

    override fun toString(): String = "$name(${variables?.joinToString(",") { variable -> variable.name ?: "" } ?: function})"

    override fun calc(): Number {
        throw Exception("No constant value")
    }

    private companion object {
        val counter: Counter = Counter(1)
        val numericPattern: Pattern = Pattern.compile("^(?<name>[a-zA-Z_]+)(?<num>[0-9]*)$")
    }

}

class Differentiate(val function: Function? = null, val operand: Operand? = null): Operand() {
    override fun funcOf(): Set<Variable> {
        return if(function ==null && operand != null) varIn(operand) else function?.variables ?: setOf()
    }

    override fun toString(): String = "(d(${operand ?: function}))"

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

    override fun calc(): Number {
        return obj
    }
}

class DecimalLiteral(obj: BigDecimal, name: String?) : Literal<BigDecimal>(obj, name) {
    constructor(obj: BigDecimal) : this(obj, null)

    override fun toString(): String = obj.toString()

    override fun calc(): Number {
        return obj
    }
}

class StringLiteral(obj: String, name: String?) : Literal<String>(obj, name) {
    constructor(obj: String) : this(obj, null)

    override fun toString(): String = "\"$obj\""

    override fun calc(): Number {
        throw Exception("No constant value")
    }
}

class BooleanLiteral(obj: Boolean, name: String?) : Literal<Boolean>(obj, name) {
    constructor(obj: Boolean) : this(obj, null)

    override fun toString(): String = obj.toString()

    override fun calc(): Number {
        throw Exception("No constant value")
    }
}

class Undefined: Operand() {
    override fun toString(): String = throw ArithmeticException()

    override fun calc(): Number {
        throw Exception("No constant value")
    }
}