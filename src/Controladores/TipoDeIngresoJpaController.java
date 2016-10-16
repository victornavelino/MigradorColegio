/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Caja.TipoDeIngreso;
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
public class TipoDeIngresoJpaController implements Serializable {

    public TipoDeIngresoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TipoDeIngreso tipoDeIngreso) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(tipoDeIngreso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TipoDeIngreso tipoDeIngreso) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            tipoDeIngreso = em.merge(tipoDeIngreso);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = tipoDeIngreso.getId();
                if (findTipoDeIngreso(id) == null) {
                    throw new NonexistentEntityException("The tipoDeIngreso with id " + id + " no longer exists.");
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
            TipoDeIngreso tipoDeIngreso;
            try {
                tipoDeIngreso = em.getReference(TipoDeIngreso.class, id);
                tipoDeIngreso.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tipoDeIngreso with id " + id + " no longer exists.", enfe);
            }
            em.remove(tipoDeIngreso);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TipoDeIngreso> findTipoDeIngresoEntities() {
        return findTipoDeIngresoEntities(true, -1, -1);
    }

    public List<TipoDeIngreso> findTipoDeIngresoEntities(int maxResults, int firstResult) {
        return findTipoDeIngresoEntities(false, maxResults, firstResult);
    }

    private List<TipoDeIngreso> findTipoDeIngresoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TipoDeIngreso.class));
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

    public TipoDeIngreso findTipoDeIngreso(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TipoDeIngreso.class, id);
        } finally {
            em.close();
        }
    }

    public int getTipoDeIngresoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TipoDeIngreso> rt = cq.from(TipoDeIngreso.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
