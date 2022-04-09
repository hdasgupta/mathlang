package math.lang.common

import com.numericalmethod.suanshu.number.big.BigDecimalUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import math.lang.Results
import math.lang.common.ExpressionConstants.Companion.varIn
import math.lang.diff
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand
import org.apache.el.parser.AstNegative
import org.jetbrains.annotations.NotNull
import java.math.BigDecimal
import java.math.BigInteger
import java.util.Objects
import java.util.regex.Matcher
import java.util.regex.Pattern

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

abstract class Operand(val leaf: Boolean = true, negative: Boolean = false, inverted : Boolean = false) : Comparable<Operand> {
    protected val id:Int = counter.getNext()
    private var negative: Boolean = negative
    private var inverted: Boolean = inverted

    override fun compareTo(other: Operand): Int {
        return id.compareTo(other.id)
    }

    companion object {
        private val counter: Counter = Counter()
        fun negate(op:Operand): Operand {
            op.negative = op.negative.not()
            return op
        }
        fun invert(op:Operand): Operand {
            op.inverted = op.inverted.not()
            return op
        }
    }

    open fun funcOf(): Set<Variable> = setOf()

    override fun toString(): String {
        return if(negative) {
            if(inverted) {
                "(-1/${string()})"
            } else {
                "(-${string()})"
            }
        } else {
            if(inverted) {
                "(1/${string()})"
            } else {
                "${string()}"
            }
        }
    }

    abstract fun string(): String

    open fun toTypeString(): String = this.javaClass.simpleName

    open fun toOperatorString(level: Int) : String = ""
    
    abstract fun calc(): Number

    abstract fun deepEquals(operand: Operand ): Boolean

    fun isNegative(): Boolean = negative

    fun isInverted(): Boolean = inverted

    fun negate(): Operand {

        val op = getOperand(TokenNode.getTree(Token.getTokens(toString())))
        op.negative = op.negative.not()
        return op
    }

    fun invert(): Operand {
        val op = getOperand(TokenNode.getTree(Token.getTokens(toString())))
        op.inverted = op.inverted.not()
        return op
    }

    fun positive(): Operand {
        val op = getOperand(TokenNode.getTree(Token.getTokens(toString())))
        op.negative = false
        return op
    }

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

    override fun string(): String {
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

    override fun toOperatorString(level: Int): String {
        return if(operands.any { it is Operation } && level > 0) {
            "(${operands.filterIsInstance<Operation>().map { it.toOperatorString(level -1 ) }.distinct().sorted().joinToString("|")}$operator)"

        } else {
            "($operator)"
        }
    }

    override fun calc(): Number {
        val numbers:List<Number> = operands.map { it.calc() }.toList()
        val isReal: Boolean = numbers.any { it is BigDecimal }

        val dnumbers:List<BigDecimal> = numbers.map { BigDecimal(if(it.toString().contains(".")) it.toString() else "${it.toString()}.${"0".repeat(200)}") }
        val result = operator.reduce(dnumbers, isReal)

        return if(isReal || operator==Operators.div) BigDecimal(result) else BigInteger(result)
    }

    override fun deepEquals(operand: Operand): Boolean {
        if(operand !is Operation) {
            return false
        }
        if(operand.operator != operator) {
            return false
        }
        if(operands.size != operand.operands.size) {
            return false
        }
        if(operands.indices.any { !operands[it].deepEquals(operand.operands[it]) }) {
            return false
        }
        return true
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
    override fun string(): String = "${lit ?: name}"

    override fun calc(): Number {
        return lit?.calc() ?: throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if(operand !is Constant) {
            return false
        }
        if(Objects.equals(lit, operand.lit)) {
            return true
        }
        if(Objects.isNull(lit) && Objects.isNull(operand.lit) &&  !name.equals(operand.name)) {
            return false
        }

        return Objects.isNull(lit) && Objects.isNull(operand.lit)
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

    override fun string(): String = "${name ?: ""}${index ?: ""}"

    override fun calc(): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if(operand !is Variable) {
            return false
        }
        if(index==operand.index) {
            return Objects.equals(name, operand.name)
        }
        return false
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

    override fun string(): String = "$name(${variables?.joinToString(",") { variable -> variable.name ?: "" } ?: function})"

    override fun calc(): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if(operand !is math.lang.common.Function) {
            return false
        }
        if(Objects.equals(nam, operand.nam) && Objects.equals(idx, operand.idx)) {
            return true
        }
        return false
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

    override fun string(): String = "(d(${operand ?: function}))"

    override fun calc(): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if(operand !is Differentiate) {
            return false
        }
        if(Objects.nonNull(function) && Objects.nonNull(operand.function)) {
            return operand.function?.let { function?.deepEquals(it) } ?: false
        }
        if(Objects.nonNull(operand) && Objects.nonNull(operand.operand)) {
            return operand.operand?.let { operand?.deepEquals(it) } ?: false
        }
        return false
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

    override fun string(): String = obj.toString()

    override fun calc(): Number {
        return obj
    }

    override fun deepEquals(operand: Operand): Boolean {
        if(operand !is IntegerLiteral) {
            return false
        }
        return obj == operand.obj
    }
}

class DecimalLiteral(obj: BigDecimal, name: String?) : Literal<BigDecimal>(obj, name) {
    constructor(obj: BigDecimal) : this(obj, null)

    override fun string(): String = obj.toString()

    override fun calc(): Number {
        return obj
    }

    override fun deepEquals(operand: Operand): Boolean {
        if(operand !is DecimalLiteral) {
            return false
        }
        return obj == operand.obj
    }
}

class StringLiteral(obj: String, name: String?) : Literal<String>(obj, name) {
    constructor(obj: String) : this(obj, null)

    override fun string(): String = "\"$obj\""

    override fun calc(): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if(operand !is StringLiteral) {
            return false
        }
        return obj == operand.obj

    }
}

class BooleanLiteral(obj: Boolean, name: String?) : Literal<Boolean>(obj, name) {
    constructor(obj: Boolean) : this(obj, null)

    override fun string(): String = obj.toString()

    override fun calc(): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if(operand !is BooleanLiteral) {
            return false
        }
        return obj == operand.obj
    }
}

class Undefined: Operand() {
    override fun string(): String = throw ArithmeticException()

    override fun calc(): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        return (operand is Undefined)
    }
}