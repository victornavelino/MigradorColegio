/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Medico.UnidadFormadora;
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
public class UnidadFormadoraJpaController implements Serializable {

    public UnidadFormadoraJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(UnidadFormadora unidadFormadora) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(unidadFormadora);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(UnidadFormadora unidadFormadora) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            unidadFormadora = em.merge(unidadFormadora);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = unidadFormadora.getId();
                if (findUnidadFormadora(id) == null) {
                    throw new NonexistentEntityException("The unidadFormadora with id " + id + " no longer exists.");
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
            UnidadFormadora unidadFormadora;
            try {
                unidadFormadora = em.getReference(UnidadFormadora.class, id);
                unidadFormadora.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The unidadFormadora with id " + id + " no longer exists.", enfe);
            }
            em.remove(unidadFormadora);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<UnidadFormadora> findUnidadFormadoraEntities() {
        return findUnidadFormadoraEntities(true, -1, -1);
    }

    public List<UnidadFormadora> findUnidadFormadoraEntities(int maxResults, int firstResult) {
        return findUnidadFormadoraEntities(false, maxResults, firstResult);
    }

    private List<UnidadFormadora> findUnidadFormadoraEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(UnidadFormadora.class));
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

    public UnidadFormadora findUnidadFormadora(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(UnidadFormadora.class, id);
        } finally {
            em.close();
        }
    }

    public int getUnidadFormadoraCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<UnidadFormadora> rt = cq.from(UnidadFormadora.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
