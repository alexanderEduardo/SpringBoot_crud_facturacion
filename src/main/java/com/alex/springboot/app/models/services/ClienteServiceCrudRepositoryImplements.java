package com.alex.springboot.app.models.services;

import com.alex.springboot.app.dao.IClienteDao_CrudRepository;
import com.alex.springboot.app.models.entity.Cliente;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service("Cliente_ServiceCrudRepository")
public class ClienteServiceCrudRepositoryImplements implements IClienteService{

    @Autowired
    IClienteDao_CrudRepository iClienteDaoCrudRepo;

    @Override @Transactional(readOnly = true)
    public List<Cliente> findAll() {
        return (List<Cliente>) iClienteDaoCrudRepo.findAll();
    }
    /*Paginacion */
    @Override @Transactional(readOnly = true)
    public Page<Cliente> findAll(Pageable pageable) {
        return iClienteDaoCrudRepo.findAll(pageable);
    }

    @Override @Transactional
    public void save(Cliente cliente) {
        iClienteDaoCrudRepo.save(cliente);
    }

    @Override @Transactional(readOnly = true)
    public Cliente findOne(Long id) {

        return iClienteDaoCrudRepo.findById(id).orElse(null);
    }

    @Override @Transactional()
    public void delete(Long id) {
        iClienteDaoCrudRepo.deleteById(id);
    }

   /*@Override*/
    public void auto_incremental() {
    //    iClienteDaoCrudRepo.auto_increment();
    }
}
