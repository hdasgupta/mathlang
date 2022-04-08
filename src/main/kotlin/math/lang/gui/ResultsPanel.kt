package math.lang.gui

import math.lang.*
import math.lang.common.Differentiate
import math.lang.common.Operand
import java.awt.Component
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.util.*
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable
import javax.swing.event.CellEditorListener
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
import kotlin.collections.ArrayList


class ResultsPanel(private val results: Results) : JTable(results.map { result-> listOf(result.operand, Differentiate(operand = result.formulaApplied.fx), result.formulaApplied.dFx, result.formulaApplied.name, result.assumption).toTypedArray() }.toTypedArray(), listOf("result", "d(formula)/dx", "formula out", "Formula name",  "assumption").toTypedArray()) {
    init {
        /*dataModel = ResultTableModel(results)*/
        columnModel.getColumn(0).cellEditor = OperandCellEditor()
        columnModel.getColumn(0).cellRenderer = OperandCellRenderer()

        columnModel.getColumn(1).cellEditor = OperandCellEditor()
        columnModel.getColumn(1).cellRenderer = OperandCellRenderer()

        columnModel.getColumn(2).cellEditor = OperandCellEditor(true)
        columnModel.getColumn(2).cellRenderer = OperandCellRenderer(true)

        columnModel.getColumn(4).cellEditor = OperandCellEditor(true)
        columnModel.getColumn(4).cellRenderer = OperandCellRenderer(true)
//
//        columnModel.getColumn(1).cellEditor = FormulaCellEditor()
//        columnModel.getColumn(1).cellRenderer = FormulaCellRenderer()

        updateRowHeights()

        addComponentListener(ResultComponentAdapter(this))
    }


    fun updateRowHeights() {
        val columnsWidth:ArrayList<Int> = ArrayList(columnCount)
        for (row in 0 until rowCount) {
            var rowHeight: Int = getRowHeight()
            for (column in 0 until columnCount) {
                val comp: Component = prepareRenderer(getCellRenderer(row, column), row, column)
                rowHeight = rowHeight.coerceAtLeast(comp.preferredSize.height)
                if(row==0) {
                    columnsWidth.add(comp.preferredSize.width)
                } else {
                    columnsWidth[column] = columnsWidth[column].coerceAtLeast(comp.preferredSize.width)
                }
            }
            setRowHeight(row, rowHeight)
        }
        for (column in 0 until columnCount) {
            columnModel.getColumn(column).preferredWidth = columnsWidth[column]
        }
    }
}

class OperandCellEditor(val center: Boolean = false): TableCellEditor {
    override fun getCellEditorValue(): Any = 1

    override fun isCellEditable(anEvent: EventObject?): Boolean = true

    override fun shouldSelectCell(anEvent: EventObject?): Boolean = true

    override fun stopCellEditing(): Boolean = false

    override fun cancelCellEditing() {

    }

    override fun addCellEditorListener(l: CellEditorListener?) {

    }

    override fun removeCellEditorListener(l: CellEditorListener?) {

    }

    override fun getTableCellEditorComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        return if(value!=null) JScrollPane(OperandPanel(value as Operand, center), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) else JPanel()
    }

}

class OperandCellRenderer(val center: Boolean = false): TableCellRenderer {
    override fun getTableCellRendererComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        hasFocus: Boolean,
        row: Int,
        column: Int
    ): Component {
        return if(value!=null) JScrollPane(OperandPanel(value as Operand, center), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER) else JPanel()
    }

}

class ResultComponentAdapter(private val resultsPanel: ResultsPanel): ComponentAdapter() {
    override fun componentResized(e: ComponentEvent?) {
        super.componentResized(e)
        println("Resized")
        resultsPanel.updateRowHeights()
    }
}