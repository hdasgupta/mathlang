package math.lang.tokenizer

import math.lang.*
import math.lang.common.*
import math.lang.common.ExpressionConstants.Companion.a
import math.lang.common.ExpressionConstants.Companion.fx1
import math.lang.common.ExpressionConstants.Companion.fx2
import math.lang.common.ExpressionConstants.Companion.x
import math.lang.common.ExpressionConstants.Companion.y
import java.util.*
import java.util.regex.Pattern
import javax.swing.tree.TreeNode

//fun main() {
//    println(getOperand(TokenNode.getTree(Token.getTokens("a+x"))))
//}

interface Nodable

private class Token private constructor(val value: String, val type: TokenType, val subTypes: SubPattern) : Nodable {

    override fun toString(): String {
        return "$value <${type.name} : ${subTypes.getName()}>"
    }

    companion object {
        private val pattern: Pattern = Pattern.compile(
            TokenType.values().joinToString("[ \\t\\r\\n\\v\\f]*|[ \\t\\r\\n\\v\\f]*") { sp -> sp.pattern() })
        private val space: Pattern = Pattern.compile("^[ \\t\\r\\n\\v\\f]*$")

        val neg = Token("-", TokenType.operators , OperatorType.neg)
        val pos = Token("+", TokenType.operators , OperatorType.pos)
        val mul = Token("*", TokenType.operators , OperatorType.mul)
        val opening = Token("(", TokenType.brackets , BracketsType.opening)
        val closing = Token(")", TokenType.brackets , BracketsType.closing)

        @JvmStatic
        fun getTokens(expression: String): List<Token> {
            val m = pattern.matcher(expression)
            val gapMisMatch: Atomic<Boolean> = Atomic(false)
            val lastEnd: Atomic<Int> = Atomic(0)
            val list: MutableList<Token?> = mutableListOf()
            while (m.find()) {
                val value: String = m.group().trim()
                val lEnd = lastEnd.get()
                val start: Int = m.start()
                val missingStr = expression.substring(lEnd, start)
                gapMisMatch.set(!gapMisMatch.get() && !space.matcher(missingStr).matches())
                lastEnd.set(m.end())
                var token: Token? = null
                outer@ for (type: TokenType in TokenType.values()) {
                    if (type.compile()?.matcher(value)?.matches() == true) {
                        for (subType: SubPattern in type.patterns()) {
                            if (subType.compile()?.matcher(value)?.matches() == true) {
                                token = Token(value, type, subType)
                                break@outer
                            }
                        }
                    }
                }
                list.add(token)
            }
            val lEnd = lastEnd.get()
            val missingStr = expression.substring(lEnd, expression.length)
            gapMisMatch.set(!gapMisMatch.get() && !space.matcher(missingStr).matches())
            if (list.any { t -> t == null } || gapMisMatch.get()) {
                throw ExpressionException()
            }
            return list as List<Token>
        }

    }
}

fun getOperand(string: String): Operand = getOperand(TokenNode.getTree(Token.getTokens(string)))

private fun getOperand(node: TokenNode): Operand {
    val token = node.token

        return when (token.type) {
            TokenType.variable ->
                when (token.subTypes as VariableType) {
                    VariableType.variable ->
                        when (token.value) {
                            "x" -> x
                            "y" -> y
                            else -> Variable(token.value)
                        }
                    VariableType.constant ->
                        when (token.value) {
                            "a" -> a
                            else -> Constant(token.value)
                        }
                    VariableType.function ->
                        when (token.value) {
                            "fx1" -> fx1
                            "fx2" -> fx2
                            else -> Function(token.value)
                        }
                    else -> try {
                        ExpressionConstants.Companion::class.java.getDeclaredMethod(
                            token.subTypes.name,
                            Operand::class.java
                        ).invoke(
                            ExpressionConstants.Companion, getOperand(node.children[0])
                        ) as Operand
                    } catch (t: Throwable) {
                        ExpressionConstants.Companion::class.java.getDeclaredMethod(token.subTypes.name, Array<Operand>::class.java)
                            .invoke(
                                ExpressionConstants.Companion, node.children.map { c -> getOperand(c) }.toTypedArray()
                            ) as Operand
                    }
                }
            TokenType.operators ->
                ExpressionConstants.Companion::class.java.getDeclaredMethod((token.subTypes as OperatorType).name, Array<Operand>::class.java)
                    .invoke(
                        ExpressionConstants.Companion, node.children.map { c -> getOperand(c) }.toTypedArray()
                    ) as Operand
            else ->
                when (token.subTypes as NumericType) {
                    NumericType.integer -> Constant(IntegerLiteral(java.lang.Integer.parseInt(token.value)))
                    else -> Constant(DecimalLiteral(java.lang.Double.parseDouble(token.value)))
                }
        }
}

