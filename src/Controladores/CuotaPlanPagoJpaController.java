/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Pago.CuotaPlanPago;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Entidades.Pago.PlanPago;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author franco
 */
public class CuotaPlanPagoJpaController implements Serializable {

    public CuotaPlanPagoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(CuotaPlanPago cuotaPlanPago) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PlanPago planPago = cuotaPlanPago.getPlanPago();
            if (planPago != null) {
                planPago = em.getReference(planPago.getClass(), planPago.getId());
                cuotaPlanPago.setPlanPago(planPago);
            }
            em.persist(cuotaPlanPago);
            if (planPago != null) {
                planPago.getCuotas().add(cuotaPlanPago);
                planPago = em.merge(planPago);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CuotaPlanPago cuotaPlanPago) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            CuotaPlanPago persistentCuotaPlanPago = em.find(CuotaPlanPago.class, cuotaPlanPago.getId());
            PlanPago planPagoOld = persistentCuotaPlanPago.getPlanPago();
            PlanPago planPagoNew = cuotaPlanPago.getPlanPago();
            if (planPagoNew != null) {
                planPagoNew = em.getReference(planPagoNew.getClass(), planPagoNew.getId());
                cuotaPlanPago.setPlanPago(planPagoNew);
            }
            cuotaPlanPago = em.merge(cuotaPlanPago);
            if (planPagoOld != null && !planPagoOld.equals(planPagoNew)) {
                planPagoOld.getCuotas().remove(cuotaPlanPago);
                planPagoOld = em.merge(planPagoOld);
            }
            if (planPagoNew != null && !planPagoNew.equals(planPagoOld)) {
                planPagoNew.getCuotas().add(cuotaPlanPago);
                planPagoNew = em.merge(planPagoNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = cuotaPlanPago.getId();
                if (findCuotaPlanPago(id) == null) {
                    throw new NonexistentEntityException("The cuotaPlanPago with id " + id + " no longer exists.");
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
            CuotaPlanPago cuotaPlanPago;
            try {
                cuotaPlanPago = em.getReference(CuotaPlanPago.class, id);
                cuotaPlanPago.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The cuotaPlanPago with id " + id + " no longer exists.", enfe);
            }
            PlanPago planPago = cuotaPlanPago.getPlanPago();
            if (planPago != null) {
                planPago.getCuotas().remove(cuotaPlanPago);
                planPago = em.merge(planPago);
            }
            em.remove(cuotaPlanPago);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<CuotaPlanPago> findCuotaPlanPagoEntities() {
        return findCuotaPlanPagoEntities(true, -1, -1);
    }

    public List<CuotaPlanPago> findCuotaPlanPagoEntities(int maxResults, int firstResult) {
        return findCuotaPlanPagoEntities(false, maxResults, firstResult);
    }

    private List<CuotaPlanPago> findCuotaPlanPagoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(CuotaPlanPago.class));
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

    public CuotaPlanPago findCuotaPlanPago(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CuotaPlanPago.class, id);
        } finally {
            em.close();
        }
    }

    public int getCuotaPlanPagoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<CuotaPlanPago> rt = cq.from(CuotaPlanPago.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
