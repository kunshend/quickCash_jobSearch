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
<img width="2552" height="1909" alt="8ff19320a4d3d8f187feeeaef0f64278" src="https://github.com/user-attachments/assets/09fcf23f-5a4c-4f1c-a24a-f303af436afe" />

（这里放你的 Register 和 Login 截图）

🗺️ 2. 当前位置定位 + Nearby Jobs

通过 Google Maps API 获取用户当前 GPS

自动展示附近 5 个兼职
（插入你“Current Location: Halifax, NS”截图）

📋 3. Job 浏览、排序与详情

查看兼职内容：描述、类别、位置

Google Map 显示工作地点（Marker + 地图缩放）
（插入你“View Job”截图）

📝 4. 发布兼职（Post Job）

城市自动补全（Google Places API）

图形化 UI
（插入 Post Job 截图）

🙋 5. 求职与申请管理

Employer 可以查看所有申请者并进行：

accept

reject

Employee 可查看自己的申请状态
（插入 My Applications / My Jobs 截图）

💸 6. 支付系统（PayPal）

Employer 接受申请后跳转支付界面

PayPal Sandbox
（插入 PayPal 截图）

🗂️ 7. 设置（Settings）

切换角色（Employee ↔ Employer）

Password Reset

Logout
（插入 Settings 截图）

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

📁 项目结构（你可加入 README）
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
