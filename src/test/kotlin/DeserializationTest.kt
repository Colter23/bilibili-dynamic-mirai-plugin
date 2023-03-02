package top.colter

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.enable
import net.mamoe.mirai.console.plugin.PluginManager.INSTANCE.load
import net.mamoe.mirai.console.terminal.MiraiConsoleTerminalLoader
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import org.jetbrains.skia.Color
import org.junit.After
import org.junit.Before
import org.junit.Test
import top.colter.mirai.plugin.bilibili.BiliBiliDynamic
import top.colter.mirai.plugin.bilibili.data.DynamicItem
import top.colter.mirai.plugin.bilibili.draw.drawDynamic
import java.io.File

class DeserializationTest {
    @OptIn(ConsoleExperimentalApi::class)
    @Before
    fun initPlugin() = runBlocking {
        MiraiConsoleTerminalLoader.startAsDaemon()
        BiliBiliDynamic.load()
        BiliBiliDynamic.enable()
    }
    @Test
    fun decodeDynamic(): Unit = runBlocking {
        val decoder = Json{
            ignoreUnknownKeys = true
        }
        val t: DynamicItem = decoder.decodeFromString("""
            {
              "basic": {
                "comment_id_str": "226709313",
                "comment_type": 11,
                "is_only_fans": true,
                "jump_url": "//www.bilibili.com/opus/767166448722247682",
                "like_icon": {
                  "action_url": "https://i0.hdslb.com/bfs/garb/item/99d0b1d248c6ff87b18ab9f4fba4d89ecd642374.bin",
                  "end_url": "",
                  "id": 40543,
                  "start_url": ""
                },
                "rid_str": "226709313"
              },
              "id_str": "767166448722247682",
              "modules": {
                "module_author": {
                  "avatar": {
                    "container_size": {
                      "height": 1.35,
                      "width": 1.35
                    },
                    "fallback_layers": {
                      "is_critical_group": true,
                      "layers": [
                        {
                          "general_spec": {
                            "pos_spec": {
                              "axis_x": 0.675,
                              "axis_y": 0.675,
                              "coordinate_pos": 2
                            },
                            "render_spec": {
                              "opacity": 1
                            },
                            "size_spec": {
                              "height": 1,
                              "width": 1
                            }
                          },
                          "layer_config": {
                            "is_critical": true,
                            "tags": {
                              "AVATAR_LAYER": {},
                              "GENERAL_CFG": {
                                "config_type": 1,
                                "general_config": {
                                  "web_css_style": {
                                    "borderRadius": "50%"
                                  }
                                }
                              }
                            }
                          },
                          "resource": {
                            "res_image": {
                              "image_src": {
                                "placeholder": 6,
                                "remote": {
                                  "bfs_style": "widget-layer-avatar",
                                  "url": "https://i0.hdslb.com/bfs/face/84b8a4562e3df5e8f830cad6fcf4b84156659f08.jpg"
                                },
                                "src_type": 1
                              }
                            },
                            "res_type": 3
                          },
                          "visible": true
                        },
                        {
                          "general_spec": {
                            "pos_spec": {
                              "axis_x": 0.8000000000000002,
                              "axis_y": 0.8000000000000002,
                              "coordinate_pos": 1
                            },
                            "render_spec": {
                              "opacity": 1
                            },
                            "size_spec": {
                              "height": 0.41666666666666663,
                              "width": 0.41666666666666663
                            }
                          },
                          "layer_config": {
                            "tags": {
                              "GENERAL_CFG": {
                                "config_type": 1,
                                "general_config": {
                                  "web_css_style": {
                                    "background-color": "rgb(255,255,255)",
                                    "border": "2px solid rgba(255,255,255,1)",
                                    "borderRadius": "50%",
                                    "boxSizing": "border-box"
                                  }
                                }
                              },
                              "ICON_LAYER": {}
                            }
                          },
                          "resource": {
                            "res_image": {
                              "image_src": {
                                "local": 1,
                                "src_type": 2
                              }
                            },
                            "res_type": 3
                          },
                          "visible": true
                        }
                      ]
                    },
                    "mid": "107251863"
                  },
                  "face": "https://i0.hdslb.com/bfs/face/84b8a4562e3df5e8f830cad6fcf4b84156659f08.jpg",
                  "face_nft": false,
                  "following": null,
                  "icon_badge": {
                    "icon": "https://i0.hdslb.com/bfs/garb/item/33e2e72d9a0c855f036b4cb55448f44af67a0635.png",
                    "render_img": "https://i0.hdslb.com/bfs/activity-plat/static/20230112/3b3c5705bda98d50983f6f47df360fef/IN4E1b8HNg.png",
                    "text": "专属动态"
                  },
                  "jump_url": "//space.bilibili.com/107251863/dynamic",
                  "label": "",
                  "mid": 107251863,
                  "name": "Nachuan川川",
                  "official_verify": {
                    "desc": "",
                    "type": -1
                  },
                  "pendant": {
                    "expire": 0,
                    "image": "",
                    "image_enhance": "",
                    "image_enhance_frame": "",
                    "name": "",
                    "pid": 0
                  },
                  "pub_action": "",
                  "pub_location_text": "",
                  "pub_time": "2023-02-27 08:37",
                  "pub_ts": 1677458258,
                  "type": "AUTHOR_TYPE_NORMAL",
                  "vip": {
                    "avatar_subscript": 1,
                    "avatar_subscript_url": "",
                    "due_date": 1712419200000,
                    "label": {
                      "bg_color": "#FB7299",
                      "bg_style": 1,
                      "border_color": "",
                      "img_label_uri_hans": "",
                      "img_label_uri_hans_static": "https://i0.hdslb.com/bfs/vip/8d4f8bfc713826a5412a0a27eaaac4d6b9ede1d9.png",
                      "img_label_uri_hant": "",
                      "img_label_uri_hant_static": "https://i0.hdslb.com/bfs/activity-plat/static/20220614/e369244d0b14644f5e1a06431e22a4d5/VEW8fCC0hg.png",
                      "label_theme": "annual_vip",
                      "path": "",
                      "text": "年度大会员",
                      "text_color": "#FFFFFF",
                      "use_img_label": true
                    },
                    "nickname_color": "#FB7299",
                    "status": 1,
                    "theme_type": 0,
                    "type": 2
                  }
                },
                "module_dynamic": {
                  "additional": null,
                  "desc": null,
                  "major": {
                    "blocked": {
                      "bg_img": {
                        "img_dark": "https://i0.hdslb.com/bfs/activity-plat/static/20221216/c103299ba3500e5000d47f2f0f04712d/wBIsPss7VZ.png",
                        "img_day": "https://i0.hdslb.com/bfs/activity-plat/static/20221216/c103299ba3500e5000d47f2f0f04712d/eqeFwt8kUe.png"
                      },
                      "blocked_type": 1,
                      "button": {
                        "icon": "https://i0.hdslb.com/bfs/activity-plat/static/20230112/3b3c5705bda98d50983f6f47df360fef/qcRJ6sJU91.png",
                        "jump_url": "https://www.bilibili.com/h5/upower/index?navhide=1\u0026mid=107251863\u0026prePage=onlyFansDynMdlBlocked",
                        "text": "充电"
                      },
                      "hint_message": "该动态为包月充电专属\n可以给UP主充电后观看",
                      "icon": {
                        "img_dark": "https://i0.hdslb.com/bfs/activity-plat/static/20221216/c103299ba3500e5000d47f2f0f04712d/RP513ypCyt.png",
                        "img_day": "https://i0.hdslb.com/bfs/activity-plat/static/20221216/c103299ba3500e5000d47f2f0f04712d/8gweMAFDvP.png"
                      }
                    },
                    "type": "MAJOR_TYPE_BLOCKED"
                  },
                  "topic": null
                },
                "module_more": {
                  "three_point_items": [
                    {
                      "label": "举报",
                      "type": "THREE_POINT_REPORT"
                    }
                  ]
                },
                "module_stat": {
                  "comment": {
                    "count": 0,
                    "forbidden": false,
                    "hidden": true
                  },
                  "forward": {
                    "count": 0,
                    "disabled": true,
                    "forbidden": true
                  },
                  "like": {
                    "count": 0,
                    "forbidden": false,
                    "status": false
                  }
                }
              },
              "type": "DYNAMIC_TYPE_DRAW",
              "visible": true
            }
        """.trimIndent())
        val img = t.drawDynamic(Color.CYAN, false)
        File("src/test/resources/output/decodedDynamic.png").writeBytes(img.encodeToData()!!.bytes)
    }

    @OptIn(ConsoleExperimentalApi::class)
    @After
    fun cleanup() = runBlocking {
        MiraiConsole.shutdown()
    }
}