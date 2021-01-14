package io.uuz.bns.bot

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import okhttp3.OkHttpClient
import org.jsoup.Jsoup
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class Commander {
    companion object {
        val client = OkHttpClient.Builder()
            .build()

        val KANDALAO = "看大佬\\s+(.+)".toRegex()
        val CHAZHUANGBEI = "查装备\\s+(.+)".toRegex()
        val JINJIASERVER = "金价\\s+(.+)".toRegex()
        val JINJIA = "金价".toRegex()
        val XIAYITUAN = "侠义团计算 (\\d+) (\\d+)".toRegex()

        val GoldPriceMap = mapOf(
            "仙履奇缘" to "https://www.dd373.com/s-5eu4fw-xj5vtb-d0dac1-0-0-0-84476u-0-0-0-0-0-1-0-5-0.html",
            "无日峰" to "https://www.dd373.com/s-5eu4fw-c-84476u-xj5vtb-5xm287.html",
            "绿明村" to "https://www.dd373.com/s-5eu4fw-c-84476u-xj5vtb-1w9e6t.html",
            "铁傀王" to "https://www.dd373.com/s-5eu4fw-c-84476u-xj5vtb-3khgvr.html",
            "飞扇堂" to "https://www.dd373.com/s-5eu4fw-c-84476u-xj5vtb-69rkkp.html",
            "落日星辉" to "https://www.dd373.com/s-5eu4fw-c-84476u-xj5vtb-856fgj.html",
            "元气无敌" to "https://www.dd373.com/s-5eu4fw-c-84476u-xj5vtb-h041rn.html"
        )
    }

    fun showCharacterImage(nickname: String): ByteArray {
        Playwright.create().use { playwright ->
            val browser = playwright.chromium().launch()
            try {
                val context = browser.newContext(Browser.NewContextOptions().withViewport(1067, 800))
                val page = context.newPage()
                try {
                    page.navigate(
                        "https://19gate.bns.qq.com/ingame/bs/character/profile?c=${nickname}",
                        Page.NavigateOptions().withTimeout(10 * 1000)
                    )
                    page.waitForFunction("() => !document.querySelector('.loading')").get()
                    page.evaluate("""
                        () => {
                          document.querySelector('#container').style.height = '800px';
                          document.querySelector('.wrapItem .accessoryArea').style.height = '460px';
                          document.querySelector('.statArea .stat').style.height = 'inherit';
                          document.querySelector('#me-stat > div.stat > div.attack > dl > dt:nth-child(15)').click();
                          document.querySelector('#me-stat > div.stat > div.attack > dl > dt:nth-child(25)').click();
                        }
                    """.trimIndent())
                    Thread.sleep(500)
                    val data = page.screenshot(Page.ScreenshotOptions().withOmitBackground(true))
                    return data
                } finally {
                    page.close()
                }
            } finally {
                browser.close()
            }
        }
    }

    fun showEquip(nickname: String): String {
        val request = okhttp3.Request.Builder()
            .url("https://19gate.bns.qq.com/ingame/bs/character/data/equipments?c=${nickname}")
            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36")
            .build()
        return client.newCall(request).execute().use { res ->
            val body = res.body?.string() ?: ""
            val doc = Jsoup.parse(body)
            "角色名: ${nickname}\n" + doc.select(".grade_7").map { it.text() }.toList().joinToString("\n")
        }
    }

    fun GoldPrice(server: String): String {
        val url = GoldPriceMap.get(server)
        if (url == null) {
            return "暂不支持该服务器"
        }
        val request = okhttp3.Request.Builder()
            .url(url)
            .header("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 11_1_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.141 Safari/537.36")
            .build()
        return client.newCall(request).execute().use { res ->
            val body = res.body?.string() ?: ""
            val doc = Jsoup.parse(body)
            val price = doc.selectFirst(".goods-list-item:first-child .colorFF5").text()
            val ddprice = doc.selectFirst(".line-height14.font12.colorFF5.t-m4.center").text()
            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
            val formatted = current.format(formatter)
            """
                「时间」${formatted}
                「区服」${server}
                「比例」${price}
                「平台比例」${ddprice}
                「来源」DD373
                「地址」${url}
            """.trimIndent()
        }
    }

    /**
     * 单笔交易费：100金以下5% 100-999金6% 1000-4999金7% 5000-9999金9% 1万以上10%
     * 日交易费: 500-999金1% 1000-1999金2% 2000-9999金3% 1万以上10%
     */
    fun taoShui(s: Double, c: Int, r: Double): String {
        val di = s * c + r;
        val ss: Double = when {
            s >= 10000 ->
                0.1
            s >= 5000 ->
                0.09
            s >= 1000 ->
                0.07
            s >= 100 ->
                0.06
            s > 0 ->
                0.05
            else ->
                0.0
        }
        val jj: Double = when {
            di > 9999 ->
                0.1
            di > 1999 ->
                0.03
            di > 999 ->
                0.02
            di > 500 ->
                0.01
            else ->
                0.0
        }

        val p = s * c
        val t = s * (ss + jj) * c
        val st = s * ss * c
        val dt = s * jj * c
        return """
            「出售手续费」${st}
            「日交易手续费」 ${dt}
            「总手续费」 ${t}
            「收益」 ${p}
        """.trimIndent()
    }

    fun xiayi(a: Int, b: Int): String {
        if (b < a) {
            return "假如时光倒流？"
        }
        val exp: Long  = (b - a) * 10000 + (a until b).toList().map{ it * it * 100L }.sum()
        return """
            ${a}升${b}侠义团需：${exp}经验
            10万经验/天需要：${Math.ceil(exp/100000.0).toInt()}天
            30万经验/天需要：${Math.ceil(exp/300000.0).toInt()}天
            50万经验/天需要：${Math.ceil(exp/500000.0).toInt()}天
        """.trimIndent()
    }
}