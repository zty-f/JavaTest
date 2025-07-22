#!/bin/bash

echo "机器人数据导入工具"
echo "=================="

# 检查Java是否安装
if ! command -v java &> /dev/null; then
    echo "错误: 未找到Java，请先安装Java 8或更高版本"
    exit 1
fi

# 检查Maven是否安装
if ! command -v mvn &> /dev/null; then
    echo "错误: 未找到Maven，请先安装Maven 3.6或更高版本"
    exit 1
fi

echo "正在编译项目..."
mvn clean compile

if [ $? -eq 0 ]; then
    echo "编译成功！"
    echo "正在运行程序..."
    mvn exec:java -Dexec.mainClass="RobotDataImporter"
else
    echo "编译失败，请检查错误信息"
    exit 1
fi 