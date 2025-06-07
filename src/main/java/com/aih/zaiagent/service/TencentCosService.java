package com.aih.zaiagent.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 腾讯云COS服务类
 * @author AiHyo
 */
@Service
public class TencentCosService {

    @Resource
    private COSClient cosClient;

    @Value("${tencent.cos.bucket-name}")
    private String bucketName;

    @Value("${tencent.cos.base-url}")
    private String baseUrl;

    /**
     * 上传文件到腾讯云COS，使用默认文件名
     * @param file 文件
     * @param directory 目录
     * @return 文件访问URL
     */
    public String uploadFile(File file, String directory) throws IOException {
        return uploadFile(file.getName(), file, directory);
    }

    /**
     * 上传文件到腾讯云COS，使用指定文件名
     * @param fileName 文件名
     * @param file 文件
     * @param directory 目录
     * @return 文件访问URL
     */
    public String uploadFile(String fileName, File file, String directory) throws IOException {
        String key = this.generateFileKey(fileName, directory);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);
        // 上传文件
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

        // 返回可访问的URL
        return getFileUrl(key);
    }

    /**
     * 上传MultipartFile到腾讯云COS
     * @param file MultipartFile
     * @param directory 目录
     * @return 文件访问URL
     */
    public String uploadFile(MultipartFile file, String directory) throws IOException {
        // 获取原始文件名 和 文件输入流
        String originalFilename = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();

        // 创建上传Object的Metadata
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize()); // 设置输入流长度
        objectMetadata.setContentType(file.getContentType()); // 设置ContentType

        // 生成文件key，并创建上传请求
        String key = this.generateFileKey(originalFilename, directory);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, inputStream, objectMetadata);

        // 上传文件，并返回可访问的URL
        cosClient.putObject(putObjectRequest);
        return getFileUrl(key);
    }

    /**
     * 获取文件的公共访问URL
     * @param key 文件key
     * @return URL字符串
     */
    public String getFileUrl(String key) {
        // 使用基础URL和文件key拼接成完整URL
        return baseUrl + "/" + key;
    }

    /**
     * 获取文件的临时访问URL，有效期为一小时
     * @param key 文件key
     * @return URL对象
     */
    public URL getTemporaryUrl(String key) {
        // 设置URL过期时间为1小时
        Date expiration = new Date(System.currentTimeMillis() + 3600 * 1000);
        // 生成临时URL
        return cosClient.generatePresignedUrl(bucketName, key, expiration);
    }

    /**
     * 删除文件
     * @param key 文件key
     */
    public void deleteFile(String key) {
        cosClient.deleteObject(bucketName, key);
    }

    /**
     * 生成文件key，简单地使用目录+文件名
     * @param fileName 文件名
     * @param directory 目录
     * @return 文件key
     */
    private String generateFileKey(String fileName, String directory) {
        // 拼接目录和文件名
        StringBuilder key = new StringBuilder();
        if (directory != null && !directory.isEmpty()) {
            if (!directory.endsWith("/")) {
                directory += "/";
            }
            key.append(directory);
        }
        
        key.append(fileName);
        
        return key.toString();
    }
}
