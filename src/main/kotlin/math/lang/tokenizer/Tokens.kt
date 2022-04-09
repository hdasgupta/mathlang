package math.lang.tokenizer

import math.lang.*
import math.lang.common.*
import math.lang.common.ExpressionConstants.Companion.a
import math.lang.common.ExpressionConstants.Companion.fx1
import math.lang.common.ExpressionConstants.Companion.fx2
import math.lang.common.ExpressionConstants.Companion.x
import math.lang.common.ExpressionConstants.Companion.y
import math.lang.common.Function
import java.math.BigDecimal
import java.math.BigInteger
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.regex.Pattern

//fun main() {
//    println(getOperand(TokenNode.getTree(Token.getTokens("a+x"))))
//}

interface Nodable

class Token private constructor(val value:String, val type: TokenType, val subTypes: SubPattern): Nodable {

    override fun toString(): String {
        return "$value <${type.name} : ${subTypes.getName()}>"
    }

    companion object {
        private val pattern: Pattern = Pattern.compile(TokenType.values().joinToString("[ \\t\\r\\n\\v\\f]*|[ \\t\\r\\n\\v\\f]*") { sp->sp.pattern() })
        private val space : Pattern = Pattern.compile("^[ \\t\\r\\n\\v\\f]*$")
        @JvmStatic
        fun getTokens(expression: String): List<Token> {
            val m = pattern.matcher(expression)
            val gapMisMatch:Atomic<Boolean> = Atomic(false)
            val lastEnd: Atomic<Int> = Atomic(0)
            val list: MutableList<Token?> = mutableListOf()
            while(m.find())
             {
                val value: String = m.group().trim()
                val lEnd = lastEnd.get()
                val start:Int = m.start()
                val missingStr = expression.substring(lEnd, start)
                gapMisMatch.set(!gapMisMatch.get() && !space.matcher(missingStr).matches())
                lastEnd.set(m.end())
                var token: Token? = null
                for (type: TokenType in TokenType.values()) {
                    if (type.compile()?.matcher(value)?.matches() == true) {
                        for (subType: SubPattern in type.patterns()) {
                            if (subType.compile()?.matcher(value)?.matches() == true) {
                                token = Token(value, type, subType)
                            }
                        }
                    }
                }
                list.add(token)
            }
            val lEnd = lastEnd.get()
            val missingStr = expression.substring(lEnd, expression.length)
            gapMisMatch.set(!gapMisMatch.get() && !space.matcher(missingStr).matches())
            if(list.any{ t->t==null} || gapMisMatch.get()) {
                throw ExpressionException()
            }
            return list as List<Token>
        }

    }
}

fun getOperand(node: TokenNode) : Operand {
    val token = node.token
    if(token is Token) {
        return when(token.type) {
            TokenType.variable ->
                when(token.subTypes as VariableType) {
                    VariableType.variable ->
                        when(token.value) {
                            "x" -> x
                            "y" -> y
                            else -> Variable(token.value)
                        }
                    VariableType.constant ->
                        when(token.value) {
                            "a" -> a
                            else -> Constant(token.value)
                        }
                    VariableType.function ->
                        when(token.value) {
                            "fx1" -> fx1
                            "fx2" -> fx2
                            else -> Function(token.value)
                        }
                    else -> ExpressionConstants.Companion::class.java.getDeclaredMethod(token.subTypes.name, Operand::class.java).invoke(
                        ExpressionConstants.Companion, getOperand(node.children[0])) as Operand
                }
            else ->
                when(token.subTypes as NumericType) {
                    NumericType.integer -> Constant(IntegerLiteral(BigInteger(token.value)))
                    else -> Constant(DecimalLiteral(BigDecimal(token.value)))
                }
        }
    } else {
        val operator  = token as OperatorType
        return ExpressionConstants.Companion::class.java.getDeclaredMethod(operator.name, Array<Operand>::class.java).invoke(
            ExpressionConstants.Companion, node.children.map { c->getOperand(c) }.toTypedArray()) as Operand
    }
}

class TokenNode private constructor(val token: Nodable, val children: MutableList<TokenNode> = mutableListOf()) : Operators {

