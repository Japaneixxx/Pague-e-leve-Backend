// src/main/java/com/japaneixxx/pagueleve/service/ImageUploadService.java
package com.japaneixxx.pagueleve.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ImageUploadService {

    @Autowired
    private Cloudinary cloudinary;

    public String uploadImage(MultipartFile file) throws IOException {
        // O método upload retorna um Map com vários detalhes sobre o arquivo enviado
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

        // A URL segura (https) da imagem fica na chave "secure_url"
        return uploadResult.get("secure_url").toString();
    }
}