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
import Entidades.Medico.Recertificacion;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author franco
 */
public class RecertificacionJpaController implements Serializable {

    public RecertificacionJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Recertificacion recertificacion) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Especializacion especializacion = recertificacion.getEspecializacion();
            if (especializacion != null) {
                especializacion = em.getReference(especializacion.getClass(), especializacion.getId());
                recertificacion.setEspecializacion(especializacion);
            }
            em.persist(recertificacion);
            if (especializacion != null) {
                especializacion.getRecertificaciones().add(recertificacion);
                especializacion = em.merge(especializacion);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Recertificacion recertificacion) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Recertificacion persistentRecertificacion = em.find(Recertificacion.class, recertificacion.getId());
            Especializacion especializacionOld = persistentRecertificacion.getEspecializacion();
            Especializacion especializacionNew = recertificacion.getEspecializacion();
            if (especializacionNew != null) {
                especializacionNew = em.getReference(especializacionNew.getClass(), especializacionNew.getId());
                recertificacion.setEspecializacion(especializacionNew);
            }
            recertificacion = em.merge(recertificacion);
            if (especializacionOld != null && !especializacionOld.equals(especializacionNew)) {
                especializacionOld.getRecertificaciones().remove(recertificacion);
                especializacionOld = em.merge(especializacionOld);
            }
            if (especializacionNew != null && !especializacionNew.equals(especializacionOld)) {
                especializacionNew.getRecertificaciones().add(recertificacion);
                especializacionNew = em.merge(especializacionNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = recertificacion.getId();
                if (findRecertificacion(id) == null) {
                    throw new NonexistentEntityException("The recertificacion with id " + id + " no longer exists.");
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
            Recertificacion recertificacion;
            try {
                recertificacion = em.getReference(Recertificacion.class, id);
                recertificacion.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The recertificacion with id " + id + " no longer exists.", enfe);
            }
            Especializacion especializacion = recertificacion.getEspecializacion();
            if (especializacion != null) {
                especializacion.getRecertificaciones().remove(recertificacion);
                especializacion = em.merge(especializacion);
            }
            em.remove(recertificacion);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Recertificacion> findRecertificacionEntities() {
        return findRecertificacionEntities(true, -1, -1);
    }

    public List<Recertificacion> findRecertificacionEntities(int maxResults, int firstResult) {
        return findRecertificacionEntities(false, maxResults, firstResult);
    }

    private List<Recertificacion> findRecertificacionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Recertificacion.class));
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

    public Recertificacion findRecertificacion(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Recertificacion.class, id);
        } finally {
            em.close();
        }
    }

    public int getRecertificacionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Recertificacion> rt = cq.from(Recertificacion.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
