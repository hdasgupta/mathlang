package math.lang.common


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import math.lang.common.ExpressionConstants.Companion.name
import math.lang.common.ExpressionConstants.Companion.one
import math.lang.common.ExpressionConstants.Companion.varIn
import math.lang.common.ExpressionConstants.Companion.x
import math.lang.diff
import org.jetbrains.annotations.NotNull
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class NoOperandOperatorException : Exception("Operator with no argument is not allowed")

class OperandCountMisMatchException(actualCount: Int, expectedCount: String) :
    Exception("Operator with $actualCount number of arguments is not allowed ($expectedCount expected)")

class Counter(val start: Int = 0) {
    private var id: ThreadLocal<Int> = ThreadLocal<Int>()
    fun getNext(): Int {
        val coroutineScope: CoroutineScope = CoroutineScope(SupervisorJob())
        return coroutineScope.let {
            if (id.get() == null) {
                id.set(1)
            }
            val old: Int = id.get()
            id.set(id.get() + 1)
            return old
        }
    }
}

enum class Operators(
    val symbol: String,
    val reduce: (numbers: List<Double>, isReal: Boolean) -> String,
    val operandCount: Int = -1
) {
    add("+", { numbers, isReal ->
        run {
            val num = numbers.reduce { number1, number2 -> number1 + number2 }
            if (isReal) num.toString() else num.toInt().toString()
        }
    }),
    sub("-", { numbers, isReal ->
        run {
            val num = numbers[0] - numbers[1]
            if (isReal) num.toString() else num.toInt().toString()
        }
    }, 2),
    mul("*", { numbers, isReal ->
        run {
            val num = numbers.reduce { number1, number2 -> number1 * number2 }
            if (isReal) num.toString() else num.toInt().toString()
        }
    }),
    div("/", { numbers, isReal ->
        run {
            val num = numbers[0].div(numbers[1])
            num.toString()
        }
    }, 2),
    mod("%", { numbers, isReal ->
        run {
            val num = numbers[0] % numbers[1]
            if (isReal) num.toString() else num.toInt().toString()
        }
    }, 2),
    pow("^", { numbers, isReal ->
        run {
            val num = Math.pow(numbers[0], numbers[1])
            if (isReal) num.toString() else num.toInt().toString()
        }
    }, 2),
    eq("=", { numbers, isReal ->
        run {
            val num = numbers[0] == numbers[1]
            num.toString()
        }
    }, 2),
    gt(">", { numbers, isReal ->
        run {
            val num = numbers[0] > numbers[1]
            num.toString()
        }
    }, 2),
    gte(">=", { numbers, isReal ->
        run {
            val num = numbers[0] >= numbers[1]
            num.toString()
        }
    }, 2),
    lt("<", { numbers, isReal ->
        run {
            val num = numbers[0] < numbers[1]
            num.toString()
        }
    }, 2),
    lte("<=", { numbers, isReal ->
        run {
            val num = numbers[0] <= numbers[1]
            num.toString()
        }
    }, 2),
    pos("+", { numbers, isReal ->
        run {
            val num = numbers[0]
            if (isReal) num.toString() else num.toInt().toString()
        }
    }, 1),
    neg("-", { numbers, isReal ->
        run {
            val num = 0 - numbers[0]
            if (isReal) num.toString() else num.toInt().toString()
        }
    }, 1),
    ln("ln", { numbers, isReal ->
        run {
            val num = Math.log(numbers[0]) / Math.log(kotlin.math.E)
            if (isReal) num.toString() else num.toInt().toString()
        }
    }, 1),
    log("log", { numbers, isReal ->
        run {
            val num = Math.log(numbers[0])
            if (isReal) num.toString() else num.toInt().toString()
        }
    }, 2),
    sin("sin", { numbers, isReal ->
        run {
            val num = kotlin.math.sin(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    cos("cos", { numbers, isReal ->
        run {
            val num = kotlin.math.cos(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    tan("tan", { numbers, isReal ->
        run {
            val num = kotlin.math.tan(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    cot("cot", { numbers, isReal ->
        run {
            val num = 1 / kotlin.math.tan(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    sec("sec", { numbers, isReal ->
        run {
            val num = 1 / kotlin.math.cos(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    cosec("cosec", { numbers, isReal ->
        run {
            val num = 1 / kotlin.math.sin(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    rad2deg("radianToDegree", { numbers, isReal ->
        run {
            val num = java.lang.Math.toRadians(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    deg2rad("degreeToRadian", { numbers, isReal ->
        run {
            val num = java.lang.Math.toDegrees(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    asin("asin", { numbers, isReal ->
        run {
            val num = kotlin.math.asin(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    acos("acos", { numbers, isReal ->
        run {
            val num = kotlin.math.acos(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    atan("atan", { numbers, isReal ->
        run {
            val num = kotlin.math.atan(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    acot("acot", { numbers, isReal ->
        run {
            val num = 1 / kotlin.math.atan(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    asec("asec", { numbers, isReal ->
        run {
            val num = 1 / kotlin.math.acos(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    acosec("acosec", { numbers, isReal ->
        run {
            val num = 1 / kotlin.math.asin(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    sinh("sinh", { numbers, isReal ->
        run {
            val num = kotlin.math.sinh(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    cosh("cosh", { numbers, isReal ->
        run {
            val num = kotlin.math.cosh(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    tanh("tanh", { numbers, isReal ->
        run {
            val num = kotlin.math.tanh(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    coth("coth", { numbers, isReal ->
        run {
            val num = 1 / kotlin.math.tanh(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    sech("sech", { numbers, isReal ->
        run {
            val num = 1 / kotlin.math.cosh(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
    cosech("cosech", { numbers, isReal ->
        run {
            val num = 1 / kotlin.math.sinh(numbers[0].toDouble())
            num.toString()
        }
    }, 1),
}

abstract class Operand(val leaf: Boolean = true) : Comparable<Operand> {
    protected val id: Int = counter.getNext()

    override fun compareTo(other: Operand): Int {
        return id.compareTo(other.id)
    }

    companion object {
        private val counter: Counter = Counter()
        fun negate(op: Operand): Operand {
            return if (op is Operation) {
                if (op.operator == Operators.neg) {
                    op.operands[0]
                } else {
                    Operation(Operators.neg, op)
                }
            } else {
                Operation(Operators.neg, op)
            }
        }

        fun invert(op: Operand): Operand {
            return if (op is Operation) {
                if (op.operator == Operators.div &&
                    op.operands[0] is Literal<*> &&
                    (op.operands[0] as Literal<*>).obj is Number &&
                    (op.operands[0] as Literal<*>).obj == 1
                ) {
                    op.operands[1]
                } else {
                    Operation(Operators.div, one, op)
                }
            } else {
                Operation(Operators.div, one, op)
            }
        }
    }

    open fun funcOf(): Set<Variable> = setOf()

    override fun toString(): String = "${string(true)}"

    fun toString(approx: Boolean): String = string(approx)

    open fun string(approx: Boolean): String = string()

    abstract fun string(): String

    open fun toTypeString(): String = this.javaClass.simpleName

    open fun toOperatorString(
        level: Int,
        operators: Map<Int, List<Operators>> = mapOf(*(0..level).map {
            Pair(
                it,
                Operators.values().toList()
            )
        }.toTypedArray())
    ): String = ""

    abstract fun calc(approx: Boolean = false): Number

    abstract fun deepEquals(operand: Operand): Boolean

    fun isNegative(): Boolean = this is Operation && this.operator == Operators.neg

    fun isInverted(): Boolean = this is Operation &&
            this.operator == Operators.div &&
            this.operands[0] is Literal<*> &&
            (this.operands[0] as Literal<*>).obj is Number &&
            (this.operands[0] as Literal<*>).obj == 1

    fun negate(): Operand {

        return Companion.negate(this)
    }

    fun invert(): Operand {
        return Companion.invert(this)
    }

    fun positive(): Operand {
        return if (this is Operation && this.operator == Operators.neg) {
            this.operands[0]
        } else {
            return this
        }
    }

}

class Operation(@NotNull operator: Operators, @NotNull vararg operands: Operand) : Operand(false) {
    val operator: Operators = operator
    val operands: Array<out Operand> = operands

    init {
        if (operands.isEmpty()) {
            throw NoOperandOperatorException()
        }
        if (operator.operandCount != -1) {
            if (operands.size != operator.operandCount) {
                throw OperandCountMisMatchException(operands.size, operator.operandCount.toString())
            }
        } else {
            if (operands.size < 2) {
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
        return when (operands.size) {
            1 -> "${operator.symbol}${if (operands[0] is Operation && (operands[0] as Operation).operands.size>1) "${operands[0]}" else "(${operands[0]})"}"
            else -> "(${operands.joinToString(separator = operator.symbol) { o -> o.toString() }})"
        }
    }

    override fun toTypeString(): String {
        return when (operands.size) {
            1 -> "${operator.name}(${operands[0].toTypeString()})"
            else -> "(${operands.joinToString(separator = operator.name) { o -> o.toTypeString() }})"
        }
    }

    override fun toOperatorString(level: Int, operators: Map<Int, List<Operators>>): String {
        val maxLevel: Int = operators.keys.maxOf { it }
        val list = operators[maxLevel - level - 1]
        val validOps: List<Operators> = list ?: Operators.values().toList()
        if (!validOps.contains(operator)) return ""
        return if (operands.any { it is Operation } && level > 0) {
            "(${
                operands.filterIsInstance<Operation>().map { it.toOperatorString(level - 1, operators) }
                    .filter { it.isNotEmpty() }.distinct().sorted().joinToString("|")
            }$operator)"

        } else {
            "($operator)"
        }
    }

    override fun calc(approx: Boolean): Number {
        val numbers: List<Number> = operands.map { it.calc(approx) }.toList()
        val isReal: Boolean = numbers.any { it is Double }

        val dnumbers: List<Double> = numbers.map {
            it.toString().toDouble()
        }
        val result = operator.reduce(dnumbers, isReal)

        return if (isReal || operator == Operators.div) java.lang.Double.parseDouble(result) else java.lang.Integer.parseInt(
            result
        )
    }

    override fun deepEquals(operand: Operand): Boolean {
        if (operand !is Operation) {
            return false
        }
        if (operand.operator != operator) {
            return false
        }
        if (operands.size != operand.operands.size) {
            return false
        }
        if (operands.indices.any { !operands[it].deepEquals(operand.operands[it]) }) {
            return false
        }
        return true
    }

    operator fun get(index: Int): Operand = operands[index]

}

open abstract class UnitOperand(val name: String?, @NotNull val isVar: Boolean) : Operand()

class Constant(name: String) : UnitOperand(name, false) {
    var lit: Literal<out Any>? = null

    constructor(lit: Literal<*>) : this("a" + counter.getNext()) {
        this.lit = lit as Literal<out Object>
    }

    constructor(lit: IntegerLiteral) : this("a" + counter.getNext()) {
        this.lit = lit as Literal<out Object>
    }

    constructor(lit: DecimalLiteral) : this("a" + counter.getNext()) {
        this.lit = lit as Literal<out Object>
    }

    constructor(lit: StringLiteral) : this("a" + counter.getNext()) {
        this.lit = lit as Literal<out Object>
    }

    constructor(lit: BooleanLiteral) : this("a" + counter.getNext()) {
        this.lit = lit as Literal<out Object>
    }

    override fun string(): String = "${lit ?: name}"


    fun clone(): Constant {
        return if (lit != null) {
            when (lit) {
                is IntegerLiteral -> Constant(lit as IntegerLiteral)
                is DecimalLiteral -> Constant(lit as DecimalLiteral)
                is StringLiteral -> Constant(lit as StringLiteral)
                else -> Constant(lit as BooleanLiteral)
            }
        } else {
            Constant(name ?: "a")
        }
    }

    override fun calc(approx: Boolean): Number {
        return lit?.calc(approx) ?: throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if (operand !is Constant && operand !is Literal<*>) {
            return false
        }
        if (operand !is Literal<*>) {
            if (lit != null)
                return operand.deepEquals(lit!!)
            else
                false
        }

        if (operand !is Constant) return false

        if (Objects.equals(lit, operand.lit)) {
            return true
        }
        if (Objects.isNull(lit) && Objects.isNull(operand.lit) && !name.equals(operand.name)) {
            return false
        }

        return Objects.isNull(lit) && Objects.isNull(operand.lit)
    }

    private companion object {
        val counter: Counter = Counter()
    }

}

class Variable(name: String) : UnitOperand(name(name).first, true) {
    var index: Int? = name(name).second

    private constructor(name: String, index: Int) : this(name) {
        this.index = index
    }

    override fun funcOf(): Set<Variable> {
        return setOf(this)
    }

    override fun string(): String = "${name ?: ""}${index ?: ""}"

    override fun calc(approx: Boolean): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if (operand !is Variable) {
            return false
        }
        if (index == operand.index) {
            return Objects.equals(name, operand.name)
        }
        return false
    }

    fun new(): Variable = Variable(this.name ?: "y", counter.getNext())

    fun clone(): Variable = Variable(this.name ?: "y")

    private companion object {
        val counter: Counter = Counter()
    }
}

class Function(
    private val nm: String,
    val variables: Set<Variable>? = null,
    val function: Function? = null,
    private val index: Int = counter.getNext()
) : UnitOperand("${nm}${index}", true) {

    private val pair = name(nm, index)

    val nam: String = pair.first
    val idx: String = pair.second?.toString() ?: ""
    override fun funcOf(): Set<Variable> {
        return variables ?: function?.variables ?: setOf()
    }

    override fun string(): String =
        "$name(${variables?.joinToString(",") { variable -> variable.name ?: "" } ?: function})"

    override fun calc(approx: Boolean): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if (operand !is math.lang.common.Function) {
            return false
        }
        if (Objects.equals(nam, operand.nam) && Objects.equals(idx, operand.idx)) {
            return true
        }
        return false
    }

    private companion object {
        val counter: Counter = Counter(1)
    }

}

class Differentiate(val function: Variable? = null, val operand: Operand? = null, val respectTo: Variable  = x) : Operand() {
    override fun funcOf(): Set<Variable> {
        return if (function == null && operand != null) varIn(operand) else function?.funcOf() ?: setOf()
    }

    override fun string(): String = "(d(${operand ?: function}, ${respectTo}))"

    override fun calc(approx: Boolean): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if (operand !is Differentiate) {
            return false
        }
        if (Objects.nonNull(function) && Objects.nonNull(operand.function)) {
            return operand.function?.let { function?.deepEquals(it) } ?: false
        }
        if (Objects.nonNull(operand) && Objects.nonNull(operand.operand)) {
            return operand.operand?.let { operand.deepEquals(it) } ?: false
        }
        return false
    }

    fun func(): String? {
        return "$respectTo"
    }

    fun apply(): Results = if (operand != null) diff(operand) else Results()
}

open abstract class Literal<out Object>(val obj: Object, name: String?) : UnitOperand(name, false)

class IntegerLiteral(obj: Int, name: String?) : Literal<Int>(obj, name) {
    constructor(obj: Int) : this(obj, null)

    override fun string(approx: Boolean): String = "${obj.toInt()}"

    override fun string(): String = obj.toString()

    override fun calc(approx: Boolean): Number {
        return obj.toInt()
    }

    override fun deepEquals(operand: Operand): Boolean {
        if (operand is IntegerLiteral) {
            return obj == operand.obj
        }

        if (operand is DecimalLiteral) {
            return obj.toDouble() == operand.obj
        }

        if (operand is Constant) {
            return if (operand.lit != null)
                Objects.equals(obj, operand.lit!!.obj)
            else
                false
        }

        return false
    }
}

class DecimalLiteral(obj: Double, name: String?) : Literal<Double>(obj, name) {
    constructor(obj: Double) : this(obj, null)

    override fun string(approx: Boolean): String = "${if(obj - obj.toInt() > 0) obj else obj.toInt()}"

    override fun string(): String = obj.toString()

    override fun calc(approx: Boolean): Number {
        return obj
    }

    override fun deepEquals(operand: Operand): Boolean {
        if (operand is DecimalLiteral) {
            return obj == operand.obj
        }

        if (operand is IntegerLiteral) {
            return obj == operand.obj.toDouble()
        }

        if (operand is Constant) {
            return if (operand.lit != null)
                Objects.equals(obj, operand.lit!!.obj)
            else
                false
        }

        return false
    }
}

class StringLiteral(obj: String, name: String?) : Literal<String>(obj, name) {
    constructor(obj: String) : this(obj, null)

    override fun string(): String = "\"$obj\""

    override fun calc(approx: Boolean): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if (operand !is StringLiteral) {
            return false
        }
        return obj == operand.obj

    }
}

class BooleanLiteral(obj: Boolean, name: String?) : Literal<Boolean>(obj, name) {
    constructor(obj: Boolean) : this(obj, null)

    override fun string(): String = obj.toString()

    override fun calc(approx: Boolean): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        if (operand !is BooleanLiteral) {
            return false
        }
        return obj == operand.obj
    }
}

class Undefined : Operand() {
    override fun string(): String = throw ArithmeticException()

    override fun calc(approx: Boolean): Number {
        throw Exception("No constant value")
    }

    override fun deepEquals(operand: Operand): Boolean {
        return (operand is Undefined)
    }
}
