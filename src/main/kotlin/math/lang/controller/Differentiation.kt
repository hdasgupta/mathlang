package math.lang.controller

import math.lang.Results
import math.lang.common.Differentiate
import math.lang.common.Operand
import math.lang.diff
import math.lang.tokenizer.Token
import math.lang.tokenizer.TokenNode
import math.lang.tokenizer.getOperand
import org.springframework.stereotype.Controller
import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpSession

@Controller
class Differentiation {
    @RequestMapping(value = ["/differentiate"])
    fun getTemplate(@RequestParam(required = false) formula: String? = null, map: ModelMap, req: HttpServletRequest): String {
        map["formula"] = formula

        return "DifferentiationPage"
    }

    @RequestMapping(value = ["/html"])
    @ResponseBody
    fun getHtml(@RequestParam(required = false) formula: String = "", @RequestParam(required = false) center: Boolean = false): String {
        return if(formula.isEmpty()) {
            ""
        } else {
            try {
                val operand: Operand = getOperand(TokenNode.getTree(Token.getTokens(formula)))

                "<body style=\"font-family: monospace; font-size:8px\">${if(center) "<center>" else ""}${operand.toHtmlString(true)}${if(center) "</center>" else ""}</body>"
            } catch (t: Throwable) {
                println(formula)
                "<body color=\"red\">Error in Expression</body> "
            }
        }
    }
    @RequestMapping(value = ["/diff"])
    @ResponseBody
    fun getDiff(@RequestParam(required = false) formula: String = "", @RequestParam(required = false) center: Boolean = false): String {
        return if(formula.isEmpty()) {
            ""
        } else {
            try {
                val operand: Operand = Differentiate(operand = getOperand(TokenNode.getTree(Token.getTokens(formula))))

                "<body style=\"font-family: monospace; font-size:8px\">${if(center) "<center>" else ""}${operand.toHtmlString(true)}${if(center) "</center>" else ""}</body>"
            } catch (t: Throwable) {
                println(formula)
                "<body color=\"red\">Error in Expression</body> "
            }
        }
    }

    @RequestMapping(value = ["/diffHtml"])
    fun getDiffHtml(@RequestParam(required = false) formula: String = "", @RequestParam(required = false) center: Boolean = false, map: ModelMap): String {
        try {
            val results: Results = diff(getOperand(TokenNode.getTree(Token.getTokens(formula))))
            map["results"] = results
            map["formula"] = formula
        } catch (t:Throwable) {
            map["results"] = Results()
        }

        return "DifferentiationResult"
    }

}
