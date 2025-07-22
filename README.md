# 机器人数据导入工具

这是一个用于处理机器人数据的Java程序，可以自动读取Excel文件中的用户信息，并为每个用户分配头像和相册图片。

## 功能特性

- 自动扫描指定目录下的所有数据文件夹
- 读取Excel文件中的用户昵称和真实姓名
- 扫描头像文件夹（TX）和相册文件夹（XC）中的图片文件
- 为每个用户随机分配一个头像和3-5张相册图片
- 避免重复使用图片文件
- 生成SQL导入脚本

## 目录结构

程序期望的数据目录结构如下：

```
机器人/
├── 001-20241117/
│   ├── 001-TX-20241117/     # 头像文件夹
│   ├── 001-XC-20241117/     # 相册文件夹
│   └── 机器人_r20241117.xlsx # 用户信息Excel文件
├── 002-20241123/
│   ├── 002-TX-20241123/
│   ├── 002-XC-20241123/
│   └── 机器人_r20241123.xlsx
└── ...
```

## 安装和运行

### 前提条件

- Java 8 或更高版本
- Maven 3.6 或更高版本

### 编译和运行

1. 克隆或下载项目文件
2. 在项目根目录执行：

```bash
# 编译项目
mvn clean compile

# 运行程序
mvn exec:java -Dexec.mainClass="RobotDataImporter"

# 或者打包成可执行jar
mvn clean package
java -jar target/robot-data-importer-1.0.0.jar
```

## Excel文件格式

Excel文件应包含以下列：
- 第1列：用户昵称
- 第2列：用户真实姓名

程序会自动跳过第一行（标题行）。

## 输出结果

程序运行后会：

1. 在控制台显示处理进度和分配结果
2. 生成 `robot_import_script.sql` 文件，包含数据库导入语句

### SQL脚本格式

生成的SQL脚本包含两个表的插入语句：

```sql
-- 用户信息表 (joy_profile)
INSERT INTO joy_profile (user_id, open_id, robot_id, nick_name, avatar, status, create_time, update_time) VALUES (...);

-- 照片表 (joy_photo)
INSERT INTO joy_photo (id, user_id, album_id, title, url, status, create_time, update_time) VALUES (...);
```

程序会自动生成：
- 唯一的用户ID (user_id)
- 唯一的open_id
- 机器人ID (robot_id)
- 照片ID (id)
- 相册ID (album_id)

## 配置说明

在 `RobotDataImporter.java` 的 `main` 方法中，可以修改以下配置：

```java
// 修改数据目录路径
String basePath = "/Users/zty/Downloads/机器人";

// 修改每个用户的相册图片数量（在assignImages方法中）
int albumCount = 3 + new Random().nextInt(3); // 3-5张
```

## 注意事项

1. 确保Excel文件格式正确，包含昵称和姓名两列
2. 图片文件支持 jpg、jpeg、png、gif 格式
3. 程序会随机分配图片，确保每个文件夹内不重复使用
4. 如果图片数量不足，程序会给出警告信息

## 错误处理

程序包含以下错误处理机制：

- 文件不存在或格式错误时的警告
- Excel文件读取异常处理
- 图片文件扫描异常处理
- SQL脚本生成异常处理

## 扩展功能

可以根据需要扩展以下功能：

- 支持更多图片格式
- 自定义图片分配规则
- 添加数据验证功能
- 支持批量处理多个目录
- 添加配置文件支持 