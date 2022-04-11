package math.lang.gui

import java.awt.Color
import java.awt.Graphics
import javax.swing.JPanel

class HorizontalRule(thickness: Int) : JPanel() {
    private val thickness: Int = if (thickness < 1) 1 else thickness


    init {
        //size = Dimension(size.width, thickness)
        //background = Color.BLACK

    }

    override fun paint(g: Graphics?) {
        super.paint(g)
        g?.color = Color.BLACK

        g?.fillRect(0, (height - thickness) / 2, width, thickness)
    }
}