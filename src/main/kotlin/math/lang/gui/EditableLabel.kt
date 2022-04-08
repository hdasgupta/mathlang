package math.lang.gui

import java.awt.CardLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

class EditableLabel(private var text : String) : JPanel(CardLayout()) {
    val label = JLabel(text)
    val textfield = JTextField()
    fun getText():String = text
    fun setText(value: String) {
        this.text = value
        label.text = value
    }
    init {
        add(label, "label component")
        add(textfield, "textfield component")
        label.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(evt: MouseEvent?) {
                textfield.text = label.text
                (layout as CardLayout).show(this@EditableLabel, "textfield component")
            }
        })
        textfield.addMouseListener(object : MouseAdapter() {
            override fun mouseExited(evt: MouseEvent?) {
                label.text = textfield.text
                (layout as CardLayout).show(this@EditableLabel, "label component")
            }
        })

    }
}