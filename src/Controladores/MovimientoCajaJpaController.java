/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Caja.MovimientoCaja;
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
public class MovimientoCajaJpaController implements Serializable {

    public MovimientoCajaJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(MovimientoCaja movimientoCaja) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(movimientoCaja);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(MovimientoCaja movimientoCaja) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            movimientoCaja = em.merge(movimientoCaja);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = movimientoCaja.getId();
                if (findMovimientoCaja(id) == null) {
                    throw new NonexistentEntityException("The movimientoCaja with id " + id + " no longer exists.");
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
            MovimientoCaja movimientoCaja;
            try {
                movimientoCaja = em.getReference(MovimientoCaja.class, id);
                movimientoCaja.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The movimientoCaja with id " + id + " no longer exists.", enfe);
            }
            em.remove(movimientoCaja);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<MovimientoCaja> findMovimientoCajaEntities() {
        return findMovimientoCajaEntities(true, -1, -1);
    }

    public List<MovimientoCaja> findMovimientoCajaEntities(int maxResults, int firstResult) {
        return findMovimientoCajaEntities(false, maxResults, firstResult);
    }

    private List<MovimientoCaja> findMovimientoCajaEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(MovimientoCaja.class));
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

    public MovimientoCaja findMovimientoCaja(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(MovimientoCaja.class, id);
        } finally {
            em.close();
        }
    }

    public int getMovimientoCajaCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<MovimientoCaja> rt = cq.from(MovimientoCaja.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
