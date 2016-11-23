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
import Entidades.Reporte.Parametro;
import Entidades.Reporte.ReporteGenerico;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author franco
 */
public class ReporteGenericoJpaController implements Serializable {

    public ReporteGenericoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(ReporteGenerico reporteGenerico) {
        if (reporteGenerico.getLstParametros() == null) {
            reporteGenerico.setLstParametros(new ArrayList<Parametro>());
        }
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            List<Parametro> attachedLstParametros = new ArrayList<Parametro>();
            for (Parametro lstParametrosParametroToAttach : reporteGenerico.getLstParametros()) {
                lstParametrosParametroToAttach = em.getReference(lstParametrosParametroToAttach.getClass(), lstParametrosParametroToAttach.getId());
                attachedLstParametros.add(lstParametrosParametroToAttach);
            }
            reporteGenerico.setLstParametros(attachedLstParametros);
            em.persist(reporteGenerico);
            for (Parametro lstParametrosParametro : reporteGenerico.getLstParametros()) {
                ReporteGenerico oldReporteGenericoOfLstParametrosParametro = lstParametrosParametro.getReporteGenerico();
                lstParametrosParametro.setReporteGenerico(reporteGenerico);
                lstParametrosParametro = em.merge(lstParametrosParametro);
                if (oldReporteGenericoOfLstParametrosParametro != null) {
                    oldReporteGenericoOfLstParametrosParametro.getLstParametros().remove(lstParametrosParametro);
                    oldReporteGenericoOfLstParametrosParametro = em.merge(oldReporteGenericoOfLstParametrosParametro);
                }
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(ReporteGenerico reporteGenerico) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ReporteGenerico persistentReporteGenerico = em.find(ReporteGenerico.class, reporteGenerico.getId());
            List<Parametro> lstParametrosOld = persistentReporteGenerico.getLstParametros();
            List<Parametro> lstParametrosNew = reporteGenerico.getLstParametros();
            List<Parametro> attachedLstParametrosNew = new ArrayList<Parametro>();
            for (Parametro lstParametrosNewParametroToAttach : lstParametrosNew) {
                lstParametrosNewParametroToAttach = em.getReference(lstParametrosNewParametroToAttach.getClass(), lstParametrosNewParametroToAttach.getId());
                attachedLstParametrosNew.add(lstParametrosNewParametroToAttach);
            }
            lstParametrosNew = attachedLstParametrosNew;
            reporteGenerico.setLstParametros(lstParametrosNew);
            reporteGenerico = em.merge(reporteGenerico);
            for (Parametro lstParametrosOldParametro : lstParametrosOld) {
                if (!lstParametrosNew.contains(lstParametrosOldParametro)) {
                    lstParametrosOldParametro.setReporteGenerico(null);
                    lstParametrosOldParametro = em.merge(lstParametrosOldParametro);
                }
            }
            for (Parametro lstParametrosNewParametro : lstParametrosNew) {
                if (!lstParametrosOld.contains(lstParametrosNewParametro)) {
                    ReporteGenerico oldReporteGenericoOfLstParametrosNewParametro = lstParametrosNewParametro.getReporteGenerico();
                    lstParametrosNewParametro.setReporteGenerico(reporteGenerico);
                    lstParametrosNewParametro = em.merge(lstParametrosNewParametro);
                    if (oldReporteGenericoOfLstParametrosNewParametro != null && !oldReporteGenericoOfLstParametrosNewParametro.equals(reporteGenerico)) {
                        oldReporteGenericoOfLstParametrosNewParametro.getLstParametros().remove(lstParametrosNewParametro);
                        oldReporteGenericoOfLstParametrosNewParametro = em.merge(oldReporteGenericoOfLstParametrosNewParametro);
                    }
                }
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = reporteGenerico.getId();
                if (findReporteGenerico(id) == null) {
                    throw new NonexistentEntityException("The reporteGenerico with id " + id + " no longer exists.");
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
            ReporteGenerico reporteGenerico;
            try {
                reporteGenerico = em.getReference(ReporteGenerico.class, id);
                reporteGenerico.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The reporteGenerico with id " + id + " no longer exists.", enfe);
            }
            List<Parametro> lstParametros = reporteGenerico.getLstParametros();
            for (Parametro lstParametrosParametro : lstParametros) {
                lstParametrosParametro.setReporteGenerico(null);
                lstParametrosParametro = em.merge(lstParametrosParametro);
            }
            em.remove(reporteGenerico);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<ReporteGenerico> findReporteGenericoEntities() {
        return findReporteGenericoEntities(true, -1, -1);
    }

    public List<ReporteGenerico> findReporteGenericoEntities(int maxResults, int firstResult) {
        return findReporteGenericoEntities(false, maxResults, firstResult);
    }

    private List<ReporteGenerico> findReporteGenericoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(ReporteGenerico.class));
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

    public ReporteGenerico findReporteGenerico(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(ReporteGenerico.class, id);
        } finally {
            em.close();
        }
    }

    public int getReporteGenericoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<ReporteGenerico> rt = cq.from(ReporteGenerico.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
