package com.tick42.quicksilver.repositories;

import com.tick42.quicksilver.models.UserModel;
import com.tick42.quicksilver.repositories.base.UserRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class UserRepositoryImpl implements UserRepository {

    private final SessionFactory sessionFactory;

    @Autowired
    public UserRepositoryImpl(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public UserModel create(UserModel userModel) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.save(userModel);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return userModel;
    }

    @Override
    public UserModel update(UserModel userModel) {
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            session.update(userModel);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return userModel;
    }

    @Override
    public List<UserModel> findAll() {
        List<UserModel> userModels = new ArrayList<>();
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            userModels = session
                    .createQuery("from UserModel order by username asc")
                    .list();
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return userModels;
    }

    @Override
    public UserModel findById(int id) {

        UserModel userModel = null;
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            userModel = session.get(UserModel.class, id);
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        return userModel;
    }

    @Override
    public UserModel findByUsername(String username) {
        UserModel userModel = null;
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            userModel = (UserModel) session
                    .createQuery("from UserModel where username = :username")
                    .setParameter("username", username)
                    .uniqueResult();
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return userModel;
    }

    @Override
    public List<UserModel> findUsersByState(boolean state) {
        List<UserModel> userModels = new ArrayList<>();
        try (Session session = sessionFactory.openSession()) {
            session.beginTransaction();
            userModels = session
                    .createQuery("from UserModel where isActive = :enabled order by username asc ")
                    .setParameter("enabled", state)
                    .list();
            session.getTransaction().commit();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return userModels;
    }
}
