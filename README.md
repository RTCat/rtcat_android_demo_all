## 说明
此样例实现视频,音频和文字聊天，摄像头切换功能

## 使用

1. `git clone https://github.com/zombiecong/rtcat_android_demo_1v1.git`

2. 通过`Android Studio`导入, `File > Import Project` ,选择项目中的build.gradle文件导入

3. 在项目中增加权限和jar，so文件（详情参考实时猫Android SDK 文档）

4. 在`MainActivity.java`文件目录下，增加 `Config.java`,并加入以下代码

```java
package com.shishimao.demo_all;

public class Config {
	 //以下内容都可以通过实时猫后台获得
    public static final String APIKEY = "xxx";
    public static final String SECRET = "xxx";
    public static final String SESSIONID = "xxx";
}

```



