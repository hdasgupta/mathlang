package math.lang.gui

import math.lang.*
import math.lang.common.Differentiate
import math.lang.common.ExpressionConstants.Companion.a
import math.lang.common.ExpressionConstants.Companion.mul
import math.lang.common.ExpressionConstants.Companion.sin
import math.lang.common.ExpressionConstants.Companion.x
import math.lang.common.Operand
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand
import java.awt.BorderLayout
import java.awt.Color
import java.awt.event.*
import javax.swing.JFrame
import javax.swing.JScrollPane
import javax.swing.JTextField
import javax.swing.SwingUtilities
import javax.swing.border.LineBorder


fun main(args: Array<String>) {
    ResultsFrame(sin(mul(a, x)))
}

class ResultsFrame(val operand: Operand) : JFrame() {
    private var formula = OperandPanel(Differentiate(operand = operand))
    private var panel = ResultsPanel(diff(operand))
    private val jTextField = JTextField("$operand")
    private val defaultBorder = jTextField.border
    private val errorBorder = LineBorder(Color.RED, 1)
    private val jScrollPane = JScrollPane(panel)
    init {
        title = "ResultFrame"
        layout = BorderLayout()

        add(BorderLayout.NORTH, formula)
        add(BorderLayout.CENTER, jScrollPane)
        add(BorderLayout.SOUTH, jTextField)

        formula.isEditable = true
        formula.border = LineBorder(Color.BLACK, 1)

        jTextField.addKeyListener(KeyPressListener(this))
        jTextField.requestFocus()

        defaultCloseOperation = EXIT_ON_CLOSE
        setLocationRelativeTo(null)
        pack()
        extendedState = MAXIMIZED_BOTH
        isVisible = true
    }

    private class KeyPressListener(val rf: ResultsFrame) : KeyAdapter() {
        override fun keyReleased(e: KeyEvent?) {
            super.keyReleased(e)
            val isValid = e?.keyChar?.let { Character.isLetterOrDigit(it) || Character.isWhitespace(it) || Character.isSpaceChar(it) || "+-/*^%()".contains(it) }
            if(isValid != true) {
                return
            }
            SwingUtilities.invokeLater {

                rf.jTextField.border = rf.defaultBorder
                try {
                    var operand: Operand = getOperand(TokenNode.getTree(Token.getTokens(rf.jTextField.text)))
                    rf.remove(rf.formula)
                    rf.formula = OperandPanel(Differentiate(operand = operand))
                    rf.add(rf.formula)
                    rf.panel = ResultsPanel(diff(operand))
                    rf.jScrollPane.viewport.view = rf.panel
                    rf.repaint()
                } catch (t: Throwable) {
                    rf.jTextField.border = rf.errorBorder
                }
            }
        }
    }

}

