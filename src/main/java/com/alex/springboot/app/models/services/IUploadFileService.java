package com.alex.springboot.app.models.services;

import org.springframework.core.io.Resource;

import java.net.MalformedURLException;

public interface IUploadFileService {
    Resource obtenerRecursoToResponseEntity(String filename) throws MalformedURLException;
    boolean deleteFile(String clienteFoto);
}
