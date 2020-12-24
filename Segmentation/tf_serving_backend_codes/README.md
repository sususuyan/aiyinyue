# 人像分割服务器端基本介绍

by 赵鹏程

## 流程简介

1. 本地读取背景图和人像图
2. 本地预处理图片
3. 服务器运行基于深度学习的图像分割模型
4. 本地接受并进行图像后处理

##  模型简介

基于Unet与MobileNeV2搭建的深度网络

## 部署方式

使用docker+tensorflow serving的方式部属于服务器端

## 效果展示

人像：

![Aaron Swartz](https://raw.githubusercontent.com/sususuyan/aiyinyue/main/Segmentation/tf_serving_backend_codes/test_images/girl4.jpg)

背景图：

替换结果：