/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facades;

import Controladores.EspecialidadJpaController;
import Controladores.exceptions.NonexistentEntityException;
import Entidades.Medico.Especialidad;
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
public class EspecialidadFacade {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("MigradorColegioMedicoPU");

    private static EspecialidadFacade instance = null;

    protected EspecialidadFacade() {
    }

    public static EspecialidadFacade getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }
    // creador sincronizado para protegerse de posibles problemas  multi-hilo
    // otra prueba para evitar instanciación múltiple

    private synchronized static void createInstance() {
        if (instance == null) {
            instance = new EspecialidadFacade();
        }
    }

//El metodo "clone" es sobreescrito por el siguiente que arroja una excepción:
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void alta(Especialidad especialidad) {
        new EspecialidadJpaController(emf).create(especialidad);
    }

    public Especialidad buscar(Long id) {
        return new EspecialidadJpaController(emf).findEspecialidad(id);
    }

    public void modificar(Especialidad especialidad) throws Exception {
        try {
            new EspecialidadJpaController(emf).edit(especialidad);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(EspecialidadFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void eliminar(long id) {
        try {
            new EspecialidadJpaController(emf).destroy(id);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(EspecialidadFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Especialidad> getTodos() {
        EntityManagerFactory emfa = Persistence.createEntityManagerFactory("ProyectoDosPU");
        EntityManager em = emfa.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Especialidad s");
        em.getEntityManagerFactory().getCache().evictAll();
        return qu.getResultList();
    }

    public List<Especialidad> buscarPorDescripcion(String descripcion) {
        EntityManager em = emf.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Especialidad s WHERE s.descripcion LIKE :descripcion");
        qu.setParameter("descripcion", "%" + descripcion.toUpperCase() + "%");
        return qu.getResultList();
    }

    public Especialidad buscarPorDescripcionExacta(String descripcion) {
        EntityManager em = emf.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Especialidad s WHERE s.descripcion LIKE :descripcion");
        qu.setParameter("descripcion", descripcion);
        qu.setMaxResults(0);
        try {
            return (Especialidad) qu.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Especialidad> buscarPorNombre(String nombre) {
        EntityManager em = emf.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Especialidad s WHERE s.nombreEspecialidad LIKE :nombre");
        qu.setParameter("nombre", "%" + nombre.toUpperCase() + "%");
        return qu.getResultList();
    }

    public Especialidad buscarPorNombreExacto(String nombre) {
        EntityManager em = emf.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Especialidad s WHERE s.nombreEspecialidad LIKE :nombre");
        qu.setParameter("nombre", nombre);
        qu.setMaxResults(0);
        try {
            return (Especialidad) qu.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    public Especialidad buscarPorCodigo(Long codigo) {
        EntityManager em = emf.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Especialidad s WHERE s.codigoEspecilidad = :codigo");
        qu.setParameter("codigo", codigo);
        qu.setMaxResults(0);
        try {
            return (Especialidad) qu.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}
