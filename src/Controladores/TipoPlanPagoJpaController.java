/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Pago.TipoPlanPago;
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
public class TipoPlanPagoJpaController implements Serializable {

    public TipoPlanPagoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TipoPlanPago tipoPlanPago) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(tipoPlanPago);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TipoPlanPago tipoPlanPago) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            tipoPlanPago = em.merge(tipoPlanPago);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = tipoPlanPago.getId();
                if (findTipoPlanPago(id) == null) {
                    throw new NonexistentEntityException("The tipoPlanPago with id " + id + " no longer exists.");
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
            TipoPlanPago tipoPlanPago;
            try {
                tipoPlanPago = em.getReference(TipoPlanPago.class, id);
                tipoPlanPago.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The tipoPlanPago with id " + id + " no longer exists.", enfe);
            }
            em.remove(tipoPlanPago);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TipoPlanPago> findTipoPlanPagoEntities() {
        return findTipoPlanPagoEntities(true, -1, -1);
    }

    public List<TipoPlanPago> findTipoPlanPagoEntities(int maxResults, int firstResult) {
        return findTipoPlanPagoEntities(false, maxResults, firstResult);
    }

    private List<TipoPlanPago> findTipoPlanPagoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TipoPlanPago.class));
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

    public TipoPlanPago findTipoPlanPago(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TipoPlanPago.class, id);
        } finally {
            em.close();
        }
    }

    public int getTipoPlanPagoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TipoPlanPago> rt = cq.from(TipoPlanPago.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
