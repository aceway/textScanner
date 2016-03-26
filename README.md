# textScanner
* Android下拍照识别文字的工具。
* [App下载](http://yun.baidu.com/share/link?shareid=4096371052&uk=706459533)
=======

# 概述 #
* 类似　微信>>发现>>扫一扫>>翻译 功能（无翻译，不过对识别文字的结果传给google或baidu的翻译web很容易实现了）。 
* 程序能够自动适应竖／横屏幕，屏幕上被识别的区域亮色可见拍摄对象，其它区域半透明可见拍摄对象。触摸屏幕可调整识别区域的亮色矩形大小。识别依赖的数据，使用的是下载的tesseract带的默认训练数据，中文准确率不好，英文和数字的依赖拍照的效果了。 

* 图像识别用的是 tesseract、tesseract-android-tools. 初次运行程序时会把识别用的中／英数据，解压出来（有进度提示）。这部分代码，数据从 code.google.com和github.com获取。 相机的使用处理参考了CSDN上　yanzi1225627的博客内容(android开发分类下的　android 多媒体和相机详解1至11 http://blog.csdn.net/niu_gao/article/details/7570967)。 

## 开发环境 ##
使用Eclipse开发, 在windows7和Ubuntu14下都编译运行，安装到android物理机过。android-support-v7-appcompat,tesserac和tesseract-android-tools代码资源未放进来。 

## 补充说明 ##
本项目的初衷是和朋友聊创业的Demo(关于汽车的，所以代码里有atires---android tires)，那个阶段的创业尝试已经放弃了。自己觉得Demo里做的一个小环节－－－手机随时随地拍照然后识别文字的代码有点价值，所以剥离出来，便于分享，供人使用了。
