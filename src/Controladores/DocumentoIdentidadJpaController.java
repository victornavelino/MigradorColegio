/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Persona.DocumentoIdentidad;
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
public class DocumentoIdentidadJpaController implements Serializable {

    public DocumentoIdentidadJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(DocumentoIdentidad documentoIdentidad) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(documentoIdentidad);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(DocumentoIdentidad documentoIdentidad) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            documentoIdentidad = em.merge(documentoIdentidad);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = documentoIdentidad.getId();
                if (findDocumentoIdentidad(id) == null) {
                    throw new NonexistentEntityException("The documentoIdentidad with id " + id + " no longer exists.");
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
            DocumentoIdentidad documentoIdentidad;
            try {
                documentoIdentidad = em.getReference(DocumentoIdentidad.class, id);
                documentoIdentidad.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The documentoIdentidad with id " + id + " no longer exists.", enfe);
            }
            em.remove(documentoIdentidad);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<DocumentoIdentidad> findDocumentoIdentidadEntities() {
        return findDocumentoIdentidadEntities(true, -1, -1);
    }

    public List<DocumentoIdentidad> findDocumentoIdentidadEntities(int maxResults, int firstResult) {
        return findDocumentoIdentidadEntities(false, maxResults, firstResult);
    }

    private List<DocumentoIdentidad> findDocumentoIdentidadEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(DocumentoIdentidad.class));
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

    public DocumentoIdentidad findDocumentoIdentidad(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(DocumentoIdentidad.class, id);
        } finally {
            em.close();
        }
    }

    public int getDocumentoIdentidadCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<DocumentoIdentidad> rt = cq.from(DocumentoIdentidad.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