private class TokenNode private constructor(val token: Token, val children: List<TokenNode> = listOf()) :
    Operators {

    override fun toString(): String {
        return "$token${if (children.isEmpty()) "" else " $children"}"
    }

    companion object {
        class Stacks(val operators: Stack<Token> = Stack(), val operands: Stack<TokenNode> = Stack())

        fun getTree(tokens: List<Token>): TokenNode {
            return brackets(expand(tokens))[0]

        }

        fun expand(tokens: List<Token>) :List<Token> {
            var i: Int = 0
            val nodes:MutableList<Token> = mutableListOf()

            while (tokens.size > i) {
                val token: Token = tokens[i]
                if(i>0 && token.type==TokenType.variable && (tokens[i-1].subTypes==VariableType.constant||tokens[i-1].type==TokenType.numeric|| tokens[i-1].subTypes==BracketsType.closing)) {
                    if(tokens[i-1].subTypes==VariableType.constant||tokens[i-1].type==TokenType.numeric) {
                        val last = tokens[i-1]
                        nodes.removeLast()
                        nodes.add(Token.opening)
                        nodes.add(last)
                        nodes.add(Token.mul)
                        nodes.add(token)
                        nodes.add(Token.closing)
                    }else {
                        nodes.add(Token.mul)
                        nodes.add(token)
                    }
                }else {
                    nodes.add(token)
                }
                i++
            }

            return nodes
        }

        fun brackets(tokens: List<Token>): List<TokenNode> {
            var i: Int = 0
            val nodes:MutableList<TokenNode> = mutableListOf()

            while (tokens.size > i) {
                val token: Token = tokens[i]
                nodes.add(TokenNode(token))
                if (token.subTypes == BracketsType.opening) {
                    val closing: Int = getMatchingClosing(tokens, i)
                    val tokenNodes: List<TokenNode> = comma(tokens.subList(i+1, closing ))
                    nodes.removeLast()
                    if(i==0|| tokens[i-1].type==TokenType.operators) {
                       if(tokenNodes.size!=1) {
                           throw ExpressionException()
                       } else {
                           nodes.add(tokenNodes[0])
                       }
                    } else {
                        nodes.removeLast()
                        nodes.add(TokenNode(tokens[i-1], tokenNodes.toMutableList()))
                    }
                    i = closing
                }

                i++
            }

            return listOf( operators(nodes))
        }

        fun comma(tokens:List<Token>): List<TokenNode> {
            var lastIndex = 0
            var bracketStackCount = 0
            var i = 0
            var list: MutableList<TokenNode> = mutableListOf()

            while(i<tokens.size) {

                if(tokens[i].type == TokenType.brackets) {
                    when(val sub = tokens[i].subTypes as BracketsType) {
                        BracketsType.opening -> bracketStackCount ++
                        BracketsType.closing -> bracketStackCount --
                        BracketsType.comma ->
                            if(bracketStackCount==0) {
                                val l = brackets(tokens.subList(lastIndex, i))
                                if(l.size == 1) {

                                    list.add(l[0])
                                } else {
                                    throw ExpressionException()
                                }
                                lastIndex = i+1
                            }

                    }
                }

                i++
            }

            if(lastIndex<tokens.size) {
                val l = brackets(tokens.subList(lastIndex, tokens.size))
                if(l.size == 1) {

                    list.add(l[0])
                } else {
                    throw ExpressionException()
                }
            }

            return list
        }

        fun signOperator(tokens: List<TokenNode>): List<TokenNode> {
            var i: Int = 0

            val nodes:MutableList<TokenNode> = mutableListOf()

            while (i<tokens.size) {
                val token: Token = tokens[i].token
                nodes.add(tokens[i])
                if (token.subTypes == OperatorType.sub || token.subTypes == OperatorType.add) {
                    if (i == 0 || (tokens[i - 1].token.type == TokenType.operators && tokens[i-1].children.isEmpty())) {
                        if(i<(tokens.size-1) && (tokens[i + 1].token.type != TokenType.operators || tokens[i+1].children.isNotEmpty())) {
                            var node = tokens[i + 1]
                            var op = when (nodes.last().token.subTypes) {
                                OperatorType.sub -> Token.neg
                                OperatorType.pos -> Token.pos
                                else -> null
                            }
                            do {
                                nodes.removeLast()
                                node = TokenNode(op!!, mutableListOf(node))
                                nodes.add(node)
                                op = when (nodes[nodes.size-2].token.subTypes) {
                                    OperatorType.sub -> Token.neg
                                    OperatorType.pos -> Token.pos
                                    else -> null
                                }
                            } while(op!=null && nodes[nodes.size-2].token.type==TokenType.operators)
                            i++
                        }
                    }
                }
                i++
            }
            return nodes
        }

        fun operators(t: List<TokenNode>): TokenNode {
            val stacks: Stacks = Stacks()
            var i: Int = 0

            val tokens = signOperator(t)

            while (i<tokens.size) {
                val token: Token = tokens[i].token

               if(token.type==TokenType.operators && tokens[i].children.isEmpty()) {
                   val subTypes = token.subTypes as OperatorType
                    if(stacks.operators.isEmpty()) {
                        stacks.operators.push(token)
                    } else if((stacks.operators.peek().subTypes as OperatorType).priority().index > subTypes.priority().index) {
                        val op2 = stacks.operands.pop()
                        val op1 = stacks.operands.pop()
                        val o = stacks.operators.pop()
                        stacks.operands.push(TokenNode(o, listOf(op1, op2)))
                        stacks.operators.push(token)
                    } else {
                        stacks.operators.push(token)
                    }
                } else {
                    stacks.operands.push(tokens[i])
               }

                i++
            }

            while(stacks.operators.isNotEmpty()) {
                var o = stacks.operators.pop()
                val ops = mutableListOf(stacks.operands.pop())
                ops.add(0, stacks.operands.pop())
                while(stacks.operators.isNotEmpty() && (stacks.operators.peek().subTypes as OperatorType).priority().index == (o.subTypes as OperatorType).priority().index) {
                    ops.add(0, stacks.operands.pop())
                    o = stacks.operators.pop()
                }
                stacks.operands.push(TokenNode(o, ops))
            }

            if(stacks.operands.size==1) {
                return stacks.operands.pop()
            } else {
                throw ExpressionException()
            }

        }

        fun getMatchingClosing(tokens: List<Token>, i: Int): Int {
            var openingBracketCount = 0
            for (index: Int in i until tokens.size) {
                if (tokens[index].subTypes == BracketsType.opening) {
                    openingBracketCount++
                } else if (tokens[index].subTypes == BracketsType.closing) {
                    if (openingBracketCount == 0) {
                        return i
                    }
                    openingBracketCount--
                    if (openingBracketCount == 0) {
                        return index
                    }
                }
            }
            throw ExpressionException()

        }
    }
}

