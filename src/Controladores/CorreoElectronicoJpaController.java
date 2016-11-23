/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Persona.CorreoElectronico;
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
public class CorreoElectronicoJpaController implements Serializable {

    public CorreoElectronicoJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(CorreoElectronico correoElectronico) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(correoElectronico);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(CorreoElectronico correoElectronico) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            correoElectronico = em.merge(correoElectronico);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = correoElectronico.getId();
                if (findCorreoElectronico(id) == null) {
                    throw new NonexistentEntityException("The correoElectronico with id " + id + " no longer exists.");
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
            CorreoElectronico correoElectronico;
            try {
                correoElectronico = em.getReference(CorreoElectronico.class, id);
                correoElectronico.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The correoElectronico with id " + id + " no longer exists.", enfe);
            }
            em.remove(correoElectronico);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<CorreoElectronico> findCorreoElectronicoEntities() {
        return findCorreoElectronicoEntities(true, -1, -1);
    }

    public List<CorreoElectronico> findCorreoElectronicoEntities(int maxResults, int firstResult) {
        return findCorreoElectronicoEntities(false, maxResults, firstResult);
    }

    private List<CorreoElectronico> findCorreoElectronicoEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(CorreoElectronico.class));
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

    public CorreoElectronico findCorreoElectronico(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(CorreoElectronico.class, id);
        } finally {
            em.close();
        }
    }

    public int getCorreoElectronicoCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<CorreoElectronico> rt = cq.from(CorreoElectronico.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
