package top.goodboyboy.wolfassistant.ui.schedulecenter.model

data class LabScheduleItem(
    // 课程名称
    val courseName: String,
    // 课程编号
    val courseCode: String,
    // 班级
    val className: String,
    // 地址
    val location: String,
    // 节次
    val section: String,
)
