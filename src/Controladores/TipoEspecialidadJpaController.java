/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Medico.TipoEspecialidad;
import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

/**
 *
 * @author hugo
 */
public class TipoEspecialidadJpaController implements Serializable {

    public TipoEspecialidadJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TipoEspecialidad tipoEspecialidad) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(tipoEspecialidad);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TipoEspecialidad tipoEspecialidad) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            tipoEspecialidad = em.merge(tipoEspecialidad);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = tipoEspecialidad.getId();
                if (findTipoEspecialidad(id) == null) {
                    throw new NonexistentEntityException("The tipoEspecialidad with id " + id + " no longer exists.");
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
            TipoEspecialidad tipoEspecialidad;
            try {
                tipoEspecialidad = em.getReference(TipoEspecialidad.class, id);
                tipoEspecialidad.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tipoEspecialidad with id " + id + " no longer exists.", enfe);
            }
            em.remove(tipoEspecialidad);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TipoEspecialidad> findTipoEspecialidadEntities() {
        return findTipoEspecialidadEntities(true, -1, -1);
    }

    public List<TipoEspecialidad> findTipoEspecialidadEntities(int maxResults, int firstResult) {
        return findTipoEspecialidadEntities(false, maxResults, firstResult);
    }

    private List<TipoEspecialidad> findTipoEspecialidadEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TipoEspecialidad.class));
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

    public TipoEspecialidad findTipoEspecialidad(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TipoEspecialidad.class, id);
        } finally {
            em.close();
        }
    }

    public int getTipoEspecialidadCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TipoEspecialidad> rt = cq.from(TipoEspecialidad.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