class ExpressionException : Exception("Expression Error")

enum class Priority(val index: Int) {
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
    fun patterns(): Array<out SubPattern>

    fun pattern(): String {
        return "[ \\t\\r\\n\\v\\f]*${patterns().joinToString("[ \\t\\r\\n\\v\\f]*|[ \\t\\r\\n\\v\\f]*") { sp -> sp.pattern() }}[ \\t\\r\\n\\v\\f]*"
    }

    fun compile(): Pattern? {
        return Pattern.compile(pattern())
    }
}

interface SubPattern {
    fun pattern(): String
    fun compile(): Pattern? {
        return Pattern.compile(pattern())
    }

    fun getName(): String
}

enum class TokenType(private val subTypes: Array<out SubPattern>) : Patterns {
    variable(VariableType.values()),
    numeric(NumericType.values()),
    brackets(BracketsType.values()),
    operators(OperatorType.values()), ;

    override fun patterns(): Array<out SubPattern> = subTypes


}

enum class VariableType(private val pattern: String) : SubPattern {
    ln("ln"),
    log("log"),
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
    variable("x[0-9]*|y[0-9]*"),
    constant("a[0-9]*|b[0-9]*|c[0-9]*|e|pi"),
    d("d"),
    function("fx[0-9]*|fy[0-9]*|f[0-9]*");

    override fun pattern(): String = pattern
    override fun getName(): String = name
}

enum class NumericType(private val pattern: String) : SubPattern {
    real("[0-9]*\\.[0-9]+"),
    integer("[0-9]+"), ;

    override fun pattern(): String = pattern
    override fun getName(): String = name
}


enum class BracketsType(private val pattern: String) : SubPattern, Operators {
    opening("\\("),
    comma("\\,"),
    closing("\\)"), ;

    override fun pattern(): String = pattern
    override fun getName(): String = name
}

enum class OperatorType(private val pattern: String, private val priority: Priority) : SubPattern, Operators, Nodable {
    add("\\+", Priority.low),
    sub("\\-", Priority.veryLow),
    mul("\\*", Priority.medium),
    div("\\/", Priority.high),
    pow("\\^", Priority.veryHigh),
    mod("\\%", Priority.extremeHigh),
    pos("\\+", Priority.low),
    neg("\\-", Priority.veryLow), ;

    override fun pattern(): String = pattern
    override fun getName(): String = name
    override fun priority(): Priority = priority
}