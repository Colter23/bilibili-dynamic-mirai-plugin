# BilibiliDynamic MiraiPlugin

一个可低延迟检测 B 站动态/直播并转发到 Q 群的 [Mirai](https://github.com/mamoe/mirai) 插件    

[![MiraiForum](https://img.shields.io/badge/post-on%20MiraiForum-yellow)](https://mirai.mamoe.net/topic/792)
![Stars](https://img.shields.io/github/stars/Colter23/bilibili-dynamic-mirai-plugin)
![Downloads](https://img.shields.io/github/downloads/Colter23/bilibili-dynamic-mirai-plugin/total)
[![Release](https://img.shields.io/github/v/release/Colter23/bilibili-dynamic-mirai-plugin)](https://github.com/Colter23/bilibili-dynamic-mirai-plugin/releases)

## V3版本重构中...
v3使用skiko绘图, 更加美观。同时增加稳定性与可配置性

## V3样式预览
<img src="docs/img/demo1.png" width="400" alt="样式1">    

## 特性    
**无论多少订阅均可在最低 10s 内检测所有动态**  
Java 原生 API 绘图  
可以设定检测内容： 动态(包括视频)   仅视频   直播    
直播@全体    
动态过滤    
扫码登录

### 基本原理
通过检测 [动态](https://t.bilibili.com/) 界面，检测账号关注的所有最新动态，再挑选出 QQ 订阅的动态，这样一个检测周期就可以检测所有最新动态    
因此，本插件需要一个 B 站账号来订阅用户。     
**强烈推荐使用小号** 如果让bot用你经常使用的账号可能会出现长期多次异地访问造成风控 [无法添加关注](https://github.com/Colter23/bilibili-dynamic-mirai-plugin/issues/40)    
(插件有扫码登陆和自动关注自动添加分组功能，详细见下文)

### 关于自动关注
如果账号没有关注过此人，bot 会自动关注并把他分到一个新分组中，方便管理    
是否开启自动关注以及新分组的名称都可以在配置文件中进行配置    
如果关闭自动关注，那么在 QQ 中订阅一个未关注的人时会提示 未关注    
如果你不想让 bot 自动关注又想检测动态，那么你可能不适合这个插件，请移步 cssxsh 大佬的[bilibili-helper](https://github.com/cssxsh/bilibili-helper)    

### 关于分组(如果你不知道b站关注可以分组的话请忽略)
开启自动关注后，bot 会生成一个新分组(分组名可以在配置中设置)，由 bot 新关注的人会被分到这个分组内，之前就关注过的不会移动    
如果你想让 bot 关注就保存在默认分组中，请在配置中替换 `followGroup` 后面的值为两个双引号 `""`

## 开始使用

### 下载
下载插件本体: [releases](https://github.com/Colter23/bilibili-dynamic-mirai-plugin/releases)    
插件命令依赖 [chat-command](https://github.com/project-mirai/chat-command) 请确保有此插件

### 配置
  下面是一个配置文件模板   
  配置文件位于 `mirai 根目录/config/top.colter.bilibili-dynamic-mirai-plugin/BiliPluginConfig.yml`  
  第一次运行插件会自动生成，也可自己创建  
  注：**v2.1.5版本及之后 `cookie` 项可以不用填写，bot管理员通过 `/bili login` 进行扫码登录**    
  **v2.1.5之前的版本 `cookie` 项为必填，并自行在配置文件中把单引号改为双引号**
```yml
# 管理员
# 可以是群号或 QQ 号
admin: 11111111111

# 推送模式
# 0 :以文字形式推送
# 1 :以图片形式推送 (默认)
pushMode: 1

# 添加订阅时是否允许 bot 自动关注未关注的用户
# 开启 true
# 关闭 false
autoFollow: true

# Bot 关注时保存的分组(最长 16 字符)
# 如果要保存到默认分组请填写为 ""
followGroup: Bot关注

# 检测间隔(推荐 15-30 最低 10) 单位秒 
# 不建议超过 60，超过 60 可能会漏动态
interval: 15

# 直播检测间隔(与动态检测独立) 单位秒
liveInterval: 20

# 低频检测时间段与倍率
# 例: 3-8x2 三点到八点检测间隔为正常间隔的2倍 
# 24小时制, 倍率不支持小数
# 时间相同关闭低频检测模式 如 0-0
lowSpeed: 0-0x2

# 图片推送模式用的字体
# 如不带后缀名则使用系统的字体，如系统中没有这个字体 则会使用系统默认字体
# 带后缀名则使用插件数据路径下 font 文件夹下的字体文件
# 如使用字体文件仅支持 ttf 格式
# 字体文件路径 data/top.colter.bilibili-dynamic-mirai-plugin/font
# 需自行创建 font 文件夹，并把字体文件放进去
#
# 支持多字体，多个字体之间使用逗号分隔，使用优先级从前往后依次减小
# 如果上一个的字体没有这个字符则会使用后面的字体来渲染该字符
# 建议最后一个字体使用字符比较全的，用于显示颜文字
font: HarmonyOS Sans SC,Source Han Sans

# 图片模式下动态/视频推送文字模板
# 配置项:
# {name} : 名字
# {uid}  : UID
# {type} : 类型 如 动态、直播等
# {time} : 时间
# {link} : 链接
# {id}   : ID
# \n     : 换行
# 如有换行请使用双引号
# 2.0.4 版本之前的模板为 {name}@{uid}@{type}\n{time}\n{link}
pushTemplate: "{name}@{type}\n{link}"

# 图片模式下直播推送文字模板, 如果为空则与上方动态推送模板保持一致
livePushTemplate: ""

# 图标页脚模板 配置项与上方一致
footerTemplate: "{type}ID: {id}"

#是否开启图片二维码
qrCode: false

#卡片圆角大小
cardArc: 20

# cookie 请包在双引号内
# 如何获取见下文 手动获取 Cookie
# 或使用 /bili login 进行扫码登录
cookie: ""

# 百度翻译
baiduTranslate:
  # 是否开启百度翻译
  enable: false
  # 百度翻译 API 密钥 如需使用请自行申请
  # https://api.fanyi.baidu.com/
  APP_ID: ''
  SECURITY_KEY: ''
```
### 手动获取 Cookie
<details>
<summary>点击展开</summary>

浏览器打开 [BiliBili](https://www.bilibili.com/) 并登陆  
注：登陆后最好不要退出登陆

按`F12`，打开`开发者工具`，找到`Network 网络`并点击  
按`F5`刷新页面，按下图复制 Cookie   
<img src="docs/img/cookie.png" width="500">

</details>

## 使用帮助
### 权限    
插件命令依赖 [chat-command](https://github.com/project-mirai/chat-command) 来正确处理聊天环境下的命令，请确保有此插件，否则插件可能无法正常工作    

| 权限名    | ID                                                    |
|--------|-------------------------------------------------------|
| 基础权限ID | top.colter.bilibili-dynamic-mirai-plugin:*            |
| 动态指令ID | top.colter.bilibili-dynamic-mirai-plugin:command.bili |

如果你不会给权限可以点击下面展开查看简略教程
<details>
<summary>点击展开</summary>

在控制台输入 `/perm add <许可人 ID> top.colter.bilibili-dynamic-mirai-plugin:*`

其中的 `许可人 ID` 如果你要让所有群所有人都可以用此插件就是 `*` 即 `/perm add * top.colter.bilibili-dynamic-mirai-plugin:*`    
具体的用户: `u123456789` 全部用户: `u*`    
具体的群: `g123456789` 全部群: `g*`    
更多的配置见 [被许可人 ID 字符串表示](https://github.com/mamoe/mirai-console/blob/master/docs/Permissions.md#%E5%AD%97%E7%AC%A6%E4%B8%B2%E8%A1%A8%E7%A4%BA)    
[授予一个用户执行所有指令的权限](https://github.com/mamoe/mirai-console/blob/master/docs/BuiltInCommands.md#%E6%8E%88%E4%BA%88%E4%B8%80%E4%B8%AA%E7%94%A8%E6%88%B7%E6%89%A7%E8%A1%8C%E6%89%80%E6%9C%89%E6%8C%87%E4%BB%A4%E7%9A%84%E6%9D%83%E9%99%90)

</details>

#### At全体权限
| 权限名   | ID                                                    |
|-------|-------------------------------------------------------|
| 直播@全体 | `top.colter.bilibili-dynamic-mirai-plugin:live.atall`   |
| 视频@全体 | `top.colter.bilibili-dynamic-mirai-plugin:video.atall`  |

通过对群设置权限来控制是否开启直播@全体    
`/perm add g123456789 top.colter.bilibili-dynamic-mirai-plugin:live.atall`
  
### 指令    

| 指令                                  | 描述                 |
|-------------------------------------|--------------------|
| `/bili <login / 登录>`                | bot管理员进行扫码登录       |
| `/bili <add / 添加> <uid> [群/Q号]`     | 为目标 [群/Q号] 添加一个订阅  |
| `/bili <list / 列表> [群/Q号]`          | 查询目标 [群/Q号] 的订阅列表  |
| `/bili <del / 删除> <uid> [群/Q号]`     | 为目标 [群/Q号] 删除一个订阅  |
| `/bili <delAll / 删除全部订阅> [群/Q号]`    | 将目标 [群/Q号] 的全部订阅删除 |
| `/bili <set / 设置> <uid> [群/Q号]`     | 为目标 [群/Q号] 设置订阅内容  |
| `/bili <color / 颜色> <uid> <16进制颜色>` | 为目标 UID 设置图片推送主题色  |
| `/bili <list / 列表> [群/Q号]`          | 查询目标 [群/Q号] 的订阅列表  |
| `/bili <listAll / la / 全部订阅列表>`     | bot管理员查询全部订阅列表     |
| `/bili <listUser / lu / 用户列表>`      | bot管理员查询用户列表       |
```
# 说明
扫码登录请在配置文件中填写管理员账号    
<..> 尖括号为必填参数   [..] 中括号为可选参数    
[群/Q号] 不填的话默认对话所在地
<16进制颜色> 必须带#号 例: #fde8ed

# 栗子
/bili add 487550002             #为当前群/好友添加 uid 为 487550002 的订阅
/bili add 487550002 111111      #为 111111 群/好友添加订阅
/bili color 487550002 #fde8ed   #设置主题色
```

#### 动态过滤指令    
过滤：当动态文字内容匹配过滤表达式时**不**推送动态    
包含：仅当动态文字内容匹配过滤表达式时**推送**动态    
过滤表达式：支持正则表达式，普通用户用文字即可，^匹配开头  $匹配结尾

| 指令                                                   | 描述                      |
|------------------------------------------------------|-------------------------|
| `/bili <filter / f / 过滤> <过滤表达式> <uid> [群/Q号]`       | 为目标 [群/Q号] 添加 uid 的动态过滤 |
| `/bili <contain / c / 包含> <过滤表达式> <uid> [群/Q号]`      | 为目标 [群/Q号] 添加 uid 的动态包含 |
| `/bili <filterList / fl / 过滤列表> <uid> [群/Q号]`        | 列出 uid 的过滤包含列表          |
| `/bili <filterDel / fd / 过滤删除> <index> <uid> [群/Q号]` | 删除 uid 中 index 的过滤包含    |

```
# 栗子
/bili f 互动抽奖 487550002    # 为当前群/好友订阅的 487550002 用户设置内容为“互动抽奖”的过滤
# 当 487550002 用户动态中包含“互动抽奖”文字时不给当前群/好友推送

/bili c ^新年好 0 111111    # 为 111111 群/好友订阅的所有用户设置内容为“^新年好”的包含
# 当 111111 群/好友订阅的所有用户的动态内容开头包含“新年好”时推送此动态

/bili fl 487550002   # 查询当前群/好友订阅的 487550002 设置过的过滤列表
# 返回的内容类似
# 过滤 
# f0: 啊这
# f1: 互动抽奖
# 包含 
# c0: 新年好
#
# f0 f1 c0 等为删除的索引<index>

/bili fd f0 487550002   # 删除上面f0的过滤

# 特例
# uid 为 0 时，代表群/好友订阅的所有用户
```

## 图片缓存
所有生成的图片都会缓存在 `data/top.colter.bilibili-dynamic-mirai-plugin/` 下    
`dynamic` 文件夹用于存放所有动态图片    
`live` 文件夹用于存放所有直播图片    
目前没有自动清除缓存功能，如果需要请自行删除

## 已知问题
- 部分颜文字会以方块显示
- 小概率图片模式有些文字或图片会错位
- 小概率图片获取失败，图片位置为全白
- 链接,话题以及@不会变色(感觉用处不大?如果你强烈需要可以去开个 issues <_<)

## 其他
- 在 [DynamicPlugin](https://github.com/Colter23/bilibili-dynamic-mirai-plugin/tree/master/DynamicPlugin) 目录下有一个`font`文件夹里面有一个`思源黑体.ttf`  
  如没有字体使用可用这个

## 更新日志
[releases](https://github.com/Colter23/bilibili-dynamic-mirai-plugin/releases)

## 相关链接
[Mirai](https://github.com/mamoe/mirai)   
[ViViD按钮](https://vividbtn.top/)   
[B站: 猫芒ベル_Official](https://space.bilibili.com/487550002/) 这么可爱不关注一下吗(❁´◡`❁)    
[B站: Colter_null](https://space.bilibili.com/32868931) 如果 GitHub 或论坛太长时间没回复可以去 B 站私信  

## 感谢
cssxsh: [bilibili-helper](https://github.com/cssxsh/bilibili-helper)


