📱 QuickCash：基于定位的兼职匹配与支付应用

（Dalhousie University — CSCI 3130 软件工程大项目）

⭐ 项目简介

QuickCash 是一款基于 Android 的兼职任务匹配应用，允许用户发布工作、申请工作，并通过 PayPal 进行支付。应用整合了 Firebase Realtime Database、Google Maps API、地点自动补全、PayPal SDK 等多项关键技术。

🔧 我的项目职责（可复制进简历）
1. 负责核心模块开发

Job Detail → Google Map 路径展示

用户当前位置定位与 Nearby Jobs 推荐

Payment 模块（PayPal 支付流程）

Role 切换（Employee / Employer）

Job Posting 与 City 自动补全

My Jobs / My Applications 页面

📌 项目功能（含截图）

📍 1. 用户注册与登录

支持 Email 注册、密码验证

Firebase Authentication

<img width="500px" height="500px" alt="8ff19320a4d3d8f187feeeaef0f64278" src="https://github.com/user-attachments/assets/09fcf23f-5a4c-4f1c-a24a-f303af436afe"  />


🗺️ 2. 当前位置定位 + Nearby Jobs

通过 Google Maps API 获取用户当前 GPS

自动展示附近 5 个兼职

<img width="500px" height="500px" alt="6c0eda5f4371163e38ac797151923230" src="https://github.com/user-attachments/assets/02c8ce64-4714-467e-9b2b-78085536bd6e" />

<img width="500px" height="500px" alt="30d6f497eb5c387e1d95b59611dabd60" src="https://github.com/user-attachments/assets/3d1e439b-5d08-48e2-a0d5-2b01cc0b0890" />

📋 3. Job 浏览、排序与详情

查看兼职内容：描述、类别、位置

Google Map 显示工作地点（Marker + 地图缩放）


<img width="600px" height="500px" alt="061416ae53b6f1b782593bf600e381de" src="https://github.com/user-attachments/assets/56c9b7ca-7a89-42a4-a072-fbaadd3eb749" />


📝 4. 发布兼职（Post Job）

城市自动补全（Google Places API）

图形化 UI

<img width="500" height="500" alt="d98800d36f730ca126f23f93fa0ba392" src="https://github.com/user-attachments/assets/a6fdcb34-698b-42b8-80f0-a276fa9a50ac" />


🙋 5. 求职与申请管理

Employer 可以查看所有申请者并进行：

accept

reject

Employee 可查看自己的申请状态

<img width="500" height="500" alt="e0362f9e722945f913cb731841ba19a7" src="https://github.com/user-attachments/assets/8be588be-af16-40f6-bcac-c5d336cb9553" /><img width="1000" height="1000" alt="c3340c9bb6e411ed29f29068bc5be45f" src="https://github.com/user-attachments/assets/ee72f4e8-b009-4944-8b06-80f10c049103" />



💸 6. 支付系统（PayPal）

Employer 接受申请后跳转支付界面

PayPal Sandbox

<img width="500" height="600" alt="5ed9be20e680493295ee499693c1c4bc" src="https://github.com/user-attachments/assets/e8bbec7f-eeb7-48f9-9563-7f2db68de1eb" />


🗂️ 7. 设置（Settings）

切换角色（Employee ↔ Employer）

Password Reset

Logout

<img width="500" height="500" alt="6c0eda5f4371163e38ac797151923230" src="https://github.com/user-attachments/assets/a0d35f19-3f54-444d-a64b-05a86309e667" />


🧩 技术栈
前端 (Android)

Java + Android Studio

ConstraintLayout

RecyclerView

SupportMapFragment

后台

Firebase Realtime Database

Firebase Authentication

支付

PayPal Android SDK

📁 项目结构
app/
 ├── java/com/example/quickcash
 │    ├── activities/
 │    ├── entities/
 │    └── utilities/
 ├── res/
 │    ├── layout/
 │    ├── drawable/
 │    └── values/
 └── assets/
