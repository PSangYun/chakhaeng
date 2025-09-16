package com.sos.chakhaeng.presentation.navigation

sealed interface BaseRoute

sealed class NoBottomRoute(val route: String) : BaseRoute {
    object Login : NoBottomRoute("login")
    object ReportDetail : NoBottomRoute("report_detail/{reportId}")
    object ViolationDetail : NoBottomRoute("violation_detail")

    companion object {
        fun reportDetail(reportId: String) = "report_detail/$reportId"
    }
}

sealed class Routes(val route: String) : BaseRoute {
    object Home : Routes("home")
    object Detection : Routes("detection")
    object Report : Routes("report")
    object Statistics : Routes("statistics")
    object Profile : Routes("profile")
    object Streaming : Routes("streaming")
}

fun String?.shouldShowBottomBar(): Boolean {
    if (this == null) return false

    val noBottomRoutes = listOf(
        NoBottomRoute.Login.route,
        NoBottomRoute.ReportDetail.route,
        NoBottomRoute.ViolationDetail.route
    )

    return noBottomRoutes.none { noBottomRoute ->
        this == noBottomRoute ||
                (noBottomRoute.contains("{") && this.startsWith(noBottomRoute.substringBefore("{")))
    }
}