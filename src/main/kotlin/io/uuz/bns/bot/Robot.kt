package io.uuz.bns.bot

import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.contact.Contact.Companion.sendImage
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.event.globalEventChannel
import net.mamoe.mirai.event.selectMessages
import net.mamoe.mirai.utils.BotConfiguration
import java.io.ByteArrayInputStream
import java.lang.Exception

class Robot(val qq: Long, val password: String) {
    companion object {
        val PRICE = "[\\d\\.]+".toRegex()
    }

    suspend fun run() {
        val commander = Commander()
        val bot = BotFactory.newBot(qq, password) {
//            fileBasedDeviceInfo("device.json")
            protocol = BotConfiguration.MiraiProtocol.ANDROID_PHONE
        }
            .alsoLogin()
        bot.globalEventChannel()
            .subscribeAlways<GroupMessageEvent> {
                val msg = this.message.contentToString()
                Commander.KANDALAO.matchEntire(msg).let {
                    if (it != null) {
                        val nickname = it.groupValues[1]
                        try {
                            subject.sendImage(ByteArrayInputStream(commander.showCharacterImage(nickname)))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            subject.sendMessage("出错啦，重试哦")
                        }

                    }
                }
                Commander.CHAZHUANGBEI.matchEntire(msg).let {
                    if (it != null) {
                        val nickname = it.groupValues[1]
                        try {
                            subject.sendMessage(commander.showEquip(nickname))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            subject.sendMessage("出错啦，重试哦")
                        }
                    }
                }
                Commander.JINJIA.matchEntire(msg).let {
                    if (it != null) {
                        try {
                            subject.sendMessage(commander.GoldPrice("仙履奇缘"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            subject.sendMessage("出错啦，重试哦")
                        }
                    }
                }
                Commander.JINJIASERVER.matchEntire(msg).let {
                    if (it != null) {
                        val server = it.groupValues[1]
                        try {
                            subject.sendMessage(commander.GoldPrice(server))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            subject.sendMessage("出错啦，重试哦")
                        }
                    }
                }
                Commander.XIAYITUAN.matchEntire(msg).let {
                    if (it != null) {
                        val a = it.groupValues[1].toInt()
                        val b = it.groupValues[2].toInt()
                        try {
                            subject.sendMessage(commander.xiayi(a, b))
                        } catch (e: Exception) {
                            e.printStackTrace()
                            subject.sendMessage("出错啦，重试哦")
                        }
                    }
                }
                if (msg == "逃税") {
                    subject.sendMessage("请输入单价")
                    val s = selectMessages<GroupMessageEvent, Double?> {
                        matching(PRICE)
                            .invoke {
                                it.toDouble()
                            }
                        content {
                            PRICE.matchEntire(it) == null
                        }.invoke {
                            subject.sendMessage("格式错误")
                            null
                        }
                        timeout(30_000) {
                            subject.sendMessage("输入超时，请重试")
                            null
                        }
                    } ?: return@subscribeAlways
                    subject.sendMessage("请输入数量")
                    val c: Int = selectMessages<GroupMessageEvent, Int?> {
                        content {
                            it.matches(PRICE)
                        }.invoke {
                            it.toInt()
                        }
                        content {
                            PRICE.matchEntire(it) == null
                        }.invoke {
                            subject.sendMessage("格式错误")
                            null
                        }
                        timeout(30_000) {
                            subject.sendMessage("输入超时，请重试")
                            null
                        }
                    } ?: return@subscribeAlways
                    subject.sendMessage("请输入日收入")
                    val r: Double = selectMessages<GroupMessageEvent, Double?> {
                        content {
                            it.matches(PRICE)
                        }.invoke {
                            it.toDouble()
                        }
                        content {
                            PRICE.matchEntire(it) == null
                        }.invoke {
                            subject.sendMessage("格式错误")
                            null
                        }
                        timeout(30_000) {
                            subject.sendMessage("输入超时，请重试")
                            null
                        }
                    } ?: return@subscribeAlways
                    subject.sendMessage(commander.taoShui(s, c, r))
                }
            }
    }
}