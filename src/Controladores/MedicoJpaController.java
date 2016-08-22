/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Entidades.Medico.Especializacion;
import Entidades.Medico.Medico;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author hugo
 */
public class MedicoJpaController implements Serializable {

    public MedicoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Medico medico) {
        if (medico.getEspecializaciones() == null) {
            medico.setEspecializaciones(new ArrayList<Especializacion>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Especializacion> attachedEspecializaciones = new ArrayList<Especializacion>();
            for (Especializacion especializacionesEspecializacionToAttach : medico.getEspecializaciones()) {
                especializacionesEspecializacionToAttach = em.getReference(especializacionesEspecializacionToAttach.getClass(), especializacionesEspecializacionToAttach.getId());
                attachedEspecializaciones.add(especializacionesEspecializacionToAttach);
            }
            medico.setEspecializaciones(attachedEspecializaciones);
            em.persist(medico);
            for (Especializacion especializacionesEspecializacion : medico.getEspecializaciones()) {
                Medico oldMedicoOfEspecializacionesEspecializacion = especializacionesEspecializacion.getMedico();
                especializacionesEspecializacion.setMedico(medico);
                especializacionesEspecializacion = em.merge(especializacionesEspecializacion);
                if (oldMedicoOfEspecializacionesEspecializacion != null) {
                    oldMedicoOfEspecializacionesEspecializacion.getEspecializaciones().remove(especializacionesEspecializacion);
                    oldMedicoOfEspecializacionesEspecializacion = em.merge(oldMedicoOfEspecializacionesEspecializacion);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Medico medico) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Medico persistentMedico = em.find(Medico.class, medico.getId());
            List<Especializacion> especializacionesOld = persistentMedico.getEspecializaciones();
            List<Especializacion> especializacionesNew = medico.getEspecializaciones();
            List<Especializacion> attachedEspecializacionesNew = new ArrayList<Especializacion>();
            for (Especializacion especializacionesNewEspecializacionToAttach : especializacionesNew) {
                especializacionesNewEspecializacionToAttach = em.getReference(especializacionesNewEspecializacionToAttach.getClass(), especializacionesNewEspecializacionToAttach.getId());
                attachedEspecializacionesNew.add(especializacionesNewEspecializacionToAttach);
            }
            especializacionesNew = attachedEspecializacionesNew;
            medico.setEspecializaciones(especializacionesNew);
            medico = em.merge(medico);
            for (Especializacion especializacionesOldEspecializacion : especializacionesOld) {
                if (!especializacionesNew.contains(especializacionesOldEspecializacion)) {
                    especializacionesOldEspecializacion.setMedico(null);
                    especializacionesOldEspecializacion = em.merge(especializacionesOldEspecializacion);
                }
            }
            for (Especializacion especializacionesNewEspecializacion : especializacionesNew) {
                if (!especializacionesOld.contains(especializacionesNewEspecializacion)) {
                    Medico oldMedicoOfEspecializacionesNewEspecializacion = especializacionesNewEspecializacion.getMedico();
                    especializacionesNewEspecializacion.setMedico(medico);
                    especializacionesNewEspecializacion = em.merge(especializacionesNewEspecializacion);
                    if (oldMedicoOfEspecializacionesNewEspecializacion != null && !oldMedicoOfEspecializacionesNewEspecializacion.equals(medico)) {
                        oldMedicoOfEspecializacionesNewEspecializacion.getEspecializaciones().remove(especializacionesNewEspecializacion);
                        oldMedicoOfEspecializacionesNewEspecializacion = em.merge(oldMedicoOfEspecializacionesNewEspecializacion);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = medico.getId();
                if (findMedico(id) == null) {
                    throw new NonexistentEntityException("The medico with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Medico medico;
            try {
                medico = em.getReference(Medico.class, id);
                medico.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The medico with id " + id + " no longer exists.", enfe);
            }
            List<Especializacion> especializaciones = medico.getEspecializaciones();
            for (Especializacion especializacionesEspecializacion : especializaciones) {
                especializacionesEspecializacion.setMedico(null);
                especializacionesEspecializacion = em.merge(especializacionesEspecializacion);
            }
            em.remove(medico);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Medico> findMedicoEntities() {
        return findMedicoEntities(true, -1, -1);
    }

    public List<Medico> findMedicoEntities(int maxResults, int firstResult) {
        return findMedicoEntities(false, maxResults, firstResult);
    }

    private List<Medico> findMedicoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Medico.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Medico findMedico(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Medico.class, id);
        } finally {
            em.close();
        }
    }

    public int getMedicoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Medico> rt = cq.from(Medico.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
