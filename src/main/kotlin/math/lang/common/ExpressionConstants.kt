package math.lang.common

import kotlin.math.E
import kotlin.math.PI;
import java.lang.Double;

class ExpressionConstants {
    companion object {
        val a = Constant("a")
        val e = DecimalLiteral(E, "e")
        val pi = DecimalLiteral(PI, "pi")
        val x = Variable("x")
        val y = Variable("y")
        val fx1 = func(x)
        val fx2 = func(x)
        val dfx1 = Differentiate(fx1)
        val dfx2 = Differentiate(fx2)
        val one = IntegerLiteral(1)
        val two = IntegerLiteral(2)
        val zero = IntegerLiteral(0)
        val half = DecimalLiteral(.5)

        fun isConst(o: Operand, num:Int): Boolean =
            when(o) {
                is IntegerLiteral -> o.obj == num
                is Constant -> if(o.lit !=null) o.lit!!.obj == num else false
                else -> false
            }

        inline fun <reified R: Number>  getConst(o: Operand): R? =
            when(o) {
                is Literal<*> -> if( o.obj is R) o.obj else null
                is Constant -> if(o.lit !=null) if(o.lit!!.obj is R) o.lit!!.obj as R else null else null
                else -> null
            }

        fun op(operators: Operators, vararg operands: Operand): Operation = Operation(operators, *operands)

        fun int(obj: Number): IntegerLiteral = IntegerLiteral(Integer.parseInt(obj.toString()))

        fun real(obj: Number): DecimalLiteral = DecimalLiteral(java.lang.Double.parseDouble(obj.toString()))

        fun str(obj: String): StringLiteral = StringLiteral(obj)

        fun bool(obj:Boolean): BooleanLiteral = BooleanLiteral(obj)

        fun add(vararg operands: Operand): Operand =
            when(operands.count { !isConst(it, 0) }) {
                0 -> zero
                1 -> operands.first { !isConst(it, 0) }
                else -> op(Operators.add,  *operands.filter { !isConst(it, 0) }.toTypedArray())
            }


        fun sub(vararg operands: Operand): Operand =
            if(isConst(operands[0], 0))
                neg(operands[1])
            else if(isConst(operands[1], 0))
                operands[0]
            else op(Operators.sub, operands[0], operands[1])



        fun mul(vararg operands: Operand): Operand {
            return if (operands.any { isConst(it, 0) })
                zero
            else if (operands.count { !isConst(it, 1) } == 0)
                one
            else if (operands.count { !isConst(it, 1) } == 1)
                operands.first { !isConst(it, 1) }
            else op(Operators.mul, *operands.filter { !isConst(it, 1) }.toTypedArray())
        }
        fun div(vararg operands: Operand): Operand =
            if(isConst(operands[0], 0))
                zero
            else if(isConst(operands[1], 0))
                Undefined()
            else if(isConst(operands[1], 1))
                operands[0]
            else
                op(Operators.div, operands[0], operands[1])

        fun eq(vararg operands: Operand): Operation = op(Operators.eq, operands[0], operands[1])

        fun gt(vararg operands: Operand): Operation = op(Operators.gt, operands[0], operands[1])

        fun gte(vararg operands: Operand): Operation = op(Operators.gte, operands[0], operands[1])

        fun lt(vararg operands: Operand): Operation = op(Operators.lt, operands[0], operands[1])

        fun lte(vararg operands: Operand): Operation = op(Operators.lte, operands[0], operands[1])

        fun mod(vararg operands: Operand): Operation = op(Operators.mod, operands[0], operands[1])

        fun pow(vararg operands: Operand): Operand =
            if(isConst(operands[1], 0))
                one
            else if(isConst(operands[1], 1))
                operands[0]
            else if(isConst(operands[0], 0))
                zero
            else if(isConst(operands[0], 1))
                one
            else
                op(Operators.pow, operands[0], operands[1])

        fun sqr(vararg operands: Operand): Operation = op(Operators.pow, operands[0], two)

        fun sqrt(vararg operands: Operand): Operation = op(Operators.pow, operands[0], half)

        fun log(vararg operands: Operand): Operation = op(Operators.log, operands[0], operands[1])

        fun ln(vararg operand: Operand): Operation = op(Operators.ln, operand[0])

        fun neg(vararg operand: Operand): Operand = Operand.negate(if(operand[0] is Variable) (operand[0] as Variable).clone() else if(operand[0] is Constant) (operand[0] as Constant).clone() else operand[0])

        fun inv(vararg operand: Operand): Operand = Operand.invert(if(operand[0] is Variable) (operand[0] as Variable).clone() else if(operand[0] is Constant) (operand[0] as Constant).clone() else operand[0])

        fun sin(operand: Operand): Operation = op(Operators.sin, operand)

        fun cos(operand: Operand): Operation = op(Operators.cos, operand)

        fun tan(operand: Operand): Operation = op(Operators.tan, operand)

        fun cot(operand: Operand): Operation = op(Operators.cot, operand)

        fun sec(operand: Operand): Operation = op(Operators.sec, operand)

        fun cosec(operand: Operand): Operation = op(Operators.csc, operand)

        fun r2d(operand: Operand): Operation = op(Operators.rad2deg, operand)

        fun d2r(operand: Operand): Operation = op(Operators.deg2rad, operand)

        fun undef(): Undefined = Undefined()

        fun asin(operand: Operand): Operation = op(Operators.asin, operand)

        fun acos(operand: Operand): Operation = op(Operators.acos, operand)

        fun atan(operand: Operand): Operation = op(Operators.atan, operand)

        fun acot(operand: Operand): Operation = op(Operators.acot, operand)

        fun asec(operand: Operand): Operation = op(Operators.asec, operand)

        fun acosec(operand: Operand): Operation = op(Operators.acsc, operand)

        fun sinh(operand: Operand): Operation = op(Operators.sinh, operand)

        fun cosh(operand: Operand): Operation = op(Operators.cosh, operand)

        fun tanh(operand: Operand): Operation = op(Operators.tanh, operand)

        fun coth(operand: Operand): Operation = op(Operators.coth, operand)

        fun sech(operand: Operand): Operation = op(Operators.sech, operand)

        fun cosech(operand: Operand): Operation = op(Operators.csch, operand)

        fun func(vararg variables: Variable): Function = Function("fx", variables.toSet())

        fun func(function: Function): Function = Function("fx", function = function)

        fun d(function: Function): Differentiate = Differentiate(function)

        fun d(operand: Operand): Differentiate = Differentiate(operand = operand)

        inline fun <reified T: Literal<*>> const(lit: T) = Constant(lit)

        fun convert(operand: Operand): Operand {
            return if(operand is Operation) {
                Operation(operand.operator, *operand.operands.map { op-> convert(op) }.toTypedArray())
            } else if(operand is IntegerLiteral) {
                const(operand)
            } else if(operand is DecimalLiteral) {
                const(operand)
            } else if(operand is StringLiteral) {
                const(operand)
            } else if(operand is BooleanLiteral) {
                const(operand)
            } else {
                operand
            }
        }

        fun compare(op1: Operand, op2: Operand) : Boolean {
            if(op1 is Constant && op2 is Constant) {
                return op1.name == op2.name
            }
            if(op1 is Variable && op2 is Variable) {
                return op1.name == op2.name
            }
            return false
        }

        fun replace(root: Operand, source: Operand, target: Operand): Operand =
            when(root) {
                is Operation -> Operation(root.operator, *root.operands.map { op-> if(compare(op,source)) target else replace(op, source, target) }.toTypedArray())
                is Literal<*> -> const(root)
                else -> root
            }

        fun isConst(operand: Operand): Boolean =
            when(operand) {
                is Operation -> operand.operands.all { isConst(it) }
                is Literal<*> -> true
                is Constant -> true
                else -> false
            }

        fun hasValue(operand: Operand):Boolean =
            when(operand) {
                is Operation -> operand.operands.all { hasValue(it) }
                is IntegerLiteral -> true
                is DecimalLiteral -> true
                is Constant -> if(operand.lit!=null) hasValue(operand.lit!!) else false
                else -> false
            }

        fun getValue(operand: Operand):Number? =
            if(hasValue(operand)) {
                operand.calc(true)
            } else {
                null
            }


        fun constIn(operand: Operand): Constant? {
            return if(operand is Operation) {
                val consts: List<Constant?> = operand.operands.filter {
                        operand -> constIn(operand) !=null
                }.map {
                        operand->
                    constIn(operand)
                }
                if(consts.isEmpty()) {
                    null
                } else {
                    consts.first()
                }
            } else if(operand is Constant) {
                operand
            } else {
                null
            }
        }

        fun varIn(operand: Operand): Set<Variable> {
            return if(operand is Operation) {
                val vars: List<Variable> = operand.operands.filter {
                        operand -> varIn(operand) !=null
                }.flatMap {
                        operand->
                    varIn(operand)
                }
                if(vars.isEmpty()) {
                    setOf()
                } else {
                    vars.toSet()
                }
            } else if(operand is Variable) {
                setOf(operand)
            } else {
                setOf()
            }
        }
    }

}