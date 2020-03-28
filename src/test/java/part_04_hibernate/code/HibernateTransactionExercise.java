package part_04_hibernate.code;

import org.h2.jdbcx.JdbcDataSource;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.H2Dialect;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.*;
import javax.sql.DataSource;
import java.util.Date;
import java.util.Properties;

/**
 * @author Marco Behler
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(
        classes = HibernateTransactionExercise.MySpringConfig.class)
@SuppressWarnings("Duplicates") // for IntelliJ idea only
public class HibernateTransactionExercise {

    @Autowired
    private SessionFactory sessionFactory;


    @Test
    public void exercise_openAndCommitTransaction() {
        // get a session == jdbc connection
        Session session = sessionFactory.openSession();

        // lets open up a transaction. remember setAutocommit(false)!
        session.beginTransaction();

        // save == insert our objects
        session.save(new Event("Jack Bauer is in the house!", new Date()));
        session.save(new Event("As is Habib Marwan", new Date()));

        // and commit it
        session.getTransaction().commit();

        // close the session == our jdbc connection
        session.close();
    }

    /**
     * The only entity/table we will have in our database
     */
    @Entity
    @Table(name = "EVENTS")
    public static class Event {

        @Id
        @GeneratedValue
        private Long id;

        private String description;

        @Temporal(TemporalType.TIMESTAMP)
        @Column(name = "EVENT_DATE")
        private Date date;

        public Event() {
        }


        public Event(String description, Date date) {
            this.description = description;
            this.date = date;
        }


        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    // our spring java-config
    @Configuration
    public static class MySpringConfig {

        @Bean
        public DataSource dataSource() {
            JdbcDataSource ds = new JdbcDataSource();
            ds.setURL("jdbc:h2:mem:exercise_db;DB_CLOSE_DELAY=-1");
            ds.setUser("sa");
            ds.setPassword("sa");
            return ds;
        }

        @Bean
        /**
         * 1. There are different LocaLSessionFactoryBeans, depending on
         * which Hibernate version you are using (3.x, 4.x, 5.x)
         * 2. Make sure to configure Hibernate with the correct database
         * dialect
         * 3. We let Hibernate auto-create our database here
         */
        public LocalSessionFactoryBean sessionFactory() {
            LocalSessionFactoryBean result =
                    new LocalSessionFactoryBean();
            result.setDataSource(dataSource());
            result.setAnnotatedClasses(Event.class);
            Properties hibernateProperties = new Properties();
            hibernateProperties.setProperty(Environment.DIALECT,
                    H2Dialect.class.getName());
            hibernateProperties.setProperty(Environment.HBM2DDL_AUTO,
                    "create-drop");
            hibernateProperties.setProperty(Environment.SHOW_SQL, "true");
            hibernateProperties.setProperty(Environment.FORMAT_SQL, "true");
            result.setHibernateProperties(hibernateProperties);
            return result;
        }


        @Bean
        public PlatformTransactionManager txManager() {
            return new HibernateTransactionManager(sessionFactory()
                    .getObject());
        }
    }
}
