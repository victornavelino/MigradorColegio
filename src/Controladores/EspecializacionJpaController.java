/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Controladores;

import Controladores.exceptions.NonexistentEntityException;
import Entidades.Medico.Especializacion;
import java.io.Serializable;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import Entidades.Medico.Medico;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

/**
 *
 * @author franco
 */
public class EspecializacionJpaController implements Serializable {

    public EspecializacionJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Especializacion especializacion) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Medico medico = especializacion.getMedico();
            if (medico != null) {
                medico = em.getReference(medico.getClass(), medico.getId());
                especializacion.setMedico(medico);
            }
            em.persist(especializacion);
            if (medico != null) {
                medico.getEspecializaciones().add(especializacion);
                medico = em.merge(medico);
            }
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Especializacion especializacion) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Especializacion persistentEspecializacion = em.find(Especializacion.class, especializacion.getId());
            Medico medicoOld = persistentEspecializacion.getMedico();
            Medico medicoNew = especializacion.getMedico();
            if (medicoNew != null) {
                medicoNew = em.getReference(medicoNew.getClass(), medicoNew.getId());
                especializacion.setMedico(medicoNew);
            }
            especializacion = em.merge(especializacion);
            if (medicoOld != null && !medicoOld.equals(medicoNew)) {
                medicoOld.getEspecializaciones().remove(especializacion);
                medicoOld = em.merge(medicoOld);
            }
            if (medicoNew != null && !medicoNew.equals(medicoOld)) {
                medicoNew.getEspecializaciones().add(especializacion);
                medicoNew = em.merge(medicoNew);
            }
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = especializacion.getId();
                if (findEspecializacion(id) == null) {
                    throw new NonexistentEntityException("The especializacion with id " + id + " no longer exists.");
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
            Especializacion especializacion;
            try {
                especializacion = em.getReference(Especializacion.class, id);
                especializacion.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The especializacion with id " + id + " no longer exists.", enfe);
            }
            Medico medico = especializacion.getMedico();
            if (medico != null) {
                medico.getEspecializaciones().remove(especializacion);
                medico = em.merge(medico);
            }
            em.remove(especializacion);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Especializacion> findEspecializacionEntities() {
        return findEspecializacionEntities(true, -1, -1);
    }

    public List<Especializacion> findEspecializacionEntities(int maxResults, int firstResult) {
        return findEspecializacionEntities(false, maxResults, firstResult);
    }

    private List<Especializacion> findEspecializacionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Especializacion.class));
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

    public Especializacion findEspecializacion(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Especializacion.class, id);
        } finally {
            em.close();
        }
    }

    public int getEspecializacionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Especializacion> rt = cq.from(Especializacion.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
}
