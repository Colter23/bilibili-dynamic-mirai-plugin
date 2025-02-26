# BilibiliDynamic MiraiPlugin


这个分支是V4重构后已完成的代码，大部分都写了，就差指令交互那部分了。    
但因为当时mirai不大行了，就懒得再对接了。   
所以就萌生出了新的动态Bot，不依赖任何框架，直接对接onebot。   

V4使用了新的绘图框架 [skiko-layout](https://github.com/Colter23/skiko-layout)，以及 [bilibili-client](https://github.com/Colter23/bilibili-client)

所以有部分代码可借鉴，比如绘图。    
测试目录里也有很多绘图测试，可以看看。    

下面大概介绍下V4模块，对应代码里的模块文件夹
- account：支持多b站账号     
- command：指令相关（未完成）     
- data：程序内部数据     
- database：实例化数据     
- draw：绘图     
- event：事件    
- exception：异常    
- filter：过滤器（V4的过滤器也是一大亮点，自定义过滤，不过后面把过滤器链给砍了）    
- handle：忘了干啥的了（未完成）    
- listener：监听器    
- mapper：忘了干啥的了（未完成）    
- message：自定义提示词（未完成）    
- service：服务（未完成）    
- task：任务    
- tools：工具    