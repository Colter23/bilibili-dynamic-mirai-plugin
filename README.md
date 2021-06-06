# BilibiliDynamic MiraiPlugin

> 一个把B站动态转发到Q群的 [mirai](https://github.com/mamoe/mirai) 插件

本人菜鸡一个，代码写的不好（   
可能不是很稳定
### 图片模式样式↓   
<img src="docs/img/demo1.jpg" width="400" alt="样式">     

### 下载
  可从 [releases](https://github.com/Colter23/bilibili-dynamic-mirai-plugin/releases) 里下载

### 配置
  下面是一个配置文件模板   
  配置文件位于 根目录/config/BilibiliDynamic/config.yml  
  第一次运行插件会自动生成，也可自己创建  
  注：**第一行的`admin`为必填**
```yml
# 管理  一个控制台 必填!!!!!!!!!!
# 可以是群号或QQ号
admin: 11111111111

# 是否开启报错推送
exception: true

# 推送模式
# 0 :图片推送 (不稳定)
# 1 :文字推送 (默认)
pushMode: 1

# bot状态
botState: true

# 插件的数据路径 基于启动器根目录 用于存放图片 字体等数据
basePath: '/DynamicPlugin'

# 字体  如使用字体文件仅支持ttf格式
# 如不带后缀名则使用系统的字体，如系统中没有这个字体 则会使用系统默认字体
# 带后缀名则使用上面的 basePath 数据路径下 font 文件夹下的字体文件
# 需自行创建font文件夹，并把字体文件放进去
font: 微软雅黑

## 好友相关
friend: 
  # 好友功能总开关 包括交互等
  # 关闭后好友将不能与bot交互,但如果好友有订阅，bot仍会推送
  enable: false
  # 自动同意好友申请
  agreeNewFriendRequest: false
  # 欢迎语 不需要欢迎语的话请删除引号中间的内容,仅留引号
  welcomeMessage: "( •̀ ω •́ )✧"

## 群相关
group: 
  # 群功能总开关 包括交互等 (管理群不受影响)
  # 关闭后群成员将不能与bot交互,但如果群有订阅，bot仍会推送
  enable: false
  # 欢迎新成员
  welcomeMemberJoin: false
  # 欢迎语 会@新成员 欢迎语后会跟一个随机表情
  welcomeMessage: 欢迎

## 动态相关
dynamic: 
  # 动态检测总开关
  enable: true
  # 访问间隔 单位:秒  范围:[1,∞]
  # 这个间隔是每次访问b站api时就会触发
  # 不建议太低 有可能会被b站封ip 推荐5-10s
  interval: 10
  # 慢速模式开启时间段 不开启则填000-000
  # 慢速模式访问api的间隔会再上面的间隔加上10s
  # 例：200..800就是凌晨2点到8点
  lowSpeed: '200-800'
  # 视频模式  此模式仅会推送视频 要开启改为 true
  videoMode: false
  # 是否保存动态图片 保存路径为上面的 basePath
  # 仅图片推送模式
  saveDynamicImage: true

## 直播相关
live: 
  # 播检测总开关
  enable: true
  
### 以上的 friend group dynamic live 的开关均可通过管理群回复 #管理 进行设置

## 百度翻译
baiduTranslate:
  # 是否开启百度翻译
  enable: false
  # 百度翻译api密钥 如需使用请自行申请
  # https://api.fanyi.baidu.com/
  APP_ID: ''
  SECURITY_KEY: ''

## B站API BiliBiliApi(BPI)
BPI: 
  # 动态
  dynamic: 'https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?visitor_uid=1111111111&offset_dynamic_id=0&need_top=0&host_uid='
  # 直播状态
  liveStatus: 'https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom?room_id='
  # 获取直播房间号
  liveRoom: 'https://api.live.bilibili.com/room/v1/Room/getRoomInfoOld?mid='
  # 粉丝数(暂时没用)
  followNum: 'https://api.bilibili.com/x/relation/stat?vmid='
  # 大航海数(暂时没用) 
  # 参数: 用户id:ruid 直播间id:roomid eg: ruid=487550002&roomid=21811136
  guard: 'https://api.live.bilibili.com/xlive/app-room/v2/guardTab/topList?page=1&page_size=1&'
```
以上的 好友、群 的开关不建议开启，避免群友过多添加订阅  
可以通过管理群统一设置

### 使用
在管理群内发送 `#?` `#help` `#帮助` `#功能` `#菜单` 中的任意一个即可查看命令
```
> #?

#管理 : 查看管理功能
#开启动态推送 [群/Q号] / #关闭动态推送 [群/Q号]
#r [指定数字] / #骰子 [指定数字]
#订阅 <UID> [群/Q号] [16进制主题色]
#删除 <UID> [群/Q号]
#订阅列表 [群/Q号]

说明: <>内为必填, []为选填. 中间用空格隔开! 不要带括号!
[群/Q号] 不填的话默认对话所在群
[16进制主题色] 必须带#号 例: #fde8ed
例: 
#订阅 487550002    :为本群/好友添加uid为487550002的订阅
#订阅 487550002 111111  :为111111群/好友添加订阅
#订阅 487550002 #fde8ed  :为本群/好友添加订阅 并指定图片主题色
```
以上命令一部分有别名  
`#订阅` 别名 `#添加` 或 `#add`  
`#删除` 别名 `#del`  
`#订阅列表` 别名 `#list `
```
> #管理

#关闭bot : 临时关闭bot
#关闭 -a : 关闭全部功能
#关闭 -d : 关闭动态功能
#关闭 -l : 关闭直播功能
#关闭 -r : 关闭命令回复(除管理群)
#关闭 -gr : 关闭群命令回复
#关闭 -fr : 关闭好友命令回复

以上 '关闭' 可换为 'close'
如要开启把 '关闭' 换成 '开启'或'open' 即可
```

### 已知问题
- 当新增或删除订阅时，会导致数据不一致，而检测失败。  
  过两分钟下一次检测就好了。
  
- 头像如果是动态头像(webp格式)的会报错

### 其他
- 在 [DynamicPlugin](https://github.com/Colter23/bilibili-dynamic-mirai-plugin/tree/master/DynamicPlugin) 目录下有一个`动态BG.psd`可以手动通过ps调节背景图片  
  不要改动里面的任何元素的位置
- 在 [DynamicPlugin](https://github.com/Colter23/bilibili-dynamic-mirai-plugin/tree/master/DynamicPlugin) 目录下有一个`font`文件夹里面有一个`思源黑体.ttf`  
  如没有字体使用可用这个

### 更新日志
[releases](https://github.com/Colter23/bilibili-dynamic-mirai-plugin/releases)
  

### 相关链接
[mirai](https://github.com/mamoe/mirai)  
[ViViD按钮](https://vividbtn.top/)  
[B站: Colter_null](https://space.bilibili.com/32868931)


