package math.lang.gui

import math.lang.common.Differentiate
import math.lang.Formula
import java.awt.BorderLayout
import javax.swing.JLabel
import javax.swing.JPanel

class FormulaPanel(val formula: Formula) : JPanel() {
    init {
        layout = BorderLayout()
        val diff: OperandPanel = OperandPanel(Differentiate(operand = formula.fx(listOf())))
        val equals : JLabel = JLabel(" = ")
        val sol:OperandPanel = OperandPanel(formula.dFx(listOf()))
        diff.alignmentX = CENTER_ALIGNMENT
        diff.alignmentY = CENTER_ALIGNMENT
        equals.alignmentX = CENTER_ALIGNMENT
        equals.alignmentY = CENTER_ALIGNMENT
        sol.alignmentX = CENTER_ALIGNMENT
        sol.alignmentY = CENTER_ALIGNMENT
        add(BorderLayout.WEST, diff)
        add(BorderLayout.CENTER, equals)
        add(BorderLayout.EAST, sol)
        if(formula.name!=null) {
            val label: JLabel = JLabel(formula.name)
            label.alignmentX = CENTER_ALIGNMENT
            label.alignmentY = CENTER_ALIGNMENT
            add(BorderLayout.SOUTH, label)
        }
        alignmentX = CENTER_ALIGNMENT
        alignmentY = CENTER_ALIGNMENT
    }
}