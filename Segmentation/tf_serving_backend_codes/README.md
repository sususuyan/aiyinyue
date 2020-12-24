# 人像分割服务器端基本介绍

by 赵鹏程

## 流程简介  

对上传人像图片进行背景替换：
1. 本地读取背景图和人像图
2. 本地预处理图片
3. 服务器运行基于深度学习的图像分割模型
4. 本地接受并进行图像后处理

##  模型简介

基于Unet与MobileNeV2搭建的深度网络,模型在测试集上达到了96.1%的MIoU。  
Dataset链接：[Dataset](http://xiaoyongshen.me/webpage_portrait/index.html)

## 环境与部署方式

opencv-python 4.4.0.46  
numpy 1.19.4  
使用docker+tensorflow serving的方式部属于服务器端  

## 使用  

``python client_rest.py``    

部署于腾讯云服务器通信较慢（40s左右，网络情况而定），将本机作为服务器测试下来1024\*1024图片大概1s左右

## 效果展示

原人像：

![Aaron Swartz](https://raw.githubusercontent.com/sususuyan/aiyinyue/main/Segmentation/tf_serving_backend_codes/test_images/girl4.jpg)

背景图：

![Aaron Swartz](https://raw.githubusercontent.com/sususuyan/aiyinyue/main/Segmentation/tf_serving_backend_codes/test_images/b2.jpg)

替换结果：

![Aaron Swartz](https://raw.githubusercontent.com/sususuyan/aiyinyue/main/Segmentation/tf_serving_backend_codes/test_images/girl4_result.png)

由于数据集的缘故，对半身的效果最佳，模型训练同时也学习了一些模糊的特征，也能分割动物的图像。

