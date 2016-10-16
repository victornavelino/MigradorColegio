/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Caja.TipoDeEgreso;
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
 * @author franco
 */
public class TipoDeEgresoJpaController implements Serializable {

    public TipoDeEgresoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TipoDeEgreso tipoDeEgreso) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(tipoDeEgreso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TipoDeEgreso tipoDeEgreso) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            tipoDeEgreso = em.merge(tipoDeEgreso);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = tipoDeEgreso.getId();
                if (findTipoDeEgreso(id) == null) {
                    throw new NonexistentEntityException("The tipoDeEgreso with id " + id + " no longer exists.");
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
            TipoDeEgreso tipoDeEgreso;
            try {
                tipoDeEgreso = em.getReference(TipoDeEgreso.class, id);
                tipoDeEgreso.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tipoDeEgreso with id " + id + " no longer exists.", enfe);
            }
            em.remove(tipoDeEgreso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TipoDeEgreso> findTipoDeEgresoEntities() {
        return findTipoDeEgresoEntities(true, -1, -1);
    }

    public List<TipoDeEgreso> findTipoDeEgresoEntities(int maxResults, int firstResult) {
        return findTipoDeEgresoEntities(false, maxResults, firstResult);
    }

    private List<TipoDeEgreso> findTipoDeEgresoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TipoDeEgreso.class));
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

    public TipoDeEgreso findTipoDeEgreso(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TipoDeEgreso.class, id);
        } finally {
            em.close();
        }
    }

    public int getTipoDeEgresoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TipoDeEgreso> rt = cq.from(TipoDeEgreso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
