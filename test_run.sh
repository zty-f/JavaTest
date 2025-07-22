#!/bin/bash

echo "机器人数据导入工具 - 测试运行"
echo "=============================="

# 检查必要文件是否存在
if [ ! -f "RobotDataImporter.java" ]; then
    echo "错误: 未找到 RobotDataImporter.java 文件"
    exit 1
fi

if [ ! -f "pom.xml" ]; then
    echo "错误: 未找到 pom.xml 文件"
    exit 1
fi

if [ ! -f "config.properties" ]; then
    echo "错误: 未找到 config.properties 文件"
    exit 1
fi

echo "检查数据目录..."
if [ ! -d "/Users/zty/Downloads/机器人" ]; then
    echo "错误: 数据目录不存在: /Users/zty/Downloads/机器人"
    exit 1
fi

echo "数据目录存在，开始编译和运行..."

# 编译项目
echo "正在编译项目..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo "正在运行程序..."
    echo "----------------------------------------"
    
    # 运行程序
    mvn exec:java -Dexec.mainClass="RobotDataImporter"
    
    echo "----------------------------------------"
    echo "程序运行完成！"
    
    # 检查输出文件
    if [ -f "robot_import_script.sql" ]; then
        echo "✅ SQL脚本已生成: robot_import_script.sql"
        echo "脚本行数: $(wc -l < robot_import_script.sql)"
    else
        echo "❌ 未找到生成的SQL脚本"
    fi
    
else
    echo "❌ 编译失败，请检查错误信息"
    exit 1
fi 