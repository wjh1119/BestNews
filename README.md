最新闻
===
一个用于阅读最新新闻的App。
## App设计
#### 应用浏览
- 以选项卡(tab) + 列表的形式作为应用主UI。
- 可以在选项卡上选择不同的新闻类别。
- 点击列表项跳转到详情页面，展示新闻详情。
- 新闻详情页面可显示评论（仅有伪数据）。
#### 应用界面
- 列表，新闻详情 和评论界面都为简单的图文混排布局。
- 应用界面符合[材料设计](https://material.io/guidelines/)的标准。
#### 应用功能
- 按照新闻类别来进行数据分类加载，显示。
- 支持应用没有打开时，自动获取新闻。
- 数据存放到本地客户端中，离线可用。
- 列表支持上拉下拉。
- 本地没有数据，或网络异常时，提示用户。
## 数据源
数据来源于[易源数据新闻API接口](https://www.showapi.com/api/lookPoint/109)
## 依赖库
- [showapi_android_sdk](https://www.showapi.com/api/lookPoint/109)
- [butterknife](https://github.com/JakeWharton/butterknife)
- [SwipeToLoadLayout](https://github.com/Aspsine/SwipeToLoadLayout)
- [CircleImageView](https://github.com/hdodenhof/CircleImageView)
- [eventbus](https://github.com/greenrobot/EventBus)

## 注意
因版权原因，本项目不提供api key。
若运行时遇到以下错误：

    Error:(150, 67) 错误: 找不到符号
    符号:   变量 MY_SHOWAPI_NEWS_API_KEY
    位置: 类 BuildConfig

只需于[易源数据新闻API接口](https://www.showapi.com/api/lookPoint/109)申请API_KEY，并配置好gradle替换以上变量即可。
