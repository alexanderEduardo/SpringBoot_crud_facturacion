package com.alex.springboot.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class MVC_config implements WebMvcConfigurer {
    private final Logger logger= LoggerFactory.getLogger(getClass());
    //Para agregar recursos estaticos a nuestro proyecto

      /*@Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        WebMvcConfigurer.super.addResourceHandlers(registry);       */
        //vamos a usar registry para registrar nuestra nueva ruta
        //se configura como directorio estatico
        /*registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:C:/Recursos_IMG/Spring1/");*/
        //Pongo el pongo el toURI para que incluye el esquema file
       /*String resourcePath= Paths.get("uploads").toAbsolutePath().toUri().toString();
        System.out.println("PATH conf : "+resourcePath);
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(resourcePath);
    } */
}
