# 🚀 推送到 GitHub 指南

## 当前状态

✅ Git 仓库已初始化  
✅ 代码已提交 (commit 33c93bc)  
✅ GitHub CLI 已安装  
⚠️ 需要 GitHub 认证  

---

## 方法 1: 使用 GitHub CLI (推荐)

### 步骤 1: 登录 GitHub

运行以下命令在终端：

```bash
cd /root/KrkrFgiEditorAndroid
gh auth login
```

然后按照提示操作：
1. 选择 `GitHub.com`
2. 选择 `HTTPS`
3. 选择 `Login with a web browser`
4. 复制给出的 one-time code
5. 在浏览器中打开显示的 URL 并粘贴代码
6. 授权访问

### 步骤 2: 创建仓库并推送

认证成功后，运行：

```bash
# 创建 GitHub 仓库
gh repo create KrkrFgiEditorAndroid --public --source=. --remote=origin --push

# 或者如果想要私有仓库
gh repo create KrkrFgiEditorAndroid --private --source=. --remote=origin --push
```

完成！🎉 你的代码已经推送到 GitHub，GitHub Actions 会自动开始构建 APK。

---

## 方法 2: 使用 Personal Access Token

### 步骤 1: 创建 Token

1. 访问：https://github.com/settings/tokens/new
2. Note: `KrkrFgiEditor Android`
3. Expiration: 选择过期时间
4. Scopes: 勾选 `repo` (所有子选项)
5. 点击 "Generate token"
6. **复制 token（只显示一次！）**

### 步骤 2: 使用 Token 认证

```bash
cd /root/KrkrFgiEditorAndroid

# 使用 token 登录
echo "YOUR_TOKEN_HERE" | gh auth login --with-token

# 创建仓库并推送
gh repo create KrkrFgiEditorAndroid --public --source=. --remote=origin --push
```

---

## 方法 3: 手动推送（不使用 gh CLI）

### 步骤 1: 在 GitHub 网页创建仓库

1. 访问 https://github.com/new
2. Repository name: `KrkrFgiEditorAndroid`
3. Description: `Android port of Kirikiri Figure Image Editor - 吉里吉里立绘合成工具`
4. 选择 Public 或 Private
5. **不要**勾选 "Add a README file"
6. 点击 "Create repository"

### 步骤 2: 推送代码

GitHub 会显示推送指令，运行类似：

```bash
cd /root/KrkrFgiEditorAndroid

# 添加 remote（替换为你的用户名）
git remote add origin https://github.com/YOUR_USERNAME/KrkrFgiEditorAndroid.git

# 推送
git push -u origin main
```

如果需要认证，输入：
- Username: 你的 GitHub 用户名
- Password: 使用 Personal Access Token（不是密码）

---

## 推送后会发生什么？

### GitHub Actions 自动构建 ✨

推送后，GitHub Actions 会自动：

1. 检测到 `.github/workflows/android.yml`
2. 在 Ubuntu 环境中设置 JDK 17
3. 运行 `./gradlew assembleDebug`
4. 构建 APK
5. 上传 APK 为 artifact

### 下载 APK

1. 访问你的仓库
2. 点击 "Actions" 标签
3. 点击最新的 workflow run
4. 在 "Artifacts" 部分下载 `app-debug`
5. 解压得到 `app-debug.apk`

---

## 快速命令参考

```bash
# 查看认证状态
gh auth status

# 查看仓库列表
gh repo list

# 查看当前仓库信息
gh repo view

# 查看 Actions 运行状态
gh run list

# 查看最新 run 的详情
gh run view

# 下载最新 artifact
gh run download
```

---

## 故障排查

### 问题 1: 认证失败

```bash
# 重新登录
gh auth logout
gh auth login
```

### 问题 2: 仓库已存在

```bash
# 删除远程仓库（谨慎！）
gh repo delete YOUR_USERNAME/KrkrFgiEditorAndroid --confirm

# 或者更改仓库名
gh repo create KrkrFgiEditorAndroid-v2 --public --source=. --remote=origin --push
```

### 问题 3: 推送被拒绝

```bash
# 强制推送（如果确定本地是最新的）
git push -u origin main --force
```

---

## 项目 URL 格式

创建后，你的仓库 URL 将是：

```
https://github.com/YOUR_USERNAME/KrkrFgiEditorAndroid
```

Actions 页面：
```
https://github.com/YOUR_USERNAME/KrkrFgiEditorAndroid/actions
```

---

## 需要帮助？

如果遇到问题，运行：

```bash
! gh auth login
```

然后在聊天中告诉我进度，我会继续帮你！

---

**准备好了吗？运行第一个命令开始吧！** 🚀

```bash
cd /root/KrkrFgiEditorAndroid
gh auth login
```
