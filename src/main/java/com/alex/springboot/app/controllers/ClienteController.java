package com.alex.springboot.app.controllers;

import com.alex.springboot.app.models.entity.Cliente;
import com.alex.springboot.app.models.entity.ItemFactura;
import com.alex.springboot.app.models.services.IClienteService;
import com.alex.springboot.app.models.services.IUploadFileService;
import com.alex.springboot.app.util.paginator.PageRender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Controller
@SessionAttributes("cliente")
public class ClienteController {
    /*@Autowired
    @Qualifier("ClienteDaoImplement")
    private IClienteDao iClienteDao;*/
    @Autowired /*@Qualifier("ClienteServiceImpls")*/
    @Qualifier("Cliente_ServiceCrudRepository")
    private IClienteService iClienteService;
    @Autowired
    IUploadFileService fileService;
    @RequestMapping(value = "/listar", method = RequestMethod.GET)
    public String listar(@RequestParam(name = "page", defaultValue = "0") int page, Model model, HttpServletResponse redirect, RedirectAttributes redirectAttributes) {
        Pageable pageRequest = PageRequest.of(page, 4); //(pag-actual , sizexpag)
        Page<Cliente> clientesPage = iClienteService.findAll(pageRequest); //en el find old carga el page con la bd
        System.out.println("totalPag: " + clientesPage.getTotalPages() + "size: " + clientesPage.getSize() + "prev: " + clientesPage.hasPrevious());
        PageRender<Cliente> pageRender = new PageRender<>("/listar", clientesPage);
        model.addAttribute("page", pageRender);
        model.addAttribute("clientes", clientesPage);
        /* iClienteService.auto_incremental();*/
        //model.addAttribute("clientes", iClienteService.findAll());
        String prueba = (String) model.asMap().get("prueba");
        System.out.println(prueba);
        model.addAttribute("prueba", prueba);
        model.addAttribute("titulo", "Listado de Clientes");
        return "listar";
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, "createAt", new CustomDateEditor(dateFormat, false));
    }

    @GetMapping("/form")
    public String crear(Map<String, Object> model) {
        Cliente cliente = new Cliente();
        model.put("cliente", cliente);
        model.put("titulo", "formulario de Clientes");
        return "form";
    }

    /**
     * Este metodo es para Editar
     **/
    @RequestMapping(value = "/form/{id}")
    public String editar(@PathVariable(value = "id") Long id, Map<String, Object> model, RedirectAttributes redirect) {
        Cliente cliente = null;
        /* if (id>0){*/
        cliente = iClienteService.findOne(id);
        /* }else {return "redirect:/listar";}*/
        if (cliente == null) { //si por ejemplo pongon /form/100 y no existe esta id me envia a /listar
            redirect.addFlashAttribute("error", "El ID del cliente no existe comprueba si el ID es corecto");
            return "redirect:/listar";
        }
        model.put("cliente", cliente);  //envio este objeto al form y carga sus atributos para luego ser encriptados de nuevo
        model.put("titulo", "Editar Cliente");
        return "form"; /* retorna al formulario html con el objeto cargado y rellena los inputs */
    }

    @RequestMapping(value = "/eliminar/{id}")
    public String delete(@PathVariable("id") Long id, RedirectAttributes redirect) {
        Cliente cliente = null;
        cliente = iClienteService.findOne(id);
        if (cliente != null) {
            iClienteService.delete(id);
            redirect.addFlashAttribute("warning", "El usuario ha sido eliminado");
            //Eliminando la foto
                if (fileService.deleteFile(cliente.getFoto())) {
                    redirect.addFlashAttribute("info", "Foto "+cliente.getFoto()+" eliminada con exito");
                }
        }else{
            redirect.addFlashAttribute("error", "El usuario que quiere eliminar no existe en la BD");
        }
        return "redirect:/listar";
    }

    /**
     * Este es el metodo de envio del formulario
     **/
    @RequestMapping(value = "/form", method = RequestMethod.POST) /*Aca si falla la validacion la url se cambia al path del post*/
    public String guardar(@Valid @ModelAttribute("cliente") Cliente cliente, BindingResult result,
                          @RequestParam("file") MultipartFile foto, Model model, RedirectAttributes redirect, SessionStatus status) {
        System.out.print("---"+"ID: " +cliente.getId());
        if (result.hasErrors()) {   /** Si cliente.getId() es != null es porque es un crear clientes sino es un editar Clientes ya que al crear el cliente no tiene id ya que el id se generea al hacer persist  **/
            String titulo = cliente.getId() != null ? "Editar Cliente" : "Formulario de Clientes ";
            model.addAttribute("titulo", titulo);
            return "form"; /* envia el objeto cliente cargado implicitamente*/
        }
        System.out.println("*ID  : "+cliente.getId());
        System.out.println("*FOTO :"+cliente.getFoto()+"--length: "+cliente.getFoto().length() );
        System.out.println(" Param ... FILE:"+foto.isEmpty());
        if (!foto.isEmpty()) {
            System.out.println("-------!foto.isEmpty()-------");
            //Este if me sirve para saber si estoy haciendo un editar ya que el cliente ya tiene id y foto creada
            if(cliente.getId()!=null && cliente.getFoto()!=null && cliente.getFoto().length()>0){
                fileService.deleteFile(cliente.getFoto());
            }
            //Path directorioResource = Paths.get("src//main//resources//static//upload");
            //String rootPath = directorioResource.toFile().getAbsolutePath();
            String uniqueFilename = UUID.randomUUID().toString() + "_" + foto.getOriginalFilename();
            /*String rootPath="C://Recursos_IMG//Spring1";
            Path rootAbsolutePath = Paths.get(rootPath + "//" + uniqueFilename);*/
            Path rootPath = Paths.get("uploads").resolve(uniqueFilename);
            Path rootAbsolutePath = rootPath.toAbsolutePath();
            System.out.println("rootpath: " + rootPath);
            System.out.println("rootAbsolutePath: " + rootAbsolutePath);
            try {
                //Primer Metodo
                /*byte[] bytes = foto.getBytes();
                Files.write(rootAbsolutePath, bytes);//aca escribiando la imagen al directorio upload*/
                Files.copy(foto.getInputStream(), rootAbsolutePath);
                cliente.setFoto(uniqueFilename);
                redirect.addFlashAttribute("info", "Has subido correctamente '"
                        + uniqueFilename + "'");
            } catch (Exception e) {
                System.out.println(e);
            }
        }else{//este else sirve para borrar la foto si no se coloca en el formulario
            fileService.deleteFile(cliente.getFoto());
            cliente.setFoto("");
        }
        System.out.println("FOTO after validation: " + cliente.getFoto());
        String msgFlash = cliente.getId() != null ? "Cliente editado correctamente" : "Cliente guardado con exito";
        System.out.println("pre-save: " + cliente.getId());
        iClienteService.save(cliente);
        System.out.println("save: " + cliente.getId());
        status.setComplete(); //aca se elimina de la sesion el cliente ya que se persistio en la bd
        redirect.addFlashAttribute("prueba", "Atributo Redirect Test");
        redirect.addFlashAttribute("success", msgFlash);

        return "redirect:listar";
    }
    /**
     * Metodo para ver el Detalle del cliente
     **/
    @GetMapping(value = "/ver/{id}")
    public String ver(@PathVariable Long id, Map<String, Object> model, RedirectAttributes flash) {
        Cliente cliente = iClienteService.findOne(id);
        if (cliente == null) {
            flash.addFlashAttribute("error", "El cliente no existe en la BD");
            return "redirect:/listar";
        }
        model.put("cliente", cliente);
        if(!cliente.getFacturas().isEmpty()){
            System.out.println(cliente.getFacturas().get(0).getItems());
            for(ItemFactura lol : cliente.getFacturas().get(0).getItems()){
                System.out.println(lol.getProducto().getNombre());
            }
        }
        model.put("titulo", "Detalle Cliente" + cliente.getNombre());
        return "ver";
    }



    /**
     * Otro metodo de obtener imagenes , el /upload/ hace referencia al src del img en el ver
     **/

    @GetMapping(value = "/upload/{filename:.+}")
    public ResponseEntity<Resource> buscarImagen(@PathVariable String filename) {
        /*Path pathfoto = Paths.get("uploads").resolve(filename).toAbsolutePath();
        System.out.println("pathFoto2 : " + pathfoto);
        Resource resource = null;
        try {
            resource = new UrlResource(pathfoto.toUri());
            System.out.println("RESOURCE : " + resource);
            if (!resource.exists() && !resource.isReadable()) {
                throw new RuntimeException("Error: no se puede cargar la imagen" + pathfoto.toString());
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }*/
        Resource resource= null;
        try {
            resource = fileService.obtenerRecursoToResponseEntity(filename);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // junta la imagen y la rspta
        return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}


