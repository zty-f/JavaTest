# OSS图片上传功能使用说明

## 功能概述

机器人数据导入工具现在支持将图片文件自动上传到OSS（对象存储服务），并在生成的SQL脚本中使用OSS链接而不是本地文件路径。

## 主要改进

1. **智能OSS上传**: 只上传分配给用户的图片文件，避免浪费
2. **并发上传**: 支持多线程并发上传，提高效率
3. **配置化管理**: 通过配置文件控制上传参数
4. **URL缓存**: 避免重复上传相同文件
5. **优化流程**: 先分配图片，再上传，最后生成SQL脚本

## 配置文件设置

在 `config.properties` 文件中添加以下OSS相关配置：

```properties
# OSS上传配置
oss.upload.url=https://app.beatconnects.com/app/upload/pic
oss.upload.threads=10
oss.upload.timeout=30000
oss.avatar.key=avatar
oss.photo.key=photo
```

### 配置参数说明

- `oss.upload.url`: OSS上传接口地址
- `oss.upload.threads`: 并发上传线程数（建议5-20）
- `oss.upload.timeout`: 上传超时时间（毫秒）
- `oss.avatar.key`: 头像图片的OSS存储键
- `oss.photo.key`: 相册图片的OSS存储键

## 上传接口说明

程序使用以下接口上传图片：

```
POST https://app.beatconnects.com/app/upload/pic
Content-Type: multipart/form-data

参数:
- picFile: 图片文件
- ossKey: OSS存储键（avatar/photo）
```

## 处理流程

1. **扫描文件夹**: 扫描所有机器人数据文件夹
2. **读取Excel**: 读取用户信息
3. **扫描图片**: 扫描头像和相册图片文件
4. **分配图片**: 为用户分配图片文件（文件名）
5. **上传OSS**: 只上传分配给用户的图片到OSS
6. **更新链接**: 将用户信息中的文件名替换为OSS链接
7. **生成脚本**: 生成包含OSS链接的SQL脚本

## 输出结果

### 控制台输出示例

```
处理文件夹: /Users/zty/Downloads/robot/001-20241117
正在读取Excel文件: /Users/zty/Downloads/robot/001-20241117/机器人_r20241117.xlsx
读取到 50 个用户
头像文件: 100 个
相册文件: 200 个
分配结果:
✅ 用户: 张三 (张先生)
  头像文件: 07e6470379e81d276a9477f92689b92e.jpeg
  性别: 男, 年龄: 65岁
  手机: 13812345678, 情感状态: 已婚, 工作状态: 退休
  家乡: 北京, 所在地: 上海
  公司: 国有企业, 职业: 工程师
  相册图片文件: 0c113eda773ce36ba287ef669be2f9d3.jpg, 1fd5e0676ba5b3d99e7471e9e2657632.jpeg

开始上传分配给用户的图片到OSS...
需要上传的头像文件: 50 个
需要上传的相册文件: 150 个
上传 /Users/zty/Downloads/robot/001-20241117/001-TX-20241117 中的 50 个指定文件到OSS (ossKey: avatar)
⏭️  跳过已上传文件: 07e6470379e81d276a9477f92689b92e.jpeg
✅ 上传成功: 0c113eda773ce36ba287ef669be2f9d3.jpg -> https://oss.example.com/avatar/2024/01/01/def456.jpg
...
图片上传完成
```

### SQL脚本输出

生成的SQL脚本现在包含OSS链接：

```sql
-- 机器人数据导入脚本
-- 生成时间: Mon Dec 18 10:30:00 CST 2024

-- 文件夹: /Users/zty/Downloads/robot/001-20241117
INSERT INTO joy_profile (user_id, open_id, nick_name, avatar, status, robot_id, phone, sex, birthday, emotion_status, work_status, verify_status, hometown, location, company, carrier) VALUES (1, '1703123400', '张三', 'https://oss.example.com/avatar/2024/01/01/abc123.jpg', 1, 7527642214542049306, '13812345678', 1, 19590115, 2, 2, 1, '北京', '上海', '国有企业', '工程师');

INSERT INTO joy_photo (id, user_id, album_id, title, url, status) VALUES (1, 1, 0, '相册图片', 'https://oss.example.com/photo/2024/01/01/xyz789.jpg', 1);
INSERT INTO joy_photo (id, user_id, album_id, title, url, status) VALUES (2, 1, 0, '相册图片', 'https://oss.example.com/photo/2024/01/01/uvw012.jpg', 1);
```

## 注意事项

1. **网络连接**: 确保程序能够访问OSS上传接口
2. **文件权限**: 确保程序有读取图片文件的权限
3. **上传限制**: 注意OSS接口的文件大小限制
4. **错误处理**: 上传失败的文件会被跳过，但会记录错误信息
5. **缓存机制**: 相同文件名的文件只会上传一次

## 故障排除

### 常见问题

1. **上传失败**: 检查网络连接和OSS接口状态
2. **超时错误**: 增加 `oss.upload.timeout` 配置值
3. **线程错误**: 减少 `oss.upload.threads` 配置值
4. **文件读取错误**: 检查文件路径和权限

### 调试模式

设置 `console.verbose=true` 可以看到详细的处理信息。

## 性能优化建议

1. **线程数调整**: 根据网络带宽调整并发线程数
2. **文件大小**: 建议图片文件大小不超过10MB
3. **批量处理**: 程序会自动批量处理多个文件夹
4. **缓存利用**: 重复运行时会利用已上传文件的缓存 