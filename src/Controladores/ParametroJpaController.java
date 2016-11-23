/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Reporte.Parametro;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Entidades.Reporte.ReporteGenerico;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author franco
 */
public class ParametroJpaController implements Serializable {

    public ParametroJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Parametro parametro) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            ReporteGenerico reporteGenerico = parametro.getReporteGenerico();
            if (reporteGenerico != null) {
                reporteGenerico = em.getReference(reporteGenerico.getClass(), reporteGenerico.getId());
                parametro.setReporteGenerico(reporteGenerico);
            }
            em.persist(parametro);
            if (reporteGenerico != null) {
                reporteGenerico.getLstParametros().add(parametro);
                reporteGenerico = em.merge(reporteGenerico);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Parametro parametro) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Parametro persistentParametro = em.find(Parametro.class, parametro.getId());
            ReporteGenerico reporteGenericoOld = persistentParametro.getReporteGenerico();
            ReporteGenerico reporteGenericoNew = parametro.getReporteGenerico();
            if (reporteGenericoNew != null) {
                reporteGenericoNew = em.getReference(reporteGenericoNew.getClass(), reporteGenericoNew.getId());
                parametro.setReporteGenerico(reporteGenericoNew);
            }
            parametro = em.merge(parametro);
            if (reporteGenericoOld != null && !reporteGenericoOld.equals(reporteGenericoNew)) {
                reporteGenericoOld.getLstParametros().remove(parametro);
                reporteGenericoOld = em.merge(reporteGenericoOld);
            }
            if (reporteGenericoNew != null && !reporteGenericoNew.equals(reporteGenericoOld)) {
                reporteGenericoNew.getLstParametros().add(parametro);
                reporteGenericoNew = em.merge(reporteGenericoNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = parametro.getId();
                if (findParametro(id) == null) {
                    throw new NonexistentEntityException("The parametro with id " + id + " no longer exists.");
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
            Parametro parametro;
            try {
                parametro = em.getReference(Parametro.class, id);
                parametro.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The parametro with id " + id + " no longer exists.", enfe);
            }
            ReporteGenerico reporteGenerico = parametro.getReporteGenerico();
            if (reporteGenerico != null) {
                reporteGenerico.getLstParametros().remove(parametro);
                reporteGenerico = em.merge(reporteGenerico);
            }
            em.remove(parametro);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Parametro> findParametroEntities() {
        return findParametroEntities(true, -1, -1);
    }

    public List<Parametro> findParametroEntities(int maxResults, int firstResult) {
        return findParametroEntities(false, maxResults, firstResult);
    }

    private List<Parametro> findParametroEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Parametro.class));
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

    public Parametro findParametro(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Parametro.class, id);
        } finally {
            em.close();
        }
    }

    public int getParametroCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Parametro> rt = cq.from(Parametro.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
