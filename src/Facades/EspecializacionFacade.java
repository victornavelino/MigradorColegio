/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Facades;

import Controladores.EspecializacionJpaController;
import Controladores.exceptions.NonexistentEntityException;
import Entidades.Medico.Especialidad;
import Entidades.Medico.Especializacion;
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
public class EspecializacionFacade {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("MigradorColegioMedicoPU");

    private static EspecializacionFacade instance = null;

    protected EspecializacionFacade() {
    }

    public static EspecializacionFacade getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }
    // creador sincronizado para protegerse de posibles problemas  multi-hilo
    // otra prueba para evitar instanciación múltiple

    private synchronized static void createInstance() {
        if (instance == null) {
            instance = new EspecializacionFacade();
        }
    }

//El metodo "clone" es sobreescrito por el siguiente que arroja una excepción:
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public void alta(Especializacion especializacion) {
        new EspecializacionJpaController(emf).create(especializacion);
    }

    public Especializacion buscar(Long id) {
        return new EspecializacionJpaController(emf).findEspecializacion(id);
    }

    public void modificar(Especializacion especializacion) throws Exception {
        try {
            new EspecializacionJpaController(emf).edit(especializacion);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(EspecializacionFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void eliminar(long id) {
        try {
            new EspecializacionJpaController(emf).destroy(id);
        } catch (NonexistentEntityException ex) {
            Logger.getLogger(EspecializacionFacade.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<Especializacion> getTodos() {
        EntityManagerFactory emfa = Persistence.createEntityManagerFactory("ProyectoDosPU");
        EntityManager em = emfa.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Especializacion s");
        em.getEntityManagerFactory().getCache().evictAll();
        return qu.getResultList();
    }

    public List<Especializacion> buscarPorEspecialidad(Especialidad especialidad) {
        EntityManager em = emf.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Especializacion s WHERE s.especialidad = :especialidad");
        qu.setParameter("especialidad", especialidad);
        return qu.getResultList();
    }

    public Especializacion buscarPorMatriculaEspecialidad(String descripcion) {
        EntityManager em = emf.createEntityManager();
        Query qu = em.createQuery("SELECT s FROM Especializacion s WHERE s.matriculaEspecialidad = :descripcion");
        qu.setParameter("descripcion", descripcion);
        qu.setMaxResults(0);
        try {
            return (Especializacion) qu.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

}
