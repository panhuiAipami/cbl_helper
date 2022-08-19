package cn.cpocar.qyc_cbl.model

data class CallLogInfo(
        var id: Int,
        val name: String?, // 对方名称
        val number: String, // 对方号码
        val date: Long,
        val duration: Int, // 持续时间（秒）
        val type: Int //   1.呼入，2.呼出()，3.呼入未接
)