    override fun toString(): String {
        return "$token${if(children.isEmpty()) "" else " $children"}"
    }
    companion object  {
        class Stacks(val operators: Stack<Operators> = Stack(), val operands: Stack<TokenNode> = Stack() ) {

        }

        fun getTree(tokens : List<Token>): TokenNode {
            val stacks: Stacks = Stacks()
            stacks.operators.push(BracketsType.opening)

            for (i: Int in tokens.indices) {
                val token: Token = tokens[i]
                if(token.subTypes == BracketsType.opening) {
                    if(i>0 && tokens[i-1].type==TokenType.variable) {
                        stacks.operators.push(stacks.operands.pop())
                    }
                    stacks.operators.push(BracketsType.opening)
                } else if(token.subTypes == BracketsType.closing) {
                    popUntilOpening(stacks)
                } else if(token.type == TokenType.operators) {
                    val operator : Operators = token.subTypes as Operators
                    if(stacks.operators.empty()) {
                        stacks.operands.push(TokenNode(token, mutableListOf(stacks.operands.pop(), stacks.operands.pop())))
                    } else if(operator.priority().index < stacks.operators.peek().priority().index) {
                        while (operator.priority().index < stacks.operators.peek().priority().index) {
                            popAllSamePriority(stacks)
                        }
                    } else if(operator.priority().index == stacks.operators.peek().priority().index) {
                        popAllSamePriority(stacks)
                    }
                    stacks.operators.push(operator)
                } else {
                    stacks.operands.push(TokenNode(token))
                }
            }

            popUntilOpening(stacks)

            if(!stacks.operators.empty() || stacks.operands.size != 1) {
                throw ExpressionException()
            }
            return stacks.operands.pop()
        }

        private fun popAllSamePriority(stacks: Stacks) {
            val operators: Operators = stacks.operators.peek()
            val lastPriority: Int = operators.priority().index
            val nodes: MutableList<TokenNode> = mutableListOf()
            nodes.add(stacks.operands.pop())
            while (stacks.operators.peek().priority().index == lastPriority) {
                stacks.operators.pop()
                nodes.add(0, stacks.operands.pop())
            }
            stacks.operands.push(TokenNode(operators as Nodable, nodes))

        }

        private fun popUntilOpening(stacks: Stacks) {
            var tokenNode: TokenNode
            while(stacks.operators.peek() != BracketsType.opening) {
                popAllSamePriority(stacks)
            }
            stacks.operators.pop()
            if(!stacks.operators.empty() && stacks.operators.peek() is TokenNode) {
                tokenNode = stacks.operators.pop() as TokenNode
                tokenNode.children.add(stacks.operands.pop())
                stacks.operands.push(tokenNode)
            }
        }
    }
}

class ExpressionException : Exception("Expression Error") {

}

enum class Priority(val index:Int) {
    no(-10),
    extremeLow(0),
    veryLow(10),
    low(20),
    medium(30),
    high(40),
    veryHigh(50),
    extremeHigh(60)
}

interface Operators {
    fun priority(): Priority = Priority.no
}

interface Patterns {
    fun patterns() : Array<out SubPattern>

    fun pattern() : String {
        return "[ \\t\\r\\n\\v\\f]*${patterns().joinToString("[ \\t\\r\\n\\v\\f]*|[ \\t\\r\\n\\v\\f]*") { sp->sp.pattern() }}[ \\t\\r\\n\\v\\f]*"
    }

    fun compile(): Pattern? {
        return Pattern.compile(pattern())
    }
}

interface SubPattern {
    fun pattern() : String
    fun compile(): Pattern? {
        return Pattern.compile(pattern())
    }
    fun getName() : String
}

enum class TokenType(private val subTypes:Array<out SubPattern>) : Patterns {
    variable(VariableType.values()),
    numeric(NumericType.values()),
    brackets(BracketsType.values()),
    operators(OperatorType.values()),;

    override fun patterns(): Array<out SubPattern> = subTypes


}

enum class VariableType(private val pattern:String) : SubPattern {
    cosech("cosech"),
    sinh("sinh"),
    cosh("cosh"),
    tanh("tanh"),
    coth("coth"),
    sech("sech"),
    cosec("cosec"),
    sin("sin"),
    cos("cos"),
    tan("tan"),
    cot("cot"),
    sec("sec"),
    acosec("acosec"),
    asin("asin"),
    acos("acos"),
    atan("atan"),
    acot("acot"),
    asec("asec"),
    variable("x|y[0-9]*"),
    constant("a[0-9]*"),
    d("d"),
    function("fx[0-9]*|fy[0-9]*|f[0-9]*");

    override fun pattern(): String = pattern
    override fun getName(): String = name
}

enum class NumericType(private val pattern:String) : SubPattern {
    real("[0-9]*\\.[0-9]+"),
    integer("[0-9]+"),;
    override fun pattern(): String = pattern
    override fun getName(): String = name
}


enum class BracketsType(private val pattern:String) : SubPattern, Operators {
    opening("\\("),
    closing("\\)"),;
    override fun pattern(): String = pattern
    override fun getName(): String = name
}

enum class OperatorType(private val pattern:String, private val priority: Priority) : SubPattern, Operators, Nodable {
    add("\\+", Priority.low),
    sub("\\-", Priority.veryLow ),
    mul("\\*", Priority.medium),
    div("\\/", Priority.high),
    pow("\\^", Priority.veryHigh),
    mod("\\%", Priority.extremeHigh);
    override fun pattern(): String = pattern
    override fun getName(): String = name
    override fun priority(): Priority = priority
}