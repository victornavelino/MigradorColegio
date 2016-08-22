/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facades;

import Controladores.MedicoJpaController;
import Controladores.exceptions.NonexistentEntityException;
import Entidades.Medico.Medico;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author hugo
 */
public class MedicoFacade {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("ProyectoDosPU");

    private static MedicoFacade instance = null;

    protected MedicoFacade() {
    }

    public static MedicoFacade getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }
    // creador sincronizado para protegerse de posibles problemas  multi-hilo
    // otra prueba para evitar instanciación múltiple

    private synchronized static void createInstance() {
        if (instance == null) {
            instance = new MedicoFacade();
        }
    }

//El metodo "clone" es sobreescrito por el siguiente que arroja una excepción:
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void alta(Medico medico) {
        new MedicoJpaController(emf).create(medico);
    }

    public Medico buscar(Long id) {
        return new MedicoJpaController(emf).findMedico(id);
    }

    public void modificar(Medico medico) throws Exception {
        try {
            new MedicoJpaController(emf).edit(medico);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(MedicoFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void eliminar(long id) {
        try {
            new MedicoJpaController(emf).destroy(id);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(MedicoFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Medico> getTodos() {
        EntityManagerFactory emfa = Persistence.createEntityManagerFactory("ProyectoDosPU");
        EntityManager em = emfa.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Medico s");
        em.getEntityManagerFactory().getCache().evictAll();
        return qu.getResultList();
    }



    public List<Medico> buscarPorDescripcion(String descripcion) {
        EntityManager em = emf.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Medico s WHERE s.titulo LIKE :descripcion");
        qu.setParameter("descripcion", "%" + descripcion.toUpperCase() + "%");
        return qu.getResultList();
    }

}
