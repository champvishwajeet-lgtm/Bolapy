package in.bolpay.app

data class PaymentModel(
    val amount: String = "",
    val source: String = "",
    val time: Long = 0L
)
