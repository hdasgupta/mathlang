package math.lang.gui

import math.lang.common.Operand
import javax.swing.JTextPane

class OperandPanel(private val operand: Operand, val center: Boolean = false) : JTextPane() {
    /*init {
        if(operand is Operation) {
            if (operand.operator==Operators.div) {
                layout = BorderLayout()
                val panel1: OperandPanel = OperandPanel(operand.operands[0])
                val panel2: OperandPanel = OperandPanel(operand.operands[1])
                add(BorderLayout.NORTH, panel1)
                add(BorderLayout.CENTER, HorizontalRule(0))
                add(BorderLayout.SOUTH, panel2)
            } else {
                if(operand.operands.size==1) {
                    layout = BorderLayout()
                    val label: JLabel = JLabel("${operand.operator.symbol}(${operand.operands[0]})")
                    add(BorderLayout.CENTER, label)
                } else {
                    layout = FlowLayout()
                    add(JLabel("("))
                    var panel: OperandPanel = OperandPanel(operand.operands[0])
                    add(panel)
                    for(i: Int in 1 until operand.operands.size) {
                        add(JLabel(operand.operator.symbol))
                        panel = OperandPanel(operand.operands[i])
                        add(panel)
                    }
                    add(JLabel(")"))
                }
            }
        } else if(operand is Differentiate) {
            layout = BorderLayout()
            val panel1:JPanel = JPanel(BorderLayout())
            val label1: JLabel = JLabel("d")
            val label2: JLabel = JLabel("d${operand.func() ?: "x"}")
            panel1.add(BorderLayout.NORTH, label1)
            panel1.add(BorderLayout.CENTER, HorizontalRule(0))
            panel1.add(BorderLayout.SOUTH, label2)
            add(BorderLayout.WEST, panel1)
            var panel: OperandPanel
            if(operand.operand!=null) {
                panel = OperandPanel(operand.operand, true)

            } else {
                panel = operand.function?.let { OperandPanel(it, true) }!!

            }
            panel.alignmentX = CENTER_ALIGNMENT
            panel.alignmentY = CENTER_ALIGNMENT
            add(BorderLayout.CENTER, panel)
        } else {
            layout = BorderLayout()
            val label: JLabel = JLabel(if(forceBracket && !operand.toString().startsWith("(")) "($operand)" else "$operand")
            label.alignmentX = CENTER_ALIGNMENT
            label.alignmentY = CENTER_ALIGNMENT
            add(BorderLayout.CENTER, label)
        }
        alignmentX = CENTER_ALIGNMENT
        alignmentY = CENTER_ALIGNMENT
    }*/

    init {
        //isEditable = false
        contentType = "text/html"
        //font = Font(Font.MONOSPACED, Font.PLAIN, 1)
        text =
            "<html><body style=\"font-family: monospace; font-size:8px\">${if (center) "<center>" else ""}$operand${if (center) "</center>" else ""}</body></html>"
    }
}