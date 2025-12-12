package top.goodboyboy.wolfassistant.util

import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import top.goodboyboy.wolfassistant.ui.schedulecenter.model.LabScheduleItem

object ParseLabScheduleUtil {
    fun parseCourseTable(html: String): Map<Int, List<LabScheduleItem?>> {
        val doc = Jsoup.parse(html)
        // 获取表格主体中的所有行
        val rows = doc.select(".qz-weeklyTable-thbody tr")

        val result = mutableMapOf<Int, List<LabScheduleItem?>>()

        // 使用 chunked 方法将行按每6行分一组，每一组代表一周
        // 假设HTML结构是完美的，每6行为一个周期
        val weeksChunks = rows.chunked(6)

        for (weekRows in weeksChunks) {
            // 1. 获取周次
            val weekNumText = weekRows[0].selectFirst("td[rowspan] .index-title")?.text()
            val weekNum = weekNumText?.toIntOrNull() ?: throw ParseException("解析周次失败！")

            val weekScheduleList = mutableListOf<LabScheduleItem?>()

            // 2. 遍历这一周的6行（6个大节）
            for ((rowIndex, row) in weekRows.withIndex()) {
                val cells = row.children()

                // 计算偏移量：
                // 第1行(rowIndex=0) 前两个td是 [周次, 节次名]，数据从下标2开始
                // 第2-6行 前一个td是 [节次名]，数据从下标1开始
                val offset = if (rowIndex == 0) 2 else 1

                // 3. 遍历周一到周日 (共7列)
                for (i in 0 until 7) {
                    val cellIndex = offset + i
                    if (cellIndex < cells.size) {
                        val cell = cells[cellIndex]
                        // 解析单元格
                        weekScheduleList.add(parseCell(cell))
                    } else {
                        // 防止越界，虽然在标准表格中不应该发生
                        weekScheduleList.add(null)
                    }
                }
            }

            // 将当前周的数据存入 Map
            result[weekNum] = weekScheduleList
        }

        return result
    }

    /**
     * 解析单个单元格数据
     */
    fun parseCell(td: Element): LabScheduleItem? {
        // 检查是否存在课程信息的 tooltip 容器
        val tooltip = td.selectFirst(".qz-tooltipContent") ?: return null

        // 1. 获取课程名称
        val name = tooltip.selectFirst(".qz-tooltipContent-title")?.text()?.trim() ?: ""

        // 2. 获取详情列表
        val details = tooltip.select(".qz-tooltipContent-detailitem")

        var code = ""
        var clazz = ""
        var loc = ""
        var sec = ""

        // 遍历详情，根据前缀提取数据
        for (detail in details) {
            val text = detail.text().trim()
            when {
                text.startsWith("课程编号：") -> code = text.substringAfter("课程编号：")
                text.startsWith("班级：") -> clazz = text.substringAfter("班级：")
                text.startsWith("地址：") -> loc = text.substringAfter("地址：")
                text.startsWith("节次：") -> sec = text.substringAfter("节次：")
            }
        }

        return LabScheduleItem(
            courseName = name,
            courseCode = code,
            className = clazz,
            location = loc,
            section = sec,
        )
    }

    class ParseException(
        message: String,
    ) : Exception(message)
}
