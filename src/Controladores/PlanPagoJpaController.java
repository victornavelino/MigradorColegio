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
import Entidades.Medico.Medico;
import Entidades.Pago.CuotaPlanPago;
import Entidades.Pago.PlanPago;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author franco
 */
public class PlanPagoJpaController implements Serializable {

    public PlanPagoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(PlanPago planPago) {
        if (planPago.getCuotas() == null) {
            planPago.setCuotas(new ArrayList<CuotaPlanPago>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Medico medico = planPago.getMedico();
            if (medico != null) {
                medico = em.getReference(medico.getClass(), medico.getId());
                planPago.setMedico(medico);
            }
            List<CuotaPlanPago> attachedCuotas = new ArrayList<CuotaPlanPago>();
            for (CuotaPlanPago cuotasCuotaPlanPagoToAttach : planPago.getCuotas()) {
                cuotasCuotaPlanPagoToAttach = em.getReference(cuotasCuotaPlanPagoToAttach.getClass(), cuotasCuotaPlanPagoToAttach.getId());
                attachedCuotas.add(cuotasCuotaPlanPagoToAttach);
            }
            planPago.setCuotas(attachedCuotas);
            em.persist(planPago);
            if (medico != null) {
                medico.getPlanPagos().add(planPago);
                medico = em.merge(medico);
            }
            for (CuotaPlanPago cuotasCuotaPlanPago : planPago.getCuotas()) {
                PlanPago oldPlanPagoOfCuotasCuotaPlanPago = cuotasCuotaPlanPago.getPlanPago();
                cuotasCuotaPlanPago.setPlanPago(planPago);
                cuotasCuotaPlanPago = em.merge(cuotasCuotaPlanPago);
                if (oldPlanPagoOfCuotasCuotaPlanPago != null) {
                    oldPlanPagoOfCuotasCuotaPlanPago.getCuotas().remove(cuotasCuotaPlanPago);
                    oldPlanPagoOfCuotasCuotaPlanPago = em.merge(oldPlanPagoOfCuotasCuotaPlanPago);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(PlanPago planPago) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            PlanPago persistentPlanPago = em.find(PlanPago.class, planPago.getId());
            Medico medicoOld = persistentPlanPago.getMedico();
            Medico medicoNew = planPago.getMedico();
            List<CuotaPlanPago> cuotasOld = persistentPlanPago.getCuotas();
            List<CuotaPlanPago> cuotasNew = planPago.getCuotas();
            if (medicoNew != null) {
                medicoNew = em.getReference(medicoNew.getClass(), medicoNew.getId());
                planPago.setMedico(medicoNew);
            }
            List<CuotaPlanPago> attachedCuotasNew = new ArrayList<CuotaPlanPago>();
            for (CuotaPlanPago cuotasNewCuotaPlanPagoToAttach : cuotasNew) {
                cuotasNewCuotaPlanPagoToAttach = em.getReference(cuotasNewCuotaPlanPagoToAttach.getClass(), cuotasNewCuotaPlanPagoToAttach.getId());
                attachedCuotasNew.add(cuotasNewCuotaPlanPagoToAttach);
            }
            cuotasNew = attachedCuotasNew;
            planPago.setCuotas(cuotasNew);
            planPago = em.merge(planPago);
            if (medicoOld != null && !medicoOld.equals(medicoNew)) {
                medicoOld.getPlanPagos().remove(planPago);
                medicoOld = em.merge(medicoOld);
            }
            if (medicoNew != null && !medicoNew.equals(medicoOld)) {
                medicoNew.getPlanPagos().add(planPago);
                medicoNew = em.merge(medicoNew);
            }
            for (CuotaPlanPago cuotasOldCuotaPlanPago : cuotasOld) {
                if (!cuotasNew.contains(cuotasOldCuotaPlanPago)) {
                    cuotasOldCuotaPlanPago.setPlanPago(null);
                    cuotasOldCuotaPlanPago = em.merge(cuotasOldCuotaPlanPago);
                }
            }
            for (CuotaPlanPago cuotasNewCuotaPlanPago : cuotasNew) {
                if (!cuotasOld.contains(cuotasNewCuotaPlanPago)) {
                    PlanPago oldPlanPagoOfCuotasNewCuotaPlanPago = cuotasNewCuotaPlanPago.getPlanPago();
                    cuotasNewCuotaPlanPago.setPlanPago(planPago);
                    cuotasNewCuotaPlanPago = em.merge(cuotasNewCuotaPlanPago);
                    if (oldPlanPagoOfCuotasNewCuotaPlanPago != null && !oldPlanPagoOfCuotasNewCuotaPlanPago.equals(planPago)) {
                        oldPlanPagoOfCuotasNewCuotaPlanPago.getCuotas().remove(cuotasNewCuotaPlanPago);
                        oldPlanPagoOfCuotasNewCuotaPlanPago = em.merge(oldPlanPagoOfCuotasNewCuotaPlanPago);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = planPago.getId();
                if (findPlanPago(id) == null) {
                    throw new NonexistentEntityException("The planPago with id " + id + " no longer exists.");
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
            PlanPago planPago;
            try {
                planPago = em.getReference(PlanPago.class, id);
                planPago.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The planPago with id " + id + " no longer exists.", enfe);
            }
            Medico medico = planPago.getMedico();
            if (medico != null) {
                medico.getPlanPagos().remove(planPago);
                medico = em.merge(medico);
            }
            List<CuotaPlanPago> cuotas = planPago.getCuotas();
            for (CuotaPlanPago cuotasCuotaPlanPago : cuotas) {
                cuotasCuotaPlanPago.setPlanPago(null);
                cuotasCuotaPlanPago = em.merge(cuotasCuotaPlanPago);
            }
            em.remove(planPago);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<PlanPago> findPlanPagoEntities() {
        return findPlanPagoEntities(true, -1, -1);
    }

    public List<PlanPago> findPlanPagoEntities(int maxResults, int firstResult) {
        return findPlanPagoEntities(false, maxResults, firstResult);
    }

    private List<PlanPago> findPlanPagoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(PlanPago.class));
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

    public PlanPago findPlanPago(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(PlanPago.class, id);
        } finally {
            em.close();
        }
    }

    public int getPlanPagoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<PlanPago> rt = cq.from(PlanPago.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
